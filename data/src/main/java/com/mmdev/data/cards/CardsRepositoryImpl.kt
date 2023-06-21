/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 14.03.20 16:55
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.data.cards

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mmdev.business.cards.repository.CardsRepository
import com.mmdev.business.conversations.ConversationItem
import com.mmdev.business.core.BaseUserInfo
import com.mmdev.business.core.UserItem
import com.mmdev.business.pairs.MatchedUserItem
import com.mmdev.data.user.UserWrapper
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is the documentation block about the class
 */

@Singleton
class CardsRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore,
                                              userWrapper: UserWrapper):
		CardsRepository {

	private val currentUser: UserItem = userWrapper.getUser()
	private var currentUserDocRef: DocumentReference

	private val allUsersList = ArrayList<UserItem>()
	private val likedList = mutableListOf<String>()
	private val matchedList = mutableListOf<String>()
	private val skippedList = mutableListOf<String>()
	private val filteredUsersList = mutableListOf<UserItem>()

	private var countCardsLoaded: Int = 0
	private lateinit var generalCardsQuery: Query
	private lateinit var maleCardsQuery: Query
	private lateinit var femaleCardsQuery: Query
	private lateinit var paginateLastLoadedCard: DocumentSnapshot

	init {
		currentUserDocRef = firestore.collection(USERS_COLLECTION_REFERENCE)
			.document(currentUser.baseUserInfo.city)
			.collection(currentUser.baseUserInfo.gender)
			.document(currentUser.baseUserInfo.userId)
		if (currentUser.baseUserInfo.preferredGender != "everyone")
			generalCardsQuery = firestore.collection(USERS_COLLECTION_REFERENCE)
				.document(currentUser.baseUserInfo.city)
				.collection(currentUser.baseUserInfo.preferredGender)
				.whereGreaterThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.minAge)
				.whereLessThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.maxAge)
				.limit(10)

	}

	companion object {
		private const val USERS_COLLECTION_REFERENCE = "users"
		private const val USER_LIKED_COLLECTION_REFERENCE = "liked"
		private const val USER_SKIPPED_COLLECTION_REFERENCE = "skipped"
		private const val USER_MATCHED_COLLECTION_REFERENCE = "matched"
		private const val CONVERSATIONS_COLLECTION_REFERENCE = "conversations"

		private const val USER_ID_FIELD = "userId"

		private const val USERS_FILTER_AGE = "baseUserInfo.age"

		private const val MALE_COLLECTION_PATH = "male"
		private const val FEMALE_COLLECTION_PATH = "female"

		private const val TAG = "mylogs_CardsRepoImpl"
	}


	/*
	* if swiped left -> add skipped userId to skipped collection
	*/
	override fun addToSkipped(skippedUserItem: UserItem) {
		currentUserDocRef
			.collection(USER_SKIPPED_COLLECTION_REFERENCE)
			.document(skippedUserItem.baseUserInfo.userId)
			.set(mapOf(USER_ID_FIELD to skippedUserItem.baseUserInfo.userId))

		skippedList.add(skippedUserItem.baseUserInfo.userId)
	}

	/*
	* if swiped right -> check if there is match
	* else -> add liked userId to liked collection
	*/
	override fun checkMatch(likedUserItem: UserItem): Single<Boolean> {
		return Single.create(SingleOnSubscribe<Boolean> { emitter ->
			val likedUserDocRef = firestore.collection(USERS_COLLECTION_REFERENCE)
				.document(likedUserItem.baseUserInfo.city)
				.collection(likedUserItem.baseUserInfo.gender)
				.document(likedUserItem.baseUserInfo.userId)

			likedUserDocRef
				.collection(USER_LIKED_COLLECTION_REFERENCE)
				.document(currentUser.baseUserInfo.userId)
				.get()
				.addOnSuccessListener { userDoc ->
					if (userDoc.exists()) {

						emitter.onSuccess(true)
						matchedList.add(likedUserItem.baseUserInfo.userId)

						//create predefined conversation for this match
						val conversationId = firestore
							.collection(CONVERSATIONS_COLLECTION_REFERENCE)
							.document()
							.id

						//execute add and remove for documents for each of users
						handleMatch(MatchedUserItem(likedUserItem.baseUserInfo, conversationId = conversationId),
						            MatchedUserItem(currentUser.baseUserInfo, conversationId = conversationId))

						//set conversation for liked user
						likedUserDocRef
							.collection(CONVERSATIONS_COLLECTION_REFERENCE)
							.document(conversationId)
							.set(ConversationItem(partner = currentUser.baseUserInfo,
							                      conversationId = conversationId,
							                      lastMessageTimestamp = null))

						//set conversation for current user
						currentUserDocRef
							.collection(CONVERSATIONS_COLLECTION_REFERENCE)
							.document(conversationId)
							.set(ConversationItem(partner = likedUserItem.baseUserInfo,
							                      conversationId = conversationId,
							                      lastMessageTimestamp = null))

							.addOnSuccessListener { emitter.onSuccess(true) }
							.addOnFailureListener { emitter.onError(it) }

					}

					else {
						currentUserDocRef
							.collection(USER_LIKED_COLLECTION_REFERENCE)
							.document(likedUserItem.baseUserInfo.userId)
							.set(mapOf(USER_ID_FIELD to likedUserItem.baseUserInfo.userId))

						likedList.add(likedUserItem.baseUserInfo.userId)

						emitter.onSuccess(false)
					}

			}.addOnFailureListener {
				Log.wtf(TAG, "check match fail")
				emitter.onError(it)
			}

		}).subscribeOn(Schedulers.io())
	}

	/* return filtered users list as Single */
	override fun getUsersByPreferences() =
		Single.zip(getAllUsersCards(),
		           zipLists(),
		           BiFunction<List<UserItem>, List<String>, List<UserItem>>
		           { userList, ids  -> filterUsers(userList, ids) })
			.subscribeOn(Schedulers.computation())

	/* return filtered all users list from already written ids as List<UserItem> */
	private fun filterUsers(usersItemsList: List<UserItem>, ids: List<String>): List<UserItem> {
		if (usersItemsList.isNotEmpty())
			for (user in usersItemsList)
				if (!ids.contains(user.baseUserInfo.userId) &&
				    user.baseUserInfo.userId != currentUser.baseUserInfo.userId)
					filteredUsersList.add(user)
		//Log.wtf(TAG, "filtered users: ${filteredUsersList.size}")
		return filteredUsersList.shuffled()
	}

	/*
	* GET ALL USERS OBJECTS
	*/
	private fun getAllUsersCards(): Single<List<UserItem>> {

		var userItem: UserItem
		return Single.create(SingleOnSubscribe<List<UserItem>>{ emitter ->

			generalCardsQuery
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it) {
							userItem = doc.toObject(UserItem::class.java)
							if (!allUsersList.contains(userItem)){
								allUsersList.add(userItem)
								countCardsLoaded++
							}
						}
						if (countCardsLoaded < 10) {
							paginateLastLoadedCard = it.documents[it.size() - 1]
							generalCardsQuery = generalCardsQuery.startAfter(paginateLastLoadedCard)
							emitter.onSuccess(allUsersList)
						}
						else {
							countCardsLoaded = 0
							emitter.onSuccess(allUsersList)
						}
					}
					else emitter.onSuccess(listOf())
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())
	}

	private fun getAllMaleUsersCards(): Single<List<UserItem>> {
		maleCardsQuery = firestore.collection(USERS_COLLECTION_REFERENCE)
					.document(currentUser.baseUserInfo.city)
					.collection(MALE_COLLECTION_PATH)
					.orderBy(USERS_FILTER_AGE)
					.whereGreaterThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.minAge)
					.whereLessThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.maxAge)

		return Single.create(SingleOnSubscribe<List<UserItem>>{ emitter ->
			maleCardsQuery
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it)
							allUsersList.add(doc.toObject(UserItem::class.java))

						emitter.onSuccess(allUsersList)
					}
					else emitter.onSuccess(allUsersList)
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())
	}

	private fun getAllFemaleUsersCards(): Single<List<UserItem>> {
		femaleCardsQuery = firestore.collection(USERS_COLLECTION_REFERENCE)
			.document(currentUser.baseUserInfo.city)
			.collection(FEMALE_COLLECTION_PATH)
			.orderBy(USERS_FILTER_AGE)
			.whereGreaterThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.minAge)
			.whereLessThanOrEqualTo(USERS_FILTER_AGE, currentUser.preferredAgeRange.maxAge)

		return Single.create(SingleOnSubscribe<List<UserItem>>{ emitter ->
			femaleCardsQuery
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it)
							allUsersList.add(doc.toObject(UserItem::class.java))

						emitter.onSuccess(allUsersList)
					}
					else emitter.onSuccess(allUsersList)
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())
	}

	/* return merged lists as Single */
	private fun zipLists(): Single<List<String>> =
		Single.zip(getLikedList(),
		           getMatchedList(),
		           getSkippedList(),
		           Function3<List<String>, List<String>, List<String>, List<String>>
		           { likes, matches, skipped -> mergeLists(likes, matches, skipped) })
			.observeOn(Schedulers.computation())

	/* merge all liked + matched + skipped users lists */
	private fun mergeLists(liked: List<String>, matched: List<String>, skipped: List<String>): List<String> {
		val uidList = mutableListOf<String>()
		uidList.addAll(liked)
		uidList.addAll(matched)
		uidList.addAll(skipped)
		return uidList
	}

	/*
	* GET LIKED USERS IDS LIST
	*/
	private fun getLikedList(): Single<List<String>> {
		return Single.create(SingleOnSubscribe<List<String>>{ emitter ->
			val query = currentUserDocRef
				.collection(USER_LIKED_COLLECTION_REFERENCE)
			query
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it){
							if (!likedList.contains(doc.id))
								likedList.add(doc.id)
						}
						emitter.onSuccess(likedList)
					}
					else emitter.onSuccess(likedList)
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())

	}

	/*
	* GET MATCHED IDS LIST
	*/
	private fun getMatchedList(): Single<List<String>> {
		//Log.wtf(TAG, "get matched called")
		return Single.create(SingleOnSubscribe<List<String>> { emitter ->
			val query = currentUserDocRef
				.collection(USER_MATCHED_COLLECTION_REFERENCE)
			query
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it){
							if (!matchedList.contains(doc.id))
								matchedList.add(doc.id)
						}
						emitter.onSuccess(matchedList)
					}
					else emitter.onSuccess(matchedList) }
				.addOnFailureListener { emitter.onError(it) }

		}).observeOn(Schedulers.io())
	}

	/*
	* GET SKIPPED USERS IDS LIST
	*/
	private fun getSkippedList(): Single<List<String>> {
		//Log.wtf(TAG, "get skipped called")
		return Single.create(SingleOnSubscribe<List<String>> { emitter ->
			val query = currentUserDocRef
				.collection(USER_SKIPPED_COLLECTION_REFERENCE)

			query
				.get()
				.addOnSuccessListener {
					if (!it.isEmpty) {
						for (doc in it) {
							if (!skippedList.contains(doc.id))
								skippedList.add(doc.id)
						}
						emitter.onSuccess(skippedList)
					}
					else emitter.onSuccess(skippedList)
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())
	}


	/**
	 * 1. add to matches collection for liked user
	 * 2. add to matches collection for CURRENT user
	 * 3. remove from likes collection for liked user
	 * 4. remove from likes collection for CURRENT user
	 */
	private fun handleMatch(matchedUserItem: MatchedUserItem, currentUserMatchedItem: MatchedUserItem) {
		addToMatchCollection(userForWhichToAdd = matchedUserItem.baseUserInfo,
		                     whomToAdd = currentUserMatchedItem)

		addToMatchCollection(userForWhichToAdd = currentUserMatchedItem.baseUserInfo,
		                     whomToAdd = matchedUserItem)

		//note:uncomment for release
		deleteFromLikesCollection(userForWhichDelete = matchedUserItem.baseUserInfo,
		                          whomToDeleteId = currentUser.baseUserInfo.userId)

		deleteFromLikesCollection(userForWhichDelete = currentUserMatchedItem.baseUserInfo,
		                          whomToDeleteId = matchedUserItem.baseUserInfo.userId)

		Log.wtf(TAG, "match handle executed")
	}

	private fun addToMatchCollection(userForWhichToAdd: BaseUserInfo, whomToAdd: MatchedUserItem) {
		firestore.collection(USERS_COLLECTION_REFERENCE)
			.document(userForWhichToAdd.city)
			.collection(userForWhichToAdd.gender)
			.document(userForWhichToAdd.userId)
			.collection(USER_MATCHED_COLLECTION_REFERENCE)
			.document(whomToAdd.baseUserInfo.userId)
			.set(whomToAdd)

	}

	private fun deleteFromLikesCollection(userForWhichDelete: BaseUserInfo, whomToDeleteId: String) {
		firestore.collection(USERS_COLLECTION_REFERENCE)
			.document(userForWhichDelete.city)
			.collection(userForWhichDelete.gender)
			.document(userForWhichDelete.userId)
			.collection(USER_LIKED_COLLECTION_REFERENCE)
			.document(whomToDeleteId)
			.delete()
	}

}