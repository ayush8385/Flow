package com.ayush.flow.adapter


import android.content.Context
import android.content.Intent
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
import com.ayush.flow.activity.Message
import com.ayush.flow.database.ContactEntity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream

class ContactAdapter(val context: Context):RecyclerView.Adapter<ContactAdapter.HomeViewHolder>() {

    val allCons=ArrayList<ContactEntity>()

    class HomeViewHolder(val view: View):RecyclerView.ViewHolder(view){
        val image:CircleImageView=view.findViewById(R.id.profile_pic)
        val name:TextView=view.findViewById(R.id.profile_name)
        val number:TextView=view.findViewById(R.id.profile_abt)
        val start_chat:RelativeLayout=view.findViewById(R.id.paren)
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

            if(cons.image!=""){
                loadImage(cons.image,holder).execute()
            }
            else{
                holder.image.setImageResource(R.drawable.user)
            }

            holder.start_chat.setOnClickListener {
                val intent= Intent(context, Message::class.java)
                intent.putExtra("name",cons.name)
                intent.putExtra("number",cons.number)
                intent.putExtra("userid",cons.id)
                intent.putExtra("image",cons.image)
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
}