/*
 * Created by Andrii Kovalchuk on 02.12.19 20:57
 * Copyright (c) 2019. All rights reserved.
 * Last modified 02.12.19 20:50
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.mmdev.roove.utils

import android.graphics.Rect
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.mmdev.roove.R

fun AppCompatActivity.showToastText(text: String) =
	Toast.makeText(this, text, Toast.LENGTH_LONG).show()


fun FragmentManager.replaceFragmentInDrawer(fragment: Fragment) {
	val fragmentName = fragment.javaClass.name
	val fragmentPopped = popBackStackImmediate(fragmentName, 0)

	if (!fragmentPopped){ //fragment not in back stack, create it.
		beginTransaction().apply {
			setCustomAnimations(R.anim.enter_from_right,
			                    R.anim.exit_to_left,
			                    R.anim.enter_from_left,
			                    R.anim.exit_to_right)
			replace(R.id.drawerContainer, fragment, fragmentName)
			addToBackStack(fragmentName)
			commit()
		}
	}
}

fun View.addSystemTopPadding(targetView: View = this, isConsumed: Boolean = false) {
	doOnApplyWindowInsets { _, insets, initialPadding ->
		targetView.updatePadding(top = initialPadding.top + insets.systemWindowInsetTop)

		if (isConsumed) {
			insets
				.replaceSystemWindowInsets(Rect(insets.systemWindowInsetLeft,
				                                0,
				                                insets.systemWindowInsetRight,
				                                insets.systemWindowInsetBottom))
		} else {
			insets
		}
	}
}

fun View.addSystemBottomPadding(targetView: View = this, isConsumed: Boolean = false) {
	doOnApplyWindowInsets { _, insets, initialPadding ->
		targetView.updatePadding(bottom = initialPadding.bottom + insets.systemWindowInsetBottom)

		if (isConsumed) {
			insets
				.replaceSystemWindowInsets(Rect(insets.systemWindowInsetLeft,
				                                insets.systemWindowInsetTop,
				                                insets.systemWindowInsetRight,
				                                0))
		} else {
			insets
		}
	}
}


fun View.doOnApplyWindowInsets(block: (View, insets: WindowInsetsCompat, initialPadding: Rect) -> WindowInsetsCompat) {
	val initialPadding = recordInitialPaddingForView(this)
	ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
		block(v, insets, initialPadding)
	}
	requestApplyInsetsWhenAttached()
}

private fun recordInitialPaddingForView(view: View) =
	Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

private fun View.requestApplyInsetsWhenAttached() {
	if (isAttachedToWindow) {
		ViewCompat.requestApplyInsets(this)
	}
	else {
		addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
			override fun onViewAttachedToWindow(v: View) {
				v.removeOnAttachStateChangeListener(this)
				ViewCompat.requestApplyInsets(v)
			}

			override fun onViewDetachedFromWindow(v: View) = Unit
		})
	}
}
