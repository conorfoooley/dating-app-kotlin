/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 01.04.20 15:54
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.cards.repository

import com.mmdev.business.core.UserItem
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


interface CardsRepository {

	fun addToSkipped(skippedUserItem: UserItem): Completable

	fun checkMatch(likedUserItem: UserItem): Single<Boolean>

	fun getUsersByPreferences(initialLoading: Boolean): Single<List<UserItem>>

}
