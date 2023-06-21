/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 10.04.20 17:16
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.places.repository

import com.mmdev.business.places.BasePlaceInfo
import com.mmdev.business.places.PlaceDetailedItem
import com.mmdev.business.places.PlacesResponse
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single


/**
 * This is the documentation block about the class
 */

interface PlacesRepository {

	fun addPlaceToWantToGoList(basePlaceInfo: BasePlaceInfo): Completable

	fun loadFirstPlaces(category: String): Single<PlacesResponse>

	fun loadMorePlaces(category: String): Single<PlacesResponse>

	fun getPlaceDetails(id: Int): Single<PlaceDetailedItem>

	fun removePlaceFromWantToGoList(basePlaceInfo: BasePlaceInfo): Completable

}