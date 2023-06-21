/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 04.12.19 19:13
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.core.di.modules

import android.app.Application
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import com.mmdev.data.user.UserRepositoryLocal
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserModule {

	/* get user info from sharedPrefs */
	@Provides
	@Singleton
	fun provideSavedUser(repository: UserRepositoryLocal) = repository.getSavedUser()

	@Provides
	@Singleton
	fun binaryPrefs(context: Application): Preferences =
		BinaryPreferencesBuilder(context).apply {
//			keyEncryption(XorKeyEncryption("JaNdRgUkXp2s5v8y/B?E(H+KbPeShVmY".toByteArray()))
//			valueEncryption(AesValueEncryption("16 bytes secret key".toByteArray(),
//			                                   "16 bytes initial vector".toByteArray()))
		}.build()

}