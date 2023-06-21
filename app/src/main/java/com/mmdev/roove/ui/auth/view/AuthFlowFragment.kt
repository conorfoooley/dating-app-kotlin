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

package com.mmdev.roove.ui.auth.view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.mmdev.roove.R
import com.mmdev.roove.ui.common.base.FlowFragment

class AuthFlowFragment : FlowFragment(R.layout.fragment_auth_flow) {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		val navHost = childFragmentManager
			.findFragmentById(R.id.authHostFragment) as NavHostFragment

		navController = navHost.findNavController()
	}
}

