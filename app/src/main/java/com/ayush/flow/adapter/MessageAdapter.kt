package com.ayush.flow.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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
import java.io.File
import java.io.FileInputStream

class MessageAdapter(val context: Context,val selectedMsg: ArrayList<MessageEntity>,private val clickListener: OnAdapterItemClickListener):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val allMsgs=ArrayList<MessageEntity>()



    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){

        val message:TextView=view.findViewById(R.id.txt_msg)
        val time:TextView=view.findViewById(R.id.msg_time)
        val msg_box:RelativeLayout=view.findViewById(R.id.msg_box)
        val seen_txt:TextView=view.findViewById(R.id.txt_seen)
        val parent:RelativeLayout=view.findViewById(R.id.msg_par)
        val select:ImageView=view.findViewById(R.id.select)
        val image_msg:ImageView=view.findViewById(R.id.img_msg)
        val progressBar:ProgressBar=view.findViewById(R.id.progressbar)
      //  val box:CardView=view.findViewById(R.id.box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType==1){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==2){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_img_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==3){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_img_single_row,parent,false)
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


        if(chat.type=="image"){

            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Chat Images"),chat.message)
            holder.image_msg.setImageBitmap(BitmapFactory.decodeStream(FileInputStream(f)))



            if(chat.sender==firebaseUser!!.uid){
                if(chat.sent){
                    holder.seen_txt.text="sent"
                    holder.progressBar.visibility=View.GONE
                }
                else{
                    holder.seen_txt.text="sending..."
                    holder.progressBar.visibility=View.VISIBLE
                }
            }

        }
        else{
            holder.message.text=chat.message
            holder.seen_txt.text="sent"
        }
        holder.time.text=chat.time

        holder.select.visibility=View.GONE

        if(chat in selectedMsg){
            holder.select.visibility=View.VISIBLE
        }

        if(chat.sender!=firebaseUser!!.uid && !chat.seen){

            val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(chat.mid)){
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

        holder.parent.setOnClickListener {
            if(selectedMsg.size!=0){
                if(chat in selectedMsg){
                    selectedMsg.remove(chat)
                    holder.select.visibility=View.GONE
                }
                else{
                    selectedMsg.add(chat)
                    holder.select.visibility=View.VISIBLE
                }
                clickListener.updateCount()
            }
        }


        holder.parent.setOnLongClickListener(object :View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {

                if(chat !in selectedMsg){
                    selectedMsg.add(chat)
                    clickListener.updateCount()
                    holder.select.visibility=View.VISIBLE
                }

                return true
            }

        })

    }

    override fun getItemCount(): Int {
        return allMsgs.size
    }

    interface OnAdapterItemClickListener {
        fun updateCount()
    }

    override fun getItemViewType(position: Int): Int {
        val id=this.allMsgs.get(position).sender
        val type=this.allMsgs.get(position).type
        if(id!=firebaseUser!!.uid && type=="image"){
            return 3
        }
        if(id==firebaseUser!!.uid && type=="image"){
            return 2
        }
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