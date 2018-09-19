package com.example.linyanan.kotlinmessenger.message

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.linyanan.kotlinmessenger.R
import com.example.linyanan.kotlinmessenger.model.ChatLog
import com.example.linyanan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.abc_tooltip.view.*
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.notification_template_lines_media.*
import kotlin.coroutines.experimental.coroutineContext

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
//        val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        recycleview_chatlog.adapter = adapter
        recycleview_chatlog.setOnTouchListener { view, motionEvent ->
            hideKeyboard(view)
            true
        }
//        setDummyData()
        LatestMessageActivity
        listenForMessages()
        button_chatlog.setOnClickListener {
            if (edittext_chatlog.text.isEmpty()) {
                Toast.makeText(this, "Because you do not type the word....", Toast.LENGTH_SHORT).show()
                hideKeyboard(it)
                return@setOnClickListener
            }

            performmessage()
            hideKeyboard(it)

        }


    }


    private fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)


    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val reference = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                Log.d("ccccccccc", "bb ${p0}")
                val message = p0.getValue(ChatLog::class.java) ?: return
                Log.d("ccccc", "${message.toId}")


                ////very important true right then false left


                Log.d(TAG, message?.text)
                if (message?.text != null) {
                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        Log.d("ChatLogActivityeeeeee", "eeeee$")

                        val currentUser = LatestMessageActivity.currentUser ?: return
                        Log.d("ChatLogActivity", "eeeee$currentUser")
                        adapter.add(ChatFromItem(message.text, currentUser))
                        Log.d("ChatLogActivityaaaa", "eeeee${message.text}")


                    } else {
                        adapter.add(ChatToItem(message.text, toUser!!))

                    }
                    recycleview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })


    }


    private fun performmessage() {
        val text = edittext_chatlog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val data = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = data.uid
        val saveTime = System.currentTimeMillis()
        val reference = FirebaseDatabase.getInstance().getReference("/users-messages/$fromId/$toId").push()
        val toreference = FirebaseDatabase.getInstance().getReference("/users-messages/$toId/$fromId").push()


        val id = reference.key


        if (fromId == null) return


        val message = ChatLog(id!!, text, fromId!!, toId, saveTime)
        reference.setValue(message)
                .addOnSuccessListener {
                    Log.d(TAG, "we are success ${reference.key}")
                    edittext_chatlog.text.clear()

                }

        ////why???????????
        toreference.setValue(message)

        val latestreference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestreference.setValue(message)
        val latesttoreference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latesttoreference.setValue(message)
    }

//    private fun setDummyData() {
//        val adapter = GroupAdapter<ViewHolder>()
//        adapter.add(ChatFromItem(""))
//        adapter.add(ChatToItem(""))
//        adapter.add(ChatFromItem(""))
//        adapter.add(ChatToItem(""))
//
//        recycleview_chatlog.adapter = adapter
//
//
//    }

    class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textview_from.text = text
            val targetView = viewHolder.itemView.chat_from_row_imageview
            val uri = user.imageUriFile
            Picasso.get().load(uri).into(targetView)
        }
    }

    class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textview_to.text = text
            val targetView = viewHolder.itemView.chat_to_row_imageview
            val uri = user.imageUriFile
            Picasso.get().load(uri).into(targetView)
        }
    }
}
