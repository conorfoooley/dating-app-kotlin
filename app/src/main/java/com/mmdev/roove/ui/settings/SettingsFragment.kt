/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 25.02.20 15:22
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmdev.business.user.UserItem
import com.mmdev.roove.BuildConfig
import com.mmdev.roove.R
import com.mmdev.roove.ui.auth.AuthViewModel
import com.mmdev.roove.ui.core.*
import com.mmdev.roove.ui.custom.HorizontalCarouselLayoutManager
import com.mmdev.roove.ui.profile.view.PlacesToGoAdapter
import com.mmdev.roove.utils.observeOnce
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File
import java.util.*
import kotlin.math.round


/**
 * This is the documentation block about the class
 */

class SettingsFragment: BaseFragment(R.layout.fragment_settings) {

	private val mSettingsPhotoAdapter = SettingsUserPhotoAdapter(listOf())
	private val mPlacesToGoAdapter = PlacesToGoAdapter(listOf())

	private lateinit var sharedViewModel: SharedViewModel
	private lateinit var authViewModel: AuthViewModel

	private lateinit var userItem: UserItem

	// File
	private lateinit var mFilePathImageCamera: File

	companion object {
		private const val IMAGE_GALLERY_REQUEST = 1
		private const val IMAGE_CAMERA_REQUEST = 2
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activity?.run {
			authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
			sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]
		} ?: throw Exception("Invalid Activity")

		sharedViewModel.getCurrentUser().observeOnce(this, Observer {
			userItem = it
		})
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		toolbarSettings.setOnMenuItemClickListener { item ->
			when (item.itemId) {
				R.id.settings_action_log_out -> { showSignOutPrompt() }
			}
			return@setOnMenuItemClickListener true
		}

		rvUserPhotosList.apply {
			adapter = mSettingsPhotoAdapter
			layoutManager = HorizontalCarouselLayoutManager(this.context, HORIZONTAL, false)
			//item decorator to make first and last item align center
			addItemDecoration(object: RecyclerView.ItemDecoration(){
				override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
				                            state: RecyclerView.State) {
					val position = parent.getChildViewHolder(view).adapterPosition
					if (position == 0 || position == state.itemCount - 1) {
						val displayWidth = resources.displayMetrics.widthPixels
						val childElementWidth = resources.getDimension(R.dimen.rvSettingsPhotoElementWidth)
						//val elementMargin = 160
						val padding = round(displayWidth / 2f - childElementWidth / 2f).toInt()
						if (position == 0) { outRect.left = padding }
						else { outRect.right = padding }
					}
				}
			})
			val snapHelper: SnapHelper = LinearSnapHelper()
			snapHelper.attachToRecyclerView(this)
		}

		rvSettingsWantToGoList.apply { adapter = mPlacesToGoAdapter }

		mPlacesToGoAdapter.setOnItemClickListener(object: PlacesToGoAdapter.OnItemClickListener {
			override fun onItemClick(view: View, position: Int) {
				val placeId = bundleOf("PLACE_ID" to
						                       mPlacesToGoAdapter.getPlaceToGoItem(position).id)
				findNavController().navigate(R.id.action_settings_to_placeDetailedFragment, placeId)
			}
		})

		fabSettingsEdit.setOnClickListener {
			findNavController().navigate(R.id.action_settings_to_settingsEditInfoFragment)
		}

		fabSettingsAddPhoto.setOnClickListener {
			//show attachment dialog picker
			val builder = AlertDialog.Builder(it.context)
				.setItems(arrayOf("Camera", "Gallery")) {
					_, itemIndex ->
					if (itemIndex == 0) { photoCameraClick() }
					else { photoGalleryClick() }
				}
			val alertDialog = builder.create()
			val params = alertDialog.window?.attributes
			params?.gravity = Gravity.CENTER
			alertDialog.show()
		}
	}

	override fun onResume() {
		super.onResume()
		if (this::userItem.isInitialized) {
			mSettingsPhotoAdapter.updateData(userItem.photoURLs)
			mPlacesToGoAdapter.updateData(userItem.placesToGo.toList())
			val nameAgeText = "${userItem.baseUserInfo.name}, ${userItem.baseUserInfo.age}"
			tvNameAge.text = nameAgeText
		}
	}

	/*
		* log out pop up
		*/
	private fun showSignOutPrompt() {
		MaterialAlertDialogBuilder(context)
			.setTitle("Do you wish to log out?")
			.setMessage("This will permanently log you out.")
			.setPositiveButton("Log out") { dialog, _ ->
				authViewModel.logOut()
				dialog.dismiss()
			}
			.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
			}
			.show()

	}

	/*
	 * Checks if the app has permissions to OPEN CAMERA and take photos
	 * If the app does not has permission then the user will be prompted to grant permissions
	 * else open camera intent
	 */
	private fun photoCameraClick() {
		handlePermission(AppPermission.CAMERA,
		                 onGranted = { startCameraIntent() },
		                 onDenied = { requestAppPermissions(it) },
		                 onExplanationNeeded = { it.explanationMessageId }
		)
	}

	//take photo directly by camera
	private fun startCameraIntent() {
		val namePhoto = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
		mFilePathImageCamera = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
		                            userItem.baseUserInfo.name + namePhoto + "camera.jpg")
		val photoURI = FileProvider.getUriForFile(context!!,
		                                          BuildConfig.APPLICATION_ID + ".provider",
		                                          mFilePathImageCamera)
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
			putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
		}
		startActivityForResult(intent, IMAGE_CAMERA_REQUEST)
	}

	/*
	 * Checks if the app has permissions to READ user files
	 * If the app does not has permission then the user will be prompted to grant permissions
	 * else open gallery to choose photo
	 */
	private fun photoGalleryClick() {
		handlePermission(AppPermission.GALLERY,
		                 onGranted = { startGalleryIntent() },
		                 onDenied = { requestAppPermissions(it) },
		                 onExplanationNeeded = { it.explanationMessageId }
		)
	}

	//open gallery chooser
	private fun startGalleryIntent() {
		val intent = Intent().apply {
			action = Intent.ACTION_GET_CONTENT
			type = "image/*"
		}
		startActivityForResult(Intent.createChooser(intent, "Select image"),
		                       IMAGE_GALLERY_REQUEST)
	}

	// start after permissions was granted
	// If request is cancelled, the result arrays are empty.
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		onRequestPermissionsResultReceived(requestCode, grantResults,
		                                   onPermissionGranted = {
			                                   when (it){
				                                   AppPermission.CAMERA -> startCameraIntent()
				                                   AppPermission.GALLERY -> startGalleryIntent()
			                                   }
		                                   },
		                                   onPermissionDenied = { it.deniedMessageId })
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		// send photo from gallery
		if (requestCode == IMAGE_GALLERY_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {

				val selectedUri = data?.data
				//send photo selectedUri.toString()
			}
		}
		// send photo taken by camera
		if (requestCode == IMAGE_CAMERA_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {

				if (mFilePathImageCamera.exists()) {
					//sendPhoto(Uri.fromFile(mFilePathImageCamera).toString(), userItemModel.baseUserInfo, partnerId)
				}
				else Toast.makeText(context,
				                    "filePathImageCamera is null or filePathImageCamera isn't exists",
				                    Toast.LENGTH_LONG)
					.show()
			}
		}
	}



}