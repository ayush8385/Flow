package com.ayush.flow.activity

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
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
import android.view.animation.LinearInterpolator

import android.view.animation.Animation

import android.view.animation.RotateAnimation
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.ayush.flow.Services.Permissions

import com.google.android.material.bottomsheet.BottomSheetDialog

import android.os.Bundle
import android.provider.ContactsContract
import com.ayush.flow.Notification.MessagingService


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
    lateinit var mode:ImageView
    lateinit var hidden:ImageView
    lateinit var hidden_close:ImageView
    val hashMap: HashMap<Int, NotificationCompat.MessagingStyle?> = HashMap()
    var messageStyle: NotificationCompat.MessagingStyle? =NotificationCompat.MessagingStyle("Me")
    lateinit var search: androidx.appcompat.widget.SearchView
    lateinit var profile:CircleImageView
    var controller:LayoutAnimationController?=null
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItem: MenuItem?=null
    lateinit var contact: ImageView
    lateinit var firebaseUser: FirebaseUser
    lateinit var story_box:LinearLayout
    lateinit var hiddenViewModel: HiddenViewModel

    lateinit var theme: SwitchCompat

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
        hidden=findViewById(R.id.hidden)
        mode=findViewById(R.id.mode)
        hidden_close=findViewById(R.id.close_hidden)
        story_box=findViewById(R.id.story_box)


        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        hiddenViewModel=ViewModelProviders.of(this).get(HiddenViewModel::class.java)

        if(Permissions().checkContactpermission(this)){

        }
        else{
            Permissions().openPermissionBottomSheet(R.drawable.contact_permission,this.resources.getString(R.string.contact_permission),this,"contact")
        }



        openChatHome(intent.getIntExtra("private",0))


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


                       openChatHome(0)

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

        if(sharedPreferences.getBoolean("nightMode",false)){
            mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.moon))
        }
        else{
            mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.sun))
        }


//        Handler().postDelayed({
//            updateImages()
//        },3000)


        hidden.setOnClickListener {

            val intent = Intent(this,Passcode::class.java)
            intent.putExtra("n",2)
            startActivity(intent)


        }

        hidden_close.setOnClickListener {
            story_box.visibility=View.VISIBLE
            story_text.visibility=View.VISIBLE
            hidden.visibility=View.VISIBLE

            chat_text.text="Chats"
            hidden_close.visibility=View.GONE

            openChatHome(0)
        }


        mode.setOnClickListener {
            val rotate = RotateAnimation(
                0F,
                360F,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 800
            rotate.interpolator = LinearInterpolator()
            mode.startAnimation(rotate)

            Handler().postDelayed({
                if(sharedPreferences.getBoolean("nightMode",false)){
                    // mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.sun))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPreferences.edit().putBoolean("nightMode",false).apply()

                }
                else{
                    //  mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.moon))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPreferences.edit().putBoolean("nightMode",true).apply()

                }
            },600)
        }

        searchElement()

        retrieveMessage(application).execute()

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

            override fun deleteChat(id: String,name: String) {
                TODO("Not yet implemented")
            }

            override fun hideChat(id: String,name: String) {
                TODO("Not yet implemented")
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

    private fun openChatHome(i: Int) {

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

            override fun deleteChat(id: String,name:String) {
                showDeleteBottomSheetDialog(id,name)

            }

            override fun hideChat(id: String,name: String) {
                showHideBottomSheetDialog(id,name)
            }

        })

        layoutManager=LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).reverseLayout=true
        chatsRecyclerView.layoutManager=layoutManager
      //  runAnimation(chatsRecyclerView,1)
        chatsRecyclerView.adapter=chatAdapter

      //  chatsRecyclerView.layoutAnimation=controller
      //  chatsRecyclerView.scheduleLayoutAnimation()

        if(i==0){
            ChatViewModel(application).allChats.observe(this, Observer { list->
                list?.let {
                    chatList.clear()
                    for(j in list){
                        if(!j.hide){
                            chatList.add(j)
                        }
                    }

                    chatAdapter.updateList(chatList)
                }
            })
        }
        else{
            story_box.visibility=View.GONE
            story_text.visibility=View.GONE
            hidden.visibility=View.GONE

            chat_text.text="Private Chats"
            hidden_close.visibility=View.VISIBLE

            ChatViewModel(application).allChats.observe(this, Observer { list->
                list?.let {
                    chatList.clear()
                    for(j in list){
                        if(j.hide){
                            chatList.add(j)
                        }
                    }

                    chatAdapter.updateList(chatList)
                }
            })
        }

   //     updateImages()

//        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        runAnimation(storyRecyclerView,1)
//        val adapter2=StoryAdapter(this,storyList)
//        adapter2.notifyDataSetChanged()
//        storyRecyclerView.adapter=adapter2
//        storyRecyclerView.layoutAnimation=controller
//        storyRecyclerView.scheduleLayoutAnimation()
    }

    private fun updateImages() {
        updateUnsavedImage().execute()
        chatAdapter.notifyDataSetChanged()
    }

    private fun showHideBottomSheetDialog(id: String,name:String) {


        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.delete_modal_bottomsheet)

        val text = bottomSheetDialog.findViewById<TextView>(R.id.textView)
        val summary = bottomSheetDialog.findViewById<TextView>(R.id.text2)
        val btn1=bottomSheetDialog.findViewById<Button>(R.id.btn_delete)
        val btn2=bottomSheetDialog.findViewById<Button>(R.id.btn_cancel)

        val hideChat =  ChatViewModel(application).getChat(id)


        if(!hideChat.hide){
            text!!.text="Hide "+name+" Chats"
            btn1!!.text="Hide"
            summary!!.text="This will Hide your selected chat to private Section"
        }
        else{
            text!!.text="Unhide "+name+" Chats"
            btn1!!.text="Unhide"
            summary!!.text="This will Unhide your selected chat to private Section"
        }


        btn1!!.setOnClickListener {



            val intent = Intent(this,Passcode::class.java)
            intent.putExtra("id",id)
            if(!hideChat.hide){
                intent.putExtra("n",1)
            }
            else{
                intent.putExtra("n",0)
            }
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        btn2!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun showDeleteBottomSheetDialog(id: String, name: String) {
        val bottomSheetDialog = BottomSheetDialog(this,R.style.AppBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.delete_modal_bottomsheet)

        val text = bottomSheetDialog.findViewById<TextView>(R.id.textView)
        val summary = bottomSheetDialog.findViewById<TextView>(R.id.text2)
        val btn1=bottomSheetDialog.findViewById<Button>(R.id.btn_delete)
        val btn2=bottomSheetDialog.findViewById<Button>(R.id.btn_cancel)


        text!!.text="Delete Chats with "+name
        summary!!.text="This will delete your selected chat including Medias"

        btn1!!.setOnClickListener {
            Toast.makeText(applicationContext,"Deleted",Toast.LENGTH_LONG).show()


            val notId: Int = Regex("[\\D]").replace(id, "").toInt()
            NotificationManagerCompat.from(applicationContext).cancel(notId)
            MessagingService.messsageHashmap.remove(notId)
            if(MessagingService.messsageHashmap.size==0){
                NotificationManagerCompat.from(applicationContext).cancel(0)
            }
            deleteMsgs(application,id).execute()
            Handler().postDelayed({
                ChatViewModel(application).deleteChat(id)
            },1000)
            bottomSheetDialog.dismiss()
        }
        btn2!!.setOnClickListener {
           bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun searchElement(){

        search.queryHint="Search Your Friends..."
        search.setIconifiedByDefault(false)
        val closeIcon: ImageView = search.findViewById(R.id.search_close_btn);

        val theTextArea = search.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete

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
            if(item.name.toLowerCase().contains(text.toLowerCase())||item.number.contains(text)||item.lst_msg.contains(text)){
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
                        val url=snapshot.child("url").value.toString()
                        val received=snapshot.child("received").value as Boolean

                        //time set
                        val sdf = SimpleDateFormat("hh:mm a")
                        var tm: Date = Date(time.toLong())

                        val date= SimpleDateFormat("dd/MM/yy")

                        var name:String=""
                        var number:String=""
                        var imagepath:String=""


                        if(ChatViewModel(application).isUserExist(sender)){
                            //get image and name from room db
                            val chatEntity=ChatViewModel(application).getChat(sender)
                            name=chatEntity.name
                            number=chatEntity.number
                            imagepath=chatEntity.image
                            var unread=chatEntity.unread
                            val hide=chatEntity.hide
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                            if(type=="doc"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                            if(type=="message"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg,sdf.format(tm),date.format(tm),hide,unread++,sender))
                            }
                        }
                        else if(ContactViewModel(application).isUserExist(sender)){
                            val contactEntity=ContactViewModel(application).getContact(sender)
                            name=contactEntity.name
                            number=contactEntity.number
                            imagepath=contactEntity.image
                            if(type=="image"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
                            }
                            if(type=="doc"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,0,sender))
                            }
                            if(type=="message"){
                                ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
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
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
                                    }
                                    else{
                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
                                    }

                                    //get image of sender
                                    val image_url=snapshot.child("profile_photo").value.toString()
                                    if(image_url!=""){
                                        GetImageFromUrl(sender,application).execute(image_url)
                                        if(type=="image"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),date.format(tm),false,1,sender))
                                        }
                                        if(type=="doc"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,1,sender))
                                        }
                                        if(type=="message"){
                                            ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),date.format(tm),false,1,sender))
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

                        if(type=="doc"){
//                            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Documents")
//
//                            val url=snapshot.child("link").value.toString()
//
//                            if(directory.exists()){
//                                val pdfFile = File(directory,msg)
//                                try {
//                                    pdfFile.createNewFile()
//                                } catch (e: IOException) {
//                                    e.printStackTrace()
//                                }
//                                FileDownloader().downloadFile(url, pdfFile)
//                            }
//                            else{
//                                directory.mkdirs()
//                                if(directory.isDirectory){
//                                    val pdfFile = File(directory,msg)
//                                    try {
//                                        pdfFile.createNewFile()
//                                    } catch (e: IOException) {
//                                        e.printStackTrace()
//                                    }
//                                    FileDownloader().downloadFile(url, pdfFile)
//                                }
//                            }
                        }

                        if(!received){
                            MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,msg,sdf.format(tm),date.format(tm),type,url,false,false,false))


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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            103 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do_SOme_Operation()
                    loadContacts(application).execute()


                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }


    inner class loadContacts(val applicat: Application) : AsyncTask<Void, Void, Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            var firebaseUser=FirebaseAuth.getInstance().currentUser!!
            val contentResolver = applicat.contentResolver
           val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)


            while (contacts?.moveToNext() == true) {
                val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                phoneNumber=phoneNumber.replace("\\s".toRegex(), "")
                if(phoneNumber.length==13){
                    phoneNumber=phoneNumber.replace("\\+91".toRegex(),"")
                }

                if (phoneNumber.length==10){
                    val ref=FirebaseDatabase.getInstance().reference.child("Users")
                    ref.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(snapshot in snapshot.children){
                                val num=snapshot.child("number").value.toString()
                                val userid=snapshot.child("uid").value.toString()
                                val url=snapshot.child("profile_photo").value.toString()

                                if(userid!=firebaseUser.uid && phoneNumber==num){

//                                    if(ContactViewModel(application).isUserExist(userid)){
//                                        ContactViewModel(application).updateDetails(userid,name,phoneNumber)
//                                    }
//                                    else{
                                        ContactViewModel(applicat).inserContact(ContactEntity(name,phoneNumber,"",userid))
                                  //  }

                                  //  val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
                                  //  var currentDateandTime: String = sdf.format(Date())

                                    if(url!=""){
                                        GetImageFromUrl(userid,applicat).execute(url)
                                    }

                                   // currentDateandTime = sdf.format(Date())

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }



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

    inner class deleteMsgs(val application: Application,val id:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val firebaseUser=FirebaseAuth.getInstance().currentUser!!
            MessageViewModel(application).deleteMsgWithId(firebaseUser.uid+"-"+id)

            val refer=FirebaseDatabase.getInstance().reference.child("Messages")
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(firebaseUser.uid)){
                        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)

                        ref.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapShot: DataSnapshot) {
                                for(snap in snapShot.children){
                                    if(snap.child("sender").value==id){
                                        snap.child("sender").ref.parent!!.removeValue()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

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
        hiddenViewModel.getText().observe(this,Observer{it->
            if(it=="0"){

            }
            else{
                story_box.visibility=View.GONE
                story_text.visibility=View.GONE
                hidden.visibility=View.GONE

                chat_text.text="Private Chats"
                hidden_close.visibility=View.VISIBLE

                openChatHome(1)
            }
        })
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
