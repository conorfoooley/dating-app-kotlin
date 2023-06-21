/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 31.12.20 18:21
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.profile.view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mmdev.business.remote.Report
import com.mmdev.business.remote.Report.ReportType.*
import com.mmdev.business.user.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentProfileBinding
import com.mmdev.roove.ui.common.ImagePagerAdapter
import com.mmdev.roove.ui.common.base.BaseFragment
import com.mmdev.roove.ui.profile.RemoteRepoViewModel
import com.mmdev.roove.utils.extensions.observeOnce
import com.mmdev.roove.utils.extensions.showToastText
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the documentation block about the class
 */

@AndroidEntryPoint
class ProfileFragment: BaseFragment<RemoteRepoViewModel, FragmentProfileBinding>(
	layoutId = R.layout.fragment_profile
) {
	
	override val mViewModel: RemoteRepoViewModel by viewModels()

	private val userPhotosAdapter = ImagePagerAdapter()

	private var isReported: Boolean = false
	private var fabVisible: Boolean = false
	private var isMatched: Boolean = false

	private var selectedUser: UserItem = UserItem()
	private var conversationId: String = ""


	companion object {
		private const val FAB_VISIBLE_KEY = "FAB_VISIBLE"
		private const val MATCHED_KEY = "IS_MATCHED"
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			fabVisible = it.getBoolean(FAB_VISIBLE_KEY)
			isMatched = it.getBoolean(MATCHED_KEY)
		}

		mViewModel.retrievedUserItem.observeOnce(this, {
			selectedUser = it
			//ui
			userPhotosAdapter.setData(it.photoURLs.map { photoItem -> photoItem.fileUrl })
		})


		//if true -> seems that we navigates here from pairs or chat fragment
		if (isMatched) {
			sharedViewModel.matchedUserItemSelected.value?.let {
				conversationId = it.conversationId
				mViewModel.getRequestedUserInfo(it.baseUserInfo)
			}
			mViewModel.unmatchStatus.observeOnce(this, {
				if (it) navController.navigateUp()
			})
		}
		//else we navigates here from cards and already have userItem in sharedViewModel
		else {
			sharedViewModel.userNavigateTo.value.let {
				mViewModel.retrievedUserItem.value = it
			}
		}

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
		mViewModel.reportSubmittingStatus.observeOnce(this@ProfileFragment, {
			isReported = it
			requireContext().showToastText(getString(R.string.toast_text_report_success))
			toolbarProfile.apply {
				val reportItem = menu.findItem(R.id.profile_action_report)
				reportItem.isVisible = !isReported
			}
		})
		
		fabProfileSendMessage.visibility = if (fabVisible) View.VISIBLE else View.GONE
		viewPagerProfilePhotos.apply {
			(getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
			adapter = userPhotosAdapter
		}

		TabLayoutMediator(tlDotsIndicatorProfile, viewPagerProfilePhotos){
			_: TabLayout.Tab, _: Int ->
			//do nothing
		}.attach()

		toolbarProfile.apply {
			//menu declared directly in xml
			//no need to inflate menu manually
			//set only title and actions
			val deleteItem = menu.findItem(R.id.profile_action_unmatch)
			deleteItem.isVisible = isMatched
			setNavigationOnClickListener { navController.navigateUp() }
			setOnMenuItemClickListener { item ->
				when (item.itemId) {
					R.id.profile_action_report -> { showReportDialog() }
					R.id.profile_action_unmatch -> {
						sharedViewModel.matchedUserItemSelected.value?.let {
							mViewModel.deleteMatchedUser(it)
						}
					}
				}
				return@setOnMenuItemClickListener true
			}
		}
		
		if (fabVisible) {
			fabProfileSendMessage.setOnClickListener {
				navController.navigate(R.id.action_profile_to_chatFragment)
			}
		}

	}

	//todo: extract to array res
	private fun showReportDialog() = MaterialAlertDialogBuilder(requireContext())
		.setItems(arrayOf(getString(R.string.report_chooser_photos),
		                  getString(R.string.report_chooser_behavior),
		                  getString(R.string.report_chooser_fake))) { _, itemIndex ->
			when (itemIndex) {
				0 -> { mViewModel.submitReport(
					Report(INELIGIBLE_PHOTOS,
						   selectedUser.baseUserInfo)
				) }
				1 -> { mViewModel.submitReport(
					Report(DISRESPECTFUL_BEHAVIOR,
						   selectedUser.baseUserInfo)
				) }
				2 -> { mViewModel.submitReport(Report(FAKE, selectedUser.baseUserInfo)) }
			}
		}
		.create()
		.apply { window?.attributes?.gravity = Gravity.CENTER }
		.show()
	

	override fun onBackPressed() {
		navController.navigateUp()
	}

}