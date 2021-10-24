package com.ayush.flow.adapter


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.activity.Message
import com.ayush.flow.database.ChatEntity
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.util.*

class ChatAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    final val viewBinderHelper:ViewBinderHelper = ViewBinderHelper()
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val message:TextView=view.findViewById(R.id.profile_msg)
        val time:TextView=view.findViewById(R.id.timer)
        val unread:TextView=view.findViewById(R.id.unread_chat)
        val chat_box:RelativeLayout=view.findViewById(R.id.paren)

        val call:ImageView=view.findViewById(R.id.call_chat)
        val vdo_call:ImageView=view.findViewById(R.id.vdo_call_chat)
        val del_chat:ImageView=view.findViewById(R.id.del_chat)
        val hide:ImageView=view.findViewById(R.id.hide_chat)
        val swipeRevealLayout:SwipeRevealLayout=view.findViewById(R.id.swipelayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.chat_tem_swipe,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=allChats[position]

        if(chat.name==""){
            holder.name.text=chat.number
        }
        else{
            holder.name.text=chat.name
        }
        holder.message.text=chat.lst_msg
        holder.time.text=chat.time

        if(chat.image!=""){
          loadImage(chat.image, holder).execute()
        }
        else{
            holder.image.setImageResource(R.drawable.user)
        }

        holder.chat_box.setOnClickListener {

            if(holder.swipeRevealLayout.isOpened){
                holder.swipeRevealLayout.close(true)
            }
            else {
                val intent = Intent(context, Message::class.java)
                intent.putExtra("name", chat.name)
                intent.putExtra("number", chat.number)
                intent.putExtra("userid", chat.id)
                intent.putExtra("image", chat.image)
                context.startActivity(intent)
            }


        }

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.swipeRevealLayout,chat.id)
        viewBinderHelper.closeLayout(chat.id)



        holder.call.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.audioCall(holder.name.text.toString(),chat.id,chat.image)

        }
        holder.vdo_call.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.videoCall(holder.name.text.toString(),chat.id,chat.image)
        }
        holder.del_chat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.deleteChat(chat.id,chat.name)
        }
        holder.hide.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.hideChat(chat.id,chat.name)
        }

        holder.image.setOnClickListener {
            Message().openProfileBottomSheet(context,chat.name,holder.image,chat.id,chat.image,false)
        }

    }

    override fun getItemCount(): Int {
        return allChats.size
    }

    fun updateList(list: List<ChatEntity>) {
        allChats.clear()
        allChats.addAll(list)
        notifyDataSetChanged()
    }

    interface OnAdapterItemClickListener {
        fun audioCall(toString: String, id: String,image: String)
        fun videoCall(toString: String,id: String,image: String)
        fun deleteChat(id:String,name:String)
        fun hideChat(id:String,name: String)
    }

    inner class loadImage(val image:String,val holder: HomeViewHolder):
        AsyncTask<Void, Void, Boolean>(){
        var b: Bitmap?=null
        var f:File?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            Picasso.get().load(f!!).into(holder.image)

        }
        override fun doInBackground(vararg params: Void?): Boolean {
            f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)

            if(f!!.exists()){
                b= BitmapFactory.decodeStream(FileInputStream(f))
            }
            return true
        }
    }
}