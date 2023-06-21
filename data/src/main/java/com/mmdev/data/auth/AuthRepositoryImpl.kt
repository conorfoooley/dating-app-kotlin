/*
 * Created by Andrii Kovalchuk
 * Copyright (c) 2020. All rights reserved.
 * Last modified 26.03.20 19:53
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.data.auth

import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.mmdev.business.auth.AuthUserItem
import com.mmdev.business.auth.repository.AuthRepository
import com.mmdev.business.core.BaseUserInfo
import com.mmdev.business.core.UserItem
import com.mmdev.data.core.BaseRepositoryImpl
import com.mmdev.data.user.UserWrapper
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(private val auth: FirebaseAuth,
                                             private val fInstance: FirebaseInstanceId,
                                             private val firestore: FirebaseFirestore,
                                             private val fbLogin: LoginManager,
                                             private val userWrapper: UserWrapper):
		AuthRepository, BaseRepositoryImpl(firestore, userWrapper) {


	/**
	 * Observable which track the auth changes of [FirebaseAuth] to listen when an user is logged or not.
	 *
	 * @return an [Observable] which emits every time that the [FirebaseAuth] state change.
	 */
	override fun isAuthenticatedListener(): Observable<Boolean> {
		return Observable.create(ObservableOnSubscribe<Boolean>{ emitter ->
			val authStateListener = FirebaseAuth.AuthStateListener { auth ->
				if (auth.currentUser == null) {
					emitter.onNext(false)
					//Log.wtf(TAG, "current user is null +false")
				}
				else if (auth.currentUser != null) {
					//Log.wtf(TAG, "current user is not null")
					val ref = firestore.collection(USERS_BASE_COLLECTION_REFERENCE)
						.document(auth.currentUser!!.uid)
					ref
						.get()
						.addOnSuccessListener {
							//Log.wtf(TAG, "success get document")
							if (it.exists()){
								emitter.onNext(true)
								//Log.wtf(TAG, "document exists")
							}
							else emitter.onNext(false)
						}
						.addOnFailureListener { emitter.onError(it) }
				}
			}
			auth.addAuthStateListener(authStateListener)
			emitter.setCancellable { auth.removeAuthStateListener(authStateListener) }
		}).subscribeOn(Schedulers.io())
	}

	override fun signIn(token: String): Single<HashMap<Boolean, BaseUserInfo>> =
		signInWithFacebook(token)
			.flatMap { checkAndRetrieveFullUser(it) }
			.subscribeOn(Schedulers.io())

	/**
	 * create new [UserItem] documents in db
	 */
	override fun registerUser(userItem: UserItem): Completable =
		Completable.create { emitter ->
			val authUserItem = AuthUserItem(userItem.baseUserInfo)
			val ref = firestore.collection(USERS_COLLECTION_REFERENCE)
				.document(userItem.baseUserInfo.city)
				.collection(userItem.baseUserInfo.gender)
				.document(userItem.baseUserInfo.userId)
			fInstance.instanceId.addOnSuccessListener { instanceResult ->
				authUserItem.registrationTokens.add(instanceResult.token)
				ref.set(userItem)
					.addOnSuccessListener {
						firestore.collection(USERS_BASE_COLLECTION_REFERENCE)
							.document(userItem.baseUserInfo.userId)
							.set(authUserItem)
							.addOnSuccessListener {
								userWrapper.setUser(userItem)
								emitter.onComplete()
							}
							.addOnFailureListener { emitter.onError(it) }
					}.addOnFailureListener { emitter.onError(it) }
			}.addOnFailureListener { emitter.onError(it) }
		}.subscribeOn(Schedulers.io())


	override fun logOut(){
		if (auth.currentUser != null) {
			auth.signOut()
			fbLogin.logOut()
		}
	}

	/**
	 * this fun is called first when user is trying to sign in via facebook
	 * creates a basic [BaseUserInfo] object based on public facebook profile
	 */
	private fun signInWithFacebook(token: String): Single<BaseUserInfo> =
		Single.create(SingleOnSubscribe<BaseUserInfo> { emitter ->
			val credential = FacebookAuthProvider.getCredential(token)
			auth.signInWithCredential(credential)
				.addOnCompleteListener{
					if (it.isSuccessful && auth.currentUser != null) {
						val firebaseUser = auth.currentUser!!
						val photoUrl = firebaseUser.photoUrl.toString() + "?height=1000"
						val baseUser = BaseUserInfo(name = firebaseUser.displayName!!,
						                            mainPhotoUrl = photoUrl,
						                            userId = firebaseUser.uid)
						emitter.onSuccess(baseUser)
					}
					else emitter.onError(Exception(it.exception))
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())

	private fun checkAndRetrieveFullUser(baseUserInfo: BaseUserInfo): Single<HashMap<Boolean, BaseUserInfo>> =
		Single.create(SingleOnSubscribe<HashMap<Boolean, BaseUserInfo>> { emitter ->
			firestore.collection(USERS_BASE_COLLECTION_REFERENCE)
				.document(baseUserInfo.userId)
				.get()
				.addOnSuccessListener { baseUserDoc ->
					//if base info about user exists in db
					if (baseUserDoc.exists()) {
						val userInBase = baseUserDoc.toObject(AuthUserItem::class.java)!!
						//Log.wtf(TAG, "$userInBase")
						firestore.collection(USERS_COLLECTION_REFERENCE)
							.document(userInBase.baseUserInfo.city)
							.collection(userInBase.baseUserInfo.gender)
							.document(userInBase.baseUserInfo.userId)
							.get()
							.addOnSuccessListener { fullUserDoc ->
								//if full user info exists in db => return continue reg flag false
								if (fullUserDoc.exists()) {
									val retrievedUser = fullUserDoc.toObject(UserItem::class.java)!!
									emitter.onSuccess(hashMapOf(false to retrievedUser.baseUserInfo))
								}
								//auth user exists but full is not => return continue reg flag "true"
								else emitter.onSuccess(hashMapOf(true to userInBase.baseUserInfo))
							}
							.addOnFailureListener { emitter.onError(it) }
					}
					//if user is not stored => return new UserItem with continue reg flag "true"
					else emitter.onSuccess(hashMapOf(true to baseUserInfo))
				}
				.addOnFailureListener { emitter.onError(it) }
		}).observeOn(Schedulers.io())

}


