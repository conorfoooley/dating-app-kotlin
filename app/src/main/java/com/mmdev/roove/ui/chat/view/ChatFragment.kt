package com.mmdev.roove.ui.chat.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmdev.business.chat.model.MessageItem
import com.mmdev.business.user.model.UserItem
import com.mmdev.roove.BuildConfig
import com.mmdev.roove.R
import com.mmdev.roove.core.injector
import com.mmdev.roove.ui.actions.conversations.viewmodel.ConversationsViewModel
import com.mmdev.roove.ui.chat.viewmodel.ChatViewModel
import com.mmdev.roove.ui.main.view.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/* Created by A on 10.07.2019.*/

/**
 * This is the documentation block about the class
 */

class ChatFragment : Fragment(R.layout.fragment_chat) {

	private lateinit var  mMainActivity: MainActivity

	private val chatViewModelFactory = injector.chatViewModelFactory()
	private lateinit var chatViewModel: ChatViewModel

	private lateinit var conversationsVM: ConversationsViewModel
	private val conversationsVMFactory = injector.conversationsViewModelFactory()

	// POJO models
	private lateinit var userItemModel: UserItem

	// Views UI
	private lateinit var edMessageWrite: EditText
	private lateinit var mChatAdapter: ChatAdapter

	// File
	private lateinit var mFilePathImageCamera: File

	private lateinit var conversationId: String

	private val disposables = CompositeDisposable()



	//static fields
	companion object {
		private const val IMAGE_GALLERY_REQUEST = 1
		private const val IMAGE_CAMERA_REQUEST = 2

		private const val TAG = "mylogs"

		// Gallery Permissions
		private const val REQUEST_STORAGE = 1
		private val PERMISSIONS_STORAGE =
			arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

		// Camera Permission
		private const val REQUEST_CAMERA = 2
		private val PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

		//todo: remove data transfer between fragments, need to make it more abstract
		private const val CONVERSATION_KEY = "CONVERSATION_ID"
		@JvmStatic
		fun newInstance(conversationId: String) = ChatFragment().apply {
			arguments = Bundle().apply {
				putString(CONVERSATION_KEY, conversationId)
			}
		}

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		activity?.let { mMainActivity = it as MainActivity }

		arguments?.let {
			conversationId = it.getString(CONVERSATION_KEY, "")
		}

		userItemModel = mMainActivity.userItemModel
		mChatAdapter = ChatAdapter(userItemModel.userId, listOf())

		chatViewModel = ViewModelProvider(mMainActivity, chatViewModelFactory).get(ChatViewModel::class.java)
		conversationsVM = ViewModelProvider(mMainActivity, conversationsVMFactory).get(ConversationsViewModel::class.java)

		if (conversationId.isNotEmpty()) {
			chatViewModel.setConversation(conversationId)
			disposables.add(chatViewModel.getMessages()
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { mMainActivity.progressDialog.showDialog() }
				.doOnNext { mMainActivity.progressDialog.dismissDialog() }
				.doFinally { mMainActivity.progressDialog.dismissDialog() }
				.subscribe({ if(it.isNotEmpty()) mChatAdapter.updateData(it)
				           Log.wtf(TAG, "messages to show + $it")},
				           { mMainActivity.showInternetError("$it") }))
		}

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		edMessageWrite = view.findViewById(R.id.editTextMessage)
		val rvMessagesList = view.findViewById<RecyclerView>(R.id.chat_messages_rv)
		val ivAttachments = view.findViewById<ImageView>(R.id.buttonAttachments)
		val ivSendMessage = view.findViewById<ImageView>(R.id.buttonMessage)
		val linearLayoutManager = LinearLayoutManager(mMainActivity)
		linearLayoutManager.stackFromEnd = true
		ivSendMessage.setOnClickListener { sendMessageClick() }
		ivAttachments.setOnClickListener { photoGalleryClick() }

		mChatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
			override fun onChanged() {
				super.onChanged()
				val friendlyMessageCount = mChatAdapter.itemCount
				rvMessagesList.scrollToPosition(friendlyMessageCount-1)
			}
		})

		mChatAdapter.setOnItemClickListener(object: ChatAdapter.OnItemClickListener{
			override fun onItemClick(view: View, position: Int) {
				val intent = Intent(mMainActivity, FullScreenImageActivity::class.java)
				intent.putExtra("urlPhotoClick",
				                mChatAdapter.getItem(position).photoAttachementItem!!.fileUrl)
				startActivity(intent)
			}
		})

		rvMessagesList.apply {
			adapter = mChatAdapter
			layoutManager = linearLayoutManager
		}

	}

	/*
	* Send plain text msg to chat if edittext is not empty
	* else shake animation
	*/
	private fun sendMessageClick() {
		if (edMessageWrite.text.isNotEmpty() &&
		    edMessageWrite.text.toString().trim().isNotEmpty()) {

			val message = MessageItem(userItemModel,
			                          edMessageWrite.text.toString().trim(),
			                          photoAttachementItem = null)

			if (conversationId.isEmpty()) {
				disposables.add(conversationsVM.createConversation(mMainActivity.cardItemClicked)
	                .map {
		                chatViewModel.setConversation(it.conversationId)
	                }
	                .observeOn(AndroidSchedulers.mainThread())
	                .subscribe({
		                           Log.wtf(TAG, "creating conversations")
		                           disposables.add(chatViewModel.getMessages()
                                       .observeOn(AndroidSchedulers.mainThread())
                                       .subscribe({
	                                                  if(it.isNotEmpty()) mChatAdapter.updateData(it)
	                                                  Log.wtf(TAG, "messages to show + $it")
                                                  },
                                                  { mMainActivity.showInternetError("$it") }))
	                           },
	                           {
		                           Log.wtf(TAG, "error + $it")
	                           }))
			}

			disposables.add(chatViewModel.sendMessage(message)
				.observeOn(AndroidSchedulers.mainThread())
                .doOnComplete{edMessageWrite.setText("")}
				.subscribe( { Log.wtf(TAG, "Message sent fragment_chat") },
							{ Log.wtf(TAG, "can't send message fragment_chat") } ))

		}
		else edMessageWrite.startAnimation(AnimationUtils.loadAnimation(mMainActivity,
		                                                                R.anim.horizontal_shake))

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

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		// If request is cancelled, the result arrays are empty.
		if (requestCode == REQUEST_CAMERA)
		// check camera permission was granted
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				startCameraIntent()
		if (requestCode == REQUEST_STORAGE)
		// check gallery permission was granted
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				startGalleryIntent()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		// send photo from gallery
		if (requestCode == IMAGE_GALLERY_REQUEST) {
			if (resultCode == RESULT_OK) {

				val selectedUri = data?.data
				disposables.add(chatViewModel.sendPhoto(selectedUri.toString())
	                .observeOn(AndroidSchedulers.mainThread())
	                .flatMapCompletable { chatViewModel.sendMessage(MessageItem(userItemModel, photoAttachementItem = it)) }
	                .doOnSubscribe { mMainActivity.progressDialog.showDialog() }
	                .doOnComplete { mMainActivity.progressDialog.dismissDialog() }
	                .subscribe({ Log.wtf(TAG, "Photo gallery sent") },
	                           { mMainActivity.showInternetError("$it") }))



			}
		}
		// send photo taken by camera
		if (requestCode == IMAGE_CAMERA_REQUEST) {
			if (resultCode == RESULT_OK) {

				if (mFilePathImageCamera.exists()) {
					disposables.add(chatViewModel.sendPhoto(Uri.fromFile(mFilePathImageCamera).toString())
		                .observeOn(AndroidSchedulers.mainThread())
		                .flatMapCompletable { chatViewModel.sendMessage(MessageItem(userItemModel, photoAttachementItem = it)) }
		                .doOnSubscribe { mMainActivity.progressDialog.showDialog() }
		                .doOnComplete { mMainActivity.progressDialog.dismissDialog() }
		                .subscribe( { Log.wtf(TAG, "Photo camera sent") },
                                    { mMainActivity.showInternetError("$it") }))

				}
				else Toast.makeText(mMainActivity,
						"filePathImageCamera is null or filePathImageCamera isn't exists",
						Toast.LENGTH_LONG)
						.show()
			}
		}
	}

	override fun onResume() {
		super.onResume()
		mMainActivity.toolbar.title = mMainActivity.partnerName
		mMainActivity.setNonScrollableToolbar()
	}

	override fun onDestroy() {
		super.onDestroy()
		disposables.clear()
	}



}
