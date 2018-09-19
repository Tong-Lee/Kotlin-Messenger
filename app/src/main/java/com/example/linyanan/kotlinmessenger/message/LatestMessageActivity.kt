package com.example.linyanan.kotlinmessenger.message

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.linyanan.kotlinmessenger.R
import com.example.linyanan.kotlinmessenger.R.layout.latest_recycleview_row
import com.example.linyanan.kotlinmessenger.model.ChatLog
import com.example.linyanan.kotlinmessenger.model.User
import com.example.linyanan.kotlinmessenger.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_recycleview_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class LatestMessageActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)
        latest_recycleview.adapter = adapter
        latest_recycleview.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)
           val row = item as LatestRecycleRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartneUser)
            startActivity(intent)

        }

//        setDummyRow()
        listenForMessage()
        fetchCurrentUser()


        verifyUserIsLoggin()
    }

    ////layoutManger very important
    class LatestRecycleRow(val message: ChatLog) : Item<ViewHolder>() {
        var chatPartneUser :User? =null
        override fun getLayout(): Int {
            return R.layout.latest_recycleview_row
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latest_message.text = message.text

            val chatPartenId: String
            if (message.fromId == FirebaseAuth.getInstance().uid) {
                chatPartenId = message.toId
            } else {
                chatPartenId = message.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartenId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartneUser = p0.getValue(User::class.java)
                    viewHolder.itemView.latest_username.text = chatPartneUser?.username
                    val targerIamgeView=viewHolder.itemView.latest_imageview
                    Picasso.get().load(chatPartneUser?.imageUriFile).into(targerIamgeView)
                }
            })
        }
    }

    private fun reresh() {
        adapter.clear()
        latestHashMap.values.forEach {
            adapter.add(LatestRecycleRow(it))
        }
    }

    val latestHashMap = HashMap<String, ChatLog>()

    private fun listenForMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(ChatLog::class.java) ?: return
                Log.d("cccccccc", "is ${message.fromId},${message.toId}")
                latestHashMap[p0.key!!] = message
                reresh()
//                var a = latestHashMap.getValue(p0.key!!)
//                 a=message
//                latestHashMap[p0.key!!]=message


            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(ChatLog::class.java) ?: return
                Log.d("cccccccc", "is ${message.fromId},${message.toId}")
                latestHashMap[p0.key!!] = message
                reresh()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
        })
    }

    val adapter = GroupAdapter<ViewHolder>()


    private fun setDummyRow() {
//        adapter.add(LatestRecycleRow())


    }


    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        Log.d("eeeeeeeeee", "eeee$uid")

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessageActivity", "CurrentUser ${currentUser?.imageUriFile}")
            }

        })
    }

    private fun verifyUserIsLoggin() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

