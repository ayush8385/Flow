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
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.util.*

class ChatAdapter(val context: Context):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
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
        }
        holder.vdo_call.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
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

    inner class loadImage(val image:String,val holder: HomeViewHolder):
        AsyncTask<Void, Void, Boolean>(){
        var b: Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            holder.image.setImageBitmap(b)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
            b= BitmapFactory.decodeStream(FileInputStream(f))
            return true
        }
    }
}