/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 19.03.20 16:18
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mmdev.roove.R
import com.mmdev.roove.core.glide.GlideApp
import com.mmdev.roove.core.injector
import com.mmdev.roove.databinding.ActivityMainBinding
import com.mmdev.roove.ui.auth.AuthViewModel
import com.mmdev.roove.ui.auth.view.AuthFlowFragment
import com.mmdev.roove.ui.common.custom.LoadingDialog
import com.mmdev.roove.ui.main.MainFlowFragment
import com.mmdev.roove.ui.profile.RemoteRepoViewModel
import com.mmdev.roove.utils.observeOnce
import com.mmdev.roove.utils.showToastText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

	private lateinit var progressDialog: LoadingDialog

	private lateinit var authViewModel: AuthViewModel
	private lateinit var remoteRepoViewModel: RemoteRepoViewModel
	private lateinit var sharedViewModel: SharedViewModel

	private val factory = injector.factory()

	companion object{
		private const val TAG = "mylogs_MainActivity"
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

		val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

		GlideApp.with(ivMainSplashLogo.context)
			.asGif()
			.load(R.drawable.logo_loading)
			.into(ivMainSplashLogo)


		progressDialog = LoadingDialog(this@MainActivity)

		remoteRepoViewModel = ViewModelProvider(this, factory)[RemoteRepoViewModel::class.java]
		sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]
		authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

		authViewModel.checkIsAuthenticated()
		authViewModel.getAuthStatus().observe(this, Observer {
			if (it == false) {
				showAuthFlowFragment()
			}
			else {
				sharedViewModel.getCurrentUser().observeOnce(this, Observer {
					userInitialized -> if (userInitialized != null) showMainFlowFragment()
				})

				remoteRepoViewModel.fetchUserItem()
				remoteRepoViewModel.actualCurrentUserItem.observe(this, Observer {
					actualUserItem -> sharedViewModel.setCurrentUser(actualUserItem)
				})
			}
		})
		authViewModel.showProgress.observe(this, Observer {
			if (it == true) progressDialog.showDialog()
			else progressDialog.dismissDialog()
		})

		remoteRepoViewModel.isUserUpdatedStatus.observe(this, Observer {
			if (it) { showToastText(getString(R.string.toast_update_success)) }
		})

		//creating fake data on remote, do not call this on UI thread
//		for (i in 0 until 50){
//			UtilityManager.generateConversationOnRemote()
//			UtilityManager.generateMatchesOnRemote()
//		}


	}

	// show auth fragment
	private fun showAuthFlowFragment() {
		Log.wtf(TAG, "USER IS NOT LOGGED IN")
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
		Log.wtf(TAG, "USER IS LOGGED IN")
		supportFragmentManager.beginTransaction().remove(AuthFlowFragment()).commitNow()
		supportFragmentManager.beginTransaction().apply {
				replace(R.id.main_activity_container,
				    MainFlowFragment(),
				    MainFlowFragment::class.java.canonicalName)
				commit()
			}
		ivMainSplashLogo.visibility = View.GONE
	}

}
