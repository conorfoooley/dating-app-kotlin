package com.mmdev.roove.ui.conversations.viewmodel

import androidx.lifecycle.ViewModel
import com.mmdev.business.conversations.usecase.CreateConversationUseCase
import com.mmdev.business.conversations.usecase.DeleteConversationUseCase
import com.mmdev.business.conversations.usecase.GetConversationsListUseCase

/* Created by A on 26.10.2019.*/

/**
 * This is the documentation block about the class
 */

class ConversationsViewModel(private val createUC: CreateConversationUseCase,
                             private val deleteUC: DeleteConversationUseCase,
                             private val getUC: GetConversationsListUseCase): ViewModel(){

	fun createConversation() = createUC.exectue()

	fun deleteConversation(conversationId: String) = deleteUC.execute(conversationId)

	fun getConversationsList() = getUC.execute()

}