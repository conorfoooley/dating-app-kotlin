/*
 * Created by Andrii Kovalchuk on 28.11.19 22:07
 * Copyright (c) 2019. All rights reserved.
 * Last modified 28.11.19 21:25
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mmdev.business.chat.model.MessageItem
import com.mmdev.business.chat.usecase.GetConversationWithPartnerUseCase
import com.mmdev.business.chat.usecase.GetMessagesUseCase
import com.mmdev.business.chat.usecase.SendMessageUseCase
import com.mmdev.business.chat.usecase.SendPhotoUseCase
import com.mmdev.business.user.model.UserItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ChatViewModel @Inject constructor(private val getConversationUC: GetConversationWithPartnerUseCase,
                                        private val getMessagesUC: GetMessagesUseCase,
                                        private val sendMessageUC: SendMessageUseCase,
                                        private val sendPhotoUC: SendPhotoUseCase) : ViewModel() {


	private val messagesList: MutableLiveData<List<MessageItem>> = MutableLiveData()
	val showLoading: MutableLiveData<Boolean> = MutableLiveData()

	private val disposables = CompositeDisposable()


	companion object {
		private const val TAG = "mylogs"
	}


	fun startListenToEmptyChat(partnerId: String){
		disposables.add(getConversationExecution(partnerId)
            .flatMapObservable { getMessagesExecution(it) }
            .doOnSubscribe { showLoading.value = true }
            .doOnNext { showLoading.value = false }
            .doFinally { showLoading.value = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
	                       if(it.isNotEmpty()) messagesList.value = it
	                       Log.wtf(TAG, "messages to show: ${it.size}")
                       },
                       {
	                       Log.wtf(TAG, "can't send message fragment_chat")
                       }))
	}

	fun loadMessages(conversationId: String){
		disposables.add(getMessagesExecution(conversationId)
            .doOnSubscribe { showLoading.value = true }
            .doOnNext { showLoading.value = false }
            .doFinally { showLoading.value = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
	                       if(it.isNotEmpty()) messagesList.value = it
	                       Log.wtf(TAG, "messages to show: ${it.size}")
                       },
                       {
	                       Log.wtf(TAG, "get messages error: $it")
                       }))
	}

	fun sendMessage(messageItem: MessageItem){
		disposables.add(sendMessageExecution(messageItem)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Log.wtf(TAG, "Message sent fragment_chat") },
                       { Log.wtf(TAG, "can't send message fragment_chat") }))
	}

	fun sendPhoto(photoUri: String, senderUserItem: UserItem){
		disposables.add(sendPhotoExecution(photoUri)
            .flatMapCompletable { sendMessageExecution(MessageItem(senderUserItem,
                                                                   photoAttachementItem = it)) }
            .doOnSubscribe { showLoading.value = true }
            .doOnComplete { showLoading.value = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Log.wtf(TAG, "Photo sent") },
                       { Log.wtf(TAG, "Sending photo error: $it") }))
	}

	fun getMessagesList() = messagesList


	private fun getConversationExecution(partnerId: String) = getConversationUC.execute(partnerId)

	private fun getMessagesExecution(conversationId: String) = getMessagesUC.execute(conversationId)

	private fun sendMessageExecution(messageItem: MessageItem) = sendMessageUC.execute(messageItem)

	private fun sendPhotoExecution(photoUri: String) = sendPhotoUC.execute(photoUri)



	override fun onCleared() {
		disposables.clear()
		super.onCleared()
	}

}