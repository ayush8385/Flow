package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.ayush.flow.R
import com.ayush.flow.Services.Constants
import com.ayush.flow.Services.SharedPreferenceUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class Register : AppCompatActivity() {
    lateinit var welc: TextView
    lateinit var email: EditText
    lateinit var numb: EditText
    lateinit var pass: EditText
    lateinit var cnf_pass: EditText
    lateinit var register: CircularProgressButton
    lateinit var sign_in: TextView
    lateinit var progressBar: ProgressBar
    lateinit var forgot: TextView
    lateinit var sign_up: TextView
    lateinit var back:ImageView
    lateinit var help:ImageView
    lateinit var sharedPreferences: SharedPreferences
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        welc=findViewById(R.id.create)
        email=findViewById(R.id.email_edt)
        numb=findViewById(R.id.number_edt)
        pass=findViewById(R.id.pass_edt)
        cnf_pass=findViewById(R.id.cnfpass_edt)
        register=findViewById(R.id.register_button)
        sign_in=findViewById(R.id.signin)
        progressBar=findViewById(R.id.progressbar)
        forgot=findViewById(R.id.forgot)
        sign_up=findViewById(R.id.signup)
        back=findViewById(R.id.back)
        help=findViewById(R.id.help)

        SharedPreferenceUtils.init(applicationContext)

        back.setOnClickListener {
            onBackPressed()
        }

        sign_in.setOnClickListener {
            openLogin()
        }
        sign_up.setOnClickListener {
            openSignUp()
        }

        forgot.setOnClickListener {
            openForgot()
        }

        help.setOnClickListener {
            openHelp()
        }

        register.setOnClickListener {
            if(register.text=="Register"){
                registerUser()
            }
            else if(register.text=="reset"){
               resetPass()
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
                        .getBounds().width()-60
                ) {
                    if (pass.transformationMethod != null) {
                        pass.transformationMethod = null
                        pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_baseline_lock_24,0,
                            R.drawable.unlock,0)
                    } else {
                        pass.transformationMethod = PasswordTransformationMethod()
                        pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_baseline_lock_24,0,
                            R.drawable.lock,0)
                    }
                    return@OnTouchListener false
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
                        .getBounds().width()-60
                ) {
                    if (cnf_pass.transformationMethod != null) {
                        cnf_pass.transformationMethod = null
                        cnf_pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_baseline_lock_24,0,
                            R.drawable.unlock,0)
                    } else {
                        cnf_pass.transformationMethod = PasswordTransformationMethod()
                        cnf_pass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_baseline_lock_24,0,
                            R.drawable.lock,0)
                    }
                    return@OnTouchListener false
                }
            }
            false
        })
    }

    private fun openHelp() {

    }

    private fun openForgot() {
        flipButton()
        register.text="reset"
        welc.text="Password Reset"

        val slide: Animation =
            AnimationUtils.loadAnimation(this, android.R.anim.fade_out) //the above transition

        Handler().postDelayed({
            sign_up.visibility=View.VISIBLE
            forgot.visibility=View.INVISIBLE
            pass.visibility=View.GONE
        },60)



    }

    private fun openSignUp() {
        flipButton()
        register.text="Register"
        welc.text="Create Account"

        val slide: Animation =
            AnimationUtils.loadAnimation(this, android.R.anim.fade_in) //the above transition
        numb.startAnimation(slide)
        cnf_pass.startAnimation(slide)
        Handler().postDelayed({
            numb.visibility=View.VISIBLE
            cnf_pass.visibility=View.VISIBLE
            sign_in.visibility=View.VISIBLE
            sign_up.visibility=View.INVISIBLE
            forgot.visibility=View.INVISIBLE
            pass.visibility=View.VISIBLE
            progressBar.visibility=View.GONE
            register.visibility=View.VISIBLE
        },60)

    }

    private fun flipButton() {
        val flip:Animation = AnimationUtils.loadAnimation(this,R.anim.flip)
        register.startAnimation(flip)

    }

    private fun openLogin() {
        flipButton()

        welc.text="Welcome Back"
        register.text="Log in"


        val slide: Animation =
            AnimationUtils.loadAnimation(this, android.R.anim.fade_out) //the above transition
        numb.startAnimation(slide)
        cnf_pass.startAnimation(slide)
        Handler().postDelayed({
            numb.visibility=View.GONE
            cnf_pass.visibility=View.GONE
            sign_in.visibility=View.INVISIBLE
            sign_up.visibility=View.VISIBLE
            forgot.visibility=View.VISIBLE
            pass.visibility= View.VISIBLE
        },400)



    }

    private fun registerUser() {

        val email:String=email.text.trim().toString()
        val num:String=numb.text.trim().toString()
        val pass:String=pass.text.trim().toString()
        val cnf:String=cnf_pass.text.trim().toString()

        if(email==""|| !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(applicationContext, "Enter Proper Email", Toast.LENGTH_SHORT).show()
        }
        else if(pass==""){
            Toast.makeText(applicationContext, "Please Enter Strong Password", Toast.LENGTH_SHORT).show()
        }
        else if(pass.length<=4){
            Toast.makeText(applicationContext, "Password is too short", Toast.LENGTH_SHORT).show()
        }
        else if(pass != cnf){
            Toast.makeText(applicationContext, "Password doesn't match", Toast.LENGTH_SHORT).show()
        }
        else if(num=="" || num.length<10 || (num[0] in '0'..'5')){
            Toast.makeText(applicationContext, "Enter Proper number", Toast.LENGTH_SHORT).show()
        }
        else{
            register.startAnimation()
            sign_in.visibility=View.GONE
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener{ text->
                    try {
                        if(text.isSuccessful){
                            val refuser= FirebaseDatabase.getInstance().reference.child("Users").child(Constants.MY_USERID)

                            val userHashmap=HashMap<String, Any>()
                            userHashmap["uid"]=Constants.MY_USERID
                            userHashmap["email"]=email
                            userHashmap["number"]=num
                            userHashmap["username"]=""
                            userHashmap["password"]=pass
                            userHashmap["about"]="I'm with the Flow"
                            userHashmap["profile_photo"]=""

                            refuser.updateChildren(userHashmap)
                                .addOnCompleteListener { text->
                                    if(text.isSuccessful){
                                        savePreferences(userHashmap)
                                        val intent=Intent(this@Register, Addprofile::class.java)
                                        startActivity(intent)
                                        finishAffinity()
                                    }
                                    else{
                                        Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        else{
                            register.revertAnimation {
                                register.text="Register"
                                register.setBackgroundResource(R.drawable.getstartedbtn_back)
                            }
                            sign_in.visibility=View.VISIBLE
                            Toast.makeText(applicationContext, "Error in registering", Toast.LENGTH_SHORT).show()
                        }
                    }catch (e:Exception){
                        Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun savePreferences(data: HashMap<String,Any>) {
        SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.IS_LOGGED,true)
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_USERID, data["uid"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.EMAIL, data["email"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.NUMBER, data["number"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.PASSWORD, data["password"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.ABOUT,data["about"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,data["username"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_PROFILE_URL,data["profile_photo"].toString())
    }

    private fun loginUser() {
        val pass:String=pass.text.toString()
        val email:String=email.text.toString()
        if(email=="" || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || pass==""){
            Toast.makeText(applicationContext, "Enter Proper Details", Toast.LENGTH_SHORT).show()
        }
        else{
            sign_up.visibility=View.GONE
            forgot.visibility=View.INVISIBLE
            register.startAnimation()

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

                                savePreferences(userHashmap)
                                val intent=Intent(this@Register, Addprofile::class.java)
                                startActivity(intent)
                                finishAffinity()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })


                    }
                    else{
                        Toast.makeText(applicationContext, "Error in login", Toast.LENGTH_SHORT).show()
                        register.revertAnimation {
                            register.text="log in"
                            register.setBackgroundResource(R.drawable.getstartedbtn_back)
                        }
                        sign_up.visibility=View.VISIBLE
                        forgot.visibility=View.VISIBLE
                    }
                }
        }
    }

    private fun resetPass() {
        val email:String=email.text.toString()
        if(email=="" || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(applicationContext, "Enter Proper Email", Toast.LENGTH_SHORT).show()
        }
        else{

            register.startAnimation()
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { text->
                if(text.isSuccessful){
                    Toast.makeText(applicationContext, "Reset link sent", Toast.LENGTH_SHORT).show()
                    register.revertAnimation()
                    numb.visibility=View.GONE
                    cnf_pass.visibility=View.GONE
                    sign_in.visibility=View.GONE
                    pass.visibility=View.VISIBLE
                    sign_up.visibility=View.VISIBLE
                    forgot.visibility=View.VISIBLE
                    welc.text="Welcome Back"

                    register.revertAnimation {
                        register.text="log in"
                        register.setBackgroundResource(R.drawable.getstartedbtn_back)
                    }
                }
                else{
                    Toast.makeText(applicationContext,"This email is not Registered",Toast.LENGTH_LONG).show()
                    register.revertAnimation {
                        register.text="reset"
                        register.setBackgroundResource(R.drawable.getstartedbtn_back)
                    }
                }
            }
        }
    }


}