/*
 * Created by Andrii Kovalchuk
 * Copyright (C) 2020. roove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses
 */

package com.mmdev.roove.ui.conversations

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import com.mmdev.business.conversations.ConversationItem
import com.mmdev.business.conversations.ConversationsRepository
import com.mmdev.roove.ui.common.base.BaseViewModel
import com.mmdev.roove.ui.common.errors.ErrorType
import com.mmdev.roove.ui.common.errors.MyError

class ConversationsViewModel @ViewModelInject constructor(
	private val repo: ConversationsRepository
): BaseViewModel() {



	private val deleteConversationStatus: MutableLiveData<Boolean> = MutableLiveData()

	val conversationsList: MutableLiveData<MutableList<ConversationItem>> = MutableLiveData()
	init {
		conversationsList.value = mutableListOf()
	}

	val showTextHelper: MutableLiveData<Boolean> = MutableLiveData()


	fun deleteConversation(conversationItem: ConversationItem){
		disposables.add(repo.deleteConversation(conversationItem)
            .observeOn(mainThread())
            .subscribe(
	            { deleteConversationStatus.value = true },
	            {
		            deleteConversationStatus.value = false
		            error.value = MyError(ErrorType.DELETING, it)
	            }
            )
		)
	}


	fun loadConversationsList(){
		disposables.add(repo.getConversationsList()
            .observeOn(mainThread())
            .subscribe(
	            {
		            if (it.isNotEmpty()) {
		            	conversationsList.value = it.toMutableList()
			            showTextHelper.value = false
		            }
		            else showTextHelper.value = true
	            },
	            {
		            showTextHelper.value = true
		            error.value = MyError(ErrorType.LOADING, it)
	            }
            )
		)
	}

	fun loadMoreConversations(){
		disposables.add(repo.getMoreConversationsList()
            .observeOn(mainThread())
            .subscribe(
	            {
		            if (it.isNotEmpty()) {
		            	conversationsList.value!!.addAll(it)
			            conversationsList.value = conversationsList.value
		            }
	            },
	            { error.value = MyError(ErrorType.LOADING, it) }
            )
		)
	}

	fun getDeleteConversationStatus() = deleteConversationStatus
	
}