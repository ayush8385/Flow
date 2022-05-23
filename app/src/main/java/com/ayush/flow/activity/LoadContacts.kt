package com.ayush.flow.activity

import android.app.IntentService
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.ImageHandling
import com.ayush.flow.database.ChatViewModel
import com.ayush.flow.database.ContactEntity
import com.ayush.flow.database.ContactViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File

class LoadContacts : IntentService(TAG) {
    override fun onHandleIntent(intent: Intent?) {

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
                ContactViewModel(application).inserContact(ContactEntity(name,phoneNumber,"",false,""))
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

                    if(userid!= Constants.MY_USERID && ContactViewModel(application).isContactExist(num)/*contactMap.containsKey(num)*/){
                        val usercon = ContactViewModel(application).getContactByNum(num)

                        val consEntity = ContactEntity(usercon.name,usercon.number,"",true,userid)
                        ContactViewModel(application).inserContact(consEntity)

                        if(profile_url!=""){
                            ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,userid+".jpg").execute(profile_url)
//                            val bmp = ImageHandling.GetImageFromUrl().execute(profile_url).get()
//                            val selectedPtah = getRealPathFromURI(getImageUri(this@Dashboard,bmp!!))
                            // ImageCompression(this@Dashboard).execute(selectedPtah)
                            //ImageHandling.GetUrlImageAndSave(Constants.ALL_PHOTO_LOCATION,userid+".jpg").execute(profile_url)
                        }
                        else{
                            val f = File(
                                File(
                                    Environment.getExternalStorageDirectory(),
                                    Constants.ALL_PHOTO_LOCATION),userid+".jpg")
                            if(f.exists()){
                                f.delete()
                            }
                        }
                        if(ChatViewModel(application).isUserExist(userid)){
                            ChatViewModel(application).updateName(usercon.name,userid)
                        }
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val TAG = LoadContacts::class.java.simpleName
    }
}