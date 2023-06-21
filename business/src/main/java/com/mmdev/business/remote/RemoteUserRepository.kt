/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 04.03.20 18:35
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.remote

import com.mmdev.business.core.BaseUserInfo
import com.mmdev.business.core.PhotoItem
import com.mmdev.business.core.UserItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

/**
 * This is the documentation block about the class
 */

interface RemoteUserRepository {

	fun createUserOnRemote(userItem: UserItem): Completable

	fun deletePhoto(photoItem: PhotoItem, userItem: UserItem, isMainPhotoDeleting: Boolean): Completable

	fun deleteUser(userItem: UserItem): Completable

	fun fetchUserInfo(): Single<UserItem>

	fun getFullUserItem(baseUserInfo: BaseUserInfo): Single<UserItem>

	fun updateUserItem(userItem: UserItem): Completable

	fun uploadUserProfilePhoto(photoUri: String, userItem: UserItem): Observable<HashMap<Double, List<PhotoItem>>>

}