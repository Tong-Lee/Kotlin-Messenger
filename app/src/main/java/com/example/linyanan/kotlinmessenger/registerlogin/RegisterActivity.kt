package com.example.linyanan.kotlinmessenger.registerlogin

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.linyanan.kotlinmessenger.message.LatestMessageActivity
import com.example.linyanan.kotlinmessenger.R
import com.example.linyanan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button.setOnClickListener {
            performregister()


        }
        already_have_account_textview.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        select_photo_btn.setOnClickListener {
            Log.d("RegisterActivity", "select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectphoto: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectphoto = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectphoto)
            selectphoto_circle.setImageBitmap(bitmap)
            select_photo_btn.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_btn.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performregister() {
        if (selectphoto == null) {
            Toast.makeText(this, "please have photo", Toast.LENGTH_SHORT).show()
            return
        }
        val email = e_mail_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        Log.d("RegisterActivity", "email is " + email)
        Log.d("RegisterActivity", "password " + password)
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter word ", Toast.LENGTH_SHORT).show()
            return
        }


        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)

        builder.setView(dialogView)
        builder.setCancelable(false)

        val dialog = builder.create()

//        toChange AlertDialog Background

//      dialog.getWindow().setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent)




        dialog.show()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    uploadImagesToFirebaseStorage()
                    register_button.visibility = View.GONE

                    Log.d("RegisterActivity", "uid   ${it.result.user.uid}")


                }.addOnFailureListener {
                    //                    val progressDialog =ProgressDialog(this)
//                    progressDialog.setMessage("Please wait for seconds")
//                    progressDialog.setCancelable(false)
//                    progressDialog.show()

                    val message = it.message
                    if (message != null) {
                        Handler().postDelayed({ dialog.dismiss() }, 2000)
                        Log.d("RegisterActivity", "Fail to create   ${it.message} ")
                        Toast.makeText(this, "Fail to create   ${it.message} ", Toast.LENGTH_SHORT).show()
                    }


                }

    }

    private fun uploadImagesToFirebaseStorage() {

//        if (selectphoto == null) return
        if (selectphoto != null) {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectphoto!!)

                    .addOnSuccessListener {
                        Log.d("RegisterActivity", "upload image${it.metadata?.path}")
                        ref.downloadUrl.addOnSuccessListener {
                            Log.d("RegisterActivity", "url  $it")

                            saveUserToFirebaseDatabase(it.toString())
                        }
                    }
                    .addOnFailureListener {

                    }

        } else {

        }


    }

    private fun saveUserToFirebaseDatabase(imageUriFile: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val username = username_edittext_register.text.toString()
        val user = User(uid, username_edittext_register.text.toString(), imageUriFile)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Finally save")

                    val intent = Intent(this, LatestMessageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Finally not save${it.message}")

                }

    }
}

