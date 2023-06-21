/*
 * Created by Andrii Kovalchuk on 21.11.19 21:02
 * Copyright (c) 2019. All rights reserved.
 * Last modified 21.11.19 20:59
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.events.view.detailed


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmdev.business.events.model.EventItem
import com.mmdev.roove.R
import com.mmdev.roove.ui.ImagePagerAdapter
import com.mmdev.roove.ui.main.view.MainActivity

/**
 * A simple [Fragment] subclass.
 */
class EventDetailedFragment: Fragment(R.layout.fragment_event_detailed) {

	private lateinit var mMainActivity: MainActivity

	private lateinit var eventItem: EventItem

	private val eventPhotosAdapter = ImagePagerAdapter(listOf())

	companion object{

		fun newInstance() = EventDetailedFragment()

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		activity?.let { mMainActivity = it as MainActivity }

		eventItem = mMainActivity.eventItem
		val eventPhotos = ArrayList<String>()
		for (imageItem in eventItem.images)
			eventPhotos.add(imageItem.image)

		eventPhotosAdapter.updateData(eventPhotos)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val dots = view.findViewById<TabLayout>(R.id.dots_indicator)
		val viewPager= view.findViewById<ViewPager2>(R.id.event_photos_vp)
		val eventTitle = view.findViewById<TextView>(R.id.event_title)
		val eventDescription = view.findViewById<TextView>(R.id.event_about_content_tv)

		viewPager.adapter = eventPhotosAdapter

		TabLayoutMediator(dots, viewPager){ _: TabLayout.Tab, _: Int -> }.attach()

		eventTitle.text = eventItem.short_title

		eventDescription.text = eventItem.description
			.replace("<p>", "")
			.replace("</p>","")
	}


}
