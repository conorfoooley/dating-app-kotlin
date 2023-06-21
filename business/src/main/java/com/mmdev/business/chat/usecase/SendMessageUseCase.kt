/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 09.12.19 20:46
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.chat.usecase

import com.mmdev.business.chat.entity.MessageItem
import com.mmdev.business.chat.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {

    fun execute(t: MessageItem, b: Boolean? = false) = repository.sendMessage(t, b)

}