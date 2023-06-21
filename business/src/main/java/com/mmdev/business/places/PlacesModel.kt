/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 10.04.20 17:27
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.business.places

/**
 * This is the documentation block about the class
 */

data class PlaceItem (val id: Int = 0,
                      val title: String = "",
                      val short_title: String = "",
                      val images: List<ImageItem> = emptyList())

data class PlacesResponse (val results: List<PlaceItem> = emptyList())

data class PlaceDetailedItem (val id: Int = 0,
                              val title: String = "",
                              val short_title: String = "",
                              val body_text: String = "",
                              val description: String = "",
                              val images: List<ImageItem> = emptyList())
