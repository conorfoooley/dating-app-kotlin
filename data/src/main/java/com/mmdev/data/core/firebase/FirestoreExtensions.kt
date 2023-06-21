/*
 * Created by Andrii Kovalchuk
 * Copyright (C) 2020. roove
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses
 */

package com.mmdev.data.core.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.mmdev.data.core.log.logDebug
import com.mmdev.data.core.log.logError
import com.mmdev.data.core.log.logInfo
import com.mmdev.data.core.log.logWarn
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.operators.completable.CompletableCreate
import io.reactivex.rxjava3.internal.operators.single.SingleCreate

/**
 * Rx wrappers for firestore callbacks api
 */

private const val TAG = "mylogs_FirestoreExtensions"
const val FIRESTORE_NO_DOCUMENT_EXCEPTION = "No such document."

internal fun <T> DocumentReference.getAndDeserializeAsSingle(clazz: Class<T>): Single<T> =
	SingleCreate { emitter ->
		get()
			.addOnSuccessListener { snapshot ->
				logInfo(TAG, "Document retrieve success")
				if (snapshot.exists() && snapshot.data != null) {
					
					logDebug(TAG, "Data is not null, deserialization in process...")
					val serializedDoc: T = snapshot.toObject(clazz)!!
					
					logInfo(TAG, "Deserialization to ${clazz.simpleName} succeed...")
					emitter.onSuccess(serializedDoc)
				}
				else {
					logError(TAG, FIRESTORE_NO_DOCUMENT_EXCEPTION)
					emitter.onError(NoSuchElementException(FIRESTORE_NO_DOCUMENT_EXCEPTION))
				}
				
			}
			.addOnFailureListener { exception ->
				logError(TAG, "Failed to retrieve document from backend: $exception")
				emitter.onError(exception)
			}
	}


fun <T> Query.executeAndDeserializeSingle(clazz: Class<T>): Single<List<T>> = SingleCreate { emitter ->
	logDebug(TAG, "Trying to execute given query...")
	get()
		.addOnSuccessListener { querySnapshot ->
			if (!querySnapshot.isEmpty) {
				logInfo(TAG, "Query executed successfully, printing first 5 documents...")
				querySnapshot.documents.take(5).forEach { logInfo(TAG, it.reference.path) }
				
				logDebug(TAG, "Query deserialization to ${clazz.simpleName} in process...")
				val resultList = querySnapshot.toObjects(clazz)
				
				logInfo(TAG, "Deserialization to ${clazz.simpleName} succeed...")
				emitter.onSuccess(resultList)
			}
			else {
				logWarn(TAG, "Query result is empty.")
				emitter.onSuccess(emptyList())
			}
			
		}
		.addOnFailureListener { exception ->
			logError(TAG, "Failed to execute given query: $exception")
			emitter.onError(exception)
		}
}



fun <T> DocumentReference.setAsCompletable(dataClass: T): Completable = CompletableCreate { emitter ->
	set(dataClass!!)
		.addOnSuccessListener {
			logInfo(TAG, "Set $dataClass as document successfully")
			emitter.onComplete()
		}
		.addOnFailureListener { exception ->
			logError(TAG, "set $dataClass as document error: $exception")
			emitter.onError(exception)
		}
		
	}
