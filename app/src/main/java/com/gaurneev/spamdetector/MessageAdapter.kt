package com.gaurneev.spamdetector

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.NonDisposableHandle.parent
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context,private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderTextView: TextView = view.findViewById(R.id.tv_add)
        val messageBodyTextView: TextView = view.findViewById(R.id.tv_body)
        val timestampTextView: TextView = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.senderTextView.text = message.sender
        holder.messageBodyTextView.text = message.messageBody
        holder.timestampTextView.text = SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault()).format(
            Date(message.timestamp)
        )
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}