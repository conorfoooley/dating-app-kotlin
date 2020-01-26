/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 25.01.20 19:12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.user

import com.mmdev.business.base.BasePlaceInfo
import com.mmdev.business.base.BaseUserInfo

data class UserItem(val baseUserInfo: BaseUserInfo = BaseUserInfo(),
                    var preferredGender: String = "",
                    val photoURLs: MutableList<String> = mutableListOf(),
                    val placesToGo: MutableList<BasePlaceInfo> = mutableListOf())




