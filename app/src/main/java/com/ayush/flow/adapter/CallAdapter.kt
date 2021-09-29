package com.ayush.flow.adapter


import android.content.Context
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
import com.ayush.flow.database.CallEntity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.util.*

class CallAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<CallAdapter.HomeViewHolder>() {
    val allCalls=ArrayList<CallEntity>()
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.caller_pic)
        val name:TextView=view.findViewById(R.id.caller_name)
        val cause:TextView = view.findViewById(R.id.call_reason)
//        val duration:TextView = view.findViewById(R.id.callDuration)
        val call_box:RelativeLayout=view.findViewById(R.id.paren)
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
        holder.cause.text=call.calltype

        if(call.image!="" && call.image!=null){
          loadImage(call.image, holder).execute()
        }

        holder.audioCall.setOnClickListener {
            clickListener.audioCall(call.name,call.id,call.image)
        }

        holder.videoCall.setOnClickListener {
            clickListener.videoCall(call.name,call.id,call.image)
        }

        holder.call_box.setOnClickListener {

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
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
            b= BitmapFactory.decodeStream(FileInputStream(f))
            return true
        }
    }
}