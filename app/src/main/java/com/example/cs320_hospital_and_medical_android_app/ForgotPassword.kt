package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.forgotpw_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailVerifyBtn: Button = findViewById(R.id.verifyBtn_1)

        emailVerifyBtn.setOnClickListener(){
            setContentView(R.layout.forgotpw_question)

            val questionVerifyBtn: Button = findViewById(R.id.verifyBtn_2)

            questionVerifyBtn.setOnClickListener(){
                setContentView(R.layout.forgotpw_password)

                val submitBtn: Button = findViewById(R.id.submitBtn)

                submitBtn.setOnClickListener(){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    }
}