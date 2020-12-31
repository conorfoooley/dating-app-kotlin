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

package com.mmdev.roove.core.permissions

import android.Manifest
import com.mmdev.roove.R

/**
 * This is the documentation block about the class
 */
sealed class AppPermission(val permissionsList: Array<String>,
                           val requestCode: Int,
                           val deniedMessageId: Int,
                           val explanationMessageId: Int) {
	companion object {
		const val REQUEST_CODE_CAMERA = 1
		const val REQUEST_CODE_GALLERY = 2
	}


	/**CAMERA PERMISSIONS**/
	object CAMERA : AppPermission(arrayOf(Manifest.permission.CAMERA,
	                                      Manifest.permission.READ_EXTERNAL_STORAGE),
	                              REQUEST_CODE_CAMERA,
	                              R.string.permission_camera_denied,
	                              R.string.permission_camera_explanation)

	/**READ/WRITE TO STORAGE PERMISSIONS**/
	object GALLERY : AppPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
	                                       Manifest.permission.WRITE_EXTERNAL_STORAGE),
	                               REQUEST_CODE_GALLERY,
	                               R.string.permission_read_ext_storage_denied,
	                               R.string.permission_read_ext_storage_explanation)


}
