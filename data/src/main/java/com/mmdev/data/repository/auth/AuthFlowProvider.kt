/*
 * Created by Andrii Kovalchuk
 * Copyright (C) 2021. roove
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

package com.mmdev.data.repository.auth

import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mmdev.data.core.firebase.FIRESTORE_NO_DOCUMENT_EXCEPTION
import com.mmdev.data.core.firebase.toUserItem
import com.mmdev.data.core.log.logDebug
import com.mmdev.data.core.log.logError
import com.mmdev.data.datasource.UserDataSource
import com.mmdev.data.datasource.auth.AuthCollector
import com.mmdev.data.datasource.auth.FirebaseUserState
import com.mmdev.data.datasource.auth.FirebaseUserState.NotNullUser
import com.mmdev.data.datasource.location.LocationDataSource
import com.mmdev.domain.auth.IAuthFlowProvider
import com.mmdev.domain.user.UserState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 */

@Singleton
class AuthFlowProvider @Inject constructor(
	private val auth: FirebaseAuth,
	private val fbLogin: LoginManager,
	private val location: LocationDataSource,
	private val userDataSource: UserDataSource
): IAuthFlowProvider {
	
	companion object {
		private const val LOCATION_FIELD = "location"
		private const val TAG = "mylogs_UserProvider"
	}
	
	private val authObservable: Observable<FirebaseUserState> = AuthCollector(auth).firebaseAuthObservable.map {
		it.currentUser?.reload()
		FirebaseUserState.pack(it.currentUser)
	}
	
	override fun getUser(): Observable<UserState> = authObservable.switchMap { firebaseUser ->
		logDebug(TAG, "Collecting auth information...")
		
		if (firebaseUser is NotNullUser) {
			
			logDebug(TAG, "Auth info exists: ${firebaseUser.user.uid}")
			
			getUserFromRemoteStorage(firebaseUser.user)
		}
		//not signed in
		else {
			logError(TAG, "Auth info does not exists...")
			
			Observable.just(UserState.UNDEFINED)
		}
	}
	
	
	private fun getUserFromRemoteStorage(firebaseUser: FirebaseUser) =
		userDataSource.getFirestoreUser(firebaseUser.uid)
			.map { UserState.registered(it) }
			.onErrorResumeNext {
				logError(TAG, "$it")
				//if no document stored on backend
				if (it is NoSuchElementException && it.message == FIRESTORE_NO_DOCUMENT_EXCEPTION)
					Single.just(UserState.unregistered(firebaseUser.toUserItem()))
				else Single.just(UserState.UNDEFINED)
			}
			.toObservable()
	
	
	
	override fun logOut(){
		if (auth.currentUser != null) {
			auth.signOut()
			fbLogin.logOut()
		}
	}
	
}