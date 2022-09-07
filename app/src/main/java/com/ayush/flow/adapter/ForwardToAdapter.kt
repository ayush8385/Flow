package com.ayush.flow.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.utils.Constants
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.utils.ImageHandling
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

class ForwardToAdapter(val context: Context,val listener:ForwardAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<ForwardToAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.story_img)
        val delete:ImageView=view.findViewById(R.id.del_fwd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.fwd_to_single_row,parent,false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=allChats[position]

        try {
            val profileUri = ImageHandling(context).getUserProfileImageUri(chat.id)
            Glide.with(context).load(profileUri).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.image)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        holder.delete.setOnClickListener {
            listener.delChat(allChats[holder.adapterPosition])
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

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}