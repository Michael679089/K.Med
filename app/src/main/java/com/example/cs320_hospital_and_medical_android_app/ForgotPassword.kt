package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        val usernameInput: EditText = findViewById(R.id.usernameInput)
        val usernameState: TextView = findViewById(R.id.usernameState)

        securityQuestion = findViewById(R.id.securityQuestion)
        val answerVerifyBtn: Button = findViewById(R.id.answerVerifyBtn)
        val securityQuestionText: TextView = findViewById(R.id.securityQuestionText)
        val answerInput: EditText = findViewById(R.id.answerInput)
        val answerValue = "Red"
        val questionState: TextView = findViewById(R.id.questionState)

        newPassword = findViewById(R.id.newPassword)
        val submitBtn: Button = findViewById(R.id.submitBtn)

        userVerifyBtn.setOnClickListener(){
            val username = usernameInput.text.toString()

            if(username.isEmpty()){
                usernameState.text = "Please input a valid username"
                usernameState.setTextColor(Color.parseColor("#ff0000"))
            } else {
                usernameState.text = null
                securityQuestionText.text = "What is your favorite color?"
                changeLayout(securityQuestion)
            }
        }

        answerVerifyBtn.setOnClickListener() {
            val answer = answerInput.text.toString()

            if (answer == answerValue) {
                usernameState.text = null
                changeLayout(newPassword)
            } else {
                questionState.text = "Incorrect answer"
                questionState.setTextColor(Color.parseColor("#ff0000"))
            }

            if (answer.isEmpty()) {
                questionState.text = "Please input an answer"
                questionState.setTextColor(Color.parseColor("#ff0000"))
            }

        }

        submitBtn.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
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
