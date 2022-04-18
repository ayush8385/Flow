package com.ayush.flow.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.ayush.flow.R
import com.ayush.flow.database.ChatViewModel
import com.hanks.passcodeview.PasscodeView

class Passcode : AppCompatActivity() {
    lateinit var passcodeView: PasscodeView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var hiddenViewModel: HiddenViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passcode)

        passcodeView=findViewById(R.id.passcode_view)
        hiddenViewModel=ViewModelProviders.of(this).get(HiddenViewModel::class.java)

        val n=intent.getIntExtra("n",0)
        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        if(sharedPreferences.getString("passcode","")==""){
            val dialog=AlertDialog.Builder(this)
            dialog.setMessage("Set your 4 digit Passcode")
            dialog.setNegativeButton("Ok",object :DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog!!.dismiss()
                    passcodeView.setPasscodeLength(4)
                        .setListener(object :PasscodeView.PasscodeViewListener{
                            override fun onFail() {
                                Toast.makeText(applicationContext,"Wrong",Toast.LENGTH_LONG).show()
                            }
                            override fun onSuccess(number: String?) {
                                sharedPreferences.edit().putString("passcode",number).apply()
                                if(n==0){
                                    ChatViewModel(application).setPrivate(intent.getStringExtra("id")!!,false)
                                }
                                if(n==1){
                                    ChatViewModel(application).setPrivate(intent.getStringExtra("id")!!,true)
                                }
                                finish()

                            }

                        })
                }

            })
            dialog.show()


        }
        else{
            passcodeView.setPasscodeLength(4)
                .setLocalPasscode(sharedPreferences.getString("passcode",""))
                .setListener(object :PasscodeView.PasscodeViewListener{
                    override fun onFail() {
                        Toast.makeText(applicationContext,"Wrong Passcode",Toast.LENGTH_LONG).show()
                    }

                    override fun onSuccess(number: String?) {
                        if(n==0){
                            ChatViewModel(application).setPrivate(intent.getStringExtra("id")!!,false)
                            finish()
                        }
                        if(n==1){
                            ChatViewModel(application).setPrivate(intent.getStringExtra("id")!!,true)
                            finish()
                        }
                        else{
//                            val intent = Intent(this@Passcode,Dashboard::class.java)
//                            intent.putExtra("private",1)
                            hiddenViewModel.setText("1")
//                            startActivity(intent)
//                            finishAffinity()
                            finish()
                        }

                    }

                })
        }


    }
}