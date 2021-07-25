package com.ayush.flow.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.database.MessageEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageAdapter(val context: Context):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val allMsgs=ArrayList<MessageEntity>()
    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val message:TextView=view.findViewById(R.id.txt_msg)
        val time:TextView=view.findViewById(R.id.msg_time)
        val msg_box:RelativeLayout=view.findViewById(R.id.msg_box)
        val seen_txt:TextView=view.findViewById(R.id.txt_seen)
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        var chat=allMsgs[position]
        holder.message.text=chat.message
        holder.time.text=chat.time
        holder.seen_txt.text="sent"

        if(chat.sender!=firebaseUser!!.uid && !chat.seen){

            val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(chat.mid).exists()){
                        FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(chat.mid).child("seen").setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
        else{
            if(chat.recev){
                holder.seen_txt.text="Delivered"
                if(chat.seen){
                    holder.seen_txt.text="seen"
                }
            }
            holder.msg_box.setOnClickListener {

                holder.seen_txt.visibility=View.VISIBLE
                Handler().postDelayed({
                    holder.seen_txt.visibility=View.GONE
                },400)
            }
        }

//        holder.msg_box.setOnLongClickListener(object :View.OnLongClickListener{
//            override fun onLongClick(v: View?): Boolean {
//
//            }
//
//        })

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