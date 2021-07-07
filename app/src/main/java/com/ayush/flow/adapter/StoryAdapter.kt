package com.ayush.flow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.model.Chats
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class StoryAdapter(val context: Context, val items: ArrayList<Chats>):RecyclerView.Adapter<StoryAdapter.HomeViewHolder>() {
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.story_img)
        val name:TextView=view.findViewById(R.id.story_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.story_single_row,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var story=items[position]
        holder.name.text=story.name
        if(story.image!=""){
            Picasso.get().load(story.image).into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}