package com.ayush.flow.flow.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ayush.flow.R

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

        register.setOnClickListener {
            startActivity(Intent(this, Addprofile::class.java))
            finishAffinity()
        }

    }
}