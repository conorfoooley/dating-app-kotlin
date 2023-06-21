/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2019. All rights reserved.
 * Last modified 07.12.19 18:12
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.cards.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mmdev.roove.R
import com.mmdev.roove.databinding.DialogMatchBinding
import com.mmdev.roove.utils.addSystemBottomPadding
import kotlinx.android.synthetic.main.dialog_match.*

/**
 * This is the documentation block about the class
 */

class MatchDialogFragment: DialogFragment() {


	private var recievedName = ""
	private var recievedPhotoUrl = ""


	companion object {

		private const val PHOTO_KEY = "PHOTO_URL"
		private const val NAME_KEY = "NAME"
		fun newInstance(name:String, photoUrl: String) = MatchDialogFragment().apply {
			arguments = Bundle().apply {
				putString(NAME_KEY, name)
				putString(PHOTO_KEY, photoUrl)
			}
		}
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
		arguments?.let {
			recievedName = it.getString(NAME_KEY, "")
			recievedPhotoUrl = it.getString(PHOTO_KEY, "")
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		DialogMatchBinding.inflate(inflater, container, false)
			.apply {
				this.name = recievedName
				this.photoUrl = recievedPhotoUrl
				executePendingBindings()
			}.root


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		tvMatchDialogBack.setOnClickListener { dialog?.dismiss() }
		dialogMatchContainer.addSystemBottomPadding()
		dialogMatchContainer.addSystemBottomPadding()

	}
}