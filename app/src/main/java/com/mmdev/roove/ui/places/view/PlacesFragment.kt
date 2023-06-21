/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 29.03.20 19:03
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.places.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentPlacesBinding
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment: Fragment(R.layout.fragment_places) {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		FragmentPlacesBinding.inflate(inflater, container, false)
			.apply {
				executePendingBindings()
			}
			.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		viewPagerPlaces.apply {
			adapter = PlacesPagerAdapter(childFragmentManager, lifecycle)
			(getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
		}

		TabLayoutMediator(tabLayoutPlaces, viewPagerPlaces) { tab: TabLayout.Tab, position: Int ->
			when (position){
				0 -> tab.text = "Бары"
				1 -> tab.text = "Развлечения"
				2 -> tab.text = "Арт"
				3 -> tab.text = "Рестораны"
			}
		}.attach()

	}
}
