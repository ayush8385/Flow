package com.ayush.flow.activity

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.Notification.Token
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
import com.google.firebase.iid.FirebaseInstanceId
import com.sinch.android.rtc.SinchError
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap





class Dashboard : BaseActivity(), SinchService.StartFailedListener {
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
    val hashMap: HashMap<Int, NotificationCompat.MessagingStyle?> = HashMap()
    var messageStyle: NotificationCompat.MessagingStyle? =NotificationCompat.MessagingStyle("Me")
    lateinit var search: androidx.appcompat.widget.SearchView
    lateinit var profile:CircleImageView
    var controller:LayoutAnimationController?=null
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItem: MenuItem?=null
    lateinit var contact: ImageView
    lateinit var firebaseUser: FirebaseUser

    override fun onServiceConnected() {
        sinchServiceInterface!!.setStartListener(this)
    }

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

        openChatHome()

        profile.setOnClickListener{
            startActivity(Intent(this,UserProfile::class.java))
        }

        contact.setOnClickListener {
            startActivity(Intent(this, Contact::class.java))
        }

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

        searchElement()

       if(checkpermission()){
           loadContacts(application).execute()
        }
        else{
            requestContactPermission()
        }

        UpdateToken()
    }

    inner class updateUnsavedImage():AsyncTask<Void,Void,Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            for(chat in chatList){
                if(chat.name==""){
                    val ref=FirebaseDatabase.getInstance().reference.child("Users").child(chat.id)
                    ref.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val image_url=snapshot.child("profile_photo").value.toString()
                            if(image_url!=""){
                                GetImageFromUrl(chat.id,application).execute(image_url)

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }
            }
            return true
        }

    }

    private fun openCallHome() {
        callAdapter= CallAdapter(this@Dashboard,object :ChatAdapter.OnAdapterItemClickListener{
            override fun audioCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUser(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("image",image)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

            override fun videoCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUserVideo(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing_vdo::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("image",image)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

        })
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
        chatAdapter= ChatAdapter(this@Dashboard,object :ChatAdapter.OnAdapterItemClickListener{
            override fun audioCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUser(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("image",image)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

            override fun videoCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUserVideo(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing_vdo::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("image",image)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

        })

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

                chatAdapter.updateList(chatList)
            }
        })

        updateUnsavedImage().execute()
        chatAdapter.notifyDataSetChanged()

//        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        runAnimation(storyRecyclerView,1)
//        val adapter2=StoryAdapter(this,storyList)
//        adapter2.notifyDataSetChanged()
//        storyRecyclerView.adapter=adapter2
//        storyRecyclerView.layoutAnimation=controller
//        storyRecyclerView.scheduleLayoutAnimation()
    }

    fun searchElement(){

        search.queryHint="Search Your friends..."
        search.setIconifiedByDefault(false)
        val searchIcon: ImageView = search.findViewById(R.id.search_mag_icon);
        searchIcon.visibility= View.GONE
        searchIcon.setImageDrawable(null)
        val closeIcon: ImageView = search.findViewById(R.id.search_close_btn);
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
            if(item.name.toLowerCase().contains(text.toLowerCase())||item.number.contains(text)){
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

//    inner class updateChatList():AsyncTask<Void,Void,Boolean>(){
//        override fun doInBackground(vararg params: Void?): Boolean {
//
//            for(chat in chatList){
//                if(ContactViewModel(application).isUserExist(chat.id)){
//                    val conEnt=ContactViewModel(application).getContact(chat.id)
//                    ChatViewModel(application).inserChat(ChatEntity(conEnt.name,conEnt.number,conEnt.image,"","678687",conEnt.id))
//                }
//            }
//
//            return true
//        }
//    }

    inner class retrieveMessage(val application:Application): AsyncTask<Void, Void, Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val firebaseUser=FirebaseAuth.getInstance().currentUser!!
            val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)

            ref.addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)

                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snapshot in snapshot.children){

                        val messageKey=snapshot.child("mid").value.toString()
                        val user=snapshot.child("userid").value.toString()
                        val sender=snapshot.child("sender").value.toString()
                        var msg=snapshot.child("message").value.toString()
                        val time=snapshot.child("time").value.toString()
                        val type=snapshot.child("type").value.toString()
                        val received=snapshot.child("received").value as Boolean

                        //time set
                        val sdf = SimpleDateFormat("hh:mm a")
                        val tm: Date = Date(time.toLong())

                        var name:String=""
                        var number:String=""
                        var imagepath:String=""


                        if(ChatViewModel(application).isUserExist(sender)){
                            //get image and name from room db
                            val chatEntity=ChatViewModel(application).getChat(sender)
                            name=chatEntity.name
                            number=chatEntity.number
                            imagepath=chatEntity.image
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),sender))
                            }
                            else{
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),sender))
                            }
                        }
                        else if(ContactViewModel(application).isUserExist(sender)){
                            val contactEntity=ContactViewModel(application).getContact(sender)
                            name=contactEntity.name
                            number=contactEntity.number
                            imagepath=contactEntity.image
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),sender))
                            }
                            else{
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),sender))
                            }
                        }
                        else{
                            //get number as a name from firebase
                            //get image and sav it to local storage and internal path from firebase
                            val refer=FirebaseDatabase.getInstance().reference.child("Users").child(sender)
                            refer.addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    number=snapshot.child("number").value.toString()
                                    //check message type
                                    if(type=="image"){
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),sender))
                                    }
                                    else{
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),sender))
                                    }

                                    //get image of sender
                                    val image_url=snapshot.child("profile_photo").value.toString()
                                    if(image_url!=""){
                                        GetImageFromUrl(sender,application).execute(image_url)
                                        if(type=="image"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),sender))
                                        }
                                        else{
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),sender))
                                        }
                                    }
                                    //     ContactViewModel(application).inserContact(ContactEntity(name,number,"",sender))

                                }
                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        }

                        if(type=="image"){

                        //    MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,"",sdf.format(tm),type,false,false,false))

                            val photo =  GetImageFromUrl().execute(msg).get()

                            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                                if (Environment.isExternalStorageManager()) {
                                    val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
                                    if(directory.exists()){
                                        msg=messageKey+".jpg"
                                        var fos: FileOutputStream =
                                            FileOutputStream(File(directory, msg))
                                        try {
                                      //      photo.compress(Bitmap.CompressFormat.JPEG, 25, fos)
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
                                            msg=messageKey+".jpg"
                                            val fos =
                                                FileOutputStream(File(directory, msg))
                                            try {
                                        //       photo.compress(Bitmap.CompressFormat.JPEG, 25, fos)
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
                                } else {
                                    //request for the permission
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivity(intent)
                                }
                            }
                            else{
                                val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
                                if(directory.exists()){
                                    msg=messageKey+".jpg"
                                    var fos: FileOutputStream =
                                        FileOutputStream(File(directory,msg))
                                    try {
                                        photo.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
                                        msg=messageKey+".jpg"
                                        val fos =
                                            FileOutputStream(File(directory, msg))
                                        try {
                                           photo.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
                            }
                        }

                        if(!received){
                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,msg,sdf.format(tm),type,false,false,false))


                           // sendNotification(sender,name,msg,imagepath,application).execute()
                            val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
                            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.child(messageKey).exists()){
                                        FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(messageKey).child("received").setValue(true)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
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

    companion object{
        const val CHANNEL_ID="com.ayush.flow"
        private const val CHANNEL_NAME="Flow"
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val bitmap2: Bitmap
        val r: Float
        if (bitmap.getWidth() > bitmap.getHeight()) {
            bitmap2 = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)
        } else {
            bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888)
        }
        val output: Bitmap = bitmap2
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        if(bitmap.getWidth() > bitmap.getHeight()){
            r= (bitmap.getHeight() / 2).toFloat()
        }
        else {
            r= (bitmap.getWidth()/2).toFloat()
        }
        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(-12434878)
        canvas.drawCircle(r, r, r, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
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
                    loadContacts(application).execute()

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

    inner class loadContacts(val applicat: Application) : AsyncTask<Void, Void, Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            var firebaseUser=FirebaseAuth.getInstance().currentUser!!
            val contentResolver = applicat.contentResolver
            val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

            while (contacts?.moveToNext() == true) {
                val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                var i=0
                var phone_num:String=""
                while(i!=phoneNumber.length){
                    if(phoneNumber[i]>='0'&&phoneNumber[i]<='9'){
                        phone_num=phone_num+phoneNumber[i]
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
                            if(userid!=firebaseUser.uid && phone_num==num){

                                if(ContactViewModel(application).isUserExist(userid)){
                                    ContactViewModel(application).updateDetails(userid,name,phone_num)
                                }
                                else{
                                    ContactViewModel(applicat).inserContact(ContactEntity(name,phone_num,"",userid))
                                }

                                val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
                                var currentDateandTime: String = sdf.format(Date())
                                Log.d("tiome1......",currentDateandTime)

                                if(url!=""){
                                   GetImageFromUrl(userid,application).execute(url)
                                }

                                currentDateandTime = sdf.format(Date())
                                Log.d("time......",currentDateandTime)
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

    inner class saveToInternalStorage:AsyncTask<Void,Void,Boolean>{

        var path:String?=null
        var user:String?=null
        var bitmapImage:Bitmap?=null
        var app:Application?=null

        constructor(bitmapImage: Bitmap, user: String,appl: Application) : super() {
            this.user=user
            this.bitmapImage=bitmapImage
            app=appl
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            ChatViewModel(app!!).updatetChat(user!!,path!!)
            ContactViewModel(app!!).updateImage(user!!,path!!)
//            chatAdapter.notifyDataSetChanged()
        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Contacts Images")
            if(directory.exists()){
                path=user+".jpg"
                var fos: FileOutputStream =
                    FileOutputStream(File(directory, path))
                try {
                    bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
                    path=user+".jpg"
                    val fos =
                        FileOutputStream(File(directory, path))
                    try {
                        bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
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
            return true
        }

    }


    inner class GetImageFromUrl(val userid: String,val appl:Application) : AsyncTask<String?, Void?, Bitmap>() {
        var bmp:Bitmap?=null

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            saveToInternalStorage(bmp!!,userid,appl).execute()
        }

        override fun doInBackground(vararg url: String?): Bitmap {
            val stringUrl = url[0]
            bmp= null

            val options = BitmapFactory.Options()
            options.inSampleSize = 1
            while (options.inSampleSize <= 32) {
                val inputStream = URL(stringUrl).openStream()
                try {
                    bmp= BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream.close()
                    break
                } catch (outOfMemoryError: OutOfMemoryError) {
                }
                options.inSampleSize++
            }

         return bmp!!
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

    override fun onStartFailed(error: SinchError?) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onStarted() {

    }

    private fun UpdateToken() {
        val refreshToken = FirebaseInstanceId.getInstance().token
        val token = Token(refreshToken!!)
        FirebaseDatabase.getInstance().getReference("Token").child(
            FirebaseAuth.getInstance().currentUser!!.uid
        ).setValue(token)
    }

}