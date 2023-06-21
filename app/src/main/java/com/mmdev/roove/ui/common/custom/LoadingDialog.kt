/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 07.03.20 19:17
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.common.custom


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.mmdev.roove.R
import com.mmdev.roove.core.glide.GlideApp


/**
 * This is the documentation block about the class
 */

class LoadingDialog(private var context: Context) {

	private lateinit var dialog: Dialog
	
	fun showDialog() {
		dialog = Dialog(context)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(false)
		dialog.setContentView(R.layout.loading)

		val gifImageView: ImageView = dialog.findViewById(R.id.iv_loading)

		GlideApp.with(context)
			.asGif()
			.load(R.drawable.loading)
			.centerCrop()
			.apply(RequestOptions().circleCrop())
			.into(gifImageView)
		dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		dialog.show()
	}

	fun dismissDialog() { if (dialog.isShowing) dialog.dismiss() }

}