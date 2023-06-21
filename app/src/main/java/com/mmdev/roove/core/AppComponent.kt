/*
 * Created by Andrii Kovalchuk on 20.08.19 16:12
 * Copyright (c) 2019. All rights reserved.
 * Last modified 18.11.19 20:01
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.core

import android.app.Application
import com.mmdev.business.auth.repository.AuthRepository
import com.mmdev.business.cards.repository.CardsRepository
import com.mmdev.business.chat.repository.ChatRepository
import com.mmdev.business.conversations.repository.ConversationsRepository
import com.mmdev.business.user.repository.UserRepository
import com.mmdev.roove.core.di.AuthModule
import com.mmdev.roove.core.di.FirebaseModule
import com.mmdev.roove.core.di.RepositoryModule
import com.mmdev.roove.core.di.UserModule
import com.mmdev.roove.ui.actions.conversations.viewmodel.ConversationsViewModelFactory
import com.mmdev.roove.ui.auth.viewmodel.AuthViewModelFactory
import com.mmdev.roove.ui.cards.viewmodel.CardsViewModelFactory
import com.mmdev.roove.ui.chat.viewmodel.ChatViewModelFactory
import com.mmdev.roove.ui.main.viewmodel.local.LocalUserRepoVMFactory
import com.mmdev.roove.ui.main.viewmodel.remote.RemoteUserRepoVMFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [
	AuthModule::class,
	RepositoryModule::class,
	FirebaseModule::class,
	ViewModelModule::class,
	UserModule::class
])

@Singleton
interface AppComponent {

	@Component.Builder
	interface Builder {
		fun build(): AppComponent

		@BindsInstance
		fun application(application: Application): Builder
	}

	//factories
	fun authViewModelFactory(): AuthViewModelFactory
	fun cardsViewModelFactory(): CardsViewModelFactory
	fun chatViewModelFactory(): ChatViewModelFactory
	fun conversationsViewModelFactory(): ConversationsViewModelFactory
	fun localUserRepoVMFactory(): LocalUserRepoVMFactory
	fun remoteUserRepoVMFactory(): RemoteUserRepoVMFactory


	//repos
	fun authRepository(): AuthRepository
	fun cardsRepository(): CardsRepository
	fun chatRepository(): ChatRepository
	fun conversationsRepository(): ConversationsRepository
	fun localUserRepository(): UserRepository.LocalUserRepository
	fun remoteUserRepository(): UserRepository.RemoteUserRepository

}