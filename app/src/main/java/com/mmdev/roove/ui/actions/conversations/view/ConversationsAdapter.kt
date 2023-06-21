package com.mmdev.roove.ui.actions.conversations.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmdev.business.conversations.model.ConversationItem
import com.mmdev.roove.R
import com.mmdev.roove.databinding.FragmentConversationsItemBinding


/* Created by A on 27.10.2019.*/

/**
 * This is the documentation block about the class
 */

class ConversationsAdapter (private var conversationsList: List<ConversationItem>):
		RecyclerView.Adapter<ConversationsAdapter.ConversationsViewHolder>() {


	private lateinit var clickListener: OnItemClickListener


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		ConversationsViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
		                                                R.layout.fragment_conversations_item,
		                                                parent,
		                                                false))

	override fun onBindViewHolder(holder: ConversationsViewHolder, position: Int) {
		holder.bind(conversationsList[position])
	}

	override fun getItemCount() = conversationsList.size

	fun updateData(conversations: List<ConversationItem>) {
		conversationsList = conversations
		notifyDataSetChanged()
	}

	fun getConversationItem(position: Int) = conversationsList[position]

	// allows clicks events to be caught
	fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
		clickListener = itemClickListener
	}

	inner class ConversationsViewHolder(private val binding: FragmentConversationsItemBinding):
			RecyclerView.ViewHolder(binding.root){

		init {
			itemView.setOnClickListener {
				clickListener.onItemClick(itemView.rootView, adapterPosition)
			}
		}

		fun bind(conversationItem: ConversationItem){
			binding.conversationItem = conversationItem
			binding.executePendingBindings()
		}

	}


	// parent fragment will override this method to respond to click events
	interface OnItemClickListener {
		fun onItemClick(view: View, position: Int)
	}
}