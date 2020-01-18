/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 18.01.20 17:59
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.user.repository

import com.mmdev.business.user.UserItem

/**
 * This is the documentation block about the class
 */

interface LocalUserRepository {

	fun getSavedUser(): UserItem?

	fun saveUserInfo(userItem: UserItem)

}