package com.ayush.flow.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.activity.ForwardViewModel
import com.ayush.flow.database.ChatEntity
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.*

class ForwardAdapter(val context: Context,val listener: OnAdapterItemClickListener):RecyclerView.Adapter<ForwardAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    val selected=ArrayList<ChatEntity>()
    lateinit var mainViewModel: ForwardViewModel
    var unselectChatEntity: ChatEntity?=null
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val select:ImageView=view.findViewById(R.id.selected_chat)
        val fwdItem:RelativeLayout=view.findViewById(R.id.paren_fwd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        mainViewModel = ViewModelProviders.of(context as FragmentActivity).get(ForwardViewModel::class.java)
        val view=LayoutInflater.from(parent.context).inflate(R.layout.fwd_single_row,parent,false)
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

        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")
        if(f.exists()){
            Glide.with(context).load(f).placeholder(R.drawable.user).into(holder.image)
        }


        holder.select.visibility=View.GONE
        if(chat in selected){
            holder.select.visibility=View.VISIBLE
        }

        holder.fwdItem.setOnClickListener {
            if(chat !in selected){
                listener.addChat(allChats[holder.adapterPosition])
                holder.select.visibility=View.VISIBLE
            }
            else{
                listener.delChat(allChats[holder.adapterPosition])
                holder.select.visibility=View.GONE
            }
        }

    }



    override fun getItemCount(): Int {
        return allChats.size
    }

    fun updateList(list: List<ChatEntity>,selected:List<ChatEntity>) {
        allChats.clear()
        allChats.addAll(list)
        this.selected.clear()
        this.selected.addAll(selected)
        notifyDataSetChanged()
    }

    fun updateSelected(selected:List<ChatEntity>) {
        this.selected.clear()
        this.selected.addAll(selected)
        notifyDataSetChanged()
    }

//    inner class loadImage(val image:String,val holder: HomeViewHolder):
//        AsyncTask<Void, Void, Boolean>(){
//        var b: Bitmap?=null
//        override fun onPostExecute(result: Boolean?) {
//            super.onPostExecute(result)
//            holder.image.setImageBitmap(b)
//        }
//        override fun doInBackground(vararg params: Void?): Boolean {
//            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
//            b= BitmapFactory.decodeStream(FileInputStream(f))
//            return true
//        }
//    }

    interface OnAdapterItemClickListener {
        fun addChat(chatEntity: ChatEntity)
        fun delChat(chatEntity: ChatEntity)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

//    fun unselectChat(chatEntity: ChatEntity) {
//        unselectChatEntity=chatEntity
//        notifyDataSetChanged()
//    }
}