/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 07.03.20 16:16
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.auth.view

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.mmdev.roove.R
import com.mmdev.roove.ui.common.base.FlowFragment

class AuthFlowFragment : FlowFragment(R.layout.fragment_auth_flow)  {

	private lateinit var navController: NavController

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		val navHost = childFragmentManager
			.findFragmentById(R.id.authHostFragment) as NavHostFragment

		navController = navHost.findNavController()
	}

	override fun onBackPressed() {
		super.onBackPressed()
		navController.navigateUp()
	}
}

