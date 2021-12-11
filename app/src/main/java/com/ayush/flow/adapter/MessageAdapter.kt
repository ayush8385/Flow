package com.ayush.flow.adapter


import android.R.attr
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.activity.SelectedImage
import com.ayush.flow.database.MessageEntity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.startActivity
import java.io.*
import android.R.attr.data
import android.graphics.Canvas
import android.graphics.Paint

import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.R.attr.mimeType

import androidx.core.content.FileProvider
import android.R.attr.src
import android.app.Activity
import android.app.DownloadManager
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.ayush.flow.activity.FileDownloader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class MessageAdapter(val context: Context,val selectedMsg: ArrayList<MessageEntity>,private val clickListener: OnAdapterItemClickListener):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    val firebaseUser=FirebaseAuth.getInstance().currentUser
    val allMsgs=ArrayList<MessageEntity>()



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

        val download:ImageView=view.findViewById(R.id.download)


      //  val box:CardView=view.findViewById(R.id.box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType==1){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==2){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_img_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==3){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_img_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==4){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_doc_single_row,parent,false)
            return MessageViewHolder(view)
        }
        if(viewType==5){
            val view=LayoutInflater.from(parent.context).inflate(R.layout.send_doc_single_row,parent,false)
            return MessageViewHolder(view)
        }
        else{
            val view=LayoutInflater.from(parent.context).inflate(R.layout.receive_single_row,parent,false)
            return MessageViewHolder(view)
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        var chat=allMsgs[position]


        if(chat.type=="image"){

            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Chat Images"),chat.message)
            Glide.with(context).load(f).into(holder.image_msg)



            if(chat.sender==firebaseUser!!.uid){
                if(chat.sent){
                    holder.seen_txt.text="sent"
                    holder.progressBar.visibility=View.GONE
                }
                else{
                    holder.seen_txt.text="sending..."
                    holder.progressBar.visibility=View.VISIBLE
                }
            }

        }
        if(chat.type=="doc"){

            holder.message.text=chat.message

            val pdfFile = File(File(Environment.getExternalStorageDirectory().toString(),"/Flow/Medias/Chat Documents"),chat.message)

            if(pdfFile.exists()){
                holder.download.visibility=View.GONE
                holder.progressBar.visibility=View.GONE
            }


            if(chat.sender==firebaseUser!!.uid){
                if(chat.sent){
                    holder.seen_txt.text="sent"
                    holder.progressBar.visibility=View.GONE
                }
                else{
                    holder.seen_txt.text="sending..."
                    holder.progressBar.visibility=View.VISIBLE
                }
            }
        }
        if(chat.type=="message"){
            holder.message.text=chat.message
            holder.seen_txt.text="sent"
        }
        holder.time.text=chat.time
        holder.date.text=chat.date

        holder.select.visibility=View.GONE

        if(chat in selectedMsg){
            holder.select.visibility=View.VISIBLE
        }

        if(chat.sender!=firebaseUser!!.uid && !chat.seen){

            val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(chat.mid)){
                        FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(chat.mid).child("seen").setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
        else{
            if(chat.recev){
                holder.seen_txt.text="Delivered"
                if(chat.seen){
                    holder.seen_txt.text="seen"
                }
            }
//            holder.msg_box.setOnClickListener {
//
//                holder.seen_txt.visibility=View.VISIBLE
//                Handler().postDelayed({
//                    holder.seen_txt.visibility=View.GONE
//                },400)
//            }
        }

//        holder.msg_box.setOnClickListener {
//
//        }

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
            if(chat.type=="image"){
                val intent = Intent(context, SelectedImage::class.java)
                var fos  =  ByteArrayOutputStream()
                ((holder.image_msg.drawable as BitmapDrawable).bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fos)
                val byteArray = fos.toByteArray()
                intent.putExtra("type","view")
                intent.putExtra("image", byteArray)
                intent.putExtra("userid","")
                intent.putExtra("name","")
                intent.putExtra("number","")
                intent.putExtra("user_image","")
                context.startActivity(intent)
            }
            if(chat.type=="doc"){




                if(holder.download.visibility==View.VISIBLE){

                    holder.download.visibility=View.GONE
                    holder.progressBar.visibility=View.VISIBLE

                    Handler().postDelayed({
                        val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Documents")
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

                    },1000)

                }
                else if(holder.download.visibility==View.GONE && holder.progressBar.visibility==View.GONE){
                    val builder = VmPolicy.Builder()
                    StrictMode.setVmPolicy(builder.build())

                    val pdfFile = File(File(Environment.getExternalStorageDirectory().toString(),"/Flow/Medias/Chat Documents"),chat.message)


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
                else{
                    //Eat Fivestar and Do nothing
                }
            }
        }


        holder.msg_box.setOnLongClickListener(object :View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {

                if(chat !in selectedMsg){
                    selectedMsg.add(chat)
                    clickListener.updateCount()
                    holder.select.visibility=View.VISIBLE
                }

                return true
            }

        })

    }

    override fun getItemCount(): Int {
        return allMsgs.size
    }

    interface OnAdapterItemClickListener {
        fun updateCount()
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




    fun updateList(msgList:ArrayList<MessageEntity>){
        allMsgs.clear()
        allMsgs.addAll(msgList)
        notifyDataSetChanged()
    }
}