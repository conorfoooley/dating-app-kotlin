/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 10.07.20 19:41
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.settings.edit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.mmdev.business.core.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentSettingsEditInfoBinding
import com.mmdev.roove.ui.common.base.BaseFragment
import com.mmdev.roove.ui.common.custom.GridItemDecoration
import com.mmdev.roove.ui.profile.RemoteRepoViewModel
import com.mmdev.roove.ui.profile.RemoteRepoViewModel.DeletingStatus.IN_PROGRESS
import com.mmdev.roove.utils.buildMaterialAlertDialog
import com.mmdev.roove.utils.observeOnce
import com.mmdev.roove.utils.showToastText
import kotlinx.android.synthetic.main.fragment_settings_edit_info.*


/**
 * This is the documentation block about the class
 */

class SettingsEditInfoFragment: BaseFragment<RemoteRepoViewModel>(true) {

	private lateinit var currentUser: UserItem
	private val mEditorPhotoAdapter = SettingsEditInfoPhotoAdapter(layoutId = R.layout.fragment_settings_edit_info_photo_item)

	private var name = ""
	private var age = 0
	private var city = ""
	private var gender = ""

	private var cityToDisplay = ""

	private var descriptionText = ""

	private lateinit var cityList: Map<String, String>

	private val male = "male"
	private val female = "female"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		associatedViewModel = getViewModel()
		sharedViewModel.getCurrentUser().observeOnce(this, Observer {
			currentUser = it
			initSettings(it)
		})
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		FragmentSettingsEditInfoBinding.inflate(inflater, container, false)
			.apply {
				lifecycleOwner = this@SettingsEditInfoFragment
				viewModel = sharedViewModel
				executePendingBindings()
			}.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		cityList = mapOf(getString(R.string.russia_ekb) to getString(R.string.city_api_ekb),
		                 getString(R.string.russia_krasnoyarsk) to getString(R.string.city_api_krasnoyarsk),
		                 getString(R.string.russia_krd) to getString(R.string.city_api_krd),
		                 getString(R.string.russia_kzn) to getString(R.string.city_api_kzn),
		                 getString(R.string.russia_msk) to getString(R.string.city_api_msk),
		                 getString(R.string.russia_nnv) to getString(R.string.city_api_nnv),
		                 getString(R.string.russia_nsk) to getString(R.string.city_api_nsk),
		                 getString(R.string.russia_sochi) to getString(R.string.city_api_sochi),
		                 getString(R.string.russia_spb) to getString(R.string.city_api_spb))

		rvSettingsEditPhotos.apply {
			layoutManager = GridLayoutManager(this.context, 2, GridLayoutManager.VERTICAL, false)
			adapter =  mEditorPhotoAdapter
			addItemDecoration(GridItemDecoration())
		}

		mEditorPhotoAdapter.setOnItemClickListener(object: SettingsEditInfoPhotoAdapter.OnItemClickListener {

			override fun onItemClick(view: View, position: Int) {
				if (mEditorPhotoAdapter.itemCount > 1) {
					val photoToDelete = mEditorPhotoAdapter.getItem(position)

					val isMainPhotoDeleting = photoToDelete.fileUrl == currentUser.baseUserInfo.mainPhotoUrl

					//deletion observer
					associatedViewModel.photoDeletingStatus.observeOnce(this@SettingsEditInfoFragment, Observer {
						if (it) {
							currentUser.photoURLs.remove(photoToDelete)
							mEditorPhotoAdapter.removeAt(position)

							if (isMainPhotoDeleting) {
								currentUser.baseUserInfo.mainPhotoUrl = currentUser.photoURLs[0].fileUrl
							}
						}
					})
					//execute deleting
					associatedViewModel.deletePhoto(photoToDelete,
					                                currentUser, isMainPhotoDeleting)
				}
				else requireContext().showToastText(getString(R.string.toast_text_at_least_1_photo_required))
			}
		})

		//touch event guarantee that if user want to scroll or touch outside of edit box
		//keyboard hide and editText focus clear
		containerScrollSettings.setOnTouchListener { v, _ ->
			v.performClick()
			val iMM = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			iMM.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
			edSettingsEditDescription.clearFocus()

			return@setOnTouchListener false
		}

		btnSettingsEditGenderMale.setOnClickListener {
			gender = male
			currentUser.baseUserInfo.gender = gender
			toggleButtonSettingsEditGender.check(R.id.btnSettingsEditGenderMale)
		}

		btnSettingsEditGenderFemale.setOnClickListener {
			gender = female
			currentUser.baseUserInfo.gender = gender
			toggleButtonSettingsEditGender.check(R.id.btnSettingsEditGenderFemale)
		}


		btnSettingsEditSave.setOnClickListener {
			associatedViewModel.updateUserItem(currentUser)
		}
		btnSettingsEditDelete.setOnClickListener {
			showDialogDeleteAttention()
		}
	}


	private fun initSettings(userItem: UserItem) {
		if (userItem.baseUserInfo.gender == male)
			toggleButtonSettingsEditGender.check(R.id.btnSettingsEditGenderMale)
		else toggleButtonSettingsEditGender.check(R.id.btnSettingsEditGenderFemale)

		changerNameSetup()
		changerAgeSetup()
		changerCitySetup()
		changerDescriptionSetup()

	}

	private fun changerNameSetup() {
		edSettingsEditName.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				layoutSettingsEditName.isCounterEnabled = true
			}

			override fun afterTextChanged(s: Editable) {
				when {
					s.length > layoutSettingsEditName.counterMaxLength -> {
						layoutSettingsEditName.error = getString(R.string.text_max_length_error)
						btnSettingsEditSave.isEnabled = false
					}
					s.isEmpty() -> {
						layoutSettingsEditName.error = getString(R.string.text_empty_error)
						btnSettingsEditSave.isEnabled = false

					}
					else -> {
						layoutSettingsEditName.error = ""
						name = s.toString().trim()
						currentUser.baseUserInfo.name = name
						btnSettingsEditSave.isEnabled = true
					}
				}
			}
		})

		edSettingsEditName.setOnEditorActionListener { editText, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				editText.text = editText.text.toString().trim()
				editText.clearFocus()
			}
			return@setOnEditorActionListener false
		}
	}

	private fun changerAgeSetup() {
		sliderSettingsEditAge.addOnChangeListener { _, value, _ ->
			age = value.toInt()
			currentUser.baseUserInfo.age = age
			tvSettingsEditAge.text = "Age: $age"
		}
	}

	private fun changerCitySetup() {
		val cityAdapter = ArrayAdapter(requireContext(),
		                               R.layout.drop_text_item,
		                               cityList.map { it.key })
		dropSettingsEditCity.setAdapter(cityAdapter)

		dropSettingsEditCity.setOnItemClickListener { _, _, position, _ ->
			city = cityList.map { it.value }[position]
			cityToDisplay = cityList.map { it.key }[position]
			currentUser.baseUserInfo.city = city
			currentUser.cityToDisplay = cityToDisplay
		}
	}

	private fun changerDescriptionSetup() {

		edSettingsEditDescription.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				layoutSettingsEditDescription.isCounterEnabled = true
			}

			override fun afterTextChanged(s: Editable) {
				if (s.length > layoutSettingsEditDescription.counterMaxLength){
					layoutSettingsEditDescription.error = getString(R.string.text_max_length_error)
					btnSettingsEditSave.isEnabled = false
				}
				else {
					layoutSettingsEditDescription.error = ""
					descriptionText = s.toString().trim()
					currentUser.aboutText = descriptionText
					btnSettingsEditSave.isEnabled = true
				}
			}
		})

		edSettingsEditDescription.setOnEditorActionListener { editText, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				editText.text = editText.text.toString().trim()
				editText.clearFocus()
			}
			return@setOnEditorActionListener false
		}

		edSettingsEditDescription.setOnTouchListener { view, event ->
			view.performClick()
			view.parent.requestDisallowInterceptTouchEvent(true)
			if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
				view.parent.requestDisallowInterceptTouchEvent(false)
			}
			return@setOnTouchListener false
		}

	}

	private fun showDialogDeleteAttention() {
		context?.buildMaterialAlertDialog(title = getString(R.string.dialog_profile_delete_title),
		                                  message = getString(R.string.dialog_profile_delete_message),
		                                  positiveText = getString(R.string.dialog_delete_btn_positive_text),
		                                  positiveClick = {
			                                  associatedViewModel.selfDeletingStatus.value = IN_PROGRESS
			                                  associatedViewModel.deleteMyAccount()
		                                  },
		                                  negativeText = getString(R.string.dialog_delete_btn_negative_text),
		                                  negativeClick = {} )?.show()
	}

	override fun onBackPressed() {
		super.onBackPressed()
		navController.navigateUp()
	}

}