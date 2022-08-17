package com.ayush.flow.adapter


import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.ImageHolder
import com.ayush.flow.activity.Message
import com.ayush.flow.activity.SelectedImage
import com.ayush.flow.database.ContactEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream

class ContactAdapter(val context: Context):ListAdapter<ContactEntity,ContactAdapter.HomeViewHolder>(DiffUtil()) {

    val allCons=ArrayList<ContactEntity>()

    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val number:TextView=view.findViewById(R.id.profile_abt)
        val start_chat:RelativeLayout=view.findViewById(R.id.paren)
        val invite:Button= view.findViewById(R.id.invite_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.contacts_single_row,parent,false)

        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        var cons=allCons[position]
        if (cons.name!=""){
            holder.name.text=cons.name
            holder.number.text=cons.number

            if(cons.isUser){
                holder.invite.visibility=View.GONE
            }
            else{
                holder.invite.visibility=View.VISIBLE
            }

//            Glide.with(context).load(File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),cons.id+".jpg")).placeholder(R.drawable.user).diskCacheStrategy(
//                DiskCacheStrategy.NONE)
//                .skipMemoryCache(true).into(holder.image)

            val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),cons.id+".jpg")
            if(f.exists()) {
                val b = BitmapFactory.decodeStream(FileInputStream(f))
                holder.image.setImageBitmap(b)
            }
            else{
                holder.image.setImageResource(R.drawable.user)
            }
            holder.start_chat.setOnClickListener {
                if(cons.isUser){
                    val intent= Intent(context, Message::class.java)
                    intent.putExtra("name",cons.name)
                    intent.putExtra("number",cons.number)
                    intent.putExtra("userid",cons.id)
                    intent.putExtra("image",cons.image)
                    context.startActivity(intent)
                }
                else{
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Flow")
                        var shareMessage = "\nLet me recommend you this Chat application\n\n"
                        shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=com.ayush.flow""".trimIndent()
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        context.startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: Exception) {
                        //e.toString();
                    }
                }
            }

            holder.invite.setOnClickListener {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Flow")
                    var shareMessage = "\nLet me recommend you this Chat application\n\n"
                    shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=com.ayush.flow""".trimIndent()
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    context.startActivity(Intent.createChooser(shareIntent, "choose one"))
                } catch (e: Exception) {
                    //e.toString();
                }
            }

            holder.image.setOnClickListener {
//                ImageHolder.imageDraw=holder.image.drawable
                val intent = Intent(context,SelectedImage::class.java)
                intent.putExtra("type","view")
                intent.putExtra("userid",cons.id)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return allCons.size
    }

    fun updateList(contactList:List<ContactEntity>){
        allCons.clear()
        allCons.addAll(contactList)
        notifyDataSetChanged()
    }

    inner class loadImage(val image:String,val holder:HomeViewHolder):AsyncTask<Void,Void, Boolean>(){
        var b:Bitmap?=null
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

    class DiffUtil:androidx.recyclerview.widget.DiffUtil.ItemCallback<ContactEntity>(){
        override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
            return oldItem==newItem
        }

    }
}