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

import android.content.Context
import android.provider.Settings.Secure
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.mmdev.data.core.BaseRepository
import com.mmdev.data.core.firebase.asSingle
import com.mmdev.data.datasource.UserDataSource
import com.mmdev.domain.auth.AuthRepository
import com.mmdev.domain.user.data.BaseUserInfo
import com.mmdev.domain.user.data.UserItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * login chain: signInWithFacebook => check if exist registered user with same uId =>
 * handle registration => after all complete fetch last results
 * fetchUserInfo() always called last no matter is user just registered or not
 *
 */

class AuthRepositoryImpl @Inject constructor(
	private val auth: FirebaseAuth,
	private val userDataSource: UserDataSource,
	context: Context
): BaseRepository(), AuthRepository {
	
	companion object {
		private const val FS_INSTALLATIONS_FIELD = "installations"
	}
	
	/**
	 * Gets the hardware serial number of this device.
	 *
	 * @return serial number or Settings.Secure.ANDROID_ID if not available.
	 * Credit: SecurePreferences for Android
	 */
	private val androidId: String = Secure.getString(context.contentResolver, Secure.ANDROID_ID)

	override fun signIn(token: String): Completable = signInWithFacebook(token)

	/**
	 * create new [UserItem] documents in db
	 */
	override fun signUp(userItem: UserItem): Single<UserItem> =
		userDataSource.writeFirestoreUser(userItem).doOnComplete {
			updateInstallations(userItem.baseUserInfo.userId)
		}.toSingle {
			userItem
		}
	
	/**
	 * this fun is called first when user is trying to sign in via facebook
	 * creates a basic [BaseUserInfo] object based on public facebook profile
	 */
	private fun signInWithFacebook(token: String) =
		auth.signInWithCredential(FacebookAuthProvider.getCredential(token))
			.asSingle()
			.concatMapCompletable {
				if (it.user != null) {
					val firebaseUser = it.user!!
					updateInstallations(firebaseUser.uid)
				}
				else Completable.error(IllegalStateException("User is not authenticated"))
			}
	
	private fun updateInstallations(id: String) = FirebaseInstallations
		.getInstance()
		.id
		.asSingle()
		.concatMapCompletable { token ->
			userDataSource.updateFirestoreUserField(
				id,
				FS_INSTALLATIONS_FIELD,
				mapOf(androidId to token)
			)
		}

}

