package com.ayush.flow.activity

import android.app.Activity
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Loader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.Cursor

import android.provider.ContactsContract

import android.widget.AdapterView

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.annotation.RequiresApi
import com.ayush.flow.R
import com.ayush.flow.adapter.ImageCursorAdapter
import java.lang.StringBuilder


class LoadingCons : Activity(), LoaderManager.LoaderCallbacks<Cursor?>,
    OnItemClickListener {
    private var mAdapter: ImageCursorAdapter? = null
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.ayush.flow.R.layout.activity_loading_cons)


        loaderManager.initLoader(CONTACTS_LOADER_ID, null, this@LoadingCons)

        setupCursorAdapter()

        val listViewContacts: ListView = findViewById<View>(com.ayush.flow.R.id.contacts_list) as ListView
        listViewContacts.setAdapter(mAdapter)
        listViewContacts.setOnItemClickListener(this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?>? {
        // Define the columns to retrieve
        val projectionFields = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ContactsContract.Contacts.DISPLAY_NAME_PRIMARY else ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )
        // Construct the loader
        // Return the loader for use
        return CursorLoader(
            this,
            ContactsContract.Contacts.CONTENT_URI,  // URI
            projectionFields,  // projection fields
            null,  // the selection criteria
            null,  // the selection args
            null // the sort order
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor?>?, data: Cursor?) {

        mAdapter!!.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>?) {
        mAdapter!!.swapCursor(null)
    }

    private fun setupCursorAdapter() {
        // Column data from cursor to bind views from
        val uiBindFrom = arrayOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ContactsContract.Contacts.DISPLAY_NAME_PRIMARY else ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )
        // View IDs which will have the respective column data inserted
        val uiBindTo = intArrayOf(R.id.contact_name_TV, R.id.contact_image_IV)
        // Create the simple cursor adapter to use for our list
        // specifying the template to inflate (item_contact),
        mAdapter = ImageCursorAdapter(
            this, R.layout.contacts_list_item,
            null, uiBindFrom, uiBindTo,
            0
        )
    }

    override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val stringBuilder = StringBuilder()
        // Get the Cursor
        val cursor: Cursor = (parent.adapter as SimpleCursorAdapter).getCursor()
        // Move to the selected contact
        cursor.moveToPosition(position)
        // Get the _ID value
        val mContactId: Long = cursor.getLong(CONTACT_ID_INDEX)

        //Get all phone numbers for the contact
        val phones: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId, null, null
        )
        if ((if (phones != null) phones.getCount() else 0) > 0) stringBuilder.append("Phones \n")
        while (phones != null && phones.moveToNext()) {
            val number: String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            stringBuilder.append("$number ")
        }
        if (phones != null) {
            phones.close()
        }

        Log.e("contacts.............",stringBuilder.toString())
    }
    companion object {
        private const val CONTACT_ID_INDEX = 0
        private const val CONTACTS_LOADER_ID = 1
    }
}