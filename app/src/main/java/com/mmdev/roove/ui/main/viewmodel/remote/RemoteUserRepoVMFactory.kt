package com.mmdev.roove.ui.main.viewmodel.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmdev.business.user.usecase.remote.CreateUserUseCase
import com.mmdev.business.user.usecase.remote.DeleteUserUseCase
import com.mmdev.business.user.usecase.remote.GetUserByIdUseCase
import javax.inject.Inject
import javax.inject.Singleton

/* Created by A on 10.11.2019.*/

/**
 * This is the documentation block about the class
 */

@Suppress("UNCHECKED_CAST")
@Singleton
class RemoteUserRepoVMFactory @Inject constructor(private val createUserUC: CreateUserUseCase,
                                                  private val deleteUserUC: DeleteUserUseCase,
                                                  private val getUserUC: GetUserByIdUseCase) :
		ViewModelProvider.Factory {

	override fun <T: ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(RemoteUserRepoVM::class.java)) {
			return RemoteUserRepoVM(createUserUC,
			                                                                 deleteUserUC,
			                                                                 getUserUC) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}

}