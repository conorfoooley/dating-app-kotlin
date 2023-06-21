/*
 * Created by Andrii Kovalchuk on 20.11.19 21:38
 * Copyright (c) 2019. All rights reserved.
 * Last modified 20.11.19 21:07
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.events.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mmdev.business.events.usecase.GetEventsUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class EventsVMFactory @Inject constructor(private val getEventsUC: GetEventsUseCase) :
		ViewModelProvider.Factory {

	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
			return EventsViewModel(getEventsUC) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}


}