package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register : AppCompatActivity() {
    lateinit var welc: TextView
    lateinit var em:TextView
    lateinit var email: EditText
    lateinit var num:TextView
    lateinit var numb: EditText
    lateinit var pas:TextView
    lateinit var pass: EditText
    lateinit var cn_pas:TextView
    lateinit var cnf_pass: EditText
    lateinit var register: Button
    lateinit var sign_in: TextView
    lateinit var progressBar: ProgressBar
    lateinit var forgot: TextView
    lateinit var sign_up: TextView
    lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        welc=findViewById(R.id.create)
        em=findViewById(R.id.email_txt)
        email=findViewById(R.id.email_edt)
        num=findViewById(R.id.number_txt)
        numb=findViewById(R.id.number_edt)
        pas=findViewById(R.id.pass_txt)
        pass=findViewById(R.id.pass_edt)
        cn_pas=findViewById(R.id.cnfpass_txt)
        cnf_pass=findViewById(R.id.cnfpass_edt)
        register=findViewById(R.id.register_button)
        sign_in=findViewById(R.id.signin)
        progressBar=findViewById(R.id.progressbar)
        forgot=findViewById(R.id.forgot)
        sign_up=findViewById(R.id.signup)
        sharedPreferences=getSharedPreferences("Shared Preference", Context.MODE_PRIVATE)

        sign_in.setOnClickListener {
            numb.visibility= View.GONE
            num.visibility=View.GONE
            cnf_pass.visibility= View.GONE
            cn_pas.visibility=View.GONE
            register.text="LOG IN"
            sign_in.visibility= View.GONE
            sign_up.visibility= View.VISIBLE
            forgot.visibility= View.VISIBLE
            pass.visibility= View.VISIBLE
            pas.visibility=View.VISIBLE
            welc.text="Welcome Back"
        }
        sign_up.setOnClickListener {
            numb.visibility=View.VISIBLE
            num.visibility=View.VISIBLE
            cnf_pass.visibility=View.VISIBLE
            cn_pas.visibility=View.VISIBLE
            register.text="Register"
            sign_in.visibility=View.VISIBLE
            sign_up.visibility=View.GONE
            forgot.visibility=View.GONE
            pass.visibility=View.VISIBLE
            pas.visibility=View.VISIBLE
            welc.text="Create Account"
            progressBar.visibility=View.GONE
            register.visibility=View.VISIBLE
        }

        forgot.setOnClickListener {
            numb.visibility=View.GONE
            num.visibility=View.GONE
            cnf_pass.visibility=View.GONE
            cn_pas.visibility=View.GONE
            register.text="reset"
            sign_in.visibility=View.GONE
            sign_up.visibility=View.VISIBLE
            forgot.visibility=View.GONE
            pass.visibility=View.GONE
            pas.visibility=View.GONE
            welc.text="Password Reset"
        }

        register.setOnClickListener {
            if(register.text=="Register"){
                registerUser()
            }
            else if(register.text=="reset"){
               // resetPass()
            }
            else{
              loginUser()
            }
        }

        pass.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= pass.getRight() - pass.getCompoundDrawables().get(DRAWABLE_RIGHT)
                        .getBounds().width()
                ) {
                    if (pass.transformationMethod != null) {
                        pass.transformationMethod = null
                        pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.key,0,
                            R.drawable.unlock,0)
                    } else {
                        pass.transformationMethod = PasswordTransformationMethod()
                        pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.key,0,
                            R.drawable.lock,0)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })

        cnf_pass.setOnTouchListener(View.OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= cnf_pass.getRight() - cnf_pass.getCompoundDrawables().get(DRAWABLE_RIGHT)
                        .getBounds().width()
                ) {
                    if (cnf_pass.transformationMethod != null) {
                        cnf_pass.transformationMethod = null
                        cnf_pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.key,0,
                            R.drawable.unlock,0)
                    } else {
                        cnf_pass.transformationMethod = PasswordTransformationMethod()
                        cnf_pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.key,0,
                            R.drawable.lock,0)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun registerUser() {

        val email:String=email.text.toString()
        val num:String=numb.text.toString()
        val pass:String=pass.text.toString()
        val cnf:String=cnf_pass.text.toString()

        if(email==""){
            Toast.makeText(applicationContext, "Enter Proper Email", Toast.LENGTH_SHORT).show()
        }
        else if(pass==""){
            Toast.makeText(applicationContext, "Please Enter Strong Password", Toast.LENGTH_SHORT).show()
        }
        else if(pass != cnf){
            Toast.makeText(applicationContext, "Password doesn't match", Toast.LENGTH_SHORT).show()
        }
        else if(num=="" || num.length<10 || (num[0] in '0'..'5')){
            Toast.makeText(applicationContext, "Enter Proper number", Toast.LENGTH_SHORT).show()
        }
        else{
            val animation: Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.button_anim)
            register.startAnimation(animation)
            register.visibility= View.GONE
            progressBar.visibility= View.VISIBLE
            register.clearAnimation()
            sign_in.visibility=View.GONE
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener{ text->
                    if(text.isSuccessful){
                        val userId=FirebaseAuth.getInstance().currentUser!!.uid
                        val refuser= FirebaseDatabase.getInstance().reference.child("Users").child(userId)

                        val userHashmap=HashMap<String, Any>()
                        userHashmap["uid"]=userId
                        userHashmap["email"]=email
                        userHashmap["number"]=num
                        userHashmap["username"]=""
                        userHashmap["password"]=pass
                        userHashmap["about"]="I'm with the Flow"
                        userHashmap["profile_photo"]=""

                        refuser.updateChildren(userHashmap)
                            .addOnCompleteListener { text->
                                if(text.isSuccessful){
                                    val intent=Intent(this@Register, Addprofile::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                    savePreferences(userHashmap)
                                }
                                else{
                                    Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    else{
                        register.visibility=View.VISIBLE
                        progressBar.visibility=View.GONE
                        sign_in.visibility=View.VISIBLE
                        Toast.makeText(applicationContext, "Error in registering", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun savePreferences(data: HashMap<String,Any>) {
        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
        sharedPreferences.edit().putString("uid", data["uid"].toString()).apply()
        sharedPreferences.edit().putString("email", data["email"].toString()).apply()
        sharedPreferences.edit().putString("number", data["number"].toString()).apply()
        sharedPreferences.edit().putString("password", data["password"].toString()).apply()
        sharedPreferences.edit().putString("about",data["about"].toString()).apply()
        sharedPreferences.edit().putString("username",data["username"].toString()).apply()
        sharedPreferences.edit().putString("profile",data["profile_photo"].toString()).apply()
    }

    private fun loginUser() {
        val pass:String=pass.text.toString()
        val email:String=email.text.toString()
        sign_up.visibility=View.GONE
        forgot.visibility=View.GONE
        if(email==""){
            Toast.makeText(applicationContext, "Enter Proper Details", Toast.LENGTH_SHORT).show()
        }
        else if(pass==""){
            Toast.makeText(applicationContext, "Enter Proper Details", Toast.LENGTH_SHORT).show()
        }
        else{
            val animation: Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.button_anim)
            register.startAnimation(animation)
            register.visibility= View.GONE
            progressBar.visibility= View.VISIBLE
            register.clearAnimation()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { text->
                    if(text.isSuccessful){

                        val userId=FirebaseAuth.getInstance().currentUser!!.uid
                        val refuser= FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                        refuser.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userHashmap=HashMap<String, Any>()
                                userHashmap["uid"]=userId
                                userHashmap["email"]=snapshot.child("email").value.toString()
                                userHashmap["number"]=snapshot.child("number").value.toString()
                                userHashmap["username"]=snapshot.child("username").value.toString()
                                userHashmap["password"]=snapshot.child("password").value.toString()
                                userHashmap["about"]=snapshot.child("about").value.toString()
                                userHashmap["profile_photo"]=snapshot.child("profile_photo").value.toString()

                                Log.d("profile_photo",userHashmap["profile_photo"].toString())
                                Log.d("about",userHashmap["about"].toString())

                                val intent=Intent(this@Register, Addprofile::class.java)
                                startActivity(intent)
                                finishAffinity()
                                savePreferences(userHashmap)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })


                    }
                    else{
                        Toast.makeText(applicationContext, "Error in login", Toast.LENGTH_SHORT).show()
                        register.visibility=View.VISIBLE
                        sign_up.visibility=View.VISIBLE
                        forgot.visibility=View.VISIBLE
                    }
                }
        }
    }


}