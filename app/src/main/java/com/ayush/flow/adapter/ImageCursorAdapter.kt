package com.ayush.flow.adapter

import android.content.Context
import android.database.Cursor

import android.graphics.drawable.Drawable
import android.net.Uri

import android.provider.ContactsContract

import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.SimpleCursorAdapter

import android.widget.TextView
import java.io.FileNotFoundException
import java.io.InputStream


class ImageCursorAdapter(
    context: Context?,
    layout: Int,
    c: Cursor?,
    from: Array<String>,
    to: IntArray?,
    flags: Int
) :
    SimpleCursorAdapter(context, layout, c, from, to, flags) {
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val contactTextView = view.findViewById(com.ayush.flow.R.id.contact_name_TV) as TextView
        val contactName: String =
            cursor.getString(cursor.getColumnIndex(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ContactsContract.Contacts.DISPLAY_NAME_PRIMARY else ContactsContract.Contacts.DISPLAY_NAME))

        contactTextView.text = contactName
    }
}