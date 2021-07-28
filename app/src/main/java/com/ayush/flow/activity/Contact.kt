package com.ayush.flow.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ayush.flow.R
import com.ayush.flow.adapter.ContactAdapter
import com.ayush.flow.database.ContactEntity
import com.ayush.flow.database.ContactViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Contact : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ContactAdapter
    lateinit var back:ImageView
    lateinit var title:TextView
    lateinit var add:ImageView
    lateinit var con_card:CardView
    lateinit var search: androidx.appcompat.widget.SearchView
    val sortCon = arrayListOf<ContactEntity>()
    lateinit var cancel:Button
    lateinit var save:Button
    lateinit var dim:View
    lateinit var name_con:EditText
    lateinit var pullToRefresh:SwipeRefreshLayout
    lateinit var  num_con:EditText
    lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        layoutManager=LinearLayoutManager(this)
        recyclerView=findViewById(R.id.contact_recycler)
        search=findViewById(R.id.searchview)
        back=findViewById(R.id.back)
        title=findViewById(R.id.title)
        add=findViewById(R.id.add_con)
        name_con=findViewById(R.id.name)
        num_con=findViewById(R.id.number)
        con_card=findViewById(R.id.contact_card)
        save=findViewById(R.id.save)
        cancel=findViewById(R.id.cancel)
        dim=findViewById(R.id.dim)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!

        pullToRefresh = findViewById(R.id.pulTooRefresh)

        pullToRefresh.setOnClickListener {
//            Handler().postDelayed(Runnable {
//                if (pullToRefresh.isRefreshing) {
//                    pullToRefresh.isRefreshing=false
//                }
//            }, 300)
//            loadContacts().execute()

        }


        searchElement()
        recyclerAdapter=ContactAdapter(this@Contact)
        recyclerView.layoutManager=layoutManager
        recyclerView.adapter=recyclerAdapter

         ContactViewModel(application).allContacts.observe(this, Observer {list->
            list?.let {

                sortCon.clear()
                sortCon.addAll(list)

                recyclerAdapter.updateList(sortCon)
            }

        })

        add.setOnClickListener {
            con_card.visibility=View.VISIBLE
            dim.visibility=View.VISIBLE
        }

        save.setOnClickListener {
            val name=name_con.text.toString()
            val phone=num_con.text.toString()
            if (name!= "" || phone != "null") {
                val addContactIntent = Intent(Intent.ACTION_INSERT)
                addContactIntent.type = ContactsContract.Contacts.CONTENT_TYPE
                addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, name)
                addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                startActivity(addContactIntent)
                con_card.visibility=View.GONE
                dim.visibility=View.GONE
            }
            else{
                Toast.makeText(applicationContext,"Enter Proper Details",Toast.LENGTH_SHORT).show()
            }

        }

        cancel.setOnClickListener {
            con_card.visibility=View.GONE
            dim.visibility=View.GONE
        }

        back.setOnClickListener {
            onBackPressed()
        }
    }



    fun searchElement() {

        search.queryHint="Search Your friends..."
        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)
        val theTextArea = search.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
        theTextArea.setTextColor(Color.WHITE)
        theTextArea.isCursorVisible=false

        search.setOnSearchClickListener {
            back.visibility= View.GONE
            title.visibility=View.GONE
            add.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            search.layoutParams=params
        }

        search.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                back.visibility= View.VISIBLE
                title.visibility=View.VISIBLE
                add.visibility=View.VISIBLE
                val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                search.layoutParams=params
                return false
            }

        })

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                search.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterr(newText!!)
                return true
            }

        })
    }

    fun filterr(text:String){
        val filteredlist:ArrayList<ContactEntity> = ArrayList()

        for(item in sortCon){
            if(item.name.toLowerCase().contains(text.toLowerCase())){
                //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                filteredlist.add(item)
            }
            else if(item.number.toLowerCase().contains(text.toLowerCase())){
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()){
            Toast.makeText(applicationContext,"No Data found", Toast.LENGTH_SHORT).show()
            recyclerAdapter.updateList(filteredlist)
        }
        else{
            recyclerAdapter.updateList(filteredlist)
        }
    }



}