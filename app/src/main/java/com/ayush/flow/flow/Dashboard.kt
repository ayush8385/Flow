package com.ayush.flow.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.flow.adapter.ChatAdapter
import com.ayush.flow.flow.adapter.StoryAdapter
import com.ayush.flow.flow.model.Chats
import com.google.android.material.bottomnavigation.BottomNavigationView

class Dashboard : AppCompatActivity() {
    lateinit var storyRecyclerView: RecyclerView
    lateinit var chatsRecyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var storyAdapter: StoryAdapter
    lateinit var chatAdapter: ChatAdapter
    lateinit var navigationView: BottomNavigationView
    lateinit var title:TextView
    lateinit var story_text:TextView
    lateinit var chat_text:TextView
    lateinit var add_text:TextView
    var chatList= arrayListOf<Chats>()
    var storyList= arrayListOf<Chats>()
    var controller:LayoutAnimationController?=null
    var previousMenuItem: MenuItem?=null
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

        title.setOnClickListener {
            startActivity(Intent(this,Testing::class.java))
        }

        var obj=Chats("","","Hello how are you doing","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        chatList.add(obj)
        obj=Chats("","","Hi","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        chatList.add(obj)
        obj=Chats("","","Good","","","today","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        chatList.add(obj)
        obj=Chats("","","Lets throw ar party today in backyard of my hosue","","","yesterday","","Papa")
        chatList.add(obj)
        obj=Chats("","","Hello how are you doing","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        chatList.add(obj)
        obj=Chats("","","Hi","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        chatList.add(obj)
        obj=Chats("","","Good","","","today","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        chatList.add(obj)
        obj=Chats("","","Lets throw ar party today in backyard of my hosue","","","yesterday","","Papa")
        chatList.add(obj)
        obj=Chats("","","Hello how are you doing","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        chatList.add(obj)
        obj=Chats("","","Hi","","","04:24 pm","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        chatList.add(obj)
        obj=Chats("","","Good","","","today","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        chatList.add(obj)
        obj=Chats("","","Lets throw ar party today in backyard of my hosue","","","yesterday","","Papa")
        chatList.add(obj)


        chatsRecyclerView.adapter=ChatAdapter(this,chatList)
        chatsRecyclerView.layoutManager=LinearLayoutManager(this)


        var obj1=Chats("","","","","","","","Papa")
        storyList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        storyList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        storyList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        storyList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        storyList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        storyList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        storyList.add(obj1)
        obj1=Chats("","","","","","","","Papa")
        storyList.add(obj1)

        storyRecyclerView.adapter=StoryAdapter(this,storyList)
        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)



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

                        chatsRecyclerView.layoutManager=LinearLayoutManager(this)
                        runAnimation(chatsRecyclerView,1)
                        val adapter=ChatAdapter(this,chatList)
                        adapter.notifyDataSetChanged()
                        chatsRecyclerView.adapter=adapter
                        chatsRecyclerView.layoutAnimation=controller
                        chatsRecyclerView.scheduleLayoutAnimation()



                        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                        runAnimation(storyRecyclerView,1)
                        val adapter2=StoryAdapter(this,storyList)
                        adapter2.notifyDataSetChanged()
                        storyRecyclerView.adapter=adapter2
                        storyRecyclerView.layoutAnimation = controller
                        storyRecyclerView.scheduleLayoutAnimation()
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

                        chatsRecyclerView.layoutManager=LinearLayoutManager(this)
                        runAnimation(chatsRecyclerView,0)

                        var adapter=ChatAdapter(this,storyList)
                        adapter.notifyDataSetChanged()
                        chatsRecyclerView.adapter=adapter
                        chatsRecyclerView.layoutAnimation=controller
                        chatsRecyclerView.scheduleLayoutAnimation()

                        storyRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                        runAnimation(storyRecyclerView,0)
                        val adapter2=StoryAdapter(this,chatList)
                        adapter2.notifyDataSetChanged()
                        storyRecyclerView.adapter=adapter2
                        storyRecyclerView.layoutAnimation=controller
                        storyRecyclerView.scheduleLayoutAnimation()

                        var current = navigationView.selectedItemId
                        it.isCheckable=true
                        it.isChecked=true
                        previousMenuItem=it
                    }
                }
            }
            return@setOnNavigationItemSelectedListener true
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
}