package com.ayush.flow.adapter


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.provider.Settings
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.utils.BlurTransformation
import com.ayush.flow.utils.Constants
import com.ayush.flow.activity.FileDownloader
import com.ayush.flow.activity.SelectedImage
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.MessageEntity
import com.ayush.flow.database.MessageViewModel
import com.ayush.flow.utils.Permissions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class MessageAdapter(val context: Context,val selectedMsg: ArrayList<MessageEntity>,private val clickListener: OnAdapterItemClickListener):ListAdapter<MessageEntity,MessageAdapter.MessageViewHolder>(DiffUtil()) {
    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val allMsgs=ArrayList<MessageEntity>()
    var searchedText: String = ""
    var isLongClick=false
    lateinit var messageViewHolder: MessageViewHolder

    private val r: Pattern = Pattern.compile(
        "^[\\s\n\r]*(?:(?:[\u00a9\u00ae\u203c\u2049\u2122\u2139\u2194-\u2199\u21a9-\u21aa\u231a-\u231b\u2328\u23cf\u23e9-\u23f3\u23f8-\u23fa\u24c2\u25aa-\u25ab\u25b6\u25c0\u25fb-\u25fe\u2600-\u2604\u260e\u2611\u2614-\u2615\u2618\u261d\u2620\u2622-\u2623\u2626\u262a\u262e-\u262f\u2638-\u263a\u2648-\u2653\u2660\u2663\u2665-\u2666\u2668\u267b\u267f\u2692-\u2694\u2696-\u2697\u2699\u269b-\u269c\u26a0-\u26a1\u26aa-\u26ab\u26b0-\u26b1\u26bd-\u26be\u26c4-\u26c5\u26c8\u26ce-\u26cf\u26d1\u26d3-\u26d4\u26e9-\u26ea\u26f0-\u26f5\u26f7-\u26fa\u26fd\u2702\u2705\u2708-\u270d\u270f\u2712\u2714\u2716\u271d\u2721\u2728\u2733-\u2734\u2744\u2747\u274c\u274e\u2753-\u2755\u2757\u2763-\u2764\u2795-\u2797\u27a1\u27b0\u27bf\u2934-\u2935\u2b05-\u2b07\u2b1b-\u2b1c\u2b50\u2b55\u3030\u303d\u3297\u3299\ud83c\udc04\ud83c\udccf\ud83c\udd70-\ud83c\udd71\ud83c\udd7e-\ud83c\udd7f\ud83c\udd8e\ud83c\udd91-\ud83c\udd9a\ud83c\ude01-\ud83c\ude02\ud83c\ude1a\ud83c\ude2f\ud83c\ude32-\ud83c\ude3a\ud83c\ude50-\ud83c\ude51\u200d\ud83c\udf00-\ud83d\uddff\ud83d\ude00-\ud83d\ude4f\ud83d\ude80-\ud83d\udeff\ud83e\udd00-\ud83e\uddff\udb40\udc20-\udb40\udc7f]|\u200d[\u2640\u2642]|[\ud83c\udde6-\ud83c\uddff]{2}|.[\u20e0\u20e3\ufe0f]+)+[\\s\n\r]*)+$"
    )

    private fun isEmojiOnly(string: String): Boolean {
        return r.matcher(string).find()
    }

    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){

        val message:TextView=view.findViewById(R.id.txt_msg)
        val time:TextView=view.findViewById(R.id.msg_time)
        val msg_box:RelativeLayout=view.findViewById(R.id.msg_box)
        val seen_txt:TextView=view.findViewById(R.id.txt_seen)
        val parent:RelativeLayout=view.findViewById(R.id.msg_par)
        val select:ImageView=view.findViewById(R.id.select)
        val image_msg:ImageView=view.findViewById(R.id.img_msg)
        val progressBar:ProgressBar=view.findViewById(R.id.progressbar)
        val date:TextView =view.findViewById(R.id.msg_date)
        val dim:View =view.findViewById(R.id.dim)
        val downloadBar:ImageView=view.findViewById(R.id.download_bar)
        val download:ImageView=view.findViewById(R.id.download)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType==1){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_single_row,parent,false)
            messageViewHolder= MessageViewHolder(view)
            return MessageViewHolder(view)
        }
        if(viewType==2){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_img_single_row,parent,false)
            messageViewHolder=MessageViewHolder(view)
            return MessageViewHolder(view)
        }
        if(viewType==3){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_img_single_row,parent,false)
            messageViewHolder=MessageViewHolder(view)
            return MessageViewHolder(view)
        }
        if(viewType==4){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_doc_single_row,parent,false)
            messageViewHolder=MessageViewHolder(view)
            return MessageViewHolder(view)
        }
        if(viewType==5){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_doc_single_row,parent,false)
            messageViewHolder=MessageViewHolder(view)
            return MessageViewHolder(view)
        }
        else{
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_single_row,parent,false)
            messageViewHolder=MessageViewHolder(view)
            return MessageViewHolder(view)
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        var chat=allMsgs[position]

        if(chat.type=="image"){

            if(chat.sender== Constants.MY_USERID){
                val f = File(File(Environment.getExternalStorageDirectory(), Constants.ALL_PHOTO_LOCATION),chat.path)
                Glide.with(context).load(f).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.image_msg)
                MessageViewModel(context).getMsgStatus(chat.mid).observe(context as LifecycleOwner) {
                    if (it == "sent" || it == "Delivered" || it == "seen") {
                        holder.seen_txt.text=it
                        holder.progressBar.visibility=View.GONE
                    }
                    else{
                        holder.seen_txt.text="sending..."
                        holder.progressBar.visibility=View.VISIBLE
                    }
                }
            }
            else{
                val f = File(File(Environment.getExternalStorageDirectory(), Constants.ALL_PHOTO_LOCATION),chat.mid+".jpg")
                if(f.exists()){
                    holder.dim.visibility=View.GONE
                    holder.downloadBar.visibility=View.GONE
                    Glide.with(context).load(f).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.image_msg)
                }
                else{
                    holder.dim.visibility=View.VISIBLE
                    holder.downloadBar.visibility=View.VISIBLE
                    Glide.with(context).load(chat.thumbnail).skipMemoryCache(false).apply(RequestOptions.bitmapTransform(
                        BlurTransformation(context)
                    )).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image_msg)
                }
            }

        }
        if(chat.type=="doc"){

            holder.message.text=chat.message

            val pdfFile = File(File(Environment.getExternalStorageDirectory().toString(), Constants.DOC_LOCATION),chat.message)

            if(pdfFile.exists()){
                holder.download.visibility=View.GONE
                holder.progressBar.visibility=View.GONE
            }
            else{
                holder.download.visibility=View.VISIBLE
                holder.progressBar.visibility=View.VISIBLE
            }


            if(chat.sender==firebaseUser!!.uid){
                MessageViewModel(context).getMsgStatus(chat.mid).observe(context as LifecycleOwner) {
                    if (it == "sent" || it == "Delivered" || it == "seen") {
                        holder.seen_txt.text=it
                        holder.progressBar.visibility=View.GONE
                    }
                    else{
                        holder.seen_txt.text="sending..."
                        holder.progressBar.visibility=View.VISIBLE
                    }
                }
            }
        }
        if(chat.type=="message"){

            holder.message.text=chat.message

            if(searchedText!=""){
                if(chat.message.toLowerCase().contains(searchedText)){
                    var start = chat.message.toLowerCase().indexOf(searchedText)
                    var end = start + searchedText.length
                    val spanString = Spannable.Factory.getInstance().newSpannable(chat.message)
                    spanString.setSpan(ForegroundColorSpan(Color.GREEN),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    holder.message.text=spanString
                }
            }
            holder.seen_txt.text="sent"
        }


        //setting date and time
        var tm: Date = Date(chat.time)
        val time = SimpleDateFormat("hh:mm a")
        val date = SimpleDateFormat("dd/MM/yy")

        holder.time.text=time.format(tm)
        holder.date.text=date.format(tm)

        holder.select.visibility=View.GONE

        if(chat in selectedMsg){
            holder.select.visibility=View.VISIBLE
        }

        if(chat.sender!=firebaseUser!!.uid){

            val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(chat.mid)){
                        FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(chat.mid).child("msgStatus").setValue("seen")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
        else{
            holder.seen_txt.text=chat.msgStatus
        }


//        holder.msg_box.setOnLongClickListener(OnLongClickListener {
//            isLongClick = true
//            true
//        })
//        holder.msg_box.setOnTouchListener(OnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_UP && isLongClick) {
//                isLongClick = false
//                return@OnTouchListener true
//            }
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                isLongClick = false
//            }
//            v.onTouchEvent(event)
//        })


        holder.msg_box.setOnClickListener {
            if(selectedMsg.size!=0){
                if(chat in selectedMsg){
                    selectedMsg.remove(chat)
                    holder.select.visibility=View.GONE
                }
                else{
                    selectedMsg.add(chat)
                    holder.select.visibility=View.VISIBLE
                }
                clickListener.updateCount()
            }
            else if(chat.type=="image"){
                if(holder.downloadBar.visibility==View.VISIBLE){
                    if(Permissions().checkWritepermission(context)){
                        downloadMsgImage(chat,holder)
                    }
                    else{
                        Permissions().openPermissionBottomSheet(R.drawable.gallery,context.resources.getString(R.string.storage_permission),context,Constants.STORAGE_PERMISSION)
                    }
                }
                else{
                    val intent = Intent(context, SelectedImage::class.java)
                    intent.putExtra("type","msgImg")
                    intent.putExtra("userid",chat.mid)
                    context.startActivity(intent)
                }
            }
            else if(chat.type=="doc"){

                if(holder.download.visibility==View.VISIBLE){

                    if(holder.downloadBar.visibility==View.VISIBLE){
                        if(Permissions().checkWritepermission(context)){
                            downloadMsgDocument(chat,holder)
                        }
                        else{
                            Permissions().openPermissionBottomSheet(R.drawable.gallery,context.resources.getString(R.string.storage_permission),context,Constants.STORAGE_PERMISSION)
                        }
                    }
                    else{
                        val intent = Intent(context, SelectedImage::class.java)
                        intent.putExtra("type","msgImg")
                        intent.putExtra("userid",chat.mid)
                        context.startActivity(intent)
                    }

                }
                else if(holder.download.visibility==View.GONE && holder.progressBar.visibility==View.GONE){
                    val builder = VmPolicy.Builder()
                    StrictMode.setVmPolicy(builder.build())

                    val pdfFile = File(File(Environment.getExternalStorageDirectory().toString(),
                        Constants.DOC_LOCATION),chat.message)


                    val path = FileProvider.getUriForFile(context,"com.ayush.flow"+".fileprovider",pdfFile)

                    val pdfIntent = Intent(Intent.ACTION_VIEW)

                    pdfIntent.setDataAndType(path, "application/pdf")
                    pdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                    try {
                        context.startActivity(pdfIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "No Application available to view PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }
            }
        }


        holder.msg_box.setOnLongClickListener(object :View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                holder.message.autoLinkMask=0
                if(chat !in selectedMsg){
                    selectedMsg.add(chat)
                    clickListener.updateCount()
                    holder.select.visibility=View.VISIBLE
                }

                return true
            }

        })

    }

    private fun downloadMsgDocument(chat: MessageEntity, holder: MessageAdapter.MessageViewHolder) {
        holder.download.visibility=View.GONE
        holder.progressBar.visibility=View.VISIBLE

        Handler().postDelayed({
            val directory: File = File(Environment.getExternalStorageDirectory().toString(), Constants.DOC_LOCATION)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val pdfFile = File(directory,chat.message)
            FileDownloader().downloadFile(chat.url, pdfFile)

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            try {

                val url = URL(chat.url)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod("GET")
//            urlConnection.setDoOutput(true)
//            urlConnection.setRequestProperty("Content-Type", "application/pdf")
                urlConnection.connect()
                val inputStream: InputStream = urlConnection.inputStream
                val fileOutputStream = FileOutputStream(pdfFile)
                val totalSize: Int = urlConnection.getContentLength()
                val buffer = ByteArray(1024*1024)
                var bufferLength = 0
                while (inputStream.read(buffer).also { bufferLength = it } != -1) {
                    fileOutputStream.write(buffer, 0, bufferLength)
                }
                fileOutputStream.close()
                holder.progressBar.visibility=View.GONE

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                holder.download.visibility=View.VISIBLE
                holder.progressBar.visibility=View.GONE
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                holder.download.visibility=View.VISIBLE
                holder.progressBar.visibility=View.GONE
            } catch (e: IOException) {
                e.printStackTrace()
                holder.download.visibility=View.VISIBLE
                holder.progressBar.visibility=View.GONE
            }

        },400)
    }

    private fun downloadMsgImage(chat:MessageEntity,holder: MessageViewHolder) {
//        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
//            if (Environment.isExternalStorageManager()) {
//                val msg = MessageEntity(chat.mid,
//                    Constants.MY_USERID+"-"+chat.sender,chat.sender,chat.message,chat.time,chat.type,"","","",chat.url,"sending...")
//                saveImagefromUrlMsg(
//                    Constants.ALL_PHOTO_LOCATION,
//                    chat.mid + ".jpg",
//                    msg,holder
//                ).execute(chat.url)
//            } else {
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                val uri = Uri.fromParts("package", context.packageName, null)
//                intent.data = uri
//                context.startActivity(intent)
//            }
//        }
//        else{
            holder.downloadBar.visibility=View.GONE
            holder.progressBar.visibility=View.VISIBLE
            val msg = MessageEntity(chat.mid,
                Constants.MY_USERID+"-"+chat.sender,chat.sender,chat.message,chat.time,chat.type,"","","",chat.url,"sending...")
            saveImagefromUrlMsg(Constants.ALL_PHOTO_LOCATION,chat.mid+".jpg",msg,holder).execute(chat.url)
//        }
    }

    override fun getItemCount(): Int {
        return allMsgs.size
    }

    interface OnAdapterItemClickListener {
        fun updateCount()
        fun updateSeen(mid: String)
    }

    override fun getItemViewType(position: Int): Int {
        val id=this.allMsgs.get(position).sender
        val type=this.allMsgs.get(position).type
        if(id!=firebaseUser!!.uid && type=="image"){
            return 3
        }
        if(id==firebaseUser!!.uid && type=="image"){
            return 2
        }
        if(id!=firebaseUser!!.uid && type=="doc"){
            return 4
        }
        if(id==firebaseUser!!.uid && type=="doc"){
            return 5
        }
        if(id==firebaseUser!!.uid){
            return 1
        }
        else{
            return 0
        }
    }

    fun colorSearchedText(allMsgs:ArrayList<MessageEntity>,searchedText:String){
        this.searchedText=searchedText
        this.allMsgs.clear()
        this.allMsgs.addAll(allMsgs)
        notifyDataSetChanged()
    }


    fun updateList(msgList:ArrayList<MessageEntity>){
        allMsgs.clear()
        allMsgs.addAll(msgList)
        notifyDataSetChanged()
    }


    inner class saveImagefromUrlMsg(val location: String,val fileName: String,val msg:MessageEntity,val holder: MessageViewHolder) : AsyncTask<String?, Void?, Boolean>() {
        var bmp: Bitmap?=null
        override fun doInBackground(vararg url: String?): Boolean {
            val url: URL = mStringToURL(url[0]!!)!!
            val connection: HttpURLConnection?
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                bmp= BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            saveImgMsgToInternalStorage(bmp!!, location, fileName, msg,holder).execute()
        }

        private fun mStringToURL(string: String): URL? {
            try {
                return URL(string)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return null
        }
    }

    inner class saveImgMsgToInternalStorage(val bitmapImage:Bitmap,val location:String,val fileName:String,val msg: MessageEntity,val holder: MessageViewHolder):AsyncTask<Void,Void,Boolean>(){
        val directory: File = File(Environment.getExternalStorageDirectory().toString(), location)
        var file: File = File(directory,fileName)
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
//            msg.path=fileName
//            MessageViewModel(context).insertMessage(msg)
            holder.progressBar.visibility=View.GONE
            holder.dim.visibility=View.GONE

            Glide.with(context).load(file).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.image_msg)

        }
        override fun doInBackground(vararg params: Void?):Boolean {
            if(!directory.exists()){
                directory.mkdirs()
            }
            var fos: FileOutputStream = FileOutputStream(file)
            try {
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return true
        }
    }

    class DiffUtil : androidx.recyclerview.widget.DiffUtil.ItemCallback<MessageEntity>(){
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            TODO("Not yet implemented")
        }


    }
}