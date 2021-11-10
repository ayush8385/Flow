package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.Notification.*
import com.ayush.flow.R
import com.ayush.flow.Services.APIService
import com.ayush.flow.adapter.ForwardAdapter
import com.ayush.flow.adapter.ForwardToAdapter
import com.ayush.flow.adapter.MessageAdapter
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.MessageEntity
import com.ayush.flow.database.MessageViewModel
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
import retrofit2.Call
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import android.graphics.drawable.BitmapDrawable
import com.ayush.flow.Services.Permissions
import com.ayush.flow.adapter.ChatAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class Message : BaseActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var fwdrecyclerView:RecyclerView
    lateinit var fwdtorecyclerView:RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    var chatList= arrayListOf<ChatEntity>()
    lateinit var more: ImageView
    lateinit var name:TextView
    lateinit var more_card:LinearLayout
    lateinit var parent:RelativeLayout
    lateinit var back: ImageView
    lateinit var send_txt:EditText
    var handler = Handler()
    var runnable: Runnable? = null
    var delay = 100
    lateinit var send: ImageView
    lateinit var image:CircleImageView
    lateinit var viewModel: MessageViewModel
    lateinit var selectAll:CheckBox
    var userid:String=""
    var user_image:String=""
    var number:String=""
    var photo:Bitmap?=null
    lateinit var status:TextView
    lateinit var firebaseUser: FirebaseUser
    lateinit var adapter: MessageAdapter
    lateinit var fwdAdapter:ForwardAdapter
    var apiService: APIService?=null
    lateinit var fwdtoAdapter:ForwardToAdapter
    lateinit var search: androidx.appcompat.widget.SearchView
    val allMsg = arrayListOf<MessageEntity>()
    val selectedMsg = arrayListOf<MessageEntity>()
    public val selectedChat = arrayListOf<ChatEntity>()
    lateinit var up: ImageView
    lateinit var down: ImageView
    lateinit var search_txt:TextView
    lateinit var searched:CardView
    lateinit var delete: ImageView
    private var imageuri: Uri?=null
    lateinit var forward: ImageView
    lateinit var close: ImageView
    lateinit var select_txt:TextView
    lateinit var selected:CardView
    lateinit var mainViewModel: MainViewModel
    lateinit var fwdViewModel: ForwardViewModel
    lateinit var send_box:CardView
    lateinit var forward_card:CardView
    lateinit var close_fwd: ImageView
    lateinit var dim:View
    private lateinit var photofile: File
    lateinit var searchfwd:SearchView
    lateinit var fwd_btn: ImageView
    lateinit var send_cam: ImageView
    lateinit var send_con: ImageView
    lateinit var send_doc: ImageView
    lateinit var send_gall: ImageView
    lateinit var details:LinearLayout
    lateinit var sharedPreferences:SharedPreferences
    lateinit var profile:ImageView


   // lateinit var option:ImageView
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        recyclerView=findViewById(R.id.message_recycler)
        name=findViewById(R.id.user_name)
        more=findViewById(R.id.more)
        more_card=findViewById(R.id.more_card)
        parent=findViewById(R.id.msg_parent)
        back=findViewById(R.id.back)
        send_txt=findViewById(R.id.send_text)
        send=findViewById(R.id.send_btn)
       search=findViewById(R.id.searchview)
        image=findViewById(R.id.user_pic)
        status=findViewById(R.id.status)
       fwd_btn=findViewById(R.id.fwd_btn)
       send_box=findViewById(R.id.send)
       close=findViewById(R.id.close)
       details=findViewById(R.id.details)
       profile=findViewById(R.id.user_profile)

       //send img doc cons box
       send_con=findViewById(R.id.send_con)
       send_doc=findViewById(R.id.send_doc)
       send_cam=findViewById(R.id.send_cam)
       send_gall=findViewById(R.id.send_gall)


       apiService= Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)
       //forwarding

       forward_card=findViewById(R.id.fwd_card)
       close_fwd=findViewById(R.id.close_fwd)
       dim=findViewById(R.id.dim)
       searchfwd=findViewById(R.id.searchview_fwd)
       fwdrecyclerView=findViewById(R.id.forwardrecycler)
       fwdtorecyclerView=findViewById(R.id.forwarded_to)
       selectAll=findViewById(R.id.selectAll)
    //    option=findViewById(R.id.more_option)



       searched=findViewById(R.id.searched)
       search_txt=findViewById(R.id.search_text)
       up=findViewById(R.id.up_log)
       down=findViewById(R.id.down_log)

       selected=findViewById(R.id.selected)
       select_txt=findViewById(R.id.select_text)
       delete=findViewById(R.id.delete)
       forward=findViewById(R.id.forward)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!

       sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)
//       if(sharedPreferences.getBoolean("nightMode",true)){
//           audiocall.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.call))
//           videocall.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.video_call))
//       }
//       else{
//           audiocall.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.audio_black))
//           videocall.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.video_black))
//       }



       fwdViewModel=ViewModelProviders.of(this).get(ForwardViewModel::class.java)
       mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel=ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MessageViewModel::class.java)

        back.setOnClickListener {
            onBackPressed()
        }


        if(intent.getStringExtra("name")==""){
            name.text=intent.getStringExtra("number")!!
        }
        else{
            name.text=intent.getStringExtra("name")!!
        }
        userid= intent.getStringExtra("userid")!!
        user_image=intent.getStringExtra("image")!!



       close.setOnClickListener {
           selected.visibility=View.GONE
           send_box.visibility=View.VISIBLE
           selectedMsg.clear()
           adapter.notifyDataSetChanged()
       }

       delete.setOnClickListener {
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
           selected.visibility=View.GONE
           send_box.visibility=View.VISIBLE

       }

       selectAll.setOnClickListener {
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

       forward.setOnClickListener {

           val toolbar:CardView=findViewById(R.id.toolbar)
           toolbar.cardElevation=0F

           send_box.visibility=View.GONE
           dim.visibility=View.VISIBLE
           forward_card.visibility=View.VISIBLE
           val animFadein: Animation = AnimationUtils.loadAnimation(
               applicationContext,
               R.anim.slide_up
           )
          forward_card.startAnimation(animFadein)

           searchfwdElement()


           layoutManager= LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
           fwdtorecyclerView.layoutManager=layoutManager
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
           fwdtorecyclerView.adapter=fwdtoAdapter

           layoutManager= LinearLayoutManager(this)
           (layoutManager as LinearLayoutManager).reverseLayout=true
           fwdrecyclerView.layoutManager=layoutManager
           fwdAdapter = ForwardAdapter(this@Message,object :ForwardAdapter.OnAdapterItemClickListener{
               override fun addChat(chatEntity: ChatEntity) {
                   selectedChat.add(chatEntity)
                   fwdtoAdapter.updateList(selectedChat)

                   if(selectedChat.size==chatList.size){
                       selectAll.isChecked=true
                   }
               }

               override fun delChat(chatEntity: ChatEntity) {
                   selectedChat.remove(chatEntity)
                   fwdtoAdapter.updateList(selectedChat)

                   if(selectedChat.size<chatList.size){
                       selectAll.isChecked=false
                   }
               }

           })
           fwdrecyclerView.adapter=fwdAdapter

           ChatViewModel(application).allChats.observe(this, Observer { list->
               list?.let {
                   chatList.clear()
                   chatList.addAll(list)
                   fwdAdapter.updateList(chatList,selectedChat)
               }
           })

           selected.visibility=View.GONE
       }

       close_fwd.setOnClickListener {
           val toolbar:CardView=findViewById(R.id.toolbar)
           toolbar.cardElevation=50F
           dim.visibility=View.GONE
           forward_card.visibility=View.GONE
           send_box.visibility=View.VISIBLE
           selectedMsg.clear()
           selectedChat.clear()
           searchfwd.isIconified=true

           adapter.notifyDataSetChanged()
       }
//        option.setOnClickListener {
//            val menuBuilder = MenuBuilder(this)
//            SupportMenuInflater(this).inflate(R.menu.popup_menu, menuBuilder)
//            menuBuilder.setCallback(object : MenuBuilder.Callback {
//                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
//                    when (item.getItemId()) {
//                        R.id.search->{
//
//                        }
//                        R.id.block -> {
//
//                        }                        // do something 2
//
//                    }
//                    return true
//                }
//
//                override fun onMenuModeChange(menu: MenuBuilder) {}
//            })
//            val menuHelper = MenuPopupHelper(this, menuBuilder, option)
//            menuHelper.setForceShowIcon(true) // show icons!!!!!!!!
//            menuHelper.show()
//        }

       if(user_image!=""){
           setIconImage(image,user_image).execute()
       }

       image.setOnClickListener {
           openProfileBottomSheet(this,name.text.toString(),image,userid,user_image,true)
       }

       details.setOnClickListener {
           openProfileBottomSheet(this,name.text.toString(),image,userid,user_image,true)
       }

       profile.setOnClickListener {
           openProfileBottomSheet(this,name.text.toString(),image,userid,user_image,true)
       }


        layoutManager= LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).stackFromEnd=true
        recyclerView.layoutManager=layoutManager
        adapter = MessageAdapter(this,selectedMsg,object:MessageAdapter.OnAdapterItemClickListener{
            override fun updateCount() {
                mainViewModel.setText(selectedMsg.size.toString())
            }

        })
        recyclerView.adapter=adapter

       mainViewModel.getText().observe(this,Observer{it->
           if(it=="0"){
               selected.visibility=View.GONE
               send_box.visibility=View.VISIBLE
           }
           else{
               select_txt.text=it+" Selected"
               selected.visibility=View.VISIBLE
               send_box.visibility=View.INVISIBLE
           }
       })


        viewModel.allMessages(firebaseUser.uid+"-"+userid).observe(this, Observer {list->
            list?.let {
                allMsg.clear()
                allMsg.addAll(list)
                adapter.updateList(list as ArrayList<MessageEntity>)
                recyclerView.smoothScrollToPosition(list.size)
            }
        })


       fwd_btn.setOnClickListener{
           forwardMsg().execute()

           dim.visibility=View.GONE
           forward_card.visibility=View.GONE
           send_box.visibility=View.VISIBLE
           searchfwd.isIconified=true
       }

        more.setOnClickListener {
            if(more_card.visibility== View.GONE){

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
                more.startAnimation(rotate)


                more_card.visibility=View.VISIBLE
                val animFadein: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.slide_up
                )
                more_card.startAnimation(animFadein)
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
                more.startAnimation(rotate)


                more_card.visibility=View.GONE
            }
        }

        send.setOnClickListener {
            val msg=send_txt.text.toString()
            if(msg!=""){
                send_txt.setText("")
                sendMessageToUser(msg,userid,intent.getStringExtra("name")!!, intent.getStringExtra("number")!!,user_image).execute()
            }
        }

        val reference=FirebaseDatabase.getInstance().reference.child("Users").child(userid)
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val stat=snapshot.child("status").value.toString()
                if(stat=="online"){
                    status.text=stat
                    status.visibility=View.VISIBLE
                }
                else{
                    status.visibility=View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


       send_cam.setOnClickListener {

           if(Permissions().checkCamerapermission(this)){
               openCamera()
               more_card.visibility=View.GONE

           }
           else{
               Permissions().openPermissionBottomSheet(R.drawable.camera_permission,
                   this.resources.getString(R.string.camera_permission),this,"camera")
           }


       }

       send_gall.setOnClickListener {
           if(Permissions().checkWritepermission(this)){
               openGallery()
               more_card.visibility=View.GONE
           }
           else{
               Permissions().openPermissionBottomSheet(R.drawable.gallery,this.resources.getString(R.string.storage_permission),this,"storage")
           }

       }

       send_doc.setOnClickListener {
          // if(Permissions())
       }

        Dashboard().checkStatus().execute()
        checkSeen().execute()
        searchElement()
       // deleteMessage()

    }

    fun openProfileBottomSheet(context: Context,username:String,userimg:CircleImageView,user_id:String,user_image_path:String,isChat:Boolean) {
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

        profile_image!!.setImageBitmap((userimg.drawable as BitmapDrawable).bitmap)
        user_name!!.text=username

        profile_image.setOnClickListener {
            val intent = Intent(context, SelectedImage::class.java)
            var fos  =  ByteArrayOutputStream()
            ((userimg.drawable as BitmapDrawable).bitmap).compress(Bitmap.CompressFormat.JPEG, 100, fos)
            val byteArray = fos.toByteArray()
            intent.putExtra("type","view")
            intent.putExtra("image", byteArray)
            intent.putExtra("userid","")
            intent.putExtra("name","")
            intent.putExtra("number","")
            intent.putExtra("user_image","")
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
                intent.putExtra("image", user_image_path)
                context.startActivity(intent)
            }
        }

        audiocall!!.setOnClickListener {
            if(Permissions().checkMicpermission(context)){
                audioCalling(username,user_id,user_image_path)
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.mic_permission,context.resources.getString(R.string.mic_permission),context,"mic")
            }

            

        }

        videocall!!.setOnClickListener {
            if(Permissions().checkMicpermission(context)){
                videoCalling(username,user_id,user_image_path)
            }
            else{
                Permissions().openPermissionBottomSheet(R.drawable.mic_permission,context.resources.getString(R.string.mic_permission),context,"mic")
            }

        }

        clear_chat?.setOnClickListener {
            bottomSheetDialog.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Are you sure want to delete")
                .setCancelable(false)
                .setPositiveButton("Delete") {
                        dialog: DialogInterface, _: Int ->
                    Dashboard().deleteMsgs((context as Dashboard).application,user_id).execute()
                    ChatViewModel((context as Dashboard).application).setLastMsg("",user_id);
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

    private fun videoCalling(username: String, userId: String, userImagePath: String) {
        val userName: String = username
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUserVideo(userId).callId
        val callScreen = Intent(this, Outgoing_vdo::class.java)
        callScreen.putExtra("name",userName)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("image",userImagePath)
        sendNotification(userid, firebaseUser.uid, "","", 1)
        startActivity(callScreen)
    }

    private fun audioCalling(username: String, userId: String, userImagePath: String) {
        val userName: String = username
        //
        val sinchServiceInterface=getSinchServiceInterface()
        val callId=sinchServiceInterface!!.callUser(userId).callId
        val callScreen = Intent(this, Outgoing::class.java)
        callScreen.putExtra("name",userName)
        callScreen.putExtra("CALL_ID", callId)
        callScreen.putExtra("image",userImagePath)
        sendNotification(userId, FirebaseAuth.getInstance().currentUser!!.uid, "", "", 1)
        startActivity(callScreen)
    }

    fun openGallery() {
        var intent= Intent(Intent.ACTION_GET_CONTENT)
        intent.type="image/*"
        startActivityForResult(intent,112)
    }

    fun openCamera(){
        photofile = getphotofile("chat_photo")
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
        super.onBackPressed()
        startActivity(Intent(this,Dashboard::class.java))
        finishAffinity()
    }

    override fun onResume() {

        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            ChatViewModel(application).setUnread(0,userid)
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

    private fun searchfwdElement() {
        searchfwd.queryHint="Search chats..."
//        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon)
//        searchIcon.setColorFilter(Color.WHITE)
        val theTextArea = searchfwd.findViewById(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
//        theTextArea.setTextColor(Color.WHITE)
        theTextArea.isCursorVisible=false

        searchfwd.setOnSearchClickListener {
            selectAll.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            params.addRule(RelativeLayout.BELOW,R.id.forwarded_to)
            searchfwd.layoutParams=params
        }

        searchfwd.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                selectAll.visibility=View.VISIBLE
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
                search.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterrFwd(newText!!)
                return true
            }

        })
    }

    fun filterrFwd(text:String){
        val filteredlist:ArrayList<ChatEntity> = ArrayList()

        for(item in chatList){
            if(item.name.toLowerCase().contains(text.toLowerCase())||item.number.contains(text)){
                //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                filteredlist.add(item)
            }
            else if(item.number.toLowerCase().contains(text.toLowerCase())){
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
    }

    fun searchElement() {

        search.queryHint="Search messages..."
        val searchIcon: ImageView = search.findViewById(R.id.search_mag_icon)

        val theTextArea = search.findViewById(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete

        theTextArea.isCursorVisible=false

        search.setOnSearchClickListener {
            back.visibility= View.GONE
            name.visibility=View.GONE
            status.visibility=View.GONE
            profile.visibility=View.GONE
            image.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            search.layoutParams=params
        }

        search.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                back.visibility= View.VISIBLE
                name.visibility=View.VISIBLE
                status.visibility=View.VISIBLE
                profile.visibility=View.VISIBLE
                image.visibility=View.VISIBLE
                searched.visibility=View.GONE
                send_box.visibility=View.VISIBLE
                val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                search.layoutParams=params
                return false
            }

        })

        val manager=getSystemService(Context.SEARCH_SERVICE) as SearchManager
        search.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            104 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do_SOme_Operation()
                    audioCalling(name.text.toString(),userid,user_image)

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
            search_txt.text="0"+" of "+"0"
        }
        else{
            searched.visibility=View.VISIBLE
            send_box.visibility=View.INVISIBLE

            search_txt.text=(n+1).toString()+" of "+filteredlist.size.toString()
            recyclerView.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))

           // adapter.updateList(filteredlist)

            up.setOnClickListener {
                if(n>0){
                    n--;
                    search_txt.text=(n+1).toString()+" of "+filteredlist.size.toString()
                    recyclerView.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))
                }
            }
            down.setOnClickListener {
                if(n<filteredlist.size-1){
                    n++;
                    search_txt.text=(n+1).toString()+" of "+filteredlist.size.toString()
                    recyclerView.smoothScrollToPosition(allMsg.indexOf(filteredlist.get(n)))
                }
            }

        }
    }

    fun filtering(text:String){
        for(item in allMsg){
            if(item.message.toLowerCase().contains(text.toLowerCase())){
                //recyclerView.scrollToPosition(mChatlist.indexOf(item))
                recyclerView.smoothScrollToPosition(allMsg.indexOf(item))
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


    inner class sendMessageToUser(val msg:String,val userid:String,val user_name:String,val user_number:String,val user_img:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            val ref=FirebaseDatabase.getInstance().reference
            val messageKey=ref.push().key

            firebaseUser=FirebaseAuth.getInstance().currentUser!!

            val sdf = SimpleDateFormat("hh:mm a")
            val tm: Date = Date(System.currentTimeMillis())

            if(application!=null){
//                if(!MessageViewModel(application).isMsgExist(messageKey!!)){
//                }
                MessageViewModel(application).insertMessage(MessageEntity(messageKey!!,firebaseUser.uid+"-"+userid,firebaseUser.uid,msg,sdf.format(tm),"message",
                    recev = false,
                    seen = false,
                    sent = false
                ))
                if(ChatViewModel(application).isUserExist(userid)){
                    val currentChat = ChatViewModel(application).getChat(userid)
                    ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,msg,sdf.format(tm),false,currentChat.unread,userid))
                }
                ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,msg,sdf.format(tm),false,0,userid))
            }

            val messageHashmap=HashMap<String,Any>()
            messageHashmap.put("mid", messageKey!!)
            messageHashmap.put("userid",userid)
            messageHashmap.put("sender",firebaseUser.uid)
            messageHashmap.put("message",msg)
            messageHashmap.put("time",System.currentTimeMillis())
            messageHashmap.put("type","message")
            messageHashmap.put("received",false)
            messageHashmap.put("seen",false)

            ref.child("Messages").child(userid).child(messageKey).setValue(messageHashmap)

            sendNotification(userid, firebaseUser.uid, msg,messageKey, 0)

            return true
        }
    }

    inner class setIconImage(val image:CircleImageView,val user_image:String):AsyncTask<Void,Void,Boolean>(){
        var b: Bitmap?=null
        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            image.setImageBitmap(b)
        }
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                val f = File(File(Environment.getExternalStorageDirectory(),"/Flow/Medias/Contacts Images"),user_image)
                b = BitmapFactory.decodeStream(FileInputStream(f))

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
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
                            val rec:Boolean=snap.child("received").value as Boolean
                            val seen:Boolean=snap.child("seen").value as Boolean
                            val mid=snap.child("mid").value.toString()
                            MessageViewModel(application).updatetMessage(mid,rec,seen)
                            adapter.notifyDataSetChanged()
                            if(seen){
                              //  snap.child(mid).ref.parent!!.removeValue()
                            }
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

    inner class forwardMsg():AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            for(chat in selectedChat){
                for (msg in selectedMsg){
                    sendMessageToUser(msg.message,chat.id,chat.name,chat.number,chat.image).execute()
                }
            }
            selectedMsg.clear()
            selectedChat.clear()
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
                                if (response.code() == 200) {
                                    if (response.body()!!.success != 1) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Hey you",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                                TODO("Not yet implemented")
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==110 && resultCode== Activity.RESULT_OK){
            if(imageuri!=null){
                try {
                    photo=MediaStore.Images.Media.getBitmap(contentResolver,imageuri)
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode==112 && resultCode== Activity.RESULT_OK){
            val filepath=data!!.data
            try {
                photo=MediaStore.Images.Media.getBitmap(contentResolver,filepath)
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        if(photo!=null){
            val name = intent.getStringExtra("name")
            val number = intent.getStringExtra("number")
            val intent = Intent(this,SelectedImage::class.java)
            var fos  =  ByteArrayOutputStream()
            photo!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            val byteArray = fos.toByteArray()
            intent.putExtra("type","message")
            intent.putExtra("image", byteArray )
            intent.putExtra("userid",userid)
            intent.putExtra("name",name)
            intent.putExtra("number",number)
            intent.putExtra("user_image",user_image)
            startActivity(intent)
        }
    }



    inner class sendImageMessageToUser(val bitmapImage: Bitmap,val userid:String,val user_name:String,val user_number:String,val user_img:String,val application: Application):AsyncTask<Void,Void,Boolean>(){
        var path:String?=null
        override fun doInBackground(vararg params: Void?): Boolean {
            val ref=FirebaseDatabase.getInstance().reference
            val messageKey=ref.push().key

            firebaseUser=FirebaseAuth.getInstance().currentUser!!

            val sdf = SimpleDateFormat("hh:mm a")
            val tm: Date = Date(System.currentTimeMillis())


            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()) {
                    val directory: File = File(Environment.getExternalStorageDirectory().toString(), "/Flow/Medias/Chat Images")
                    if(directory.exists()){
                        path=messageKey+".jpg"
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
                            path=messageKey+".jpg"
                            val fos =
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
                    path=messageKey+".jpg"
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
                        path=messageKey+".jpg"
                        val fos =
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
                }
            }


            if(application!=null){
                if(!MessageViewModel(application).isMsgExist(messageKey!!)){
                    MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+userid,firebaseUser.uid,path!!,sdf.format(tm),"image",false,false,false))
                }
                if(ChatViewModel(application).isUserExist(userid)){
                    val currentChat = ChatViewModel(application).getChat(userid)
                    ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Photo",sdf.format(tm),false,currentChat.unread,userid))
                }
                ChatViewModel(application).inserChat(ChatEntity(user_name,user_number,user_img,"Photo",sdf.format(tm),false,0,userid))

            }


            val baos= ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.JPEG,25,baos)
            val fileinBytes: ByteArray =baos.toByteArray()

            val refStore= FirebaseDatabase.getInstance().reference
            val profilekey=refStore.push().key

            val store: StorageReference = FirebaseStorage.getInstance().reference.child("Chat Images/")
            val pathupload=store.child("$profilekey.jpg")
            val uploadTask: StorageTask<*>
            uploadTask=pathupload.putBytes(fileinBytes)

            uploadTask.addOnSuccessListener(OnSuccessListener { taskSnapshot ->
                val firebaseUri = taskSnapshot.storage.downloadUrl
                firebaseUri.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    val messageHashmap=HashMap<String,Any>()
                    messageHashmap.put("mid", messageKey!!)
                    messageHashmap.put("userid",userid)
                    messageHashmap.put("sender",firebaseUser.uid)
                    messageHashmap.put("message",url)  //image url here
                    messageHashmap.put("time",System.currentTimeMillis())
                    messageHashmap.put("type","image")
                    messageHashmap.put("received",false)
                    messageHashmap.put("seen",false)

                    ref.child("Messages").child(userid).child(messageKey).setValue(messageHashmap)

                    MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+userid,firebaseUser.uid,path!!,sdf.format(tm),"image",false,false,true))
                    MessageAdapter(this@Message,selectedMsg,object:MessageAdapter.OnAdapterItemClickListener{
                        override fun updateCount() {
                            mainViewModel.setText(selectedMsg.size.toString())
                        }

                    }).notifyDataSetChanged()

                    sendNotification(userid, firebaseUser.uid, "Image", messageKey!!, 0)
                }
            })

            return true
        }

    }
}