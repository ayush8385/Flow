package com.ayush.flow.activity

import android.app.IntentService
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.ContactEntity
import com.ayush.flow.database.ContactViewModel
import com.ayush.flow.utils.Constants
import com.ayush.flow.utils.ImageHandling
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class LoadContacts : IntentService(TAG) {
    override fun onHandleIntent(intent: Intent?) {

        val contactMap : MutableMap<String,ContactEntity> = mutableMapOf()

        val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

        //       var contactMap:HashMap<String,String> = HashMap<String,String>()
        var curr_profile_url = ""
        while (contacts?.moveToNext() == true) {
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phoneNumber=phoneNumber.replace("\\s|[(]|[)]|[-]".toRegex(), "")

            if(phoneNumber.length==13){
                phoneNumber=phoneNumber.replace("\\+91".toRegex(),"")
            }

            if (phoneNumber.length==10){
                if(ContactViewModel(application).isContactExist(phoneNumber)){
                    curr_profile_url = ContactViewModel(application).getCurrProfileUrl("",phoneNumber)
                }
                contactMap[phoneNumber]=ContactEntity(name,phoneNumber,"",false,"",curr_profile_url)
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

                    if(userid!= Constants.MY_USERID && contactMap.containsKey(num)/*contactMap.containsKey(num)*/){
//                        val usercon = ContactViewModel(application).getContactByNum(num)

                        val contactObj = contactMap.get(num)
                        contactObj!!.isUser=true
                        contactObj.id=userid
                        contactMap[num]=contactObj

//                        val consEntity = ContactEntity(usercon.name,usercon.number,"",true,userid)
//                        ContactViewModel(application).inserContact(consEntity)
                        if(profile_url!=contactObj.profile_url){
                            contactObj.profile_url=profile_url
//                            ImageHandling(this@LoadContacts).GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,userid,"profile").execute(profile_url)
//                            val bmp = ImageHandling.GetImageFromUrl().execute(profile_url).get()
//                            val selectedPtah = getRealPathFromURI(getImageUri(this@Dashboard,bmp!!))
//                             ImageCompression(this@Dashboard).execute(selectedPtah)
                            ImageHandling(this@LoadContacts).GetUrlImageAndSave("",userid,"profile").execute(profile_url)
                        }
                        if(profile_url==""){
                            val imageName = "/${userid}.jpg"
                            val imagePath = File(this@LoadContacts.filesDir, "profile_images")
                            val newFile = File(imagePath, imageName)
                            if(imagePath.exists()){
                                if(newFile.exists()){
                                    newFile.delete()
                                }
                            }
                        }
                        if(ChatViewModel(application).isUserExist(userid)){
                            ChatViewModel(application).updateName(contactObj.name,userid)
                        }
                        ContactViewModel(application).inserContact(contactObj)
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        for (contact in contactMap){
            ContactViewModel(application).inserContact(contact.value)
        }

    }

    companion object {
        val TAG = LoadContacts::class.java.simpleName
    }
}