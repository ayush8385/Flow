package com.ayush.flow.activity

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    lateinit var search: androidx.appcompat.widget.SearchView
    val sortCon = arrayListOf<ContactEntity>()
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
        firebaseUser= FirebaseAuth.getInstance().currentUser!!


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

        back.setOnClickListener {
            onBackPressed()
        }
    }



    private fun searchElement() {

        search.queryHint="Search Your friends..."
        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)
        val theTextArea = search.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
        theTextArea.setTextColor(Color.WHITE)

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