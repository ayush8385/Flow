package com.ayush.flow.adapter

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ayush.flow.R
import com.ayush.flow.model.ScreenItem


class ViewPagerAdapter : PagerAdapter {

    var mContext: Context
    var screenItem: List<ScreenItem>

    constructor(mContext: Context, screenItem: List<ScreenItem>) : super() {
        this.mContext = mContext
        this.screenItem = screenItem
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val inflater =  mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutScreen:View = inflater.inflate(R.layout.viewpager_item,null)

        var image :ImageView=layoutScreen.findViewById(R.id.image)
        val title:TextView=layoutScreen.findViewById(R.id.title)
        val desc:TextView=layoutScreen.findViewById(R.id.desc)

        val img = BitmapFactory.decodeResource(mContext.resources,screenItem[position].image)
        image.setImageBitmap(resizeBitmap(img))
        title.setText(screenItem[position].title)
        desc.setText(screenItem[position].desc)

        container.addView(layoutScreen)

        return layoutScreen

    }

    override fun getCount(): Int {
        return screenItem.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
       return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {

        container.removeView(`object` as View)
    }

    fun resizeBitmap(source: Bitmap): Bitmap {
        val maxResolution = 400    //edit 'maxResolution' to fit your need
        val width = source.width
        val height = source.height
        var newWidth = width
        var newHeight = height
        val rate: Float

        if (width > height) {
            if (maxResolution < width) {
                rate = maxResolution / width.toFloat()
                newHeight = (height * rate).toInt()
                newWidth = maxResolution
            }
        } else {
            if (maxResolution < height) {
                rate = maxResolution / height.toFloat()
                newWidth = (width * rate).toInt()
                newHeight = maxResolution
            }
        }
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
}