package com.mmdev.roove.ui.main.viewmodel.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmdev.business.user.usecase.local.GetSavedUserUseCase
import com.mmdev.business.user.usecase.local.SaveUserInfoUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class LocalUserRepoVMFactory @Inject constructor(private val getSavedUser: GetSavedUserUseCase,
                                                 private val saveUserInfo: SaveUserInfoUseCase) :
		ViewModelProvider.Factory {

	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(LocalUserRepoVM::class.java)) {
			return LocalUserRepoVM(getSavedUser,
			                       saveUserInfo) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}

}