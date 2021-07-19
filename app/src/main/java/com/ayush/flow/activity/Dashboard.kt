package com.ayush.flow.activity

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.adapter.CallAdapter
import com.ayush.flow.adapter.ChatAdapter
import com.ayush.flow.adapter.StoryAdapter
import com.ayush.flow.database.*
import com.ayush.flow.model.Chats
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Dashboard : AppCompatActivity() {
    lateinit var storyRecyclerView: RecyclerView
    lateinit var chatsRecyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var storyAdapter: StoryAdapter
    lateinit var chatAdapter: ChatAdapter
    lateinit var callAdapter: CallAdapter
    lateinit var navigationView: BottomNavigationView
    lateinit var title:TextView
    lateinit var story_text:TextView
    lateinit var chat_text:TextView
    lateinit var add_text:TextView
    var chatList= arrayListOf<ChatEntity>()
    var callList= arrayListOf<CallEntity>()
    var storyList= arrayListOf<Chats>()
    lateinit var search: androidx.appcompat.widget.SearchView
    lateinit var profile:CircleImageView
    var controller:LayoutAnimationController?=null
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItem: MenuItem?=null
    lateinit var contact:ImageView
    lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        storyRecyclerView=findViewById(R.id.storyrecycler)
        chatsRecyclerView=findViewById(R.id.chatsrecycler)
        navigationView=findViewById(R.id.bottom_navigation)
        title=findViewById(R.id.title)
        story_text=findViewById(R.id.stories)
        add_text=findViewById(R.id.story_txt)
        chat_text=findViewById(R.id.chats)
        contact=findViewById(R.id.contact)
        search=findViewById(R.id.chat_searchview)
        profile=findViewById(R.id.user_img)
        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!

        checkStatus().execute()

        openChatHome()
        retrieveMessage().execute()
        if(checkpermission()){
            loadContacts().execute()
        }
        else{
            requestContactPermission()
        }

        profile.setOnClickListener{
            startActivity(Intent(this,UserProfile::class.java))
        }


//        chatAdapter= ChatAdapter(this@Dashboard)
//        layoutManager=LinearLayoutManager(this)
//        (layoutManager as LinearLayoutManager).reverseLayout=true
//        chatsRecyclerView.layoutManager=layoutManager
//
//        chatsRecyclerView.adapter=chatAdapter
//
//        ChatViewModel(application).allChats.observe(this, Observer { list->
//            list?.let {
//                chatList.clear()
//                chatList.addAll(list)
//                chatAdapter.updateList(list)
//            }
//
//        })

        storyRecyclerView.adapter=StoryAdapter(this,storyList)
        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        contact.setOnClickListener {
            startActivity(Intent(this, Contact::class.java))
        }

        searchElement()

        navigationView.setOnNavigationItemSelectedListener{
            if(previousMenuItem==null){
                previousMenuItem=navigationView.menu.findItem(R.id.chat)
            }
            when(it.itemId){
                R.id.chat ->{
                    if(previousMenuItem!=it){
                        title.text="Messages"
                        story_text.text="Stories"
                        add_text.text="Me"
                        chat_text.text="Chats"


                       openChatHome()

                        it.isCheckable=true
                        it.isChecked=true
                        previousMenuItem=it
                    }
                }
                R.id.call ->{
                    if(previousMenuItem!=it){
                        title.text="Calls"
                        story_text.text="Favorites"
                        add_text.text="New"
                        chat_text.text="Calls"


                        openCallHome()

                        it.isCheckable=true
                        it.isChecked=true
                        previousMenuItem=it
                    }
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun openCallHome() {
        callAdapter= CallAdapter(this@Dashboard)
        layoutManager=LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).reverseLayout=true
        chatsRecyclerView.layoutManager=layoutManager
        runAnimation(chatsRecyclerView,0)
        chatsRecyclerView.adapter=callAdapter
        chatsRecyclerView.layoutAnimation=controller
        chatsRecyclerView.scheduleLayoutAnimation()

        CallViewModel(application).allCalls.observe(this, Observer { list->
            list?.let {
                callList.clear()
                callList.addAll(list)
                callAdapter.updateList(list)
            }
        })

        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        runAnimation(storyRecyclerView,0)
        val adapter2=StoryAdapter(this,storyList)
        adapter2.notifyDataSetChanged()
        storyRecyclerView.adapter=adapter2
        storyRecyclerView.layoutAnimation=controller
        storyRecyclerView.scheduleLayoutAnimation()
    }

    private fun openChatHome() {
        chatAdapter= ChatAdapter(this@Dashboard)
        layoutManager=LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).reverseLayout=true
        chatsRecyclerView.layoutManager=layoutManager
        runAnimation(chatsRecyclerView,1)
        chatsRecyclerView.adapter=chatAdapter
        chatsRecyclerView.layoutAnimation=controller
        chatsRecyclerView.scheduleLayoutAnimation()

        ChatViewModel(application).allChats.observe(this, Observer { list->
            list?.let {
                chatList.clear()
                chatList.addAll(list)
                chatAdapter.updateList(list)
            }
        })

        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        runAnimation(storyRecyclerView,1)
        val adapter2=StoryAdapter(this,storyList)
        adapter2.notifyDataSetChanged()
        storyRecyclerView.adapter=adapter2
        storyRecyclerView.layoutAnimation=controller
        storyRecyclerView.scheduleLayoutAnimation()
    }

    fun searchElement() {

        search.queryHint="Search Your friends..."
        search.setIconifiedByDefault(false)
        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon);
        searchIcon.visibility= View.GONE
        searchIcon.setImageDrawable(null)
        val closeIcon:ImageView = search.findViewById(R.id.search_close_btn);
        closeIcon.setColorFilter(Color.WHITE)
        val theTextArea = search.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
        theTextArea.setTextColor(Color.WHITE)
        theTextArea.setHintTextColor(Color.LTGRAY)
        theTextArea.isCursorVisible=false

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
        val filteredlist:ArrayList<ChatEntity> = ArrayList()

        for(item in chatList){
            if(item.name.toLowerCase().contains(text.toLowerCase())){
                //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                filteredlist.add(item)
            }
//            else if(item.number.toLowerCase().contains(text.toLowerCase())){
//                filteredlist.add(item)
//            }
        }
        if (filteredlist.isEmpty()){
            Toast.makeText(applicationContext,"No Data found", Toast.LENGTH_SHORT).show()
            chatAdapter.updateList(filteredlist)
        }
        else{
            chatAdapter.updateList(filteredlist)
        }
    }

    inner class checkStatus():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            var firebaseUser=FirebaseAuth.getInstance().currentUser!!
            val connectionReference=FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
            val lastConnected=FirebaseDatabase.getInstance().reference.child("lastConnected")
            val infoConnected=FirebaseDatabase.getInstance().getReference(".info/connected")

            infoConnected.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val connected:Boolean=snapshot.value as Boolean

                    if(connected){
                        val con=connectionReference.child("status")
                        con.setValue("online")
                        con.onDisconnect().setValue("")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            return true
        }
    }


    private fun runAnimation(chatsRecyclerView: RecyclerView?, i: Int) {
        val context:Context=chatsRecyclerView!!.context

        if(i==0){
            controller=AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_right)
        }
        if(i==1){
            controller=AnimationUtils.loadLayoutAnimation(context,R.anim.layout_slide_left)
        }
    }

    inner class retrieveMessage(): AsyncTask<Void, Void, Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)

            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snapshot in snapshot.children){
                        val messageKey=snapshot.child("mid").value.toString()
                        val user=snapshot.child("userid").value.toString()
                        val sender=snapshot.child("sender").value.toString()
                        val msg=snapshot.child("message").value.toString()
                        val time=snapshot.child("time").value.toString()
                        val type=snapshot.child("type").value.toString()

                        //time set
                        val sdf = SimpleDateFormat("hh:mm a")
                        val tm: Date = Date(time.toLong())

                        if(sender!=firebaseUser.uid){
                            var name:String=""
                            var imagepath:String=""
                            if(ChatViewModel(application).isUserExist(sender)){
                                ChatViewModel(application).deleteChat(sender)
                            }
                            if(ContactViewModel(application).isUserExist(sender)){
                                //get image and name from room db
                                val contactEntity=ContactViewModel(application).getContact(sender)
                                name=contactEntity.name
                                imagepath=contactEntity.image
                                ChatViewModel(application).inserChat(ChatEntity(name,imagepath,msg, sdf.format(tm),sender))
                            }
                            else{
                                //get number as a name from firebase
                                //get image and sav it to local storage and ibternal path from firebase

                                val ref=FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                                ref.addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        name=snapshot.child("number").value.toString()
                                        val image_url=snapshot.child("profile_photo").value.toString()
                                        if(image_url!=""){
                                            val photo=GetImageFromUrl().execute(image_url).get()
                                            imagepath=saveToInternalStorage(photo).execute().get()
                                        }

                                        ChatViewModel(application).inserChat(ChatEntity(name,imagepath,msg,sdf.format(tm),sender))

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            }

                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,msg,sdf.format(tm),type))
                            snapshot.child(messageKey).ref.parent!!.removeValue()

                            //FirebaseDatabase.getInstance().reference.child("Messages").child(id).child(messageKey).child("received").setValue(true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            return true
        }

    }

    fun checkpermission():Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            return false
        }
    }

    fun requestContactPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            1234
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            1234 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do_SOme_Operation()
                    loadContacts().execute()

                }
                else{
                    showMessageOKCancel("You need to allow access permissions",
                        DialogInterface.OnClickListener { dialog, which ->
                            requestContactPermission()
                        })
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    inner class loadContacts(): AsyncTask<Void, Void, Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            val contentResolver = contentResolver
            val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

            while (contacts?.moveToNext() == true) {
                val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                var i=0
                var number:String=""
                while(i!=phoneNumber.length){
                    if(phoneNumber[i]>='0'&&phoneNumber[i]<='9'){
                        number=number+phoneNumber[i]
                    }
                    i++
                }

                val ref=FirebaseDatabase.getInstance().reference.child("Users")
                ref.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(snapshot in snapshot.children){
                            val num=snapshot.child("number").value.toString()
                            val userid=snapshot.child("uid").value.toString()
                            val url=snapshot.child("profile_photo").value.toString()
                            if(userid!=firebaseUser.uid){
                                if(number==num){
                                    var imagepath=""
                                    if(url!=""){
                                        val photo=GetImageFromUrl().execute(url).get()
                                        imagepath=saveToInternalStorage(photo).execute().get()
                                    }
                                    ContactViewModel(application).inserContact(ContactEntity(name,number,imagepath,userid))
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            contacts!!.close()
            return true
        }
    }

    inner class saveToInternalStorage(val bitmapImage: Bitmap):AsyncTask<Void,Void,String>(){
        var path:String?=null
        override fun doInBackground(vararg params: Void?): String {
            val cw = ContextWrapper(applicationContext)
            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Contacts Images")
            if(directory.exists()){
                path=System.currentTimeMillis().toString()+".jpg"
                var fos: FileOutputStream =
                    FileOutputStream(File(directory, path))
                try {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            else{
                directory.mkdirs()
                if (directory.isDirectory) {
                    path=System.currentTimeMillis().toString()+".jpg"
                    val fos =
                        FileOutputStream(File(directory, path))
                    try {
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            return path!!
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Flow Profile photos"),sharedPreferences.getString("profile",""))
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            profile.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

}