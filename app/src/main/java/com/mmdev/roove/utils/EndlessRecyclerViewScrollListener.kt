/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 05.02.20 16:39
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessRecyclerViewScrollListener: OnScrollListener {
	// The minimum amount of items to have below your current scroll position
	// before loading more.
	private var visibleThreshold = 5
	// The current offset index of data you have loaded
	private var currentPage = 0
	// The total number of items in the dataset after the last load
	private var previousTotalItemCount = 0
	// True if we are still waiting for the last set of data to load.
	private var loading = true
	// Sets the starting page index
	private val startingPageIndex = 0
	private var mLayoutManager: LayoutManager

	internal constructor(layoutManager: LinearLayoutManager) {
		mLayoutManager = layoutManager
	}

	internal constructor(layoutManager: GridLayoutManager) {
		mLayoutManager = layoutManager
		visibleThreshold *= layoutManager.spanCount
	}

	internal constructor(layoutManager: StaggeredGridLayoutManager) {
		mLayoutManager = layoutManager
		visibleThreshold *= layoutManager.spanCount
	}

	private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
		var maxSize = 0
		for (i in lastVisibleItemPositions.indices) if (i == 0) maxSize =
			lastVisibleItemPositions[i]
		else if (lastVisibleItemPositions[i] > maxSize) maxSize = lastVisibleItemPositions[i]
		return maxSize
	}

	// This happens many times a second during a scroll, so be wary of the code you place here.
	// We are given a few useful parameters to help us work out if we need to load some more data,
	// but first we check if we are waiting for the previous load to finish.
	override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
		var lastVisibleItemPosition = 0
		val totalItemCount = mLayoutManager.itemCount
		when (mLayoutManager) {
			is StaggeredGridLayoutManager -> {
				val lastVisibleItemPositions: IntArray = (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
				// get maximum element within the list
				lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
			}

			// If the total item count is zero and the previous isn't, assume the
			// list is invalidated and should be reset back to initial state
			is LinearLayoutManager -> lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
			is GridLayoutManager -> lastVisibleItemPosition = (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
			// If it’s still loading, we check to see if the dataset count has
			// changed, if so we conclude it has finished loading and update the current page
			// number and total item count.

			// If it isn’t currently loading, we check to see if we have breached
			// the visibleThreshold and need to reload more data.
			// If we do need to reload some more data, we execute onLoadMore to fetch the data.
			// threshold should reflect how many total columns there are too
		}

		if (totalItemCount < previousTotalItemCount) {
			currentPage = startingPageIndex
			previousTotalItemCount = totalItemCount
			if (totalItemCount == 0) loading = true
		}
		// If it’s still loading, we check to see if the dataset count has
		// changed, if so we conclude it has finished loading and update the current page
		// number and total item count.


		if (loading && totalItemCount > previousTotalItemCount) {
			loading = false
			previousTotalItemCount = totalItemCount
		}

		// If it isn’t currently loading, we check to see if we have breached
		// the visibleThreshold and need to reload more data.
		// If we do need to reload some more data, we execute onLoadMore to fetch the data.
		// threshold should reflect how many total columns there are too


		if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
			currentPage++
			onLoadMore(currentPage, totalItemCount)
			loading = true
		}
	}

	// Defines the process for actually loading more data based on page
	abstract fun onLoadMore(page: Int, totalItemsCount: Int)
}