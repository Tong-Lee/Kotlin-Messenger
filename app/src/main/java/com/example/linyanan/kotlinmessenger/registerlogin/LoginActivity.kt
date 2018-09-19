package com.example.linyanan.kotlinmessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.linyanan.kotlinmessenger.R
import com.example.linyanan.kotlinmessenger.message.LatestMessageActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            val email = login_email_edittext.text.toString()
            val password = login_password_edittext.text.toString()

            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                Log.d("aaaaaaaaaa","why${it.message}")
                Toast.makeText(this, "Because ${it.message}", Toast.LENGTH_SHORT).show()

            }
        }

        backtoregister_textView.setOnClickListener {
            finish()
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
        }
    }

}