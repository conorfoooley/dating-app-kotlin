/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 04.02.20 18:35
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.dating.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mmdev.business.chat.entity.MessageItem
import com.mmdev.business.chat.usecase.GetConversationWithPartnerUseCase
import com.mmdev.business.chat.usecase.ObserveNewMessagesUseCase
import com.mmdev.business.chat.usecase.SendMessageUseCase
import com.mmdev.business.chat.usecase.UploadMessagePhotoUseCase
import com.mmdev.business.conversations.ConversationItem
import com.mmdev.business.user.BaseUserInfo
import com.mmdev.roove.ui.core.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ChatViewModel
@Inject constructor(private val getConversationWPartnerUC: GetConversationWithPartnerUseCase,
                    private val observeNewMessagesUC: ObserveNewMessagesUseCase,
                    private val sendMessageUC: SendMessageUseCase,
                    private val uploadMessagePhotoUC: UploadMessagePhotoUseCase) : BaseViewModel() {


	private lateinit var selectedConversation: ConversationItem

	private var emptyChat = false
	private val messagesList: MutableLiveData<List<MessageItem>> = MutableLiveData()
	val showLoading: MutableLiveData<Boolean> = MutableLiveData()

	fun startListenToEmptyChat(partnerId: String){
		disposables.add(getConversationWPartnerExecution(partnerId)
            .flatMapObservable {
	            selectedConversation = it
	            observeNewMessagesExecution(it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
	                       if(it.isNotEmpty()) {
		                       messagesList.value = it
		                       emptyChat = false
	                       }
	                       else emptyChat = true
	                       Log.wtf(TAG, "empty chat messages to show: ${it.size}")
	                       Log.wtf(TAG, "is empty chat? + $emptyChat")
                       },
                       {
	                       Log.wtf(TAG, "get messages empty chat error: $it")
                       }))
	}

	fun observeNewMessages(conversation: ConversationItem){
		selectedConversation = conversation
		disposables.add(observeNewMessagesExecution(conversation)
            .doOnSubscribe { showLoading.value = true }
            .doOnNext { showLoading.value = false }
            .doFinally { showLoading.value = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
	                       if(it.isNotEmpty()) {
		                       messagesList.value = it
		                       emptyChat = false
	                       }
	                       else emptyChat = true
	                       Log.wtf(TAG, "messages to show: ${it.size}")
                       },
                       {
	                       Log.wtf(TAG, "get messages error: $it")
                       }))
	}

	fun sendMessage(messageItem: MessageItem){
		disposables.add(sendMessageExecution(messageItem, emptyChat)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Log.wtf(TAG, "Message sent fragment_chat") },
                       { Log.wtf(TAG, "can't send message fragment_chat, $emptyChat") }))
	}

	//upload photo then send it as message item
	fun sendPhoto(photoUri: String, sender: BaseUserInfo, recipient: String){
		disposables.add(sendPhotoExecution(photoUri)
            .flatMapCompletable {
	            sendMessageExecution(MessageItem(sender = sender,
	                                             recipientId = recipient,
	                                             photoAttachmentItem = it,
	                                             conversationId = selectedConversation.conversationId),
	                                 emptyChat)
            }
            .doOnSubscribe { showLoading.value = true }
            .doOnComplete { showLoading.value = false }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Log.wtf(TAG, "Photo sent") },
                       { Log.wtf(TAG, "Sending photo error: $it") }))
	}

	fun getMessagesList() = messagesList

	private fun getConversationWPartnerExecution(partnerId: String) =
		getConversationWPartnerUC.execute(partnerId)

	private fun observeNewMessagesExecution(conversation: ConversationItem) =
		observeNewMessagesUC.execute(conversation)

	private fun sendMessageExecution(messageItem: MessageItem, emptyChat: Boolean? = false) =
		sendMessageUC.execute(messageItem, emptyChat)

	private fun sendPhotoExecution(photoUri: String) =
		uploadMessagePhotoUC.execute(photoUri)



}