package com.ayush.flow.adapter

import android.content.Context
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R

class SelectedImgAdapter(val context: Context,val selectedList:ArrayList<Uri>,val listener:SelectedImgAdapter.OnImageCardClickListener):RecyclerView.Adapter<SelectedImgAdapter.SelectedViewHolder>() {
    var selectedPos = 0
    class SelectedViewHolder(view: View):RecyclerView.ViewHolder(view){
        val imageView:ImageView = view.findViewById(R.id.selected_img)
        val imageCard:CardView = view.findViewById(R.id.selected_img_card)
        val removeImage:ImageView = view.findViewById(R.id.remove_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_img_item,parent,false)
        return SelectedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedViewHolder, position: Int) {
        holder.imageView.setImageURI(selectedList.get(position))

        holder.imageCard.setOnClickListener {
            selectedPos = position
            listener.loadNewImage(selectedList.get(position))
        }

        holder.removeImage.setOnClickListener {
            if(selectedPos==position){
                if(position+1<selectedList.size){
                    listener.loadNewImage(selectedList.get(position+1))
                }
                else{
                    listener.loadNewImage(selectedList.get(0))
                }
            }
            listener.removeImage(position)
        }
    }

    override fun getItemCount(): Int {
        return selectedList.size
    }

    interface OnImageCardClickListener{
        fun loadNewImage(uri: Uri)
        fun removeImage(position: Int)
    }
}