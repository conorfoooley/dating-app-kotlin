/*
 * Created by Andrii Kovalchuk on 29.09.19 21:59
 * Copyright (c) 2019. All rights reserved.
 * Last modified 18.11.19 20:01
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.data.chat

import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.mmdev.business.chat.model.MessageItem
import com.mmdev.business.chat.model.PhotoAttachementItem
import com.mmdev.business.chat.repository.ChatRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class ChatRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore,
                                             private val storage: StorageReference): ChatRepository{


	private var conversationId = ""

	companion object {
		// Firebase firestore references
		private const val GENERAL_COLLECTION_REFERENCE = "conversations"
		private const val SECONDARY_COLLECTION_REFERENCE = "messages"
		// Firebase Storage references

		private const val GENERAL_FOLDER_STORAGE_IMG = "images"
	}


	override fun setConversation(conversationId: String){
		this.conversationId = conversationId
	}



	override fun getMessagesList(conversationId: String): Observable<List<MessageItem>> {
		setConversation(conversationId)
		Log.wtf("mylogs", "conversation set, id = $conversationId")
		return Observable.create(ObservableOnSubscribe<List<MessageItem>> { emitter ->
			val listener = firestore.collection(GENERAL_COLLECTION_REFERENCE)
				.document(conversationId)
				.collection(SECONDARY_COLLECTION_REFERENCE)
				.orderBy("timestamp")
				.addSnapshotListener { snapshots, e ->
					if (e != null) {
						emitter.onError(e)
						return@addSnapshotListener
					}
					val messages = ArrayList<MessageItem>()
					for (doc in snapshots!!) {
						messages.add(doc.toObject(MessageItem::class.java))
					}
					emitter.onNext(messages)
				}
			emitter.setCancellable{ listener.remove() }
		}).subscribeOn(Schedulers.io())
	}


	override fun sendMessage(messageItem: MessageItem): Completable {
		return Completable.create { emitter ->
			firestore.collection(GENERAL_COLLECTION_REFERENCE)
				.document(conversationId)
				.collection(SECONDARY_COLLECTION_REFERENCE)
				.document()
				.set(messageItem)
				.addOnSuccessListener { emitter.onComplete() }
				.addOnFailureListener { emitter.onError(it) }

		}.subscribeOn(Schedulers.io())
	}

	override fun sendPhoto(photoUri: String): Observable<PhotoAttachementItem> {
		val namePhoto = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()+".jpg"
		val storageRef = storage
			.child(GENERAL_FOLDER_STORAGE_IMG)
			.child(conversationId)
			.child(namePhoto)
		return Observable.create(ObservableOnSubscribe<PhotoAttachementItem>{ emitter ->
			val uploadTask = storageRef.putFile(Uri.parse(photoUri))
				.addOnSuccessListener {
					storageRef.downloadUrl.addOnSuccessListener{
						val photoAttached = PhotoAttachementItem(it.toString(), namePhoto)
						emitter.onNext(photoAttached)
					}
				}
				.addOnFailureListener { emitter.onError(it) }
			emitter.setCancellable{ uploadTask.cancel() }
		}).subscribeOn(Schedulers.io())
	}



}