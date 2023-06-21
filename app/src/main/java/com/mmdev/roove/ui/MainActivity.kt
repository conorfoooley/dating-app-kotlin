/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 26.01.20 15:43
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mmdev.roove.R
import com.mmdev.roove.core.glide.GlideApp
import com.mmdev.roove.core.injector
import com.mmdev.roove.ui.auth.AuthViewModel
import com.mmdev.roove.ui.auth.view.AuthFlowFragment
import com.mmdev.roove.ui.core.SharedViewModel
import com.mmdev.roove.ui.core.viewmodel.LocalUserRepoViewModel
import com.mmdev.roove.ui.custom.LoadingDialog
import com.mmdev.roove.ui.main.MainFlowFragment
import com.mmdev.roove.utils.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity(R.layout.activity_main) {

	private lateinit var progressDialog: LoadingDialog

	private lateinit var authViewModel: AuthViewModel
	private lateinit var localRepoViewModel: LocalUserRepoViewModel
	private lateinit var sharedViewModel: SharedViewModel

	private val factory = injector.factory()

	companion object{
		private const val TAG_LOG = "mylogs_MainActivity"
	}

	override fun onCreate(savedInstanceState: Bundle?) {

		window.apply {
			clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
			addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			decorView.systemUiVisibility =
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			//status bar and navigation bar colors assigned in theme
		}

		super.onCreate(savedInstanceState)

		main_activity_container.doOnApplyWindowInsets { view, insets, initialPadding ->
			view.updatePadding(left = initialPadding.left + insets.systemWindowInsetLeft,
			                   right = initialPadding.right + insets.systemWindowInsetRight)
			insets.replaceSystemWindowInsets(Rect(0,
			                                      insets.systemWindowInsetTop,
			                                      0,
			                                      insets.systemWindowInsetBottom))
		}

		GlideApp.with(ivMainSplashLogo.context)
			.asGif()
			.load(R.drawable.logo_loading)
			.into(ivMainSplashLogo)


		progressDialog = LoadingDialog(this@MainActivity)

		localRepoViewModel = ViewModelProvider(this, factory)[LocalUserRepoViewModel::class.java]
		sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]
		authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
		authViewModel.checkIsAuthenticated()
//		authViewModel.getAuthStatus().observeOnce(this, Observer {
//			if (it == false) {
//				showAuthFlowFragment()
//				Log.wtf(TAG_LOG, "USER IS NOT LOGGED IN")
//			}
//			else {
//				showDrawerFlowFragment()
//				Log.wtf(TAG_LOG, "USER IS LOGGED IN")
//			}
//		})
		authViewModel.getAuthStatus().observe(this, Observer {
			if (it == false) {
				showAuthFlowFragment()
				Log.wtf(TAG_LOG, "USER IS NOT LOGGED IN")
			}
			else {
				showMainFlowFragment()
				localRepoViewModel.getSavedUser()?.let {
					savedUser -> sharedViewModel.setCurrentUser(savedUser)
				}
				Log.wtf(TAG_LOG, "USER IS LOGGED IN")
			}
		})
		authViewModel.showProgress.observe(this, Observer {
			if (it == true) progressDialog.showDialog()
			else progressDialog.dismissDialog()
		})



	}

	// show auth fragment
	private fun showAuthFlowFragment() {
		supportFragmentManager.beginTransaction().remove(MainFlowFragment()).commitNow()
		supportFragmentManager.beginTransaction().apply {
			replace(R.id.main_activity_container,
			    AuthFlowFragment(),
			    AuthFlowFragment::class.java.canonicalName)
			commit()
		}
		ivMainSplashLogo.visibility = View.GONE
	}

	// show main fragment
	private fun showMainFlowFragment() {
		supportFragmentManager.beginTransaction().remove(AuthFlowFragment()).commitNow()
		supportFragmentManager.beginTransaction().apply {
				replace(R.id.main_activity_container,
				    MainFlowFragment(),
				    MainFlowFragment::class.java.canonicalName)
				commit()
			}
		ivMainSplashLogo.visibility = View.GONE

	}


	//hide keyboard by clicking outside
	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		currentFocus?.let {
			val imm: InputMethodManager = getSystemService(
					Context.INPUT_METHOD_SERVICE
			) as (InputMethodManager)
			imm.hideSoftInputFromWindow(it.windowToken, 0)
		}
		return super.dispatchTouchEvent(ev)
	}



}
