package com.ayush.flow.activity

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alimuzaffar.lib.pin.PinEntryEditText
import com.ayush.flow.Notification.MessagingService
import com.ayush.flow.Notification.Token
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.Services.SharedPreferenceUtils.init
import com.ayush.flow.adapter.CallAdapter
import com.ayush.flow.adapter.ChatAdapter
import com.ayush.flow.adapter.StoryAdapter
import com.ayush.flow.database.*
import com.ayush.flow.model.Chats
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.sinch.android.rtc.SinchError
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


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
    var previousMenuItem: MenuItem?=null
    lateinit var contact: ImageView
    lateinit var firebaseUser: FirebaseUser
    lateinit var story_box:LinearLayout
    lateinit var hiddenViewModel: HiddenViewModel

    lateinit var theme: SwitchCompat
    lateinit var vibrator: Vibrator

    override fun onServiceConnected() {
        sinchServiceInterface!!.setStartListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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


        SharedPreferenceUtils.init(applicationContext)
        hiddenViewModel= ViewModelProvider(this).get(HiddenViewModel::class.java)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if(ConnectionManager().checkconnectivity(this)){
            if(Permissions().checkContactpermission(this)){
                GlobalScope.launch {
                    loadContacts(application)
                }
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.contact_permission,this.resources.getString(R.string.contact_permission),this,"contact")
            }
        }


        openChatHome(SharedPreferenceUtils.getIntPreference(SharedPreferenceUtils.IS_PRIVATE,0))

        GlobalScope.launch(Dispatchers.Main) {
            searchElement()
            UpdateToken()
        }


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
        //sharedPreferences.edit().putString("passcode","").apply()

        if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
            mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.moon))
        }
        else{
            mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.sun))
        }


        hidden.setOnClickListener {
//            val intent = Intent(this,Passcode::class.java)
//            intent.putExtra("n",2)
//            startActivity(intent)
            showPasscodeBox("",2)

        }

        hidden_close.setOnClickListener {
            SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,0)
            openChatHome(0)
//            Toast.makeText(applicationContext,"hide 222",Toast.LENGTH_SHORT).show()
//            openChatHome(0)
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
                if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
                    // mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.sun))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)

                }
                else{
                    //  mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.moon))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,true)

                }
            },600)
        }

//        GlobalScope.launch {
//            retrieveMessage(application)
//        }


    }

    private fun showPasscodeBox(id:String,n:Int) {
        val passBoxView = LayoutInflater.from(this).inflate(R.layout.pass_verify, null,false)
        val passBoxBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        passBoxBuilder.setView(passBoxView)
        passBoxBuilder.setCancelable(false)
        val instance = passBoxBuilder.show()

        val passCard:CardView = passBoxView.findViewById(R.id.pass_card)
        val title:TextView = passBoxView.findViewById(R.id.title)
        val cancel:Button = passBoxView.findViewById(R.id.cancel)

        var passcode = SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.HIDE_PASS,"")
        if(passcode==""){
            title.text="Set Passcode"
        }

        val pinEntry:PinEntryEditText = passBoxView.findViewById(R.id.txt_pin_entry)
        pinEntry.requestFocus()

        pinEntry.postDelayed(Runnable { // TODO Auto-generated method stub
            val keyboard = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(pinEntry, 0)
        }, 100)

        pinEntry.setOnPinEnteredListener { str ->

            if(passcode==""){
                passcode=str.toString()
                title.text="Confirm Password"
                pinEntry.setText(null)
            }
            else{
                if(title.text=="Confirm Password"){
                    if(passcode==str.toString()) {
                        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.HIDE_PASS,str.toString())
                        SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,1)
                        openChatHome(1)
                        instance.dismiss()
                        //  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                    }
                    else{
                        val animShake = AnimationUtils.loadAnimation(this, R.anim.shake)
                        passCard.startAnimation(animShake)
                        startVibrationforPassBox()
                        Toast.makeText(this,"Wrong Passcode",Toast.LENGTH_SHORT).show()
                        pinEntry.setText(null)
                    }

                }
                else if(passcode==str.toString()){
                    if(n==0){
                        ChatViewModel(application).setPrivate(id,false)
                    }
                    else if(n==1){
                        ChatViewModel(application).setPrivate(id,true)
                    }
                    else{
//                        hiddenViewModel.setText("1")
                        SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,1)
                        openChatHome(1)
                    }
                    instance.dismiss()
                    //  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
                else{
                    val animShake = AnimationUtils.loadAnimation(this, R.anim.shake)
                    passCard.startAnimation(animShake)
                    startVibrationforPassBox()
                    Toast.makeText(this,"Wrong Passcode",Toast.LENGTH_SHORT).show()
                    pinEntry.setText(null)
                }
            }
        }

        cancel.setOnClickListener {
            instance.dismiss()
        }
    }

    private fun startVibrationforPassBox() {
        var vibrationEffect1 : VibrationEffect
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrationEffect1 = VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_DOUBLE_CLICK);
            vibrator.cancel();
            vibrator.vibrate(vibrationEffect1);
        }

    }

    fun loadContacts(applicat: Application){
        val contentResolver = applicat.contentResolver
        val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

 //       var contactMap:HashMap<String,String> = HashMap<String,String>()

        while (contacts?.moveToNext() == true) {
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phoneNumber=phoneNumber.replace("\\s|[(]|[)]|[-]".toRegex(), "")

            if(phoneNumber.length==13){
                phoneNumber=phoneNumber.replace("\\+91".toRegex(),"")
            }

            if (phoneNumber.length==10){
                ContactViewModel(applicat).inserContact(ContactEntity(name,phoneNumber,"",false,""))
//                contactMap.put(phoneNumber,name)
            }
        }
        contacts!!.close()


        val ref= FirebaseDatabase.getInstance().reference.child("Users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshot in snapshot.children){
                    val num=snapshot.child("number").value.toString()
                    val userid=snapshot.child("uid").value.toString()
                    val profile_url=snapshot.child("profile_photo").value.toString()

                    if(userid!=Constants.MY_USERID && ContactViewModel(applicat).isContactExist(num)/*contactMap.containsKey(num)*/){
                        val usercon = ContactViewModel(applicat).getContactByNum(num)

                        val consEntity = ContactEntity(usercon.name,usercon.number,"",true,userid)
                        ContactViewModel(applicat).inserContact(consEntity)

                        if(profile_url!=""){
                            ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,userid+".jpg").execute(profile_url)
//                            val bmp = ImageHandling.GetImageFromUrl().execute(profile_url).get()
//                            val selectedPtah = getRealPathFromURI(getImageUri(this@Dashboard,bmp!!))
                           // ImageCompression(this@Dashboard).execute(selectedPtah)
                            //ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,userid+".jpg").execute(profile_url)
                        }
                        if(ChatViewModel(applicat).isUserExist(userid)){
                            ChatViewModel(applicat).updateName(usercon.name,userid)
                        }
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getRealPathFromURI(contentURI: Uri): String {
        val cursor: Cursor? = this.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            return contentURI.getPath()!!
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            return cursor.getString(idx)
        }
    }


    inner class updateUnsavedImage():AsyncTask<Void,Void,Boolean>() {
        override fun onPostExecute(result: Boolean?) {
            chatAdapter.notifyDataSetChanged()
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            for(chat in chatList){
                if(chat.name==""){
                    val ref=FirebaseDatabase.getInstance().reference.child("Users").child(chat.id)
                    ref.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val image_url=snapshot.child("profile_photo").value.toString()
                            if(image_url!=""){
                                ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,chat.id+".jpg").execute(image_url)
//                                GetImageFromUrl(chat.id,application).execute(image_url)
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
                callScreen.putExtra("userid",id)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

            override fun videoCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUserVideo(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing_vdo::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("userid",id)
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
                callScreen.putExtra("userid",id)
                Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
                startActivity(callScreen)
            }

            override fun videoCall(name: String, id: String,image:String) {

                val sinchServiceInterface=getSinchServiceInterface()
                val callId=sinchServiceInterface!!.callUserVideo(id).callId
                val callScreen = Intent(this@Dashboard, Outgoing_vdo::class.java)
                callScreen.putExtra("name",name)
                callScreen.putExtra("CALL_ID", callId)
                callScreen.putExtra("userid",id)
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
        runAnimation(chatsRecyclerView,1)
        chatsRecyclerView.adapter=chatAdapter
        chatsRecyclerView.layoutAnimation=controller
        chatsRecyclerView.scheduleLayoutAnimation()

        if(i==0){
            story_box.visibility=View.VISIBLE
            story_text.visibility=View.VISIBLE
            hidden.visibility=View.VISIBLE

            chat_text.text="Chats"
            hidden_close.visibility=View.GONE

            navigationView.visibility=View.VISIBLE
            ChatViewModel(application).allChats.observe(this, Observer { list->
                list?.let {
                    chatList.clear()
                    for(j in list){
                        if(!j.hide){
                            chatList.add(j)
                        }
                    }
                    chatList.sortBy { it.time }
                    chatAdapter.updateList(chatList)
                }
            })
        }
        else{
            navigationView.visibility=View.GONE
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
                    chatList.sortBy { it.time }
                    chatAdapter.updateList(chatList)
                }
            })
        }

        updateUnsavedImage().execute()

//        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        runAnimation(storyRecyclerView,1)
//        val adapter2=StoryAdapter(this,storyList)
//        adapter2.notifyDataSetChanged()
//        storyRecyclerView.adapter=adapter2
//        storyRecyclerView.layoutAnimation=controller
//        storyRecyclerView.scheduleLayoutAnimation()
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
//            val intent = Intent(this,Passcode::class.java)
//            intent.putExtra("id",id)
            if(!hideChat.hide){
                showPasscodeBox(id,1)
            }
            else{
                showPasscodeBox(id,0)
            }
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
        }
        if (filteredlist.isEmpty()){
            Toast.makeText(applicationContext,"No Chats found", Toast.LENGTH_SHORT).show()
        }
        chatAdapter.updateList(filteredlist)
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
//                    ChatViewModel(application).updateName(c)
//                }
//            }
//
//            return true
//        }
//    }

//    suspend fun retrieveMessage(application:Application){
//        val firebaseUser=FirebaseAuth.getInstance().currentUser!!
//        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
//
//        ref.addValueEventListener(object : ValueEventListener {
//            @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for(snapshot in snapshot.children){
//
//                    val messageKey=snapshot.child("mid").value.toString()
//                    val user=snapshot.child("userid").value.toString()
//                    val sender=snapshot.child("sender").value.toString()
//                    var msg=snapshot.child("message").value.toString()
//                    val time=snapshot.child("time").value.toString()
//                    val type=snapshot.child("type").value.toString()
//                    val url=snapshot.child("url").value.toString()
//                    val received=snapshot.child("received").value as Boolean
//
//                    //time set
//                    var tm: Date = Date(time.toLong())
//
//                    val sdf = SimpleDateFormat("hh:mm a")
//                    val date= SimpleDateFormat("dd/MM/yy")
//
//                    var name:String=""
//                    var number:String=""
//                    var imagepath:String=""
//
//
//                    if(ContactViewModel(application).isUserExist(sender)){
//                        val contactEntity=ContactViewModel(application).getContact(sender)
//                        name=contactEntity.name
//                        number=contactEntity.number
//                        imagepath=contactEntity.image
//                        if(type=="image"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
//                        }
//                        if(type=="doc"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,0,sender))
//                        }
//                        if(type=="message"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
//                        }
//                    }
//                    else if(ChatViewModel(application).isUserExist(sender)){
//                        //get image and name from room db
//                        val chatEntity=ChatViewModel(application).getChat(sender)
//                        name=chatEntity.name
//                        number=chatEntity.number
//                        imagepath=chatEntity.image
//                        var unread=chatEntity.unread
//                        val hide=chatEntity.hide
//                        if(type=="image"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),hide,unread++,sender))
//                        }
//                        if(type=="doc"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),hide,unread++,sender))
//                        }
//                        if(type=="message"){
//                            ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg,sdf.format(tm),date.format(tm),hide,unread++,sender))
//                        }
//                    }
//                    else{
//                        //get number as a name from firebase
//                        //get image and sav it to local storage and internal path from firebase
//                        val refer=FirebaseDatabase.getInstance().reference.child("Users").child(sender)
//                        refer.addValueEventListener(object :ValueEventListener{
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                number=snapshot.child("number").value.toString()
//                                //check message type
//                                if(type=="image"){
//                                    ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Photo", sdf.format(tm),date.format(tm),false,0,sender))
//                                }
//                                else{
//                                    ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,msg, sdf.format(tm),date.format(tm),false,0,sender))
//                                }
//
//                                //get image of sender
//                                val image_url=snapshot.child("profile_photo").value.toString()
//                                if(image_url!=""){
//                                    GetImageFromUrl(sender,application).execute(image_url)
//                                    if(type=="image"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"","Photo", sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
//                                    if(type=="doc"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,imagepath,"Document",sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
//                                    if(type=="message"){
//                                        ChatViewModel(application).inserChat(ChatEntity(name,number,"",msg, sdf.format(tm),date.format(tm),false,1,sender))
//                                    }
//                                }
//                                //     ContactViewModel(application).inserContact(ContactEntity(name,number,"",sender))
//
//                            }
//                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
//                            }
//
//                        })
//                    }
//
//                    if(type=="image"){
//
//                        //    MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,"",sdf.format(tm),type,false,false,false))
//
//                        val photo =  GetImageFromUrl().execute(msg).get()
//
//                        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
//                            if (Environment.isExternalStorageManager()) {
//                                val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
//                                if(!directory.exists()){
//                                    directory.mkdirs()
//                                }
//                                msg=messageKey+".jpg"
//                                var fos: FileOutputStream =
//                                    FileOutputStream(File(directory, msg))
//                                try {
//                                    //      photo.compress(Bitmap.CompressFormat.JPEG, 25, fos)
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                } finally {
//                                    try {
//                                        fos.close()
//                                    } catch (e: IOException) {
//                                        e.printStackTrace()
//                                    }
//                                }
//                            } else {
//                                //request for the permission
//                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                                val uri = Uri.fromParts("package", packageName, null)
//                                intent.data = uri
//                                startActivity(intent)
//                            }
//                        }
//                        else{
//                            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
//                            if(directory.exists()){
//                                directory.mkdirs()
//                            }
//                            msg=messageKey+".jpg"
//                            var fos: FileOutputStream =
//                                FileOutputStream(File(directory,msg))
//                            try {
//                                photo.compress(Bitmap.CompressFormat.JPEG, 50, fos)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            } finally {
//                                try {
//                                    fos.close()
//                                } catch (e: IOException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//                    }
//
//                    if(!received){
//                        MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+sender,sender,msg,sdf.format(tm),date.format(tm),type,url,false,false,false))
//
//
//                        // sendNotification(sender,name,msg,imagepath,application).execute()
//                        val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid)
//                        refer.addListenerForSingleValueEvent(object :ValueEventListener{
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if(snapshot.child(messageKey).exists()){
//                                    FirebaseDatabase.getInstance().reference.child("Messages").child(firebaseUser.uid).child(messageKey).child("received").setValue(true)
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                TODO("Not yet implemented")
//                            }
//
//                        })
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
//    }

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


//
//    inner class saveToInternalStorage:AsyncTask<Void,Void,Boolean>{
//
//        var path:String?=null
//        var user:String?=null
//        var bitmapImage:Bitmap?=null
//        var appl:Application?=null
//
//        constructor(bitmapImage: Bitmap, user: String,appl: Application) : super() {
//            this.user=user
//            this.bitmapImage=bitmapImage
//            this.appl=appl
//        }
//
//        override fun onPostExecute(result: Boolean?) {
//            super.onPostExecute(result)
//            ContactViewModel(appl!!).updateImage(user!!,user+".jpg")
//        }
//
//        override fun doInBackground(vararg params: Void?): Boolean {
//            val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Contacts Images")
//            if(directory.exists()){
//                path=user+".jpg"
//                var fos: FileOutputStream = FileOutputStream(File(directory, path))
//                try {
//                    bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                } finally {
//                    try {
//                        fos.close()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
//            }
//            else{
//                directory.mkdirs()
//                if (directory.isDirectory) {
//                    path=user+".jpg"
//                    val fos = FileOutputStream(File(directory, path))
//                    try {
//                        bitmapImage!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    } finally {
//                        try {
//                            fos.close()
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//            return true
//        }
//
//    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            103 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do_SOme_Operation()
                    val executorService: ExecutorService = Executors.newFixedThreadPool(1)
                    val task1 = java.lang.Runnable {
                        try {
                            GlobalScope.launch {
                                loadContacts(application)
                            }
                        } catch (ex: InterruptedException) {
                            throw IllegalStateException(ex)
                        }
                    }
                    executorService.submit(task1)

                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }

    }


    inner class deleteMsgs(val application: Application,val id:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {
            val myuserId=SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")
            MessageViewModel(application).deleteMsgWithId(myuserId+"-"+id)

            val refer=FirebaseDatabase.getInstance().reference.child("Messages")
            refer.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(myuserId!!)){
                        val ref = FirebaseDatabase.getInstance().reference.child("Messages").child(myuserId)

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


//    inner class GetImageFromUrl(val userid: String,val appl:Application) : AsyncTask<String?, Void?, Bitmap>() {
//        var bmp:Bitmap?=null
//
//        override fun onPostExecute(result: Bitmap?) {
//            super.onPostExecute(result)
//            saveToInternalStorage(bmp!!,userid,appl).execute()
//        }
//
//        override fun doInBackground(vararg url: String?): Bitmap {
//            val stringUrl = url[0]
//            val options = BitmapFactory.Options()
//            options.inSampleSize = 1
//            while (options.inSampleSize <= 32) {
//                val inputStream = URL(stringUrl).openStream()
//                try {
//                    bmp= BitmapFactory.decodeStream(inputStream, null, options)
//                    inputStream.close()
//                    break
//                } catch (outOfMemoryError: OutOfMemoryError) {
//                }
//                options.inSampleSize++
//            }
//
//         return bmp!!
//        }
//    }

    override fun onResume() {
        super.onResume()
        try {
            val f = File(File(Environment.getExternalStorageDirectory(),Constants.PROFILE_PHOTO_LOCATION),SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")+".jpg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            profile.setImageBitmap(b)
//            Glide.with(this).load(f).diskCacheStrategy(
//                DiskCacheStrategy.RESOURCE)
//                .skipMemoryCache(true).into(profile)
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
            SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")!!
        ).setValue(token)
    }


}
