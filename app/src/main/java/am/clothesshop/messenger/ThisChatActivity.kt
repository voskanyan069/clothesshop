package am.clothesshop.messenger

import am.clothesshop.R
import am.clothesshop.user.MessengerActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_this_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ThisChatActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val storesRef: DatabaseReference = root.reference.child("Stores")
    private val messengerRef: DatabaseReference = root.reference.child("Messenger")
    private var thisChatRef: DatabaseReference? = null
    private var thisChatOtherUserRef: DatabaseReference? = null

    private var thisChatKey: String? = null
    private var thisChatName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_this_chat)

        thisChatKey = intent.getStringExtra("visitChatKey").toString()
        thisChatName = intent.getStringExtra("visitChatName").toString()

        toolbarInit()

        thisChatRef = messengerRef.child(mAuth.currentUser?.uid.toString()).child(thisChatKey!!)
        thisChatOtherUserRef = messengerRef.child(thisChatKey!!).child(mAuth.currentUser?.uid.toString())

        thisChatRef!!.child("username").setValue(thisChatName)
        val otherUserAccountListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                thisChatRef!!.child("profileImage").setValue(snapshot.child("image").value)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        storesRef.child(thisChatKey!!).addValueEventListener(otherUserAccountListener)

        thisChatOtherUserRef!!.child("username").setValue(mAuth.currentUser?.displayName.toString())
        thisChatOtherUserRef!!.child("profileImage").setValue(mAuth.currentUser?.photoUrl.toString())

        setHeader()
        sendMessage()
        getMessages()
    }

    private fun toolbarInit() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = thisChatName
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun setHeader() {
//        val chatListener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.child("profileImage").value.toString() != "null") {
//                    Picasso.get().load(snapshot.child("profileImage").value.toString())
//                        .into(chat_header_user_image)
//                }
//                chat_header_user_name.text = snapshot.child("username").value.toString()
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        }
//        thisChatRef?.addValueEventListener(chatListener)
//
//        chat_header_back_button.setOnClickListener {
//            finish()
//        }
    }

    private fun getMessages() {
        list_of_messages.layoutManager = LinearLayoutManager(this)
        val query: DatabaseReference = thisChatRef?.child("Messages")!!

        val options: FirebaseRecyclerOptions<Message?> = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()

        val adapter: FirebaseRecyclerAdapter<Message?, MessagesViewHolder> =
            object :
                FirebaseRecyclerAdapter<Message?, MessagesViewHolder>(options) {

                override fun onBindViewHolder(
                    holder: MessagesViewHolder,
                    position: Int,
                    model: Message
                ) {
                    list_of_messages.smoothScrollToPosition(list_of_messages.adapter?.itemCount?.minus(1)!!)
                    holder.messageUsername.text = model.getMessageUsername()
                    holder.messageText.text = model.getMessageText()
                    holder.messageDate.text = model.getMessageDate()
                    if (model.getMessageImage() != "null") {
                        Picasso.get().load(model.getMessageImage()).into(holder.messageImage)
                    }
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): MessagesViewHolder {
                    val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_list_item, parent, false)
                    return MessagesViewHolder(view)
                }
            }

        list_of_messages.adapter = adapter
        adapter.startListening()

        list_of_messages.scrollToPosition(list_of_messages.adapter!!.itemCount - 1)
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendMessage() {
        send_message_button.setOnClickListener {
            val messageText: String = message_input.text.toString()

            if (messageText.replace(" ", "") != "") {
                val messageHashMap = HashMap<String, String>()
                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
                val currentDate = sdf.format(Date())

                messageHashMap["messageUsername"] = mAuth.currentUser?.displayName.toString()
                messageHashMap["messageText"] = messageText
                messageHashMap["messageDate"] = currentDate
                messageHashMap["messageImage"] = mAuth.currentUser?.photoUrl.toString()

                thisChatRef
                    ?.child("Messages")
                    ?.push()
                    ?.setValue(messageHashMap)
                    ?.addOnCompleteListener {
                        when {
                            it.isSuccessful -> {
                                pushMessageToOtherUser(messageHashMap)
                                scrollToEnd()
                            }
                            it.isCanceled -> Toast.makeText(
                                this,
                                it.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun pushMessageToOtherUser(messageHashMap: HashMap<String, String>) {
        thisChatOtherUserRef
            ?.child("Messages")
            ?.push()
            ?.setValue(messageHashMap)
            ?.addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        message_input.text.clear()
                        scrollToEnd()
                    }
                    it.isCanceled -> Toast.makeText(
                        this,
                        it.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun scrollToEnd() {
        try {
            list_of_messages.scrollToPosition(
                list_of_messages.adapter?.itemCount?.minus(1)!!
            )
        } catch (e: NullPointerException) {
            println("NullPointer -> " + e.message)
        }
    }
}