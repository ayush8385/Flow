package com.ayush.flow.adapter



import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.utils.Constants
import com.ayush.flow.database.CallEntity
import com.ayush.flow.utils.ImageHandling
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*


class CallAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):
    ListAdapter<CallEntity, CallAdapter.HomeViewHolder>(DiffUtil()) {
    val allCalls=ArrayList<CallEntity>()
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.caller_pic)
        val name:TextView=view.findViewById(R.id.caller_name)
        val time:TextView = view.findViewById(R.id.call_time)
        val call_box:ConstraintLayout=view.findViewById(R.id.paren)
        val audioCall:CircleImageView=view.findViewById(R.id.audiocall_btn)
        val videoCall:CircleImageView=view.findViewById(R.id.vdocall_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.calls_single_row,parent,false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var call=allCalls[position]

        holder.name.text=call.name

        var tm: Date = Date((call.time)!!)
        val time = SimpleDateFormat("hh:mm a")
        holder.time.text=time.format(tm)

        var dr:Drawable? = null
        if(call.calltype=="incoming"){
            dr = ContextCompat.getDrawable(context, R.drawable.incoming_icon)
        }
        if(call.calltype=="outgoing"){
            dr = ContextCompat.getDrawable(context, R.drawable.out_icon)
        }
        if(call.calltype=="missed"){
            dr = ContextCompat.getDrawable(context, R.drawable.miss_icon)
        }
        dr!!.setBounds(0, 0, 34, 34)
        holder.time.setCompoundDrawables(dr, null, null, null)

        try {
            val profileUri = ImageHandling(context).getUserProfileImageUri(call.id)
            Glide.with(context).load(profileUri).placeholder(R.drawable.user).diskCacheStrategy(
                DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.image)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        holder.audioCall.setOnClickListener {
            clickListener.audioCall(call.name,call.id)
        }

        holder.videoCall.setOnClickListener {
            clickListener.videoCall(call.name,call.id)
        }

        holder.call_box.setOnClickListener {
            clickListener.callHistoryBox(call.id,call.name)
        }

    }

    override fun getItemCount(): Int {
        return allCalls.size
    }

    fun updateList(list: List<CallEntity>) {
        allCalls.clear()
        allCalls.addAll(list)
        notifyDataSetChanged()
    }

    class DiffUtil:androidx.recyclerview.widget.DiffUtil.ItemCallback<CallEntity>(){
        override fun areItemsTheSame(oldItem: CallEntity, newItem: CallEntity): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: CallEntity, newItem: CallEntity): Boolean {
            return oldItem==newItem
        }
    }
}