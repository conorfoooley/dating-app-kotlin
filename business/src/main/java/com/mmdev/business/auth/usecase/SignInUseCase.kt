/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 21.12.19 18:52
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.auth.usecase

import com.mmdev.business.auth.repository.AuthRepository

/**
 * This is the documentation block about the class
 */

class SignInUseCase (private val repository: AuthRepository) {

	fun execute(s: String) = repository.signIn(s)

}