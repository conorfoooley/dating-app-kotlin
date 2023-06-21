/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 07.03.20 19:14
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.ui.dating.cards.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mmdev.business.core.UserItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentCardsBinding
import com.mmdev.roove.ui.SharedViewModel
import com.mmdev.roove.ui.common.base.BaseAdapter
import com.mmdev.roove.ui.common.base.BaseFragment
import com.mmdev.roove.ui.dating.cards.CardsViewModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.fragment_cards.*


class CardsFragment: BaseFragment() {

	private val mCardsStackAdapter =
		CardsStackAdapter(listOf(), R.layout.fragment_cards_item)

	private var mCardsList = mutableListOf<UserItem>()

	private lateinit var mAppearedUserItem: UserItem
	private lateinit var mDisappearedUserItem: UserItem

	private lateinit var sharedViewModel: SharedViewModel
	private lateinit var cardsViewModel: CardsViewModel

	companion object{
		private const val TAG = "mylogs_CardsFragment"
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		sharedViewModel = activity?.run {
			ViewModelProvider(this, factory)[SharedViewModel::class.java]
		} ?: throw Exception("Invalid Activity")

		cardsViewModel = ViewModelProvider(this@CardsFragment, factory)[CardsViewModel::class.java]
		cardsViewModel.loadUsersByPreferences()
		cardsViewModel.usersCardsList.observe(this, Observer {
			mCardsList = it.toMutableList()
		})
		cardsViewModel.showMatchDialog.observe(this, Observer {
			if (it) showMatchDialog(mDisappearedUserItem)
		})

	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?) =
		FragmentCardsBinding.inflate(inflater, container, false)
			.apply {
				lifecycleOwner = this@CardsFragment
				viewModel = cardsViewModel
				executePendingBindings()
			}
			.root

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		val cardStackLayoutManager = CardStackLayoutManager(cardStackView.context,
		                                                    object: CardStackListener {

			override fun onCardAppeared(view: View, position: Int) {
				//get current displayed on card profile
				mAppearedUserItem = mCardsStackAdapter.getItem(position)
				//Log.wtf(TAG, "appeared name = ${mAppearedUserItem.baseUserInfo.name}")
			}

			override fun onCardDragging(direction: Direction, ratio: Float) {}

			override fun onCardSwiped(direction: Direction) {
				//if right = add to liked
				if (direction == Direction.Right) {
					cardsViewModel.checkMatch(mAppearedUserItem)
				}
				//left = add to skipped
				if (direction == Direction.Left) {
					cardsViewModel.addToSkipped(mAppearedUserItem)
				}
			}

			override fun onCardRewound() {}
			override fun onCardCanceled() {}

			override fun onCardDisappeared(view: View, position: Int) {
				//needed to show match
				mDisappearedUserItem = mCardsStackAdapter.getItem(position)
				//if there is no available user to show - show loading
				if (position == mCardsStackAdapter.itemCount - 1) {
					cardsViewModel.showLoading.value = true
					cardsViewModel.showTextHelper.value = true
				}
				else mCardsList.removeAt(position)

			}

		})

		cardStackView.apply {
			adapter = mCardsStackAdapter
			layoutManager = cardStackLayoutManager
		}

		mCardsStackAdapter.setOnItemClickListener(object: BaseAdapter.OnItemClickListener<UserItem> {
			override fun onItemClick(item: UserItem, position: Int) {

				sharedViewModel.setUserSelected(mAppearedUserItem)
				findNavController().navigate(R.id.action_cards_to_profileFragment)
			}
		})

	}

	override fun onStop() {
		super.onStop()
		mCardsStackAdapter.setData(mCardsList)
	}

	private fun showMatchDialog(userItem: UserItem) {
		val dialog = MatchDialogFragment.newInstance(userItem.baseUserInfo.name,
		                                             userItem.baseUserInfo.mainPhotoUrl)

		dialog.show(childFragmentManager, MatchDialogFragment::class.java.canonicalName)
	}

}
