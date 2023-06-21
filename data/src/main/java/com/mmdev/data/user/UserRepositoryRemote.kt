package com.mmdev.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.mmdev.business.user.model.UserItem
import com.mmdev.business.user.repository.UserRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/* Created by A on 10.11.2019.*/

/**
 * This is the documentation block about the class
 */

@Singleton
class UserRepositoryRemote @Inject constructor(private val firestore: FirebaseFirestore):
		UserRepository.RemoteUserRepository {

	companion object {
		private const val GENERAL_COLLECTION_REFERENCE = "users"
	}

	override fun createUserOnRemote(): Completable {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun deleteUser(userId: String): Completable {
		return Completable.create { emitter ->
			val ref = firestore
				.collection(GENERAL_COLLECTION_REFERENCE)
				.document(userId)
			ref.delete()
				.addOnSuccessListener {
					emitter.onComplete()
				}
				.addOnFailureListener { emitter.onError(it) }
		}.subscribeOn(Schedulers.io())
	}

	override fun getUserById(userId: String): Single<UserItem> {
		return Single.create(SingleOnSubscribe<UserItem> { emitter ->
			firestore.collection(GENERAL_COLLECTION_REFERENCE)
				.document(userId)
				.get()
				.addOnSuccessListener {
					if (it.exists() && it != null)
						emitter.onSuccess(it.toObject(UserItem::class.java)!!)
					else emitter.onError(Exception("User doesn't exist"))
				}
				.addOnFailureListener { emitter.onError(it) }
		}).subscribeOn(Schedulers.io())
	}
}