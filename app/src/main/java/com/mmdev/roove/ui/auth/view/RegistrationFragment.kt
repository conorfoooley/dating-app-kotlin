/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 30.03.20 20:45
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.auth.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.ArrayAdapter
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mmdev.business.core.BaseUserInfo
import com.mmdev.business.core.PhotoItem
import com.mmdev.business.core.PreferredAgeRange
import com.mmdev.business.core.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentAuthRegistrationBinding
import com.mmdev.roove.ui.auth.AuthViewModel
import com.mmdev.roove.ui.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_auth_registration.*


/**
 * This is the documentation block about the class
 */

class RegistrationFragment: BaseFragment<AuthViewModel>(true){

	private var registrationStep = 1

	private var baseUserInfo = BaseUserInfo()
	private var name = "no name"
	private var age = 18
	private var city = ""
	private var gender = ""
	private var preferredGender = ""
	private val preferredAgeRange = PreferredAgeRange(minAge = 18, maxAge = 24)

	private var cityToDisplay = ""
	private var preferredGenderToDisplay = ""
	private var genderToDisplay = ""


	private lateinit var cityList: Map<String, String>

	private val male = "male"
	private val female = "female"
	private val everyone = "everyone"


	override fun onAttach(context: Context) {
		super.onAttach(context)
		cityList = mapOf(getString(R.string.russia_ekb) to "ekb",
		                 getString(R.string.russia_krasnoyarsk) to "krasnoyarsk",
		                 getString(R.string.russia_krd) to "krd",
		                 getString(R.string.russia_kzn) to "kzn",
		                 getString(R.string.russia_msk) to "msk",
		                 getString(R.string.russia_nnv) to "nnv",
		                 getString(R.string.russia_nsk) to "nsk",
		                 getString(R.string.russia_sochi) to "sochi",
		                 getString(R.string.russia_spb) to "spb")
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		associatedViewModel = getViewModel()
		associatedViewModel.getBaseUserInfo().observe(this, Observer {
			baseUserInfo = it
		})
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		FragmentAuthRegistrationBinding.inflate(inflater, container, false)
			.apply {
				executePendingBindings()
			}
			.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		containerRegistration.transitionToState(R.id.step_1)

		disableFab()

		// prevent to click buttons during transitions
		containerRegistration.setTransitionListener(object : MotionLayout.TransitionListener {

			override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

			override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
				btnRegistrationBack.isClickable = false
				btnRegistrationNext.isClickable = false
			}

			override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

			override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
				btnRegistrationBack.isClickable = true
				btnRegistrationNext.isClickable = true
			}
		})



		//step 1 your gender
		btnGenderMale.setOnClickListener {
			setSelectedMale()

			when (registrationStep) {
				1 -> {
					gender = male
					genderToDisplay = getString(R.string.genderMale)
				}
				2 -> {
					preferredGender = male
					preferredGenderToDisplay = getString(R.string.preferredGenderMale)
				}
			}

			enableFab()

		}

		btnGenderFemale.setOnClickListener {
			setSelectedFemale()

			when (registrationStep){
				1 -> {
					gender = female
					genderToDisplay = getString(R.string.genderFemale)
				}
				2 -> {
					preferredGender = female
					preferredGenderToDisplay = getString(R.string.preferredGenderFemale)
				}
			}

			enableFab()

		}


		//step 2 gender your prefer
		btnGenderEveryone.setOnClickListener {
			setSelectedEveryone()
			preferredGender = everyone
			preferredGenderToDisplay = getString(R.string.preferredGenderEveryone)
			enableFab()
		}


		//step 3 age
		sliderAge.setOnChangeListener { _, value ->
			age = value.toInt()
			tvAgeDisplay.text = age.toString()
		}
		//step 3 pref age
		rangeSeekBarRegAgePicker.setOnRangeSeekBarChangeListener { _, number, number2 ->
			preferredAgeRange.minAge = number.toInt()
			preferredAgeRange.maxAge = number2.toInt()
		}


		//step 4 name
		edInputChangeName.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				layoutInputChangeName.isCounterEnabled = true
			}

			override fun afterTextChanged(s: Editable) {
				when {
					s.length > layoutInputChangeName.counterMaxLength -> {
						layoutInputChangeName.error = getString(R.string.text_max_length_error)
						disableFab()
					}
					s.isEmpty() -> {
						layoutInputChangeName.error = getString(R.string.text_empty_error)
						disableFab()
					}
					else -> {
						if (cityToDisplay.isNotEmpty() && city.isNotEmpty()) enableFab()
						layoutInputChangeName.error = ""
						name = s.toString().trim()
					}
				}
			}
		})

		edInputChangeName.setOnEditorActionListener { v, actionId, _ ->
			if (actionId == IME_ACTION_DONE) {
				v.text = v.text.toString().trim()
				v.clearFocus()
			}
			return@setOnEditorActionListener false
		}


		//step 4 city
		val cityAdapter = ArrayAdapter(context!!,
		                               R.layout.drop_text_item,
		                               cityList.map { it.key })
		dropdownCityChooser.setAdapter(cityAdapter)

		dropdownCityChooser.setOnItemClickListener { _, _, position, _ ->
			city = cityList.map { it.value }[position]
			cityToDisplay = cityList.map { it.key }[position]
			if (layoutInputChangeName.error.isNullOrEmpty()) enableFab()
		}


		//final step
		btnFinalBack.setOnClickListener {
			containerRegistration.transitionToState(R.id.step_4)
			restoreStep4State()
			registrationStep -= 1
		}

		btnRegistrationDone.setOnClickListener {
			val finalUserModel = BaseUserInfo(name,
			                                  age,
			                                  city,
			                                  gender,
			                                  preferredGender,
			                                  baseUserInfo.mainPhotoUrl,
			                                  baseUserInfo.userId)

			associatedViewModel.register(
					UserItem(finalUserModel,
					         cityToDisplay = cityToDisplay,
					         photoURLs = mutableListOf(PhotoItem(fileName = "facebookPhoto",
					                                             fileUrl = finalUserModel.mainPhotoUrl)),
					         preferredAgeRange = preferredAgeRange)
			)
		}



		btnRegistrationBack.setOnClickListener {
			when (registrationStep) {
				1 -> findNavController().navigateUp()

				2 -> {
					containerRegistration.transitionToState(R.id.step_1)
					restoreStep1State()
				}

				3 -> {
					containerRegistration.transitionToState(R.id.step_2)
					restoreStep2State()
				}

				4 -> {
					containerRegistration.transitionToState(R.id.step_3)
					restoreStep3State()
				}

			}
			registrationStep -= 1

			//Toast.makeText(context, "reg step = $registrationStep", Toast.LENGTH_SHORT).show()
		}

		btnRegistrationNext.setOnClickListener {
			when (registrationStep) {
				1 -> {
					containerRegistration.transitionToState(R.id.step_2)
					restoreStep2State()
				}

				2 -> {
					containerRegistration.transitionToState(R.id.step_3)
					restoreStep3State()
				}

				3 -> {
					containerRegistration.transitionToState(R.id.step_4)
					restoreStep4State()
				}

				4 -> {
					containerRegistration.transitionToState(R.id.step_final)
					setupFinalForm()
				}
			}
			registrationStep += 1

			//Toast.makeText(context, "reg step = $registrationStep", Toast.LENGTH_SHORT).show()
		}

	}

	private fun restoreStep1State() {
		setUnselectedAllButtons()
		changeStringsForSelfChoosing()
		if (gender.isNotEmpty()){
			enableFab()
			when (gender) {
				male -> setSelectedMale()
				female -> setSelectedFemale()
			}
		}
		else disableFab()
	}

	private fun restoreStep2State() {
		setUnselectedAllButtons()
		changeStringsForPreferred()
		if (preferredGender.isNotEmpty()){
			enableFab()
			when (preferredGender){
				male -> setSelectedMale()
				female -> setSelectedFemale()
				everyone -> setSelectedEveryone()
			}
		}
		else disableFab()
	}

	private fun restoreStep3State() {
		enableFab()
		sliderAge.value = age.toFloat()
		tvAgeDisplay.text = age.toString()
		rangeSeekBarRegAgePicker.selectedMinValue = preferredAgeRange.minAge
		rangeSeekBarRegAgePicker.selectedMaxValue = preferredAgeRange.maxAge
	}

	private fun restoreStep4State() {
		disableFab()
		if (name.isNotEmpty() && name != "no name") {
			edInputChangeName.setText(name)
		}
		else if (baseUserInfo.name.isNotEmpty()) {
			edInputChangeName.setText(baseUserInfo.name)
		}
		else if (edInputChangeName.text.isNullOrEmpty()) {
			layoutInputChangeName.error = getString(R.string.text_empty_error)
		}
		else if (city.isNotEmpty() && cityToDisplay.isNotEmpty()) enableFab()

		layoutInputChangeName.isCounterEnabled = false

	}

	private fun setupFinalForm() {
		edFinalName.setText(name)
		edFinalGender.setText(genderToDisplay)
		edFinalPreferredGender.setText(preferredGenderToDisplay)
		edFinalAge.setText(age.toString())
		edFinalCity.setText(cityToDisplay)
	}


	private fun enableFab() {
		if (!btnRegistrationNext.isEnabled) btnRegistrationNext.isEnabled = true
	}

	private fun disableFab() {
		if (btnRegistrationNext.isEnabled) btnRegistrationNext.isEnabled = false
	}

	private fun changeStringsForSelfChoosing() {
		tvGenderFemale.text = getString(R.string.genderFemale)
		tvGenderMale.text = getString(R.string.genderMale)
	}

	private fun changeStringsForPreferred() {
		tvGenderFemale.text = getString(R.string.preferredGenderFemale)
		tvGenderMale.text = getString(R.string.preferredGenderMale)
	}

	private fun setUnselectedAllButtons() {
		btnGenderEveryone.isSelected = false
		btnGenderFemale.isSelected = false
		btnGenderMale.isSelected = false
	}

	private fun setSelectedMale(){
		btnGenderEveryone.isSelected = false
		btnGenderFemale.isSelected = false
		btnGenderMale.isSelected = true
	}

	private fun setSelectedFemale(){
		btnGenderEveryone.isSelected = false
		btnGenderFemale.isSelected = true
		btnGenderMale.isSelected = false

	}

	private fun setSelectedEveryone(){
		btnGenderEveryone.isSelected = true
		btnGenderFemale.isSelected = false
		btnGenderMale.isSelected = false
	}

}