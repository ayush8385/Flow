package com.ayush.flow.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ayush.flow.R
import com.ayush.flow.utils.Constants
import com.ayush.flow.utils.SharedPreferenceUtils
import com.ayush.flow.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Register : AppCompatActivity() {

    lateinit var binding:ActivityRegisterBinding
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        SharedPreferenceUtils.init(applicationContext)

        moveOtpNum()

        getFullScreenViewBack()

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.help.setOnClickListener {
            openHelp()
        }

        binding.registerButton.setOnClickListener {

            if(binding.registerButton.text.toString().toLowerCase()=="get otp"){
                sendCode()
            }
            else{
                verifyOtp()
            }

        }

        binding.resendCodeBtn.setOnClickListener {
            binding.resendCodeBtn.isEnabled=false
           resendVerificationCode("+91"+binding.numberEdt.text.toString(), resendToken!!)
        }

    }

    private fun getFullScreenViewBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val decorView: View = window.decorView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun sendCode() {
        if (!TextUtils.isEmpty(binding.numberEdt.getText().toString())) {
            if(binding.numberEdt.text.toString().trim().length==10){

                binding.registerButton.startAnimation()

                val phone = "+91" + binding.numberEdt.getText().toString()
                sendVerificationCode(phone)
            }
            else{
                Toast.makeText(
                    this,
                    "Please enter a valid phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Enter a phone number",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openHelp() {

    }

    fun savePreferences(data: HashMap<String,Any>) {
        SharedPreferenceUtils.setBooleanPreference(SharedPreferenceUtils.IS_LOGGED,true)
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_USERID, data["uid"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.NUMBER, data["number"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.ABOUT,data["about"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_NAME,data["username"].toString())
        SharedPreferenceUtils.setStringPreference(SharedPreferenceUtils.MY_PROFILE_URL,data["profile_photo"].toString())
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(OnCompleteListener<AuthResult?> { task ->
                try {
                    if(task.isSuccessful){
                        val refuser= FirebaseDatabase.getInstance().reference.child("Users")
                        refuser.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userHashmap=HashMap<String, Any>()
                                if(!snapshot.hasChild(Constants.MY_USERID)){
                                    userHashmap["uid"]= Constants.MY_USERID
                                    userHashmap["number"]=binding.numberEdt.text.toString()
                                    userHashmap["username"]=""
                                    userHashmap["about"]="I'm with the Flow"
                                    userHashmap["profile_photo"]=""
                                }
                                else{
                                    userHashmap["uid"]= Constants.MY_USERID
                                    userHashmap["number"]=snapshot.child(Constants.MY_USERID).child("number").value.toString()
                                    userHashmap["username"]=snapshot.child(Constants.MY_USERID).child("username").value.toString()
                                    userHashmap["about"]=snapshot.child(Constants.MY_USERID).child("about").value.toString()
                                    userHashmap["profile_photo"]=snapshot.child(Constants.MY_USERID).child("profile_photo").value.toString()

                                }

                                refuser.child(Constants.MY_USERID).updateChildren(userHashmap)
                                    .addOnCompleteListener { text->
                                        if(text.isSuccessful){
                                            savePreferences(userHashmap)
                                            val intent=Intent(this@Register, Addprofile::class.java)
                                            startActivity(intent)
                                            finishAffinity()
                                        }
                                        else{
                                            Toast.makeText(this@Register, "Unexpected Error", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                    else{
                        binding.registerButton.revertAnimation{
                            binding.registerButton.text="Verify"
                            binding.registerButton.setBackgroundResource(R.drawable.getstartedbtn_back)
                        }
                        Toast.makeText(
                            this,
                            task.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }catch (e:Exception){
                    binding.registerButton.revertAnimation{
                        binding.registerButton.text="Verify"
                        binding.registerButton.setBackgroundResource(R.drawable.getstartedbtn_back)
                    }
                    Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_LONG).show()
                }
            })
    }


    private fun sendVerificationCode(number: String) {
        // this method is used for getting
        // OTP on user phone number.
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack)
            .setForceResendingToken(token)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var verificationId: String? = null
    private var resendToken:ForceResendingToken?=null

    // callback method is called on Phone auth provider.
    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
                resendToken = forceResendingToken

                binding.create.text="Verification Code"
                binding.otpDesc.text=coloredText("We have sent 6-Digit Code to your Mobille number ","+91-${binding.numberEdt.text.toString()}",R.color.black)

                binding.imageStat.setImageResource(R.drawable.otp_verify)
                binding.numberBox.visibility=View.GONE
                binding.otpBox.visibility=View.VISIBLE
                binding.registerButton.revertAnimation {
                    binding.registerButton.text="Verify"
                    binding.registerButton.setBackgroundResource(R.drawable.getstartedbtn_back)
                }
                binding.resendDetail.visibility=View.VISIBLE

                setTimer()
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode

                if (code != null) {
//                    otpBox.setText(code)
                    val codeChar = code.toCharArray()
                    binding.et1.setText(code[0].toString())
                    binding.et2.setText(code[1].toString())
                    binding.et3.setText(code[2].toString())
                    binding.et4.setText(code[3].toString())
                    binding.et5.setText(code[4].toString())
                    binding.et6.setText(code[5].toString())

                    verifyOtp()
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.registerButton.revertAnimation{
                    binding.registerButton.text="Get Otp"
                    binding.registerButton.setBackgroundResource(R.drawable.getstartedbtn_back)
                }
                Toast.makeText(this@Register, e.message, Toast.LENGTH_LONG).show()
            }
        }

    // below method is use to verify code from Firebase.
    private fun verifyCode(code: String) {
        // below line is used for getting
        // credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential)
    }

    fun coloredText(
        baseText: String,
        coloredText: String,
        targetColor: Int
    ): SpannableStringBuilder {
        val transformText = "$baseText $coloredText"
        return SpannableStringBuilder(transformText).apply {
            setSpan(
                ForegroundColorSpan(targetColor),
                transformText.indexOf(coloredText),
                (transformText.indexOf(coloredText) + coloredText.length),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                transformText.indexOf(coloredText),
                (transformText.indexOf(coloredText) + coloredText.length),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun moveOtpNum() {
        binding.et1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.et2.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
//                TODO("Not yet implemented")
            }

        })

        binding.et2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.et3.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.isEmpty()){
                    binding.et1.requestFocus()
                }
            }

        })

        binding.et3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.et4.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.length==0){
                    binding.et2.requestFocus()
                }
            }

        })

        binding.et4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.et5.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.length==0){
                    binding.et3.requestFocus()
                }
            }

        })

        binding.et5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.et6.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.length==0){
                    binding.et4.requestFocus()
                }
            }

        })

        binding.et6.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.toString().isEmpty()){
                    binding.root.hideKeyboard()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.length==0){
                    binding.et5.requestFocus()
                }
            }

        })
    }

    fun View.hideKeyboard() {
        val imm = ContextCompat.getSystemService(applicationContext,InputMethodManager::class.java) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun verifyOtp() {
        if(!binding.et1.text.isEmpty() && !binding.et2.text.isEmpty() && !binding.et3.text.isEmpty() && !binding.et4.text.isEmpty() && !binding.et5.text.isEmpty() && !binding.et6.text.isEmpty()){
            val otpCode=binding.et1.text.toString()+binding.et2.text+binding.et3.text+binding.et4.text+binding.et5.text+binding.et6.text
            binding.registerButton.startAnimation()
            verifyCode(otpCode)
        }
        else{
            Toast.makeText(applicationContext,"Please Enter OTP",Toast.LENGTH_SHORT).show()
        }
    }

    fun setTimer() {
        binding.resendCodeBtn.isEnabled=false
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainedSecs = millisUntilFinished / 1000
                binding.resendCodeBtn.setText("" + remainedSecs / 60 + ":" + remainedSecs % 60)
            }

            override fun onFinish() {
                binding.resendCodeBtn.isEnabled=true
                binding.resendCodeBtn.setText("Resend Code")
                binding.resendCodeBtn.setOnClickListener(View.OnClickListener { setTimer() })
            }
        }.start()
    }

}