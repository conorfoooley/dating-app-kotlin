package com.mmdev.business.user.usecase

import com.mmdev.business.user.model.UserItem
import com.mmdev.business.user.repository.UserRepository

/* Created by A on 29.09.2019.*/

/**
 * This is the documentation block about the class
 */

class SaveUserInfoUseCase (private val repository: UserRepository.LocalUserRepository) {

	fun execute(currentUserItem: UserItem) = repository.saveUserInfo(currentUserItem)

}