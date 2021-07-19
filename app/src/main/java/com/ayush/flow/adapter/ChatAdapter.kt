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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.activity.Message
import com.ayush.flow.database.ChatEntity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.util.*

class ChatAdapter(val context: Context):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val message:TextView=view.findViewById(R.id.profile_msg)
        val time:TextView=view.findViewById(R.id.timer)
        val unread:TextView=view.findViewById(R.id.unread_chat)
        val chat_box:RelativeLayout=view.findViewById(R.id.paren)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.chats_single_row,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=allChats[position]

        holder.name.text=chat.name
        holder.message.text=chat.lst_msg
        holder.time.text=chat.time

        if(chat.image!=""){
          loadImage(chat.image, holder).execute()
        }

        holder.chat_box.setOnClickListener {
            val intent=Intent(context,Message::class.java)
            intent.putExtra("name",chat.name)
            intent.putExtra("userid",chat.id)
            intent.putExtra("image",chat.image)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return allChats.size
    }

    fun updateList(list: List<ChatEntity>) {
        allChats.clear()
        allChats.addAll(list)
        notifyItemInserted(list.size)
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