/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 22.01.20 19:08
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.places.view.detailed


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmdev.roove.R
import com.mmdev.roove.ui.core.BaseFragment
import com.mmdev.roove.ui.core.ImagePagerAdapter
import com.mmdev.roove.ui.places.PlacesViewModel
import kotlinx.android.synthetic.main.fragment_place_detailed.*

/**
 * A simple [Fragment] subclass.
 */
class PlaceDetailedFragment: BaseFragment(R.layout.fragment_place_detailed) {

	private val placePhotosAdapter = ImagePagerAdapter(listOf())

	private var receivedPlaceId = 0

	private lateinit var placesViewModel: PlacesViewModel


	companion object{
		private const val PLACE_ID_KEY = "PLACE_ID"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			receivedPlaceId = it.getInt(PLACE_ID_KEY)
		}

		placesViewModel = ViewModelProvider(this, factory)[PlacesViewModel::class.java]

		placesViewModel.loadPlaceDetails(receivedPlaceId)

		placesViewModel.getPlaceDetailed().observe(this, Observer {
			val placePhotos = ArrayList<String>()
			for (imageItem in it.images)
				placePhotos.add(imageItem.image)


			placePhotosAdapter.updateData(placePhotos)

			collapseBarPlaceDetailed.title = it.short_title

			tvPlaceAboutText.text = it.description
		})

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		toolbarPlaceDetailed.setNavigationOnClickListener { findNavController().navigateUp() }

		viewPagerPlacePhotos.apply {
			(getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
			adapter = placePhotosAdapter
		}

		TabLayoutMediator(tlDotsIndicatorPlace, viewPagerPlacePhotos){
			_: TabLayout.Tab, _: Int ->
			//do nothing
		}.attach()

	}

	override fun onBackPressed() {
		findNavController().navigateUp()
	}


}
