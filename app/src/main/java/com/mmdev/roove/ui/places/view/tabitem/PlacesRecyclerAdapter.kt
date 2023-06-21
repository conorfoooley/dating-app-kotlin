/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 19.12.19 21:59
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.places.view.tabitem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmdev.business.events.EventItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentPlacesRvItemBinding

class PlacesRecyclerAdapter (private var mPlaceList: List<EventItem>):
		RecyclerView.Adapter<PlacesRecyclerAdapter.PlacesItemHolder>() {

	private lateinit var mClickListener: OnItemClickListener

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		PlacesItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
		                                         R.layout.fragment_places_rv_item,
		                                         parent,
		                                         false))


	override fun onBindViewHolder(holder: PlacesItemHolder, position: Int) =
		holder.bind(mPlaceList[position])


	override fun getItemCount() = mPlaceList.size

	fun updateData(newPlaces: List<EventItem>) {
		mPlaceList = newPlaces
		notifyDataSetChanged()
	}

	fun getPlaceItem(position: Int) = mPlaceList[position]


	// allows clicks events to be caught
	fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
		mClickListener = itemClickListener
	}


	inner class PlacesItemHolder(private val binding: FragmentPlacesRvItemBinding) :
			RecyclerView.ViewHolder(binding.root) {

		init {
			itemView.setOnClickListener {
				mClickListener.onItemClick(itemView.rootView, adapterPosition)
			}
		}

		/*
		*   executePendingBindings()
		*   Evaluates the pending bindings,
		*   updating any Views that have expressions bound to modified variables.
		*   This must be run on the UI thread.
		*/
		fun bind(eventItem: EventItem) {
			binding.eventItem = eventItem
			binding.executePendingBindings()
		}

	}

	// parent fragment will override this method to respond to click events
	interface OnItemClickListener {
		fun onItemClick(view: View, position: Int)
	}

}
