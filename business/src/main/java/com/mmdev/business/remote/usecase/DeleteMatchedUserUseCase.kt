/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 26.03.20 17:31
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.remote.usecase

import com.mmdev.business.pairs.MatchedUserItem
import com.mmdev.business.remote.RemoteUserRepository

/**
 * This is the documentation block about the class
 */

class DeleteMatchedUserUseCase(private val repository: RemoteUserRepository) {

	fun execute(t: MatchedUserItem) = repository.deleteMatchedUser(t)

}