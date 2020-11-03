package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.messenger.Chats
import am.clothesshop.messenger.MessengerViewHolder
import am.clothesshop.messenger.ThisChatActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_messenger.*
import kotlinx.android.synthetic.main.bottom_menu.*

class MessengerActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val messengerRef = root.reference.child("Messenger")
    private var currentUserChatsRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)

        if (mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            currentUserChatsRef = messengerRef.child(mAuth.currentUser!!.uid)
        }

        lottie_loading_messenger.visibility = View.VISIBLE
        lottie_loading_messenger.playAnimation()
        lottie_loading_messenger.loop(true)

        checkConnection()
        displayChats(currentUserChatsRef!!)
        bottomMenu()
    }

    private fun displayChats(query: DatabaseReference) {
        list_of_chats.layoutManager = LinearLayoutManager(this)
        var isExist: Boolean = false

        val existListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isExist = true
                } else {
                    lottie_loading_messenger.visibility = View.INVISIBLE
                    lottie_loading_messenger.loop(false)
                    lottie_loading_messenger.pauseAnimation()
                    lottie_loading_messenger.cancelAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        currentUserChatsRef?.addValueEventListener(existListener)

        if (isExist) {
            val options: FirebaseRecyclerOptions<Chats?> = FirebaseRecyclerOptions.Builder<Chats>()
                .setQuery(query, Chats::class.java)
                .build()

            val adapter: FirebaseRecyclerAdapter<Chats?, MessengerViewHolder> =
                object :
                    FirebaseRecyclerAdapter<Chats?, MessengerViewHolder>(options) {

                    override fun onBindViewHolder(
                        holder: MessengerViewHolder,
                        position: Int,
                        model: Chats
                    ) {

                        holder.chatName.text = model.getUsername()
                        if (model.getProfileImage() != "null") {
                            Picasso.get().load(model.getProfileImage()).into(holder.chatImage)
                        }

                        lottie_loading_messenger.visibility = View.INVISIBLE
                        lottie_loading_messenger.loop(false)
                        lottie_loading_messenger.pauseAnimation()
                        lottie_loading_messenger.cancelAnimation()

                        holder.itemView.setOnClickListener {
                            val visitChatKey = getRef(position).key

                            val intent =
                                Intent(this@MessengerActivity, ThisChatActivity::class.java)
                            intent.putExtra("visitChatKey", visitChatKey)
                            startActivity(intent)
                        }
                    }

                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): MessengerViewHolder {
                        val view: View = LayoutInflater.from(parent.context)
                            .inflate(R.layout.chats_list_item, parent, false)
                        return MessengerViewHolder(view)
                    }
                }

            list_of_chats.adapter = adapter
            adapter.startListening()
        }
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun bottomMenu() {
        menu_products.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        menu_stores.setOnClickListener {
            startActivity(Intent(this, StoresActivity::class.java))
        }
        menu_notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        menu_favorite.setOnClickListener {
            startActivity(Intent(this, FavoriteProductsActivity::class.java))
        }
    }
}