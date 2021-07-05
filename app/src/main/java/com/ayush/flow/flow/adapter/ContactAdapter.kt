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

class ContactAdapter(val context: Context, val items: ArrayList<Chats>):RecyclerView.Adapter<ContactAdapter.HomeViewHolder>() {
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val about:TextView=view.findViewById(R.id.profile_abt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.contacts_single_row,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=items[position]
        if(chat.image!=""){
            Picasso.get().load(chat.image).into(holder.image)
        }
        holder.name.text=chat.name
        holder.about.text=chat.message


    }

    override fun getItemCount(): Int {
        return items.size
    }
}