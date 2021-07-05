package com.ayush.flow.flow.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayush.flow.R
import com.ayush.flow.flow.adapter.ContactAdapter
import com.ayush.flow.flow.model.Chats

class Contact : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter:ContactAdapter
    var contactList= arrayListOf<Chats>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        layoutManager=LinearLayoutManager(this)
        recyclerView=findViewById(R.id.contact_recycler)

        var obj1=Chats("","","","","","","","Papa")
        contactList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        contactList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        contactList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        contactList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        contactList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        contactList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        contactList.add(obj1)
        obj1=Chats("","","","","","","","Papa")
        contactList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        contactList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        contactList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        contactList.add(obj1)
        obj1=Chats("","","","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Ayush Mishra")
        contactList.add(obj1)
        obj1=Chats("","","Hi","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Brother")
        contactList.add(obj1)
        obj1=Chats("","","Good","","","","https://cdn-res.keymedia.com/cms/images/us/026/0222_637049384911763251.JPG","Mummy")
        contactList.add(obj1)
        obj1=Chats("","","","","","","","Papa")
        contactList.add(obj1)

        recyclerView.adapter=ContactAdapter(this,contactList)
        recyclerView.layoutManager=layoutManager


    }
}