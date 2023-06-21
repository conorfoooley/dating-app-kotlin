package com.mmdev.roove.ui.chat.view

/* Created by A on 06.06.2019.*/

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.mmdev.business.chat.model.MessageItem
import com.mmdev.roove.R
import com.mmdev.roove.core.GlideApp
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter (private var userId: String,
                   private var listMessageItems: List<MessageItem>):
	RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){

	private lateinit var clickListener: OnItemClickListener

	companion object {
		private const val RIGHT_MSG = 0
		private const val LEFT_MSG = 1
		private const val RIGHT_MSG_IMG = 2
		private const val LEFT_MSG_IMG = 3
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		if (viewType == RIGHT_MSG || viewType == RIGHT_MSG_IMG)
			ChatViewHolder(LayoutInflater.from(parent.context)
				               .inflate(R.layout.fragment_chat_item_right,
				                        parent,
				                        false))
			else ChatViewHolder(LayoutInflater.from(parent.context)
				.inflate(R.layout.fragment_chat_item_left,
				         parent,
				         false))

	override fun onBindViewHolder(viewHolder: ChatViewHolder, position: Int) {
		viewHolder.setMessageType(getItemViewType(position))
		viewHolder.bind(listMessageItems[position])
	}

	override fun getItemViewType(position: Int): Int {
		val (sender, _, _,photoAttached) = listMessageItems[position]
		return if (photoAttached != null)
			if (sender.userId == userId) RIGHT_MSG_IMG else LEFT_MSG_IMG
		else if (sender.userId == userId) RIGHT_MSG else LEFT_MSG
	}

	override fun getItemCount() = listMessageItems.size

	fun updateData(chats: List<MessageItem>) {
		this.listMessageItems = chats
		notifyDataSetChanged()
	}

	fun getItem(position: Int) = listMessageItems[position]


	/* note: USE FOR -DEBUG ONLY */
//	fun changeSenderName(name:String){
//		userId = name
//	}

	// allows clicks events to be caught
	fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
		clickListener = itemClickListener
	}

	inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view){

		init {
			itemView.setOnClickListener {
				clickListener.onItemClick(itemView.rootView, adapterPosition)
			}
		}

		private val tvTextMessage: TextView = itemView.findViewById(R.id.item_message_tvMessage)
		private val tvTimestamp: TextView = itemView.findViewById(R.id.item_message_tvTimestamp)
		private val ivUserAvatar: ImageView = itemView.findViewById(R.id.item_message_ivUserPic)
		private val ivChatPhoto: ImageView = itemView.findViewById(R.id.img_chat)

		fun setMessageType(messageType: Int) {
			when (messageType) {
				RIGHT_MSG -> ivChatPhoto.visibility = View.GONE
				LEFT_MSG -> ivChatPhoto.visibility = View.GONE
				RIGHT_MSG_IMG -> tvTextMessage.visibility = View.GONE
				LEFT_MSG_IMG -> tvTextMessage.visibility = View.GONE
			}
		}

		fun bind(messageItem: MessageItem) {
			setIvUserAvatar(messageItem.sender.mainPhotoUrl)
			setTextMessage(messageItem.text)
			messageItem.timestamp?.let { setTvTimestamp(convertTimestamp(messageItem.timestamp!!)) }
			setIvChatPhoto(messageItem.photoAttachementItem?.fileUrl)
		}

		/* sets user profile pic in ImgView binded layout */
		private fun setIvUserAvatar(urlPhotoUser: String) {
			Glide.with(ivUserAvatar.context)
				.load(urlPhotoUser)
				.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
				.centerCrop()
				.apply(RequestOptions().circleCrop())
				.into(ivUserAvatar)
		}

		/* sets text message in TxtView binded layout */
		private fun setTextMessage(message: String?) { tvTextMessage.text = message }

		/* set timestamp in TxtView located below message with time when this message was sent */
		private fun setTvTimestamp(timestamp: String) { tvTimestamp.text = timestamp }

		/* set photo that user sends in chat */
		private fun setIvChatPhoto(url: String?) {
			GlideApp.with(ivChatPhoto.context)
				.load(url)
				.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
				.centerCrop()
				.apply(RequestOptions().circleCrop())
				.into(ivChatPhoto)
		}

	}

	/**
	 * parsing timestamp to display in traditional format
	 * @param date timestamp made by firestore
	 * @return string in format hh:mm AM/PM
	 */
	private fun convertTimestamp(date: Date): String {
		return SimpleDateFormat("EEE, d MMM yyyy hh:mm a", Locale.ENGLISH).format(date)
	}

	// parent fragment will override this method to respond to click events
	interface OnItemClickListener {
		fun onItemClick(view: View, position: Int)
	}

}
