/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 04.12.19 19:13
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.feed.usecase

import com.mmdev.business.feed.repository.FeedRepository

class GetFeedListUseCase (private val repository: FeedRepository) {

    fun execute() = repository.getFeedList()

}