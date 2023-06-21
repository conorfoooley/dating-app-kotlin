package com.mmdev.roove.ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import com.mmdev.business.chat.model.MessageItem
import com.mmdev.business.chat.usecase.GetMessagesUseCase
import com.mmdev.business.chat.usecase.SendMessageUseCase
import com.mmdev.business.chat.usecase.SendPhotoUseCase
import com.mmdev.business.chat.usecase.SetConversationUseCase

class ChatViewModel(private val getMessagesUC: GetMessagesUseCase,
                    private val sendMessageUC: SendMessageUseCase,
                    private val sendPhotoUC: SendPhotoUseCase,
                    private val setConversationUC: SetConversationUseCase) : ViewModel() {



	fun getMessages() = getMessagesUC.execute()

	fun sendMessage(messageItem: MessageItem) = sendMessageUC.execute(messageItem)

	fun sendPhoto(photoUri: String) = sendPhotoUC.execute(photoUri)

	fun setConversation(conversationId: String) = setConversationUC.execute(conversationId)


}