package com.ayush.flow.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.database.CallEntity
import com.ayush.flow.database.CallHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

class CallHistoryAdapter(val context: Context):RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>(){
    val allHistory= ArrayList<CallHistoryEntity>()
    class CallHistoryViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image: ImageView = view.findViewById(R.id.status_img)
        val time:TextView=view.findViewById(R.id.time)
        val duration:TextView=view.findViewById(R.id.duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recent_call_single_row,parent,false)
        return CallHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val call = allHistory[position]

        var dr: Drawable? = null
        if(call.calltype=="incoming"){
            holder.image.setImageResource(R.drawable.incoming_icon)
        }
        if(call.calltype=="outgoing"){
            holder.image.setImageResource(R.drawable.out_icon)
        }
        if(call.calltype=="missed"){
            holder.image.setImageResource(R.drawable.miss_icon)
        }

        var tm: Date = Date((call.time)*1000)
        val time = SimpleDateFormat("hh:mm a")
        holder.time.text=time.format(tm)

        holder.duration.text=convertSeconds(call.duration)

    }

    fun convertSeconds(seconds: Int): String? {
        val h = seconds / 3600
        val m = seconds % 3600 / 60
        val s = seconds % 60
        val sh = if (h > 0) h.toString() + "h" else ""
        val sm =
            (if (m < 10 && m > 0 && h > 0) "0" else "") + if (m > 0) if (h > 0 && s == 0) m.toString() else m.toString() + "m" else ""
        val ss =
            if (s == 0 && (h > 0 || m > 0)) "" else (if (s < 10 && (h > 0 || m > 0)) "0" else "") + s.toString() + "s"
        return sh + sm + ss
    }

    override fun getItemCount(): Int {
        return allHistory.size
    }

    fun updateList(list: List<CallHistoryEntity>) {
        allHistory.clear()
        allHistory.addAll(list)
        notifyDataSetChanged()
    }
}