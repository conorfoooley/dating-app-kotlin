/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 08.04.20 19:04
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.auth.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentAuthLandingBinding
import com.mmdev.roove.ui.auth.AuthViewModel
import com.mmdev.roove.ui.common.base.BaseFragment
import com.mmdev.roove.utils.showToastText
import kotlinx.android.synthetic.main.fragment_auth_landing.*

class AuthLandingFragment: BaseFragment<AuthViewModel>(true, R.layout.fragment_auth_landing) {

	//Progress dialog for any authentication action
	private lateinit var mCallbackManager: CallbackManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		mCallbackManager = CallbackManager.Factory.create()

		associatedViewModel = getViewModel()

		associatedViewModel.continueRegistration.observe(this, Observer {
			if (it == true) navController.navigate(R.id.action_auth_landing_to_registrationFragment)
		})

	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		FragmentAuthLandingBinding.inflate(inflater, container, false)
			.apply { executePendingBindings() }
			.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		btnFacebookLogin.fragment = this
		btnFacebookLogin.registerCallback(mCallbackManager, object: FacebookCallback<LoginResult> {

			override fun onSuccess(loginResult: LoginResult) {
				associatedViewModel.signIn(loginResult.accessToken.token)
			}

			override fun onCancel() {}

			override fun onError(error: FacebookException) {
				view.context.showToastText("$error")
			}
		})
		btnFacebookLoginDelegate.setOnClickListener {
			associatedViewModel.logOut()
			btnFacebookLogin.performClick()
		}

		tvOpenPolicies.setOnClickListener {
			var url = getString(R.string.privacy_policy_url)
			if (!url.startsWith("http://") && !url.startsWith("https://"))
				url = "http://$url"
			val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
			startActivity(browserIntent)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		mCallbackManager.onActivityResult(requestCode, resultCode, data)
	}

}

