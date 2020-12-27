package am.clothesshop.user.ui

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.messenger.Chats
import am.clothesshop.messenger.MessengerViewHolder
import am.clothesshop.messenger.ThisChatActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class MessengerFragment : Fragment() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val messengerRef = databaseRoot.reference.child("Messenger")
    private var currentUserChatsRef: DatabaseReference = messengerRef.child(mAuth.currentUser!!.uid)

    private var lottieAnimationView: LottieAnimationView? = null
    private var listOfChats: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_messenger, container, false)
        init(root)
        checkConnection()
        displayChats(currentUserChatsRef)

        return root
    }

    private fun init(root: View) {
        lottieAnimationView = root.findViewById(R.id.lottie_loading_messenger)
        listOfChats = root.findViewById(R.id.list_of_chats)

        lottieAnimationView?.visibility = View.VISIBLE
        lottieAnimationView?.playAnimation()
        lottieAnimationView?.loop(true)
    }

    private fun displayChats(query: DatabaseReference) {
        listOfChats?.layoutManager = LinearLayoutManager(this.context)

        val options: FirebaseRecyclerOptions<Chats> = FirebaseRecyclerOptions.Builder<Chats>()
            .setQuery(query, Chats::class.java)
            .build()

        val adapter: FirebaseRecyclerAdapter<Chats, MessengerViewHolder> =
            object :
                FirebaseRecyclerAdapter<Chats, MessengerViewHolder>(options) {

                override fun onBindViewHolder(
                    holder: MessengerViewHolder,
                    position: Int,
                    model: Chats
                ) {

                    holder.chatName.text = model.getUsername()
                    if (model.getProfileImage() != "null") {
                        Picasso.get().load(model.getProfileImage()).into(holder.chatImage)
                    }

                    lottieAnimationView?.visibility = View.INVISIBLE
                    lottieAnimationView?.loop(false)
                    lottieAnimationView?.pauseAnimation()
                    lottieAnimationView?.cancelAnimation()

                    holder.itemView.setOnClickListener {
                        val visitChatKey = getRef(position).key

                        val intent = Intent(this@MessengerFragment.context, ThisChatActivity::class.java)
                        intent.putExtra("visitChatKey", visitChatKey)
                        intent.putExtra("visitChatName", model.getUsername())
                        startActivity(intent)
                        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

        listOfChats?.adapter = adapter
        adapter.startListening()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this.requireContext()) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this.context, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}