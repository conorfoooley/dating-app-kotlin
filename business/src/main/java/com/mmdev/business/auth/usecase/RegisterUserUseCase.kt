/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 26.03.20 17:52
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.auth.usecase

import com.mmdev.business.auth.repository.AuthRepository
import com.mmdev.business.core.UserItem

class RegisterUserUseCase(private val repository: AuthRepository) {

    fun execute(t: UserItem) = repository.registerUser(t)

}