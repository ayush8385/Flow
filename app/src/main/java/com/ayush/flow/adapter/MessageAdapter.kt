package com.ayush.flow.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.database.MessageEntity
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val allMsgs=ArrayList<MessageEntity>()
    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val message:TextView=view.findViewById(R.id.txt_msg)
        val time:TextView=view.findViewById(R.id.msg_time)
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
        var chat=allMsgs[position]
        holder.message.text=chat.message
        holder.time.text=chat.time

    }

    override fun getItemCount(): Int {
        return allMsgs.size
    }

    override fun getItemViewType(position: Int): Int {
        val id=this.allMsgs.get(position).sender
        if(id==firebaseUser!!.uid){
            return 1
        }
        else{
            return 0
        }
    }

    fun updateList(msgList:ArrayList<MessageEntity>){
        allMsgs.clear()
        allMsgs.addAll(msgList)
        notifyDataSetChanged()
    }
}