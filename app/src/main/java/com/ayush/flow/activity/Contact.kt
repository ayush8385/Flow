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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ayush.flow.R
import com.ayush.flow.Services.ConnectionManager
import com.ayush.flow.utils.Permissions
import com.ayush.flow.adapter.ContactAdapter
import com.ayush.flow.adapter.ForwardAdapter
import com.ayush.flow.adapter.ForwardToAdapter
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.ContactEntity
import com.ayush.flow.database.ContactViewModel
import com.ayush.flow.databinding.ActivityContactBinding
import com.ayush.flow.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Contact : AppCompatActivity() {
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ContactAdapter
    val sortCon = arrayListOf<ContactEntity>()
    lateinit var firebaseUser: FirebaseUser
    lateinit var binding:ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutManager=LinearLayoutManager(this)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!

        binding.pullToRefresh.setOnRefreshListener {
            loadContacts()
            Handler().postDelayed({
                binding.pullToRefresh.isRefreshing=false
            },4000)
        }



        recyclerAdapter=ContactAdapter(this@Contact)
        binding.contactRecycler.layoutManager=layoutManager
        binding.contactRecycler.adapter=recyclerAdapter

         ContactViewModel(application).allContacts.observe(this, Observer {list->
            list?.let {

                sortCon.clear()
                sortCon.addAll(list)

//                val sortedCon = sortCon.sortedWith(compareBy({!it.isUser},{it.name}))

                recyclerAdapter.updateList(sortCon)
            }

        })


        searchElement()

        binding.addCon.setOnClickListener {
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

//        binding.inviteBtn.setOnClickListener {
//            openInviteBottomSheet(this)
//        }

        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

//    private fun openInviteBottomSheet(context:Context) {
//        val bottomSheetDialog = BottomSheetDialog(context,R.style.RoundedBottomSheetTheme)
//        bottomSheetDialog.setContentView(R.layout.forward_modal_bottomsheet)
//        bottomSheetDialog.setCancelable(true)
//        bottomSheetDialog.show()
//
//
//    }

    private fun loadContacts() {
        if(ConnectionManager().checkconnectivity(this)){
            if(Permissions().checkContactpermission(this)){
                startService(Intent(this, LoadContacts::class.java))
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.contact_permission,this.resources.getString(R.string.contact_permission),this,Constants.CONTACT_PERMISSION)
            }
        }
    }

    override fun onResume() {
        loadContacts()
        super.onResume()
    }

    fun searchElement() {

        binding.searchview.queryHint="Search Your friends..."
        val searchIcon: ImageView = binding.searchview.findViewById(R.id.search_mag_icon)

        val theTextArea = binding.searchview.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete

        theTextArea.isCursorVisible=false

        binding.searchview.setOnSearchClickListener {
            binding.back.visibility= View.GONE
            binding.title.visibility=View.GONE
            binding.addCon.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            binding.searchview.layoutParams=params
        }

        binding.searchview.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                binding.back.visibility= View.VISIBLE
                binding.title.visibility=View.VISIBLE
                binding.addCon.visibility=View.VISIBLE
                val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                binding.searchview.layoutParams=params
                return false
            }

        })

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchview.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchview.clearFocus()
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