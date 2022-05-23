package com.ayush.flow.adapter


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(val context: Context,private val clickListener: ChatAdapter.OnAdapterItemClickListener):RecyclerView.Adapter<ChatAdapter.HomeViewHolder>() {
    val allChats=ArrayList<ChatEntity>()
    final val viewBinderHelper:ViewBinderHelper = ViewBinderHelper()
    lateinit var viewModel: MessageViewModel
    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val message:TextView=view.findViewById(R.id.profile_msg)
        val time:TextView=view.findViewById(R.id.timer)
        val unread:TextView=view.findViewById(R.id.unread_chat)
        val chat_box:ConstraintLayout=view.findViewById(R.id.paren)

        val call:ImageView=view.findViewById(R.id.call_chat)
        val vdo_call:ImageView=view.findViewById(R.id.vdo_call_chat)
        val del_chat:ImageView=view.findViewById(R.id.del_chat)
        val hide:ImageView=view.findViewById(R.id.hide_chat)
        val swipeRevealLayout:SwipeRevealLayout=view.findViewById(R.id.swipelayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.chat_tem_swipe,parent,false)

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

        MessageViewModel(context).getUnreads(chat.id).observe(context as LifecycleOwner) {
            if(it!=0){
                holder.unread.visibility=View.VISIBLE
                if(it>99){
                    holder.unread.text= "99+"
                }
                else{
                    holder.unread.text= it.toString()
                }
            }
            else{
                holder.unread.visibility=View.GONE
            }
        }




        val currentTimestamp = System.currentTimeMillis()
        val msgtime = chat.time

        val diff = ((currentTimestamp-msgtime)/(1000*24*60*60)).toInt()
        if(diff==0){
            //set in time
            var tm: Date = Date(chat.time)
            val time = SimpleDateFormat("hh:mm a")
            holder.time.text=time.format(tm)
        }
        else if(diff==1){
            //set yesterday
            holder.time.text="Yesterday"
        }
        else{
            //set date
            var tm: Date = Date(chat.time)
            val date = SimpleDateFormat("dd/MM/yy")

            holder.time.text=date.format(tm)
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

        holder.message.text = chat.lst_msg
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
            holder.message.setCompoundDrawables(dr, null, null, null)
        }


        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")
        if(f.exists()){
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            holder.image.setImageBitmap(b)
        }
        else{
            holder.image.setImageResource(R.drawable.user)
        }
//        Glide.with(context).load(File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),chat.id+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
//            DiskCacheStrategy.NONE)
//            .skipMemoryCache(true).into(holder.image)

        holder.chat_box.setOnClickListener {

            if(holder.swipeRevealLayout.isOpened){
                holder.swipeRevealLayout.close(true)
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
        viewBinderHelper.bind(holder.swipeRevealLayout,chat.id)
        viewBinderHelper.closeLayout(chat.id)



        holder.call.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.audioCall(holder.name.text.toString(),chat.id)
//            if(Permissions().checkMicpermission(context)){
//
//            }
//            else{
//                Permissions().openPermissionBottomSheet(R.drawable.mic_permission,context.resources.getString(R.string.mic_permission),context,"mic")
//            }
        }

        holder.vdo_call.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.videoCall(holder.name.text.toString(),chat.id)
//            if(Permissions().checkCamAndMicPermission(context)){
//
//            }
//            else{
//                Permissions().openPermissionBottomSheet(R.drawable.camera_mic_permission,context.resources.getString(R.string.mic_and_cam_permission),context,"micandcam")
//            }

        }
        holder.del_chat.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.deleteChat(chat.id,chat.name)
        }
        holder.hide.setOnClickListener {
            viewBinderHelper.closeLayout(chat.id)
            clickListener.hideChat(chat.id,chat.name)
        }

        holder.image.setOnClickListener {
            val intent = Intent(context, SelectedImage::class.java)
//            var fos  =  ByteArrayOutputStream()
//            ((holder.image.drawable as BitmapDrawable).bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fos)
//            val byteArray = fos.toByteArray()
//            ImageHolder.imageDraw=holder.image.drawable
            intent.putExtra("type","view")
//            intent.putExtra("image", byteArray)
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
            holder.image.setImageBitmap(b)
//            Picasso.get().load(f!!).into(image)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),image)
            b= BitmapFactory.decodeStream(FileInputStream(f))
            return true
        }
    }

}