package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.Notification.*
import com.ayush.flow.R
import com.ayush.flow.Services.*
import com.ayush.flow.Services.Constants
import com.ayush.flow.adapter.ForwardAdapter
import com.ayush.flow.adapter.ForwardToAdapter
import com.ayush.flow.adapter.MessageAdapter
import com.ayush.flow.adapter.SelectedImgAdapter
import com.ayush.flow.database.*
import com.ayush.flow.databinding.ActivityMessageBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class Message : BaseActivity() {
    lateinit var layoutManager: RecyclerView.LayoutManager
    var handler = Handler()
    var runnable: Runnable? = null
    var delay = 100
    lateinit var viewModel: MessageViewModel
    var userid:String=""
    var user_image:String=""
    var number:String=""
    var photo:Bitmap?=null
    var rotatedBitmap:Bitmap?=null
    lateinit var firebaseUser: FirebaseUser
    var adapter: MessageAdapter? = null
    var apiService: APIService?=null
    val allMsg = arrayListOf<MessageEntity>()
    val selectedMsg = arrayListOf<MessageEntity>()
    private var imageuri: Uri?=null
    lateinit var mainViewModel: MainViewModel
    lateinit var fwdViewModel: ForwardViewModel
    private lateinit var photofile: File
    lateinit var sharedPreferences:SharedPreferences
    lateinit var selectedPath:String
    lateinit var gallImagesPath:ArrayList<String>
    lateinit var allSelectedUri:ArrayList<Uri>
    lateinit var selectedImgAdapter: SelectedImgAdapter
    lateinit var selectedLayoutManager: RecyclerView.LayoutManager

    lateinit var binding:ActivityMessageBinding
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedPath=""
        gallImagesPath= arrayListOf<String>()
        allSelectedUri= arrayListOf<Uri>()
        apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)
       fwdViewModel=ViewModelProviders.of(this).get(ForwardViewModel::class.java)
       mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        if(intent.getStringExtra("name")==""){
            binding.userName.text=intent.getStringExtra("number")!!
        }
        else{
            binding.userName.text=intent.getStringExtra("name")!!
        }
        userid= intent.getStringExtra("userid")!!
        user_image=intent.getStringExtra("image")!!

       setAllMsgSeen()

       GlobalScope.launch{
           checkStatus()
           setOnlineStatus()
       }


       binding.close.setOnClickListener {
           binding.selected.visibility=View.GONE
           binding.sendBox.visibility=View.VISIBLE
           selectedMsg.clear()
           adapter!!.notifyDataSetChanged()
       }

       binding.delete.setOnClickListener {
           AlertDialog.Builder(this)
               .setTitle("Are you sure want to Delete")
               .setCancelable(false)
               .setPositiveButton("Yes") {
                       dialog: DialogInterface, _: Int ->
                   dialog.dismiss()
                   deleteMsg().execute()
               }
               .setNegativeButton("No") {
                       dialog: DialogInterface, _: Int ->
                   dialog.dismiss()
               }
               .show()
           binding.selected.visibility=View.GONE
           binding.sendBox.visibility=View.VISIBLE

       }


       binding.forward.setOnClickListener {
           openForwardBottomSheet(this)
       }

       binding.userPic.setOnClickListener {
           openProfileBottomSheet(this,binding.userName.text.toString(),userid,true)
       }

       binding.details.setOnClickListener {
           openProfileBottomSheet(this,binding.userName.text.toString(),userid,true)
       }

       binding.userProfile.setOnClickListener {
           openProfileBottomSheet(this,binding.userName.text.toString(),userid,true)
       }


        layoutManager= LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).stackFromEnd=true
        binding.messageRecycler.layoutManager=layoutManager
        adapter = MessageAdapter(this,selectedMsg,object:MessageAdapter.OnAdapterItemClickListener{
            override fun updateCount() {
                mainViewModel.setText(selectedMsg.size.toString())
            }

            override fun updateSeen(mid: String) {
//                MessageViewModel(application).isMsgSeen(true,mid)
            }

        })
        binding.messageRecycler.adapter=adapter
//       val n = allMsg.size
//       recyclerView.smoothScrollToPosition(intent.getIntExtra("unread",0))

       mainViewModel.getText().observe(this,Observer{it->
           if(it=="0"){
               binding.selected.visibility=View.GONE
               binding.sendBox.visibility=View.VISIBLE
           }
           else{
               binding.selectText.text=it+" Selected"
               binding.selected.visibility=View.VISIBLE
               binding.sendBox.visibility=View.INVISIBLE
           }
       })


        MessageViewModel(application).allMessages(firebaseUser.uid+"-"+userid).observe(this, Observer {list->
            list?.let {
                allMsg.clear()
                allMsg.addAll(list)
                adapter!!.updateList(list as ArrayList<MessageEntity>)
                binding.messageRecycler.smoothScrollToPosition(list.size)
            }
        })

        binding.more.setOnClickListener {
            if(binding.moreCard.visibility== View.GONE){
                val rotate = RotateAnimation(
                    0F,
                    360F,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotate.duration = 400
                rotate.fillAfter=true
                rotate.interpolator = LinearInterpolator()
                binding.more.startAnimation(rotate)


                binding.moreCard.visibility=View.VISIBLE
                val animFadein: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.slide_up
                )
                binding.moreCard.startAnimation(animFadein)
            }
            else{
                val rotate = RotateAnimation(
                    0F,
                    180F,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                rotate.duration = 300
                rotate.fillAfter=true
                rotate.interpolator = LinearInterpolator()
                binding.more.startAnimation(rotate)


                binding.moreCard.visibility=View.GONE
            }
        }

        binding.send.setOnClickListener {
            val msg=binding.sendText.text.toString()
            if(msg!=""){
                val ref=FirebaseDatabase.getInstance().reference
                val messageKey=ref.push().key
                MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,
                    Constants.MY_USERID+"-"+userid,Constants.MY_USERID,msg,System.currentTimeMillis(),"message","","","","","sending..."))
                sendMessageToUser(msg,messageKey,userid,intent.getStringExtra("name")!!, intent.getStringExtra("number")!!,user_image).execute()
                binding.sendText.setText("")
            }
        }


       binding.sendCam.setOnClickListener {

           if(Permissions().checkCamerapermission(this)){
               openCamera()
               binding.moreCard.visibility=View.GONE

           }
           else{
               Permissions().openPermissionBottomSheet(R.drawable.camera_permission,
                   this.resources.getString(R.string.camera_permission),this,"camera")
           }


       }

       binding.sendGall.setOnClickListener {
           if(Permissions().checkWritepermission(this)){
               openGallery()
               binding.moreCard.visibility=View.GONE
           }
           else{
               Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
           }

       }

       binding.sendDoc.setOnClickListener {
           if(Permissions().checkWritepermission(this)){
               openDocuments()
               binding.moreCard.visibility=View.GONE
           }
           else{
               Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
           }

       }


       binding.sendimgBtn.setOnClickListener {
           if(gallImagesPath.size!=0){
               for(imgpath in gallImagesPath){
                   ImageCompression(this,"message",intent,application).execute(imgpath)
               }
           }
           else if(selectedPath!=null && selectedPath!=""){
               ImageCompression(this,"message",intent,application).execute(selectedPath)
           }
           binding.sendSelectImg.setImageResource(android.R.color.transparent)
           binding.sendImgNow.visibility=View.GONE
           gallImagesPath.clear()
           allSelectedUri.clear()
       }

       binding.backNow.setOnClickListener {
           binding.sendSelectImg.setImageResource(android.R.color.transparent)
           photo=null
           binding.sendImgNow.visibility=View.GONE
           gallImagesPath.clear()
           allSelectedUri.clear()
       }

//        Dashboard().checkStatus()
        checkSeen().execute()
        searchElement()
       // deleteMessage()

    }

    private fun setAllMsgSeen() {
        MessageViewModel(application).setAllMsgSeen(userid)
    }

    fun checkStatus(){
        var firebaseUser=FirebaseAuth.getInstance().currentUser!!
        val connectionReference= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        val lastConnected= FirebaseDatabase.getInstance().reference.child("lastConnected")
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


    private fun openDocuments() {
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "application/pdf"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        startActivityForResult(Intent.createChooser(intent, "Open With"), 113)
        startActivityForResult(galleryIntent, 113)
    }

    fun openForwardBottomSheet(context: Context) {
        val selectedChat = arrayListOf<ChatEntity>()
        var chatList= arrayListOf<ChatEntity>()
        lateinit var fwdtoAdapter:ForwardToAdapter
        lateinit var fwdAdapter:ForwardAdapter

        val bottomSheetDialog = BottomSheetDialog(context,R.style.RoundedBottomSheetTheme)
        bottomSheetDialog.setContentView(R.layout.forward_modal_bottomsheet)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()

        val close = bottomSheetDialog.findViewById<ImageView>(R.id.close_fwd)
        val fwdtorecyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.forwarded_to)
        val fwdrecyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.forwardrecycler)
        val selectAll = bottomSheetDialog.findViewById<CheckBox>(R.id.selectAll)
        val fwd_btn = bottomSheetDialog.findViewById<ImageView>(R.id.fwd_btn)
        val searchfwd = bottomSheetDialog.findViewById<SearchView>(R.id.searchview_fwd)

//        val toolbar:CardView=findViewById(R.id.toolbar)
//        toolbar.cardElevation=0F

//        send_box.visibility=View.GONE
//        dim.visibility=View.VISIBLE
//        forward_card.visibility=View.VISIBLE
//        val animFadein: Animation = AnimationUtils.loadAnimation(
//            applicationContext,
//            R.anim.slide_up
//        )
//        forward_card.startAnimation(animFadein)

        //searchfwdElement()

        searchfwd!!.queryHint="Search chats..."
//        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon)
//        searchIcon.setColorFilter(Color.WHITE)
        val theTextArea = searchfwd.findViewById(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
//        theTextArea.setTextColor(Color.WHITE)
        theTextArea.isCursorVisible=false

        searchfwd.setOnSearchClickListener {
            selectAll!!.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.BELOW,R.id.forwarded_to)
            searchfwd.layoutParams=params
        }

        searchfwd.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                selectAll!!.visibility=View.VISIBLE
                val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                params.addRule(RelativeLayout.BELOW,R.id.forwarded_to)
                searchfwd.layoutParams=params
                return false
            }

        })

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchfwd.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchfwd.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredlist:ArrayList<ChatEntity> = ArrayList()

                for(item in chatList){
                    if(item.name.toLowerCase().contains(newText!!.toLowerCase())||item.number.contains(newText)){
                        //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                        filteredlist.add(item)
                    }
                }
                if (filteredlist.isEmpty()){
                    Toast.makeText(applicationContext,"No Data found", Toast.LENGTH_SHORT).show()
                    fwdAdapter.updateList(filteredlist,selectedChat)
                }
                else{
                    fwdAdapter.updateList(filteredlist,selectedChat)
                }
                return true
            }

        })

        close?.setOnClickListener {
            bottomSheetDialog.dismiss()
            selectedMsg.clear()
            selectedChat.clear()
            searchfwd!!.isIconified=true

            adapter!!.notifyDataSetChanged()
        }

        selectAll!!.setOnClickListener {
            if(selectAll.isChecked){
                selectedChat.clear()
                selectedChat.addAll(chatList)
                fwdtoAdapter.updateList(selectedChat)
                fwdAdapter.updateList(chatList,selectedChat)
            }
            if(!selectAll.isChecked){
                selectedChat.clear()
                fwdtoAdapter.updateList(selectedChat)
                fwdAdapter.updateList(chatList,selectedChat)
            }
        }

        fwd_btn!!.setOnClickListener {
            if(selectedChat.isEmpty()){
                Toast.makeText(this,"Please select user",Toast.LENGTH_SHORT).show()
            }
            else{
                forwardMsg(selectedChat,selectedMsg).execute()
                bottomSheetDialog.dismiss()
                searchfwd!!.isIconified=true
            }
        }

        layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        fwdtorecyclerView?.layoutManager=layoutManager
        fwdtoAdapter = ForwardToAdapter(this@Message,object :ForwardAdapter.OnAdapterItemClickListener{
            override fun addChat(chatEntity: ChatEntity) {
                TODO("Not yet implemented")
            }

            override fun delChat(chatEntity: ChatEntity) {
                selectedChat.remove(chatEntity)
                fwdtoAdapter.updateList(selectedChat)
                fwdAdapter.updateList(chatList,selectedChat)

                if(selectedChat.size<chatList.size){
                    selectAll.isChecked=false
                }
            }

        })
        fwdtorecyclerView!!.adapter=fwdtoAdapter

        layoutManager= LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).reverseLayout=true
        fwdrecyclerView!!.layoutManager=layoutManager
        fwdAdapter = ForwardAdapter(this@Message,object :ForwardAdapter.OnAdapterItemClickListener{
            override fun addChat(chatEntity: ChatEntity) {
                if(!selectedChat.contains(chatEntity)){
                    selectedChat.add(chatEntity)
                }
                fwdtoAdapter.updateList(selectedChat)
                fwdAdapter.updateSelected(selectedChat)

                if(selectedChat.size==chatList.size){
                    selectAll.isChecked=true
                }
            }

            override fun delChat(chatEntity: ChatEntity) {
                if(selectedChat.contains(chatEntity)){
                    selectedChat.remove(chatEntity)
                }
                fwdtoAdapter.updateList(selectedChat)
                fwdAdapter.updateSelected(selectedChat)

                if(selectedChat.size<chatList.size){
                    selectAll.isChecked=false
                }
            }

        })
        fwdrecyclerView!!.adapter=fwdAdapter

        var c=0

        ChatViewModel(application).allChats.observe(this, Observer { list->
            list?.let {
                if (c==0) {
                    chatList.clear()
                    chatList.addAll(list)
                    fwdAdapter.updateList(chatList, selectedChat)
                    c = 1;
                }
            }
        })

        binding.sendBox.visibility=View.VISIBLE
        binding.selected.visibility=View.GONE

    }

    fun openProfileBottomSheet(context: Context,username:String,user_id:String,isChat:Boolean) {
        val bottomSheetDialog = BottomSheetDialog(context,R.style.AppBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.profile_modal_bottomsheet)


        val profile_image = bottomSheetDialog.findViewById<CircleImageView>(R.id.profile_image)
        val user_name = bottomSheetDialog.findViewById<TextView>(R.id.user_name)
        val user_about = bottomSheetDialog.findViewById<TextView>(R.id.user_about)


        val chat = bottomSheetDialog.findViewById<CircleImageView>(R.id.chatview)
        val audiocall=bottomSheetDialog.findViewById<CircleImageView>(R.id.call)
        val videocall=bottomSheetDialog.findViewById<CircleImageView>(R.id.video_call)

        val clear_chat =bottomSheetDialog.findViewById<TextView>(R.id.clear_chat)
        val block=bottomSheetDialog.findViewById<TextView>(R.id.user_block)
        val report=bottomSheetDialog.findViewById<TextView>(R.id.user_report)

        val f = File(File(Environment.getExternalStorageDirectory(), Constants.ALL_PHOTO_LOCATION),userid+".jpg")
        Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
            DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(profile_image!!)

        user_name!!.text=username

        profile_image.setOnClickListener {
            val intent = Intent(context, SelectedImage::class.java)
            intent.putExtra("type","view")
            intent.putExtra("userid",user_id)
            context.startActivity(intent)
        }

        chat!!.setOnClickListener {
            if(isChat){
                bottomSheetDialog.dismiss()
            }
            else{
                val intent = Intent(context, Message::class.java)
                intent.putExtra("name", username)
                intent.putExtra("number",username)
                intent.putExtra("userid", user_id)
                context.startActivity(intent)
            }
        }

        audiocall!!.setOnClickListener {
            if(Permissions().checkMicpermission(context)){
                audioCalling(username,user_id)
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.mic_permission,context.resources.getString(R.string.mic_permission),context,"mic")
            }
        }

        videocall!!.setOnClickListener {
            if(Permissions().checkCamAndMicPermission(context)){
                videoCalling(username,user_id)
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.camera_mic_permission,context.resources.getString(R.string.mic_and_cam_permission),context,"micandcam")
            }

        }

        clear_chat?.setOnClickListener {
            bottomSheetDialog.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Are you sure want to delete")
                .setCancelable(false)
                .setPositiveButton("Delete") {
                        dialog: DialogInterface, _: Int ->
                    Dashboard().deleteMsgs(application,user_id).execute()
                    ChatViewModel(application).setLastMsg("",user_id);
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }

        block?.setOnClickListener {
            bottomSheetDialog.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Are you sure want to block")
                .setCancelable(false)
                .setPositiveButton("Block") {
                        dialog: DialogInterface, _: Int ->

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") {
                        dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
        }

        bottomSheetDialog.show()
    }

    private fun videoCalling(username: String, userId: String) {
        val userName: String = username
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUserVideo(userId).callId

        Constants.isCurrentUser=true
        CallViewModel(application).inserCall(CallEntity(userName,"video","outgoing",System.currentTimeMillis()/1000,0,userId))
        CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(userName,"video","outgoing",System.currentTimeMillis()/1000,0,userid,callId))

        val callScreen = Intent(this, Outgoing_vdo::class.java)
        callScreen.putExtra("name",userName)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("userid",userId)
        sendNotification(userId, firebaseUser.uid, "","", 1)
        startActivity(callScreen)
    }

    private fun audioCalling(username: String, userId: String) {
        val userName: String = username
        //
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUser(userId).callId

        Constants.isCurrentUser=true
        CallViewModel(application).inserCall(CallEntity(userName,"audio","outgoing",System.currentTimeMillis()/1000,0,userId))
        CallHistoryViewModel(application).insertCallHistory(CallHistoryEntity(userName,"audio","outgoing",System.currentTimeMillis()/1000,0,userid,callId))

        val callScreen = Intent(this, Outgoing::class.java)
        callScreen.putExtra("name",userName)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("userid",userId)
        sendNotification(userId, FirebaseAuth.getInstance().currentUser!!.uid, "", "", 1)
        startActivity(callScreen)
    }

    fun openGallery() {
//        var intent= Intent(Intent.ACTION_GET_CONTENT)
//        intent.type="image/*"
//        startActivityForResult(intent,112)
        val intent = Intent()
        intent.type = "image/*"
        // allowing multiple image to be selected
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 111)
    }

    fun openCamera(){
        photofile = getphotofile("chat_photo")
        selectedPath=photofile.absolutePath
        imageuri = let { it1 -> FileProvider.getUriForFile(it1, "com.ayush.flow.fileprovider", photofile) }
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri)
        startActivityForResult(intent,110)
    }

    fun getphotofile(fileName: String):File{
        val storage= getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }


    private fun convertImageViewToBitmap(v: ImageView): Bitmap? {
        return (v.drawable as BitmapDrawable).bitmap
    }

    override fun onBackPressed() {

        if(binding.selected.visibility===View.VISIBLE){
            binding.selected.visibility=View.GONE
            binding.sendBox.visibility=View.VISIBLE
            selectedMsg.clear()
            adapter!!.notifyDataSetChanged()
        }
        else if(binding.sendImgNow.visibility==View.VISIBLE){
            binding.sendImgNow.visibility=View.GONE
            gallImagesPath.clear()
            allSelectedUri.clear()
            binding.sendSelectImg.setImageResource(android.R.color.transparent)
        }
        else{
            setAllMsgSeen()
            super.onBackPressed()
        }
//        finishAffinity()
    }

    override fun onResume() {

        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),userid+".jpg")
        Glide.with(this).load(f).placeholder(R.drawable.user).diskCacheStrategy(
            DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(binding.userPic)

        RetrieveMessage(application).execute()
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
//            ChatViewModel(application).setUnread(0,userid)
            val notId: Int = Regex("[\\D]").replace(userid, "").toInt()
            NotificationManagerCompat.from(applicationContext).cancel(notId)
            MessagingService.messsageHashmap.remove(notId)
            if(MessagingService.messsageHashmap.size==0){
                NotificationManagerCompat.from(applicationContext).cancel(0)
            }
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        handler.removeCallbacks(runnable!!) //stop handler when activity not visible super.onPause();
        super.onPause()
    }


    fun searchElement() {

        binding.searchview.queryHint="Search messages..."
        val theTextArea = binding.searchview.findViewById(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete

        theTextArea.isCursorVisible=false

        binding.searchview.setOnSearchClickListener {
            binding.back.visibility= View.GONE
            binding.userName.visibility=View.GONE
            binding.status.visibility=View.GONE
            binding.userProfile.visibility=View.GONE
            binding.userPic.visibility=View.GONE

            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            binding.searchview.layoutParams=params
        }

        binding.searchview.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                binding.back.visibility= View.VISIBLE
                binding.userName.visibility=View.VISIBLE
                binding.userProfile.visibility=View.VISIBLE
                binding.userPic.visibility=View.VISIBLE
                binding.searched.visibility=View.GONE
                binding.sendBox.visibility=View.VISIBLE
                setOnlineStatus()
                val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                binding.searchview.layoutParams=params
                return false
            }

        })

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchview.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
//                search.clearFocus()
                filterr(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtering(newText!!)
                return true
            }

        })
    }

    private fun setOnlineStatus() {
        val reference=FirebaseDatabase.getInstance().reference.child("Users").child(userid)
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val stat=snapshot.child("status").value.toString()
                if(stat=="online"){
                    binding.status.visibility=View.VISIBLE
                }
                else{
                    binding.status.visibility=View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            104 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    audioCalling(binding.userName.text.toString(),userid)
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            105->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    videoCalling(binding.userName.text.toString(),userid)
                }
                super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }

    private fun filterr(query: String?) {
        val filteredlist:ArrayList<MessageEntity> = ArrayList()
        for(chat in allMsg){
            if(chat.message.toLowerCase().contains(query!!.toLowerCase())){
                filteredlist.add(chat)
            }
        }

        var n=filteredlist.size-1
        if(filteredlist.isEmpty()){
            Toast.makeText(applicationContext,"No Data Found",Toast.LENGTH_SHORT).show()
            binding.searchText.text="0"+" of "+"0"
        }
        else{
            binding.searched.visibility=View.VISIBLE
            binding.sendBox.visibility=View.INVISIBLE

            binding.searchText.text=(n+1).toString()+" of "+filteredlist.size.toString()
            binding.messageRecycler.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))

           // adapter.updateList(filteredlist)

            binding.upLog.setOnClickListener {
                if(n>0){
                    n--
                    binding.searchText.text=(n+1).toString()+" of "+filteredlist.size.toString()
                    binding.messageRecycler.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))
                }
            }
            binding.downLog.setOnClickListener {
                if(n<filteredlist.size-1){
                    n++;
                    binding.searchText.text=(n+1).toString()+" of "+filteredlist.size.toString()
                    binding.messageRecycler.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))
                }
            }

        }
    }

    fun filtering(text:String){
        for(item in allMsg){
            if(item.message.toLowerCase().contains(text.toLowerCase())){
                //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                binding.messageRecycler.smoothScrollToPosition(allMsg.indexOf(item))
                adapter?.colorSearchedText(allMsg,text.toLowerCase())

            }
        }
//        if (filteredlist.isEmpty()){
//            Toast.makeText(applicationContext,"No Data found", Toast.LENGTH_SHORT).show()
//            adapter.updateList(filteredlist)
//        }
//        else{
//            adapter.updateList(filteredlist)
//        }
    }

//    private fun deleteMessage() {
//        val id=userid+"-"+firebaseUser.uid
//        val refer=FirebaseDatabase.getInstance().reference.child("Messages").child(id)
//        refer.addValueEventListener(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (snaps in snapshot.children){
//                    val seen=snaps.child("seen").value as Boolean
//                    val mid=snaps.child("mid").value.toString()
//                    if(seen==true){
//                        snaps.child(mid).ref.parent!!.removeValue()
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
//    }


    inner class sendMessageToUser(val msg: String,val messageKey:String,val userid:String,val user_name:String,val user_number:String,val user_img:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            val ref=FirebaseDatabase.getInstance().reference

            if(application!=null){
                if(ChatViewModel(application).isUserExist(userid)){
                    val currentChat = ChatViewModel(application).getChat(userid)
                    ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,msg,Constants.MY_USERID,messageKey,"",System.currentTimeMillis(),currentChat.hide,currentChat.unread,userid))
                }
                ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,msg,Constants.MY_USERID,messageKey,"",System.currentTimeMillis(),false,0,userid))
            }

            val messageHashmap=HashMap<String,Any>()
            messageHashmap.put("mid", messageKey!!)
            messageHashmap.put("userid",userid)
            messageHashmap.put("sender",Constants.MY_USERID)
            messageHashmap.put("message",msg)
            messageHashmap.put("msgStatus","")
            messageHashmap.put("url","")
            messageHashmap.put("time",System.currentTimeMillis())
            messageHashmap.put("type","message")


            ref.child("Messages").child(userid).child(messageKey).setValue(messageHashmap).addOnSuccessListener {
                MessageViewModel(application).updateMsgStatus("sent",messageKey)
            }

            sendNotification(userid, Constants.MY_USERID, msg,messageKey, 0)

            return true
        }
    }

    inner class checkSeen():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?):Boolean {

            val ref=FirebaseDatabase.getInstance().reference.child("Messages").child(userid)
            ref.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        val sender=snap.child("sender").value.toString()

                        if(sender==firebaseUser.uid){
                            val msgStatus:String=snap.child("msgStatus").value.toString()
                            val mid=snap.child("mid").value.toString()
                            MessageViewModel(application).updateMsgStatus(msgStatus,mid)
                            adapter!!.notifyDataSetChanged()
//                            if(seen){
//                                snap.child(mid).ref.parent!!.removeValue()
//                            }
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


    inner class deleteMsg():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            if(selectedMsg.size!=0){
                for(item in selectedMsg){
                    MessageViewModel(application).deleteMsg(item)
                }
                selectedMsg.clear()
            }
            return true
        }
    }

    inner class forwardMsg(val fwdChat: ArrayList<ChatEntity>, val fwdMsg: ArrayList<MessageEntity>):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            for(chat in fwdChat){
                for (msg in fwdMsg){
                    if(msg.type=="doc"){
                        val ref=FirebaseDatabase.getInstance().reference
                        val messageKey=ref.push().key
                        msg.mid=messageKey!!
                        msg.sender=Constants.MY_USERID
                        msg.userid=Constants.MY_USERID+"-"+chat.id
                        msg.msgStatus=""

                        MessageViewModel(application).insertMessage(msg)

                        chat.lst_msg="Document"
                        chat.time=System.currentTimeMillis()
                        chat.last_mid=messageKey
                        if(ChatViewModel(application).isUserExist(chat.id)){
                            chat.unread+=1
                        }
                        else{
                            chat.unread=0
                        }
                        ChatViewModel(application).inserChat(chat)

//                        saveDoctoLocalStorage(msg.url,msg.message)

                        val messageHashmap=HashMap<String,Any>()
                        messageHashmap.put("mid", messageKey!!)
                        messageHashmap.put("userid",chat.id)
                        messageHashmap.put("sender",Constants.MY_USERID)
                        messageHashmap.put("message",msg.message)
                        messageHashmap.put("time",System.currentTimeMillis())
                        messageHashmap.put("type","doc")
                        messageHashmap.put("url",msg.url)
                        messageHashmap.put("received",false)
                        messageHashmap.put("seen",false)

                        ref.child("Messages").child(chat.id).child(messageKey).setValue(messageHashmap)

                        GlobalScope.launch(Dispatchers.IO) {
                            MessageViewModel(application).updateMsgStatus("sent",messageKey)
                            sendNotification(chat.id, Constants.MY_USERID, "Document", messageKey!!, 0)
                        }
//                        uploadDocumentToFirebase(null,messageKey,msg.message).execute()
                    }
                    else if(msg.type=="image"){
                        val ref=FirebaseDatabase.getInstance().reference
                        val messageKey=ref.push().key
                        msg.mid=messageKey!!
                        msg.sender=Constants.MY_USERID
                        msg.userid=Constants.MY_USERID+"-"+chat.id
                        msg.msgStatus=""

                        MessageViewModel(application).insertMessage(msg)

                        chat.lst_msg="Photo"
                        chat.time=System.currentTimeMillis()
                        chat.last_mid=messageKey
                        if(ChatViewModel(application).isUserExist(chat.id)){
                            chat.unread+=1
                        }
                        else{
                            chat.unread=0
                        }
                        ChatViewModel(application).inserChat(chat)

                        val f = File(File(Environment.getExternalStorageDirectory(),Constants.ALL_PHOTO_LOCATION),msg.path)
                        val bmp= BitmapFactory.decodeStream(FileInputStream(f))
                        uploadImageToFirebase(bmp,messageKey,chat.id,application).execute()
//                        sendImageMessageToUser(bmp,chat.id,chat.name,chat.number,chat.image,application).execute()
                    }
                    else{
                        val ref=FirebaseDatabase.getInstance().reference
                        val messageKey=ref.push().key
                        MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,Constants.MY_USERID+"-"+chat.id,Constants.MY_USERID,msg.message,System.currentTimeMillis(),"message","","","","","sending..."))
                        sendMessageToUser(msg.message,messageKey,chat.id,chat.name,chat.number,chat.image).execute()
                    }
                }
            }
            selectedMsg.clear()
//            selectedChat.clear()
            return true
        }
    }

    fun sendNotification(
        recieverid: String,
        senderid: String,
        msg: String,
        messageKey: String,
        type: Int
    ) {

        val ref=FirebaseDatabase.getInstance().reference.child("Token")
        val query=ref.orderByKey().equalTo(recieverid)

        val apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshoshot in snapshot.children) {
                    val token: Token? = snapshoshot.getValue(Token::class.java)

                    val data = Data(recieverid, senderid, msg, messageKey, type)

                    val sender = Sender(data, token!!.getToken().toString())

                    apiService.sendNotification(sender)
                        .enqueue(object : retrofit2.Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {

                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
//                                TODO("Not yet implemented")
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
    }

    inner class getRealPathFromURI(val context: Context,val uri: Uri?):AsyncTask<Void,Void,String>() {

        override fun doInBackground(vararg p0: Void?): String {
            var filePath = ""
            val wholeID = DocumentsContract.getDocumentId(uri)

            // Split at colon, use second item in the array
            val id = wholeID.split(":".toRegex()).toTypedArray()[1]
            val column = arrayOf(MediaStore.Images.Media.DATA)

            // where id is equal to
            val sel = MediaStore.Images.Media._ID + "=?"
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, arrayOf(id), null
            )
            val columnIndex = cursor!!.getColumnIndex(column[0])
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
            return filePath
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==110 && resultCode== Activity.RESULT_OK){
            try {
//                photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                if(imageuri!=null){
                    binding.sendImgNow.visibility=View.VISIBLE
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.sendSelectImg.setImageBitmap(photo)
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        if(requestCode==112 && resultCode== Activity.RESULT_OK){
            imageuri=data!!.data
            try {
//                photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                if(imageuri!=null){
                    binding.sendImgNow.visibility=View.VISIBLE
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                    binding.sendSelectImg.setImageBitmap(photo)
                    selectedPath = getRealPathFromURI(this,imageuri!!).execute().get()  //get path of image and compress in onPostexecute and store in photo bitmap compressed image
                }
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        if(requestCode==113 && resultCode== Activity.RESULT_OK){

            if (data?.clipData != null) {
                val mClipData: ClipData? = data.clipData
                val count: Int = mClipData!!.getItemCount()
                for (i in 0 until count) {
                    val docurI: Uri = mClipData.getItemAt(i).getUri()
                    sendDocumentMessage(docurI,userid,intent.getStringExtra("name")!!,intent.getStringExtra("number")!!,user_image).execute()
                }
            } else {
                val docUri: Uri = data?.getData()!!
                sendDocumentMessage(docUri,userid,intent.getStringExtra("name")!!,intent.getStringExtra("number")!!,user_image).execute()
            }


//            val docpath=data!!.data
//             sendDocumentMessage(docpath,userid,intent.getStringExtra("name")!!,intent.getStringExtra("number")!!,user_image).execute()
        }
        if (requestCode === 111 && resultCode === RESULT_OK && null != data) {
            // Get the Image from data
            if (data.getClipData() != null) {

                //setting recycelerview
                selectedLayoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                binding.selectedImgRecycler.layoutManager=selectedLayoutManager

                val mClipData: ClipData? = data.clipData
                val count: Int = mClipData!!.getItemCount()
                for (i in 0 until count) {
                    // adding imageuri in array
                    val imageurI: Uri = mClipData.getItemAt(i).getUri()
                    allSelectedUri.add(imageurI)
                    gallImagesPath.add(getRealPathFromURI(this,imageurI).execute().get())
                }
                selectedImgAdapter=SelectedImgAdapter(this,allSelectedUri,object :SelectedImgAdapter.OnImageCardClickListener{
                    override fun loadNewImage(uri: Uri) {
                        binding.sendSelectImg.setImageURI(uri)
                    }

                    override fun removeImage(position: Int) {
                        allSelectedUri.removeAt(position)
                        gallImagesPath.removeAt(position)
                        selectedImgAdapter.notifyDataSetChanged()
                        if(allSelectedUri.size==0){
                            binding.sendImgNow.visibility=View.GONE
                        }
                    }

                })
                binding.selectedImgRecycler.adapter=selectedImgAdapter
                // setting 1st selected image into image switcher
                binding.sendImgNow.visibility=View.VISIBLE
                binding.sendSelectImg.setImageURI(mClipData.getItemAt(0)?.uri)
                //position = 0
            } else {
                val imageurl: Uri = data.getData()!!
                binding.sendImgNow.visibility=View.VISIBLE
                photo=MediaStore.Images.Media.getBitmap(contentResolver,imageurl)
//                sendImg.setImageURI(imageurl)
                binding.sendSelectImg.setImageBitmap(photo)
                gallImagesPath.add(getRealPathFromURI(this,imageurl).execute().get())

//                position = 0
            }
        }
//        if(photo!=null){
//
//            val rotationMatrix = Matrix()
//            if (photo?.getWidth()!! >= photo!!.getHeight() ) {
//                rotationMatrix.setRotate(90F)
//            } else {
//                rotationMatrix.setRotate(0F)
//            }
//
//            rotatedBitmap = Bitmap.createBitmap(
//                photo!!,
//                0,
//                0,
//                photo!!.getWidth(),
//                photo!!.getHeight(),
//                rotationMatrix,
//                true
//            )
//
//            sendImg.setImageBitmap(rotatedBitmap)
//            sendImgLayout.visibility=View.VISIBLE
////
////            val name = intent.getStringExtra("name")
////            val number = intent.getStringExtra("number")
////            val intent = Intent(this,SelectedImage::class.java)
////            var fos  =  ByteArrayOutputStream()
////            photo!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
////            val byteArray = fos.toByteArray()
////            intent.putExtra("type","message")
//////            intent.putExtra("image", )
////            intent.setData(imageuri)
////            intent.putExtra("userid",userid)
////            intent.putExtra("name",name)
////            intent.putExtra("number",number)
////            intent.putExtra("user_image",user_image)
////            startActivity(intent)
//        }
    }

    inner class sendDocumentMessage(val docpath: Uri?,val userid:String,val user_name:String,val user_number:String,val user_img:String):AsyncTask<Void,Void,Boolean>() {
        var messageKey:String=""
        var displayName:String =""
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            uploadDocumentToFirebase(docpath,messageKey,displayName).execute()
        }

        override fun doInBackground(vararg params: Void?): Boolean {


            var path:String?=null
            val ref=FirebaseDatabase.getInstance().reference
            messageKey= ref.push().key!!

            val uriString = docpath.toString()
            val myFile = File(uriString)
            if (uriString.startsWith("content://")) {
                var cursor: Cursor? = null
                try {

                    cursor = contentResolver.query(docpath!!, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        Log.e("Ayush",displayName.toString())
                    }
                    cursor!!.close()
                }catch (e:java.lang.Exception){

                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.name
            }


            if(application!=null){
                if(!MessageViewModel(application).isMsgExist(messageKey!!)){
                    MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,firebaseUser.uid+"-"+userid,firebaseUser.uid,displayName!!,System.currentTimeMillis(),"doc","",uriString,"","","sending..."))
                }
                if(ChatViewModel(application).isUserExist(userid)){
                    val currentChat = ChatViewModel(application).getChat(userid)
                    ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Document",Constants.MY_USERID,messageKey,userid,System.currentTimeMillis(),false,currentChat.unread,userid))
                }
                ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Document",Constants.MY_USERID,messageKey,userid,System.currentTimeMillis(),false,0,userid))

            }

            return true
        }

    }

    fun saveDoctoLocalStorage(url:String,name:String){

        val directory: File = File(Environment.getExternalStorageDirectory().toString(), Constants.DOC_LOCATION)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val pdfFile = File(directory,name)
        FileDownloader().downloadFile(url, pdfFile)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {

            val url = URL(url)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestMethod("GET")
//            urlConnection.setDoOutput(true)
//            urlConnection.setRequestProperty("Content-Type", "application/pdf")
            urlConnection.connect()
            val inputStream: InputStream = urlConnection.inputStream
            val fileOutputStream = FileOutputStream(pdfFile)
            val totalSize: Int = urlConnection.getContentLength()
            val buffer = ByteArray(1024*1024)
            var bufferLength = 0
            while (inputStream.read(buffer).also { bufferLength = it } != -1) {
                fileOutputStream.write(buffer, 0, bufferLength)
            }
            fileOutputStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    inner class uploadDocumentToFirebase(val docpath: Uri?,val messageKey: String,val displayName:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg p0: Void?): Boolean {

            val store: StorageReference = FirebaseStorage.getInstance().reference.child("Chat Documents/")
            val pathupload=store.child("$displayName")
            val uploadTask: StorageTask<*>
            uploadTask=pathupload.putFile(docpath!!)

            val ref=FirebaseDatabase.getInstance().reference

            uploadTask.addOnSuccessListener(OnSuccessListener { taskSnapshot ->
                val firebaseUri = taskSnapshot.storage.downloadUrl
                firebaseUri.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    val messageHashmap=HashMap<String,Any>()
                    messageHashmap.put("mid", messageKey!!)
                    messageHashmap.put("userid",userid)
                    messageHashmap.put("sender",firebaseUser.uid)
                    messageHashmap.put("message",displayName!!)
                    messageHashmap.put("msgStatus","sent")
                    messageHashmap.put("time",System.currentTimeMillis())
                    messageHashmap.put("type","doc")
                    messageHashmap.put("url",url)

                    ref.child("Messages").child(userid).child(messageKey).setValue(messageHashmap)

//                    MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,firebaseUser.uid+"-"+userid,firebaseUser.uid,displayName!!,System.currentTimeMillis(),"doc","","",url,"","seen"))
                    GlobalScope.launch(Dispatchers.IO) {
                        MessageViewModel(application).updateMsgStatus("sent",messageKey)

                        saveDoctoLocalStorage(url,displayName)

//                        MessageAdapter(this@Message,selectedMsg,object:MessageAdapter.OnAdapterItemClickListener{
//                            override fun updateCount() {
//                                mainViewModel.setText(selectedMsg.size.toString())
//                            }
//
//                        }).notifyDataSetChanged()

                        sendNotification(userid, firebaseUser.uid, "Document", messageKey!!, 0)
                    }
                }
            })
            return true
        }
    }

    inner class uploadImageToFirebase(val bitmapImage: Bitmap,val messageKey: String,val recvid:String,val applicat: Application):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg p0: Void?): Boolean {

            val imagebaos= ByteArrayOutputStream()
            val thumbnailbaos= ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,imagebaos)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG,0,thumbnailbaos)
            val imagefileinBytes: ByteArray = imagebaos.toByteArray()
            val thumbnailfileinBytes: ByteArray = thumbnailbaos.toByteArray()

            val refStore= FirebaseDatabase.getInstance().reference
            val profilekey=refStore.push().key

            val imagestore: StorageReference = FirebaseStorage.getInstance().reference.child("Chat Images/")
            val imagepathupload=imagestore.child("$profilekey.jpg")
            val imageuploadTask: StorageTask<*>
            imageuploadTask=imagepathupload.putBytes(imagefileinBytes)

            val ref = FirebaseDatabase.getInstance().reference
            imageuploadTask.addOnSuccessListener(OnSuccessListener { taskSnapshot ->
                val firebaseUri = taskSnapshot.storage.downloadUrl
                firebaseUri.addOnSuccessListener { uri ->
                    val image_url = uri.toString()

                    val thumbstore: StorageReference = FirebaseStorage.getInstance().reference.child("Thumbnail/")
                    val thumbpathupload=thumbstore.child("$profilekey.jpg")
                    val thumbuploadTask: StorageTask<*>
                    thumbuploadTask=thumbpathupload.putBytes(thumbnailfileinBytes)

                    thumbuploadTask.addOnSuccessListener(OnSuccessListener { taskSnapshot->
                            val thumbFirebaseUri = taskSnapshot.storage.downloadUrl
                            thumbFirebaseUri.addOnSuccessListener{ thumbUri->
                                val thumb_url = thumbUri.toString()

                                val messageHashmap=HashMap<String,Any>()
                                messageHashmap.put("mid", messageKey!!)
                                messageHashmap.put("userid",recvid)
                                messageHashmap.put("sender",Constants.MY_USERID)
                                messageHashmap.put("message","")
                                messageHashmap.put("msgStatus","sent")
                                messageHashmap.put("time",System.currentTimeMillis())
                                messageHashmap.put("type","image")
                                messageHashmap.put("url",image_url)
                                messageHashmap.put("thumbnail",thumb_url)

                                ref.child("Messages").child(recvid).child(messageKey).setValue(messageHashmap)

                                MessageViewModel(applicat).updateMsgStatus("sent",messageKey)
                                sendNotification(recvid, Constants.MY_USERID, "Image", messageKey!!, 0)
                            }
                        }
                    )
                }
            })
            return true
        }

    }



    inner class sendImageMessageToUser(val bitmapImage: Bitmap,val userid:String,val user_name:String,val user_number:String,val user_img:String,val application: Application):AsyncTask<Void,Void,Boolean>(){
        var path:String?=null
        var messageKey:String?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            uploadImageToFirebase(bitmapImage,messageKey!!,userid,application).execute()
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref=FirebaseDatabase.getInstance().reference
            messageKey=ref.push().key

            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()) {
                    val directory: File = File(Environment.getExternalStorageDirectory().toString(), Constants.ALL_PHOTO_LOCATION)
                    if(!directory.exists()){
                        directory.mkdirs()
                    }
                    path=messageKey+".jpg"
                    var fos: FileOutputStream =
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
                } else {
                    //request for the permission
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
            else{
                val directory: File = File(Environment.getExternalStorageDirectory().toString(), Constants.ALL_PHOTO_LOCATION)
                if(!directory.exists()){
                   directory.mkdirs()
                }
                path=messageKey+".jpg"
                var fos: FileOutputStream =
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


            if(application!=null){
                if(!MessageViewModel(application).isMsgExist(messageKey!!)){
                    MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,Constants.MY_USERID+"-"+userid,Constants.MY_USERID,"",System.currentTimeMillis(),"image",path!!,"","","","sending..."))
                }
                if(ChatViewModel(application).isUserExist(userid)){
                    val currentChat = ChatViewModel(application).getChat(userid)
                    ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Photo",Constants.MY_USERID,messageKey!!,userid+".jpg",System.currentTimeMillis(),false,currentChat.unread,userid))
                }
                ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Photo",Constants.MY_USERID,messageKey!!,userid+".jpg",System.currentTimeMillis(),false,0,userid))

            }

            return true
        }

    }
}