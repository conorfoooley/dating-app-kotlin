package com.mmdev.meetapp.ui.chat.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmdev.domain.messages.model.Message
import com.mmdev.domain.messages.model.Sender
import com.mmdev.meetapp.BuildConfig
import com.mmdev.meetapp.R
import com.mmdev.meetapp.core.injector
import com.mmdev.meetapp.models.ProfileModel
import com.mmdev.meetapp.ui.MainActivity
import com.mmdev.meetapp.ui.ProfileViewModel
import com.mmdev.meetapp.ui.chat.viewmodel.ChatViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.util.*

/* Created by A on 10.07.2019.*/

/**
 * This is the documentation block about the class
 */

class ChatFragment : Fragment(), ClickChatAttachmentFirebase {

	private lateinit var  mMainActivity: MainActivity

	private val factory = injector.chatViewModelFactory()
	private lateinit var chatViewModel: ChatViewModel

	// POJO models
	private lateinit var mProfileModel: ProfileModel
	private lateinit var mSender: Sender

	// Views UI
	private lateinit var edMessageWrite: EditText
	private lateinit var mChatAdapter: ChatAdapter

	// File
	private lateinit var mFilePathImageCamera: File

	private val disposables = CompositeDisposable()


	//static fields
	companion object {
		private const val IMAGE_GALLERY_REQUEST = 1
		private const val IMAGE_CAMERA_REQUEST = 2

		private val TAG = MainActivity::class.java.simpleName
		//static final String CHAT_REFERENCE = "chatmodel";

		// Gallery Permissions
		private const val REQUEST_STORAGE = 1
		private val PERMISSIONS_STORAGE =
			arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

		// Camera Permission
		private const val REQUEST_CAMERA = 2
		private val PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

		private const val ARG_USERNAME = "arg_username"

		fun newInstance(username: String): ChatFragment {
			val args = Bundle()
			args.putString(ARG_USERNAME, username)

			val fragment = ChatFragment()
			fragment.arguments = args
			return fragment
		}
	}


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_chat, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		activity?.let { mMainActivity = it as MainActivity }
		setupViews(view)
		chatViewModel = ViewModelProvider(mMainActivity, factory).get(ChatViewModel::class.java)

		mProfileModel = ViewModelProvider(mMainActivity, defaultViewModelProviderFactory)
			.get(ProfileViewModel::class.java)
			.getProfileModel(mMainActivity).value!!
		mSender = Sender(mProfileModel.name, mProfileModel.gender, mProfileModel.mainPhotoUrl,
			mProfileModel.userId)

		disposables
			.add(chatViewModel.getMessages()
				     .observeOn(AndroidSchedulers.mainThread())
				     .subscribe(
							{ mChatAdapter.updateData(it) },
							{ showInternetError() }))
	}

	/*
	 * setup managers and adapters for views
	 */
	private fun setupViews(view: View) {
		edMessageWrite = view.findViewById(R.id.editTextMessage)
		val rvMessagesList: RecyclerView = view.findViewById(R.id.messageRecyclerView)
		val ivAttachments: ImageView = view.findViewById(R.id.buttonAttachments)
		val ivSendMessage: ImageView = view.findViewById(R.id.buttonMessage)
		val linearLayoutManager = LinearLayoutManager(mMainActivity)
		linearLayoutManager.stackFromEnd = true
		rvMessagesList.layoutManager = linearLayoutManager
		ivSendMessage.setOnClickListener { sendMessageClick() }
		ivAttachments.setOnClickListener { photoCameraClick() }
		mChatAdapter = ChatAdapter(mSender.name, listOf(),this)
		mChatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
			override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
				super.onItemRangeInserted(positionStart, itemCount)
				val friendlyMessageCount = mChatAdapter.itemCount
				val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
				if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
					rvMessagesList.scrollToPosition(positionStart)
				}
			}
		})
		rvMessagesList.adapter = mChatAdapter
	}

	/*
	* Send plain text msg to chat if edittext is not empty
	* else shake animation
	*/
	private fun sendMessageClick() {
		if (edMessageWrite.text.isNotEmpty()) {
			val message = Message(mSender, edMessageWrite.text.toString(), null)
			disposables
				.add(chatViewModel.sendMessage(message)
					     .observeOn(AndroidSchedulers.mainThread())
					     .subscribe( { Log.d("ChatFragment", "Message sent") },
					                 { showInternetError() } ))
			edMessageWrite.setText("")
		}
		else edMessageWrite
			.startAnimation(AnimationUtils.loadAnimation(mMainActivity, R.anim.edittext_horizontal_shake))

	}

	/*
	 * Checks if the app has permissions to OPEN CAMERA and take photos
	 * If the app does not has permission then the user will be prompted to grant permissions
	 */
	private fun photoCameraClick() {
		// Check if we have needed permissions
		var result: Int
		val listPermissionsNeeded = ArrayList<String>()
		for (permission in PERMISSIONS_CAMERA) {
			result = ActivityCompat.checkSelfPermission(mMainActivity, permission)
			if (result != PackageManager.PERMISSION_GRANTED) listPermissionsNeeded.add(permission)
		}
		if (listPermissionsNeeded.isNotEmpty()) requestPermissions(PERMISSIONS_CAMERA, REQUEST_CAMERA)
		else startCameraIntent()
	}

	/*
	 * take photo directly by camera
	 */
	private fun startCameraIntent() {
		val namePhoto = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
		mFilePathImageCamera = File(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
		                            namePhoto + "camera.jpg")
		val photoURI = FileProvider.getUriForFile(mMainActivity,
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
	 */
	private fun photoGalleryClick() {
		if (ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
			requestPermissions(PERMISSIONS_STORAGE, REQUEST_STORAGE)
		else startGalleryIntent()
	}

	/*
	 * choose photo from gallery
	 */
	private fun startGalleryIntent() {
		val intent = Intent().apply {
			action = Intent.ACTION_GET_CONTENT
			type = "image/*"

		}
		startActivityForResult(Intent.createChooser(intent, "Select picture"), IMAGE_GALLERY_REQUEST)
	}

	/**
	 * click attached photo in chat
	 * @param view your view
	 * @param position pos
	 * @param nameUser sender name
	 * @param urlPhotoUser photo profile url sender
	 * @param urlPhotoClick clicked photo in chat url
	 */
	override fun clickImageChat(view: View, position: Int, nameUser: String, urlPhotoUser: String, urlPhotoClick: String) {
		val intent = Intent(mMainActivity, FullScreenImageActivity::class.java)
		intent.putExtra("urlPhotoClick", urlPhotoClick)
		startActivity(intent)
	}

	private fun showInternetError() {
		Toast.makeText(context, "Check internet connection", Toast.LENGTH_SHORT).show()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		// If request is cancelled, the result arrays are empty.
		if (requestCode == REQUEST_CAMERA)
		// permission was granted
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCameraIntent()
		if (requestCode == REQUEST_STORAGE)
		// permission was granted
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				startGalleryIntent()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == IMAGE_GALLERY_REQUEST) {
			if (resultCode == RESULT_OK) {
				val selectedImage = data?.data?.toFile()
				if (selectedImage != null) {
					disposables
						.add(chatViewModel.sendPhoto(Message(mSender,"", null),
						                             selectedImage)
						     .observeOn(AndroidSchedulers.mainThread())
						     .subscribe( { Log.d("ChatFragment", "Photo gallery sent") },
						                 { showInternetError() }))
				}
				else Toast.makeText(mMainActivity, "Photo uri is null", Toast.LENGTH_SHORT).show()
			}
		}

		if (requestCode == IMAGE_CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {
				if (mFilePathImageCamera.exists()) {
					disposables
						.add(chatViewModel.sendPhoto(Message(mSender,"", null),
						                            mFilePathImageCamera)
							     .observeOn(AndroidSchedulers.mainThread())
							     .subscribe( { Log.d("ChatFragment", "Photo camera sent") },
							                 { showInternetError() }))
				}
				else Toast.makeText(mMainActivity,
						"filePathImageCamera is null or filePathImageCamera isn't exists",
						Toast.LENGTH_SHORT)
						.show()
			}
		}
	}

	override fun onStop() {
		super.onStop()
		disposables.clear()
	}



}
