package com.ayush.flow.flow.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import java.util.*

class MessageAdapter(val context: Context, val items: ArrayList<com.ayush.flow.flow.model.Message>):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val message:TextView=view.findViewById(R.id.txt_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType==1){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_single_row,parent,false)
            return MessageViewHolder(view)
        }
        else{
             val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_single_row,parent,false)
            return MessageViewHolder(view)
        }


    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        var chat=items[position]
        holder.message.text=chat.message

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return this.items.get(position).id
    }
}