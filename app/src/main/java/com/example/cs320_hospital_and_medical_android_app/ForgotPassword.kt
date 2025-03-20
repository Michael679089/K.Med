package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible

class ForgotPassword : AppCompatActivity() {

    private lateinit var  userVerify: LinearLayout
    private lateinit var  securityQuestion: LinearLayout
    private lateinit var  newPassword: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.forgot_password)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.emailVerify)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        userVerify = findViewById(R.id.emailVerify)
        val userVerifyBtn: Button = findViewById(R.id.userVerifyBtn)

        securityQuestion = findViewById(R.id.securityQuestion)
        val answerVerifyBtn: Button = findViewById(R.id.answerVerifyBtn)

        newPassword = findViewById(R.id.newPassword)
        val submitBtn: Button = findViewById(R.id.submitBtn)

        userVerifyBtn.setOnClickListener(){
            changeLayout(securityQuestion)
        }

        answerVerifyBtn.setOnClickListener() {
            changeLayout(newPassword)
        }

        submitBtn.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun changeLayout(stepLayout: LinearLayout) {
        userVerify.visibility = View.GONE
        securityQuestion.visibility = View.GONE
        newPassword.visibility = View.GONE
        stepLayout.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        when {
            securityQuestion.isVisible -> {
                changeLayout(userVerify)

            }
            newPassword.isVisible -> {
                changeLayout(securityQuestion)
            }
            else -> super.onBackPressed()
        }
    }
}
