/*
 * Created by Andrii Kovalchuk
 * Copyright (C) 2020. roove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses
 */

package com.mmdev.roove.utils

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mmdev.business.data.PhotoItem
import com.mmdev.roove.R
import com.mmdev.roove.core.glide.GlideApp
import com.mmdev.roove.core.glide.GlideImageLoader
import com.mmdev.roove.ui.common.base.BaseRecyclerAdapter.BindableAdapter
import com.mmdev.roove.utils.extensions.doOnApplyWindowInsets


object BindingAdapterUtils {


	@JvmStatic
	@BindingAdapter("app:visibilityInvisible")
	fun handleViewInvisibleVisibility(view: View, show: Boolean = false) {
		view.visibility = if (show) View.VISIBLE else View.INVISIBLE
	}

	@JvmStatic
	@BindingAdapter("app:visibilityGone")
	fun handleViewGoneVisibility(view: View, show: Boolean = false) {
		view.visibility = if (show) View.VISIBLE else View.GONE
	}

	@JvmStatic
	@BindingAdapter("app:bindData")
	@Suppress("UNCHECKED_CAST")
	fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, data: T) {
		if (recyclerView.adapter is BindableAdapter<*>) {
			(recyclerView.adapter as BindableAdapter<T>).setData(data)
		}
	}

	@JvmStatic
	@BindingAdapter("app:bindPhotos")
	@Suppress("UNCHECKED_CAST")
	fun setViewPager2ImageAdapterProperties(viewPager2: ViewPager2, data: List<PhotoItem>) {
		(viewPager2.adapter as BindableAdapter<List<String>>).setData(data.map { it.fileUrl })
	}

	@JvmStatic
	@BindingAdapter(value = ["app:bindImageUrl", "app:progressBar"], requireAll = false)
	fun loadPhotoUrlWithProgress(imageView: ImageView, url: String, progressBar: ProgressBar?) {
		if (url.isNotEmpty())
			if (progressBar != null) {
				GlideImageLoader(imageView, progressBar)
					.load(url,
					      RequestOptions()
						      .dontAnimate()
						      .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						      .error(R.drawable.placeholder_image)
					)
			}
			else {
				GlideApp.with(imageView.context)
					.load(url)
					.dontAnimate()
					.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
					.into(imageView)
			}
	}

	@JvmStatic
	@BindingAdapter("app:paddingLeftSystemWindowInsets",
	                "app:paddingTopSystemWindowInsets",
	                "app:paddingRightSystemWindowInsets",
	                "app:paddingBottomSystemWindowInsets",
	                requireAll = false)
	fun applySystemWindowInsets(view: View,
	                            applyLeft: Boolean,
	                            applyTop: Boolean,
	                            applyRight: Boolean,
	                            applyBottom: Boolean) {
		view.doOnApplyWindowInsets { targetView, insets, padding ->

			val left = if (applyLeft) insets.systemWindowInsetLeft else 0
			val top = if (applyTop) insets.systemWindowInsetTop else 0
			val right = if (applyRight) insets.systemWindowInsetRight else 0
			val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

			targetView.setPadding(padding.left + left,
			                      padding.top + top,
			                      padding.right + right,
			                      padding.bottom + bottom)
		}
	}
}
