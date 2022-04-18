package com.ayush.flow.activity

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.LayoutInflater
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
import com.ayush.flow.Services.ConnectionManager
import com.ayush.flow.Services.Permissions
import com.ayush.flow.adapter.ContactAdapter
import com.ayush.flow.database.ContactEntity
import com.ayush.flow.database.ContactViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Contact : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ContactAdapter
    lateinit var back: ImageView
    lateinit var title:TextView
    lateinit var add: ImageView
    lateinit var search: androidx.appcompat.widget.SearchView
    val sortCon = arrayListOf<ContactEntity>()
    lateinit var dim:View
    lateinit var pullToRefresh:SwipeRefreshLayout
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
        dim=findViewById(R.id.dim)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!

        pullToRefresh = findViewById(R.id.pullToRefresh)

        pullToRefresh.setOnRefreshListener {
            if(ConnectionManager().checkconnectivity(this)){
                if(Permissions().checkContactpermission(this)){
                    GlobalScope.launch {
                        Dashboard().loadContacts(application)
                    }
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.contact_permission,this.resources.getString(R.string.contact_permission),this,"contact")
                }
            }
            Handler().postDelayed({
                pullToRefresh.isRefreshing=false
            },2000)
        }



        recyclerAdapter=ContactAdapter(this@Contact)
        recyclerView.layoutManager=layoutManager
        recyclerView.adapter=recyclerAdapter

         ContactViewModel(application).allContacts.observe(this, Observer {list->
            list?.let {

                sortCon.clear()
                sortCon.addAll(list)

                val sortedCon = sortCon.sortedWith(compareBy({!it.isUser},{it.name}))

                recyclerAdapter.updateList(sortedCon)
            }

        })


        searchElement()

        add.setOnClickListener {
            val contactBoxView = LayoutInflater.from(this).inflate(R.layout.add_contact_box, null,false)
            val contactBoxBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
            contactBoxBuilder.setView(contactBoxView)
            contactBoxBuilder.setCancelable(false)
            val instance = contactBoxBuilder.show()

            val addname:TextView = contactBoxView.findViewById(R.id.name)
            val addnum:TextView = contactBoxView.findViewById(R.id.number)
            val cancel:Button = contactBoxView.findViewById(R.id.cancel)
            val save:Button=contactBoxView.findViewById(R.id.save)

            save.setOnClickListener {
                val name= addname.text.toString()
                val phone=addnum.text.toString()
                if (name!= "" || phone != "null") {
                    val addContactIntent = Intent(Intent.ACTION_INSERT)
                    addContactIntent.type = ContactsContract.Contacts.CONTENT_TYPE
                    addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, name)
                    addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                    startActivity(addContactIntent)
                    instance.dismiss()
//                GlobalScope.launch {
//                    loadContacts(application)
//                }
                }
                else{
                    Toast.makeText(applicationContext,"Enter Proper Details",Toast.LENGTH_SHORT).show()
                }
            }

            cancel.setOnClickListener {
                instance.dismiss()
            }
        }

        back.setOnClickListener {
            onBackPressed()
        }
    }





    fun searchElement() {

        search.queryHint="Search Your friends..."
        val searchIcon: ImageView = search.findViewById(R.id.search_mag_icon)

        val theTextArea = search.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete

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
            val sortedCon =filteredlist.sortedWith(compareBy({!it.isUser},{it.name}))
            recyclerAdapter.updateList(sortedCon)
        }
    }



}