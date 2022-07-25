package com.ayush.flow.adapter


import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Environment
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.database.CallEntity
import com.ayush.flow.database.CallHistoryViewModel
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*


class CallAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<CallAdapter.HomeViewHolder>() {
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

        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),call.id+".jpg")
        if(f.exists()){
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            holder.image.setImageBitmap(b)
        }
        else{
            holder.image.setImageResource(R.drawable.user)
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

    inner class loadImage(val image:String,val holder: HomeViewHolder):
        AsyncTask<Void, Void, Boolean>(){
        var b: Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            holder.image.setImageBitmap(b)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),image)
            b= BitmapFactory.decodeStream(FileInputStream(f))
            return true
        }
    }
}