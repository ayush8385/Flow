package com.ayush.flow.adapter


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.activity.Message
import com.ayush.flow.activity.SelectedImage
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.MessageViewModel
import com.ayush.flow.databinding.ChatTemSwipeBinding
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    final val viewBinderHelper:ViewBinderHelper = ViewBinderHelper()
    lateinit var viewModel: MessageViewModel

    class HomeViewHolder(val binding: ChatTemSwipeBinding):RecyclerView.ViewHolder(binding.root){
        var chatTembinding: ChatTemSwipeBinding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding:ChatTemSwipeBinding=ChatTemSwipeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var chat=allChats[position]

        if(chat.name==""){
            holder.chatTembinding.profileName.text=chat.number
        }
        else{
            holder.chatTembinding.profileName.text=chat.name
        }

        if(chat.last_sender==Constants.MY_USERID){
            holder.chatTembinding.unreadChat.visibility=View.GONE
            holder.chatTembinding.waitingTick.visibility=View.VISIBLE
            MessageViewModel(context).getMsgStatus(chat.last_mid).observe(context as LifecycleOwner,
                androidx.lifecycle.Observer {
                    if(it=="sent"){
                        holder.chatTembinding.waitingTick.visibility=View.GONE
                        holder.chatTembinding.sentTick.visibility=View.VISIBLE
                        holder.chatTembinding.seenTick.visibility=View.GONE
                        holder.chatTembinding.sentTick.visibility=View.VISIBLE
                    }
                    else if(it=="Delivered"){
                        holder.chatTembinding.sentTick.visibility=View.GONE
                        holder.chatTembinding.waitingTick.visibility=View.GONE
                        holder.chatTembinding.seenTick.visibility=View.GONE
                        holder.chatTembinding.deliveredTick.visibility=View.VISIBLE
                    }
                    else if(it=="seen"){
                        holder.chatTembinding.deliveredTick.visibility=View.GONE
                        holder.chatTembinding.waitingTick.visibility=View.GONE
                        holder.chatTembinding.sentTick.visibility=View.GONE
                        holder.chatTembinding.seenTick.visibility=View.VISIBLE
                    }
                })
        }
        else{
            holder.chatTembinding.waitingTick.visibility=View.GONE
            holder.chatTembinding.sentTick.visibility=View.GONE
            holder.chatTembinding.seenTick.visibility=View.GONE
            holder.chatTembinding.sentTick.visibility=View.GONE
            MessageViewModel(context).getUnreads(chat.id).observe(context as LifecycleOwner) {
                if(it!=0){
                    holder.chatTembinding.unreadChat.visibility=View.VISIBLE
                    if(it>99){
                        holder.chatTembinding.unreadChat.text= "99+"
                    }
                    else{
                        holder.chatTembinding.unreadChat.text= it.toString()
                    }
                }
                else{
                    holder.chatTembinding.unreadChat.visibility=View.GONE
                }
            }
        }




        val currentTimestamp = System.currentTimeMillis()
        val msgtime = chat.time

        val diff = ((currentTimestamp-msgtime)/(1000*24*60*60)).toInt()
        if(diff==0){
            //set in time
            var tm: Date = Date(chat.time)
            val time = SimpleDateFormat("hh:mm a")
            holder.chatTembinding.timer.text=time.format(tm)
        }
        else if(diff==1){
            //set yesterday
            holder.chatTembinding.timer.text="Yesterday"
        }
        else{
            //set date
            var tm: Date = Date(chat.time)
            val date = SimpleDateFormat("dd/MM/yy")

            holder.chatTembinding.timer.text=date.format(tm)
        }



//        val wholeDateFormat= SimpleDateFormat("dd/MM/yy")
//
//        val dateFormat = SimpleDateFormat("dd")
//        val monthFormat = SimpleDateFormat("MM")
//        val yearFormat = SimpleDateFormat("yy")
//
//        val deliveryDate = chat.time
//        val d = wholeDateFormat.parse(deliveryDate)
//
//        val formatDate = dateFormat.format(d).toInt()
//        val formatMonth = monthFormat.format(d).toInt()
//        val formatYear = yearFormat.format(d).toInt()
//
//        val currentDate = dateFormat.format(currentTimestamp).toInt()
//        val currentMonth = monthFormat.format(currentTimestamp).toInt()
//        val currentYear = yearFormat.format(currentTimestamp).toInt()
//
//
//        if(formatDate-currentDate==0 && formatMonth-currentMonth==0 && formatYear-currentYear==0){
//            holder.time.text=chat.time
//        }
//        else if(abs(formatDate-currentDate)==1 && formatMonth-currentMonth==0 && formatYear-currentYear==0){
//            holder.time.text="Yesterday"
//        }
//        else{
//            holder.time.text=chat.date
//        }

        holder.chatTembinding.profileMsg.text = chat.lst_msg
        var dr: Drawable? = null
        if(chat.path!=""){
            if(chat.lst_msg=="Photo"){
                dr = ContextCompat.getDrawable(context, R.drawable.gallery)
            }
            if(chat.lst_msg=="Document"){
                dr = ContextCompat.getDrawable(context, R.drawable.documents)
            }
        }
        if(dr!=null){
            dr!!.setBounds(0, 0, 36, 36)
            holder.chatTembinding.profileMsg.setCompoundDrawables(dr, null, null, null)
        }


        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")
        if(f.exists()){
//            val b = BitmapFactory.decodeStream(FileInputStream(f))
//            holder.image.setImageBitmap(b)
            Glide.with(context).load(f).placeholder(R.drawable.user).into(holder.chatTembinding.profilePic)
        }
        else{
            holder.chatTembinding.profilePic.setImageResource(R.drawable.user)
        }
//        Glide.with(context).load(File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
//            DiskCacheStrategy.NONE)
//            .skipMemoryCache(true).into(holder.image)

        holder.chatTembinding.paren.setOnClickListener {

            if(holder.chatTembinding.swipelayout.isOpened){
                holder.chatTembinding.swipelayout.close(true)
            }
            else {
                val intent = Intent(context, Message::class.java)
                intent.putExtra("name", chat.name)
                intent.putExtra("number", chat.number)
                intent.putExtra("userid", chat.id)
                intent.putExtra("image", chat.image)
                intent.putExtra("unread",chat.unread)
                context.startActivity(intent)
            }


        }

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.chatTembinding.swipelayout,chat.id)
        viewBinderHelper.closeLayout(chat.id)



        holder.chatTembinding.callChat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.audioCall(holder.chatTembinding.profileName.text.toString(),chat.id)
//            if(Permissions().checkMicpermission(context)){
//
//            }
//            else{
//                Permissions().openPermissionBottomSheet(R.drawable.mic_permission,context.resources.getString(R.string.mic_permission),context,"mic")
//            }
        }

        holder.chatTembinding.vdoCallChat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.videoCall(holder.chatTembinding.profileName.text.toString(),chat.id)
//            if(Permissions().checkCamAndMicPermission(context)){
//
//            }
//            else{
//                Permissions().openPermissionBottomSheet(R.drawable.camera_mic_permission,context.resources.getString(R.string.mic_and_cam_permission),context,"micandcam")
//            }

        }
        holder.chatTembinding.delChat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.deleteChat(chat.id,chat.name)
        }
        holder.chatTembinding.hideChat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.hideChat(chat.id,chat.name)
        }

        holder.chatTembinding.profilePic.setOnClickListener {
            val intent = Intent(context, SelectedImage::class.java)
            intent.putExtra("type","view")
            intent.putExtra("userid",chat.id)
            intent.putExtra("name",chat.name)
            intent.putExtra("number",chat.number)
            intent.putExtra("user_image","")
            context.startActivity(intent)
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

    interface OnAdapterItemClickListener {
        fun audioCall(toString: String, id: String)
        fun videoCall(toString: String,id: String)
        fun deleteChat(id:String,name:String)
        fun hideChat(id:String,name: String)

        fun callHistoryBox(id:String,name: String)
    }

    inner class loadImage(val image:String,val holder: HomeViewHolder):AsyncTask<Void,Void, Boolean>(){
        var b:Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            holder.chatTembinding.profilePic.setImageBitmap(b)
//            Picasso.get().load(f!!).into(image)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
            b= BitmapFactory.decodeStream(FileInputStream(f))
            return true
        }
    }

}