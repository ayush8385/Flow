package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.adapter.ForwardAdapter
import com.ayush.flow.adapter.ForwardToAdapter
import com.ayush.flow.adapter.MessageAdapter
import com.ayush.flow.database.ChatEntity
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.MessageEntity
import com.ayush.flow.database.MessageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Message : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var fwdrecyclerView:RecyclerView
    lateinit var fwdtorecyclerView:RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    var chatList= arrayListOf<ChatEntity>()
    lateinit var more:ImageView
    lateinit var name:TextView
    lateinit var more_card:CardView
    lateinit var parent:RelativeLayout
    lateinit var audiocall:ImageView
    lateinit var videocall:ImageView
    lateinit var back:ImageView
    lateinit var send_txt:EditText
    lateinit var send:ImageView
    lateinit var image:CircleImageView
    lateinit var viewModel: MessageViewModel
    lateinit var selectAll:CheckBox
    var userid:String=""
    var user_image:String=""
    var number:String=""
    lateinit var status:TextView
    lateinit var firebaseUser: FirebaseUser
    lateinit var adapter: MessageAdapter
    lateinit var fwdAdapter:ForwardAdapter
    lateinit var fwdtoAdapter:ForwardToAdapter
    lateinit var search: androidx.appcompat.widget.SearchView
    val allMsg = arrayListOf<MessageEntity>()
    val selectedMsg = arrayListOf<MessageEntity>()
    val selectedChat = arrayListOf<ChatEntity>()
    lateinit var up:ImageView
    lateinit var down:ImageView
    lateinit var search_txt:TextView
    lateinit var searched:CardView
    lateinit var delete:ImageView
    lateinit var forward:ImageView
    lateinit var close:ImageView
    lateinit var select_txt:TextView
    lateinit var selected:CardView
    lateinit var mainViewModel: MainViewModel
    lateinit var fwdViewModel: ForwardViewModel
    lateinit var send_box:CardView
    lateinit var forward_card:CardView
    lateinit var close_fwd:ImageView
    lateinit var dim:View
    lateinit var searchfwd:SearchView
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
       send_box=findViewById(R.id.send)
       close=findViewById(R.id.close)


       //forwarding

       forward_card=findViewById(R.id.fwd_card)
       close_fwd=findViewById(R.id.close_fwd)
       dim=findViewById(R.id.dim)
       searchfwd=findViewById(R.id.searchview_fwd)
       fwdrecyclerView=findViewById(R.id.forwardrecycler)
       fwdtorecyclerView=findViewById(R.id.forwarded_to)
       selectAll=findViewById(R.id.selectAll)
    //    option=findViewById(R.id.more_option)

        audiocall=findViewById(R.id.call)
       videocall=findViewById(R.id.video_call)

       searched=findViewById(R.id.searched)
       search_txt=findViewById(R.id.search_text)
       up=findViewById(R.id.up_log)
       down=findViewById(R.id.down_log)

       selected=findViewById(R.id.selected)
       select_txt=findViewById(R.id.select_text)
       delete=findViewById(R.id.delete)
       forward=findViewById(R.id.forward)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!

       fwdViewModel=ViewModelProviders.of(this).get(ForwardViewModel::class.java)
       mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel=ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MessageViewModel::class.java)

        back.setOnClickListener {
            onBackPressed()
        }

        audiocall.setOnClickListener {
            startActivity(Intent(this,Outgoing::class.java))
        }

        number=intent.getStringExtra("number")!!
        if(intent.getStringExtra("name")==""){
            name.text=number
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
       }

       delete.setOnClickListener {
           deleteMsg().execute()
           selected.visibility=View.GONE
           send_box.visibility=View.VISIBLE

       }

       forward.setOnClickListener {

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
                   fwdAdapter.notifyDataSetChanged()
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
               }

               override fun delChat(chatEntity: ChatEntity) {
                   selectedChat.remove(chatEntity)
                   fwdtoAdapter.updateList(selectedChat)
               }

           })
           fwdrecyclerView.adapter=fwdAdapter

           ChatViewModel(application).allChats.observe(this, Observer { list->
               list?.let {
                   chatList.clear()
                   chatList.addAll(list)
                   fwdAdapter.updateList(list)
               }
           })

           selected.visibility=View.GONE
       }

       close_fwd.setOnClickListener {
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
            setIconImage(image).execute()
        }

        layoutManager= LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).stackFromEnd=true
        recyclerView.layoutManager=layoutManager
        adapter= MessageAdapter(this,selectedMsg,object:MessageAdapter.OnAdapterItemClickListener{
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


        more.setOnClickListener {
            if(more_card.visibility== View.GONE){
                more_card.visibility=View.VISIBLE
                val animFadein: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.slide_up
                )
                more_card.startAnimation(animFadein)
            }
            else{
                more_card.visibility=View.GONE
            }
        }

        send.setOnClickListener {
            val msg=send_txt.text.toString()
            if(msg!=""){
                send_txt.setText("")
                sendMessageToUser(msg,userid).execute()
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

        Dashboard().checkStatus()
        checkSeen().execute()
        searchElement()
       // deleteMessage()

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
            fwdAdapter.updateList(filteredlist)
        }
        else{
            fwdAdapter.updateList(filteredlist)
        }
    }

    fun searchElement() {

        search.queryHint="Search messages..."
        val searchIcon:ImageView = search.findViewById(R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.WHITE)
        val theTextArea = search.findViewById(R.id.search_src_text) as androidx.appcompat.widget.SearchView.SearchAutoComplete
        theTextArea.setTextColor(Color.WHITE)
        theTextArea.isCursorVisible=false

        search.setOnSearchClickListener {
            back.visibility= View.GONE
            name.visibility=View.GONE
            status.visibility=View.GONE
            audiocall.visibility=View.GONE
            videocall.visibility=View.GONE
            image.visibility=View.GONE
            val params:RelativeLayout.LayoutParams=RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            search.layoutParams=params
        }

        search.setOnCloseListener(object :SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                back.visibility= View.VISIBLE
                name.visibility=View.VISIBLE
                status.visibility=View.VISIBLE
                audiocall.visibility=View.VISIBLE
                videocall.visibility=View.VISIBLE
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


    inner class sendMessageToUser(val msg:String,val userid:String):AsyncTask<Void,Void,Boolean>(){
        override fun doInBackground(vararg params: Void?): Boolean {

            if(msg!=""){
                val ref=FirebaseDatabase.getInstance().reference
                val messageKey=ref.push().key

                firebaseUser=FirebaseAuth.getInstance().currentUser!!

                val sdf = SimpleDateFormat("hh:mm a")
                val tm: Date = Date(System.currentTimeMillis())
               if(application!=null){
                   if(!MessageViewModel(application).isMsgExist(messageKey!!)){
                       MessageViewModel(application).insertMessage(MessageEntity(messageKey,firebaseUser.uid+"-"+userid,firebaseUser.uid,msg,sdf.format(tm),"message",false,false))
                   }
                   ChatViewModel(application).inserChat(ChatEntity(intent.getStringExtra("name")!!,number,intent.getStringExtra("image")!!,msg,sdf.format(tm),userid))
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
            }
            return true
        }
    }

    inner class setIconImage(val image:CircleImageView):AsyncTask<Void,Void,Boolean>(){
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
                            if(MessageViewModel(application).isMsgExist(mid)){
                                MessageViewModel(application).updatetMessage(mid,rec,seen)
                            }
                            adapter.notifyDataSetChanged()
                            if(seen){
                                snap.child(mid).ref.parent!!.removeValue()
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

}