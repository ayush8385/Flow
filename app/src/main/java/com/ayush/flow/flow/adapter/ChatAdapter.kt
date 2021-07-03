package com.ayush.flow.flow.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.flow.model.Chats
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatAdapter(val context: Context, val items: ArrayList<Chats>):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val message:TextView=view.findViewById(R.id.profile_msg)
        val time:TextView=view.findViewById(R.id.timer)
        val unread:TextView=view.findViewById(R.id.unread_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.chats_single_row,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=items[position]
        if(chat.image!=""){
            Picasso.get().load(chat.image).into(holder.image)
        }
        holder.name.text=chat.name
        holder.unread.visibility=View.VISIBLE
        holder.unread.text="25"
        holder.message.text=chat.message
        holder.time.text=chat.time

    }

    override fun getItemCount(): Int {
        return items.size
    }
}