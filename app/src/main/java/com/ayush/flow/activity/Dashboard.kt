package com.ayush.flow.activity

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.*
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.Notification.MessagingService
import com.ayush.flow.Notification.Token
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.adapter.CallAdapter
import com.ayush.flow.adapter.CallHistoryAdapter
import com.ayush.flow.adapter.ChatAdapter
import com.ayush.flow.adapter.StoryAdapter
import com.ayush.flow.database.*
import com.ayush.flow.databinding.ActivityDashboardBinding
import com.ayush.flow.databinding.DeleteModalBottomsheetBinding
import com.ayush.flow.databinding.PassVerifyBinding
import com.ayush.flow.model.Chats
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.Exception


class Dashboard : BaseActivity(), SinchService.StartFailedListener {

    var chatList= arrayListOf<ChatEntity>()
    var callList= arrayListOf<CallEntity>()
    var storyList= arrayListOf<Chats>()

    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var storyAdapter: StoryAdapter
    lateinit var chatAdapter: ChatAdapter
    lateinit var callAdapter: CallAdapter

    val hashMap: HashMap<Int, NotificationCompat.MessagingStyle?> = HashMap()
    var controller:LayoutAnimationController?=null
    var previousMenuItem: MenuItem?=null
    lateinit var hiddenViewModel: HiddenViewModel
    lateinit var vibrator: Vibrator
    var callname=""
    var callId=""
    lateinit var binding: ActivityDashboardBinding

    override fun onServiceConnected() {
        sinchServiceInterface!!.setStartListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_dashboard)

        SharedPreferenceUtils.init(applicationContext)
        hiddenViewModel= ViewModelProvider(this).get(HiddenViewModel::class.java)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        loadContacts()

        GlobalScope.launch(Dispatchers.IO){
            checkStatus()
            UpdateToken()
        }


        binding.userImg.setOnClickListener{
            startActivity(Intent(this,UserProfile::class.java))
        }

        binding.contact.setOnClickListener {
            startActivity(Intent(this, Contact::class.java))
        }

        binding.startChat.setOnClickListener { binding.contact.performClick() }

        binding.bottomNavigation.setOnNavigationItemSelectedListener{
            if(previousMenuItem==null){
                previousMenuItem=binding.bottomNavigation.menu.findItem(R.id.chat)
            }
            when(it.itemId){
                R.id.chat ->{
                    if(previousMenuItem!=it){
                        binding.title.text="Messages"
                        binding.stories.text="Stories"
                        binding.storyTxt.text="Me"
                        binding.chats.text="Chats"

                        openChatHome(Constants.OPEN_CHAT_HOME)

                        it.isCheckable=true
                        it.isChecked=true
                        previousMenuItem=it
                    }
                }
                R.id.call ->{
                    if(previousMenuItem!=it){
                        binding.title.text="Calls"
                        binding.stories.text="Favorites"
                        binding.storyTxt.text="New"
                        binding.chats.text="Calls"


                        openCallHome()

                        it.isCheckable=true
                        it.isChecked=true
                        previousMenuItem=it
                    }
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
            binding.mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.moon))
        }
        else{
            binding.mode.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.sun))
        }

        binding.hidden.setOnClickListener {
            showPasscodeBox("",2)
        }

        binding.closeHidden.setOnClickListener {
            SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,0)
            openChatHome(Constants.OPEN_CHAT_HOME)
        }

        binding.mode.setOnClickListener {
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
            binding.mode.startAnimation(rotate)

            Handler().postDelayed({
                if(SharedPreferenceUtils.getBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,false)
                }
                else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.NIGHT_MODE,true)
                }
            },600)
        }

        GlobalScope.launch(Dispatchers.Main) { searchElement() }
    }

    private fun loadContacts() {
        if(ConnectionManager().checkconnectivity(this)){
            if(Permissions().checkContactpermission(this)){
                startService(Intent(this, LoadContacts::class.java))
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.contact_permission,this.resources.getString(R.string.contact_permission),this,"contact")
            }
        }

    }

    fun checkStatus(){
        val connectionReference= FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID)
//        val lastConnected= FirebaseDatabase.getInstance().reference.child("lastConnected")
        val infoConnected= FirebaseDatabase.getInstance().getReference(".info/connected")

        infoConnected.addValueEventListener(object : ValueEventListener {
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
    }

    private fun showPasscodeBox(id:String,n:Int) {
        var passCodeBinding:PassVerifyBinding = PassVerifyBinding.inflate(layoutInflater)
        val passBoxBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        passBoxBuilder.setView(passCodeBinding.root)
        passBoxBuilder.setCancelable(false)
        val instance = passBoxBuilder.show()

        var passcode = SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.HIDE_PASS,"")
        if(passcode==""){
            passCodeBinding.title.text="Set Passcode"
        }

        passCodeBinding.txtPinEntry.requestFocus()

        passCodeBinding.txtPinEntry.postDelayed(Runnable { // TODO Auto-generated method stub
            val keyboard = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(passCodeBinding.txtPinEntry, 0)
        }, 100)

        passCodeBinding.txtPinEntry.setOnPinEnteredListener { str ->

            if(passcode==""){
                passcode=str.toString()
                passCodeBinding.title.text="Confirm Password"
                passCodeBinding.txtPinEntry.setText(null)
            }
            else{
                if(passCodeBinding.title.text=="Confirm Password"){
                    if(passcode==str.toString()) {
                        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.HIDE_PASS,str.toString())
                        SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,1)
                        openChatHome(Constants.OPEN_HIDE_HOME)
                        instance.dismiss()
                    }
                    else{
                        val animShake = AnimationUtils.loadAnimation(this, R.anim.shake)
                        passCodeBinding.passCard.startAnimation(animShake)
                        startVibrationforPassBox()
                        Toast.makeText(this,"Wrong Passcode",Toast.LENGTH_SHORT).show()
                        passCodeBinding.txtPinEntry.setText(null)
                    }

                }
                else if(passcode==str.toString()){
                    if(n==Constants.OPEN_CHAT_HOME){
                        ChatViewModel(application).setPrivate(id,false)
                    }
                    else if(n==Constants.OPEN_HIDE_HOME){
                        ChatViewModel(application).setPrivate(id,true)
                    }
                    else{
                        SharedPreferenceUtils.setIntPreference(SharedPreferenceUtils.IS_PRIVATE,1)
                        openChatHome(Constants.OPEN_HIDE_HOME)
                    }
                    instance.dismiss()
                }
                else{
                    val animShake = AnimationUtils.loadAnimation(this, R.anim.shake)
                    passCodeBinding.passCard.startAnimation(animShake)
                    startVibrationforPassBox()
                    Toast.makeText(this,"Wrong Passcode",Toast.LENGTH_SHORT).show()
                    passCodeBinding.txtPinEntry.setText(null)
                }
            }
        }

        passCodeBinding.cancel.setOnClickListener {
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
        val itemselect = binding.bottomNavigation.menu.findItem(R.id.call)
        itemselect.isChecked=true
        itemselect.isCheckable=true
        previousMenuItem=itemselect


        callAdapter= CallAdapter(this@Dashboard,object :ChatAdapter.OnAdapterItemClickListener{
            override fun audioCall(name: String, id: String) {

                callname=name
                callId=id
                if(Permissions().checkMicpermission(this@Dashboard)){
                    audioCalling(name,id)
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.mic_permission,getString(R.string.mic_permission),this@Dashboard,"mic")
                }
            }

            override fun videoCall(name: String, id: String) {

                callname = name
                callId = id
                if(Permissions().checkCamAndMicPermission(this@Dashboard)){
                    videoCalling(name,id)
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.camera_mic_permission,getString(R.string.mic_and_cam_permission),this@Dashboard,"micandcam")
                }
            }

            override fun deleteChat(id: String,name: String) {
                TODO("Not yet implemented")
            }

            override fun hideChat(id: String,name: String) {
                TODO("Not yet implemented")
            }

            override fun callHistoryBox(id: String,name: String) {
                val historyBoxView = LayoutInflater.from(this@Dashboard).inflate(R.layout.history_box, null,false)
                val historyBoxBuilder = AlertDialog.Builder(this@Dashboard,R.style.CustomAlertDialog)
                historyBoxBuilder.setView(historyBoxView)
                historyBoxBuilder.setCancelable(true)

                val username:TextView = historyBoxView.findViewById(R.id.user_name)
                val userimage:CircleImageView = historyBoxView.findViewById(R.id.user_img)
                val historyRecycler:RecyclerView = historyBoxView.findViewById(R.id.history_recycler)
                val audioCall:ImageView = historyBoxView.findViewById(R.id.audiocall_btn)
                val videoCall:ImageView = historyBoxView.findViewById(R.id.vdocall_btn)

                var layoutManager = LinearLayoutManager(this@Dashboard)
                val historyAdapter = CallHistoryAdapter(this@Dashboard)

                username.text=name
                val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),id+".jpg")
                Glide.with(this@Dashboard).load(f).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.user).into(userimage)

                (layoutManager as LinearLayoutManager).reverseLayout = true
                historyRecycler.setHasFixedSize(true)
                historyRecycler.layoutManager=layoutManager
                historyRecycler.adapter=historyAdapter
                CallHistoryViewModel(this@Dashboard).getCallHistory(id).observe(this@Dashboard){
                    if(it!=null){
                        historyAdapter.updateList(it)
                        historyRecycler.smoothScrollToPosition(it.size)
                    }
                }

                audioCall.setOnClickListener {
                    audioCalling(name,id)
                }

                videoCall.setOnClickListener {
                    videoCalling(name,id)
                }

                historyBoxBuilder.show()

            }

        })
        layoutManager=LinearLayoutManager(this)
        binding.chatsrecycler.layoutManager=layoutManager
        runAnimation(binding.chatsrecycler,0)
        binding.chatsrecycler.adapter=callAdapter
        binding.chatsrecycler.layoutAnimation=controller
        binding.chatsrecycler.scheduleLayoutAnimation()

        CallViewModel(application).allCalls.observe(this, Observer { list->
            list?.let {
                callList.clear()
                callList.addAll(list)

                callList.sortByDescending{ it.time }
                if(callList.isEmpty()){
                    binding.emptyLayout.visibility=View.VISIBLE
                    binding.emptyImg.setImageResource(R.drawable.no_call)
                    binding.startChat.visibility=View.GONE
                }
                else{
                    binding.emptyLayout.visibility=View.GONE
                }

                callAdapter.updateList(callList)
            }
        })

        binding.storyrecycler.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        runAnimation(binding.storyrecycler,0)
        val adapter2=StoryAdapter(this,storyList)
        adapter2.notifyDataSetChanged()
        binding.storyrecycler.adapter=adapter2
        binding.storyrecycler.layoutAnimation=controller
        binding.storyrecycler.scheduleLayoutAnimation()
    }

    private fun openChatHome(i: Int) {

        val itemselect = binding.bottomNavigation.menu.findItem(R.id.chat)
        itemselect.isChecked=true
        itemselect.isCheckable=true
        previousMenuItem=itemselect

        chatAdapter= ChatAdapter(this@Dashboard,object :ChatAdapter.OnAdapterItemClickListener{

            override fun audioCall(name: String, id: String) {
                callname=name
                callId=id
                if(Permissions().checkMicpermission(this@Dashboard)){
                    audioCalling(name,id)
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.mic_permission,getString(R.string.mic_permission),this@Dashboard,"mic")
                }
            }

            override fun videoCall(name: String, id: String) {
                callname = name
                callId = id
                if(Permissions().checkCamAndMicPermission(this@Dashboard)){
                    videoCalling(name,id)
                }
                else{
                    Permissions().openPermissionBottomSheet(R.drawable.camera_mic_permission,getString(R.string.mic_and_cam_permission),this@Dashboard,"micandcam")
                }
            }

            override fun deleteChat(id: String,name:String) {
                showDeleteBottomSheetDialog(id,name)
            }

            override fun hideChat(id: String,name: String) {
                showHideBottomSheetDialog(id,name)
            }

            override fun callHistoryBox(id: String,name: String) {

            }

        })

        layoutManager=LinearLayoutManager(this)
        binding.chatsrecycler.layoutManager=layoutManager
        runAnimation(binding.chatsrecycler,1)
        binding.chatsrecycler.adapter=chatAdapter
        binding.chatsrecycler.layoutAnimation=controller
        binding.chatsrecycler.scheduleLayoutAnimation()

        if(i==0){
//            story_box.visibility=View.VISIBLE
//            story_text.visibility=View.VISIBLE
            binding.hidden.visibility=View.VISIBLE

            binding.chats.text="Chats"
            binding.closeHidden.visibility=View.GONE

            binding.bottomNavigation.visibility=View.VISIBLE
            ChatViewModel(application).allChats.observe(this, Observer { list->
                list?.let {
                    chatList.clear()
                    for(j in list){
                        if(!j.hide){
                            chatList.add(j)
                        }
                    }
                    chatList.sortByDescending { it.time }
                    if(chatList.isEmpty()){
                        binding.emptyLayout.visibility=View.VISIBLE
                        binding.emptyImg.setImageResource(R.drawable.start_chat)
                        binding.startChat.visibility=View.VISIBLE
                        binding.startChat.isClickable=true
                    }
                    else{
                        binding.emptyLayout.visibility=View.GONE
                    }
                    chatAdapter.updateList(chatList)
                }
            })
        }
        else{
            binding.bottomNavigation.visibility=View.GONE
            binding.storyBox.visibility=View.GONE
            binding.storyTxt.visibility=View.GONE
            binding.hidden.visibility=View.GONE

            binding.chats.text="Private Chats"
            binding.closeHidden.visibility=View.VISIBLE

            ChatViewModel(application).allChats.observe(this, Observer { list->
                list?.let {
                    chatList.clear()
                    for(j in list){
                        if(j.hide){
                            chatList.add(j)
                        }
                    }
                    chatList.sortBy { it.time }
                    if(chatList.isEmpty()){
                        binding.emptyLayout.visibility=View.VISIBLE
                        binding.emptyImg.setImageResource(R.drawable.no_hidden_chat)
                        binding.startChat.visibility=View.GONE
                        binding.startChat.isClickable=false
                    }
                    else{
                        binding.emptyLayout.visibility=View.GONE
                    }
                    chatAdapter.updateList(chatList)
                }
            })
        }

        try {
            updateUnsavedImage().execute()
        }catch (e:Exception){

        }

//        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        runAnimation(storyRecyclerView,1)
//        val adapter2=StoryAdapter(this,storyList)
//        adapter2.notifyDataSetChanged()
//        storyRecyclerView.adapter=adapter2
//        storyRecyclerView.layoutAnimation=controller
//        storyRecyclerView.scheduleLayoutAnimation()
    }

    private fun videoCalling(name: String, id: String) {
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUserVideo(id).callId

        Constants.isCurrentUser=true
        CallViewModel(application).inserCall(CallEntity(name,"video","outgoing",System.currentTimeMillis(),0,id))
        CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(name,"video","outgoing",System.currentTimeMillis(),0,id,callId))

        val callScreen = Intent(this@Dashboard, Outgoing_vdo::class.java)
        callScreen.putExtra("name",name)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("userid",id)
        Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
        startActivity(callScreen)
    }

    private fun audioCalling(name: String, id: String) {
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUser(id).callId

        Constants.isCurrentUser=true
        CallViewModel(application).inserCall(CallEntity(name,"audio","outgoing",System.currentTimeMillis(),0,id))
        CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(name,"audio","outgoing",System.currentTimeMillis(),0,id,callId))

        val callScreen = Intent(this@Dashboard, Outgoing::class.java)
        callScreen.putExtra("name",name)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("userid",id)
        Message().sendNotification(id, FirebaseAuth.getInstance().currentUser!!.uid, "","", 1)
        startActivity(callScreen)
    }

    private fun showHideBottomSheetDialog(id: String,name:String) {

        val deleteDialogBinding:DeleteModalBottomsheetBinding = DeleteModalBottomsheetBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(deleteDialogBinding.root)

        val hideChat =  ChatViewModel(application).getChat(id)

        if(!hideChat.hide){
            deleteDialogBinding.textView.text="Hide "+name+" Chats"
            deleteDialogBinding.btnDelete.text="Hide"
            deleteDialogBinding.text2.text="This will Hide your selected chat to private Section"
        }
        else{
            deleteDialogBinding.textView.text="Unhide "+name+" Chats"
            deleteDialogBinding.btnDelete.text="Unhide"
            deleteDialogBinding.text2.text="This will Unhide your selected chat to private Section"
        }


        deleteDialogBinding.btnDelete.setOnClickListener {
            if(!hideChat.hide){
                showPasscodeBox(id,1)
            }
            else{
                showPasscodeBox(id,0)
            }
            bottomSheetDialog.dismiss()
        }
        deleteDialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun showDeleteBottomSheetDialog(id: String, name: String) {
        val deleteDialogBinding:DeleteModalBottomsheetBinding = DeleteModalBottomsheetBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this,R.style.AppBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(deleteDialogBinding.root)

        deleteDialogBinding.textView.text="Delete Chats with "+name
        deleteDialogBinding.text2.text="This will delete your selected chat including Medias"

        deleteDialogBinding.btnDelete.setOnClickListener {
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
        deleteDialogBinding.btnCancel.setOnClickListener {
           bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun searchElement(){
        binding.chatSearchview.queryHint="Search Your Friends..."
        binding.chatSearchview.setIconifiedByDefault(false)

        val theTextArea = binding.chatSearchview.findViewById<View>(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
        theTextArea.isCursorVisible=false

        binding.chatSearchview.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.chatSearchview.clearFocus()
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(Intent(this, LoadContacts::class.java))
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            104->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audioCalling(callname,callId)
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            105->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    videoCalling(callname,callId)
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

    override fun onResume() {
        super.onResume()
        if(binding.bottomNavigation.selectedItemId==R.id.chat){
            openChatHome(SharedPreferenceUtils.getIntPreference(SharedPreferenceUtils.IS_PRIVATE,0))
        }
        else{
            openCallHome()
        }
        try {
            val f = File(File(Environment.getExternalStorageDirectory(),Constants.PROFILE_PHOTO_LOCATION),SharedPreferenceUtils.getStringPreference(SharedPreferenceUtils.MY_USERID,"")+".jpg")
            if(f.exists()){
                val b = BitmapFactory.decodeStream(FileInputStream(f))
                binding.userImg.setImageBitmap(b)
            }
            else{
                binding.userImg.setImageResource(R.drawable.user)
            }
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
