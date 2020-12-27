package am.clothesshop.user.ui

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.global.Stores
import am.clothesshop.global.StoresViewHolder
import am.clothesshop.user.ThisStoreActivity
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_following_stores.*
import java.lang.IndexOutOfBoundsException

class FollowingStoresFragment : Fragment() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = databaseRoot.reference.child("Users")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFollowingRef: DatabaseReference = currentUserRef.child("Following Stores")

    private var lottieAnimationView: LottieAnimationView? = null
    private var listOfFollowingStores: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_following_stores, container, false)
        init(root)
        checkConnection()
        displayFollowingStores(currentUserFollowingRef)

        return root
    }

    private fun init(root: View) {
        lottieAnimationView = root.findViewById(R.id.lottie_loading_following)
        listOfFollowingStores = root.findViewById(R.id.list_of_following_stores)

        lottieAnimationView?.visibility = View.VISIBLE
        lottieAnimationView?.playAnimation()
        lottieAnimationView?.loop(true)
    }

    private fun displayFollowingStores(query: DatabaseReference) {
        listOfFollowingStores?.layoutManager = LinearLayoutManager(this.context)

        val options: FirebaseRecyclerOptions<Stores?> = FirebaseRecyclerOptions.Builder<Stores>()
            .setQuery(query, Stores::class.java)
            .build()

        val adapter: FirebaseRecyclerAdapter<Stores?, StoresViewHolder> =
            object :
                FirebaseRecyclerAdapter<Stores?, StoresViewHolder>(options) {

                override fun onBindViewHolder(
                    holder: StoresViewHolder,
                    position: Int,
                    model: Stores
                ) {

                    holder.storeName.text = model.getName()
                    holder.storeAddress.text = model.getAddress()

                    holder.itemView.setOnClickListener {
                        try {
                            val visitStoreKey = getRef(position).key

                            val intent = Intent(
                                this@FollowingStoresFragment.context,
                                ThisStoreActivity::class.java
                            )
                            intent.putExtra("visitStoreKey", visitStoreKey)
                            intent.putExtra("visitStoreName", model.getName())
                            startActivity(intent)
                            activity?.overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        } catch (ignored: IndexOutOfBoundsException) {}
                    }

                    lottieAnimationView?.visibility = View.INVISIBLE
                    lottieAnimationView?.loop(false)
                    lottieAnimationView?.pauseAnimation()
                    lottieAnimationView?.cancelAnimation()
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): StoresViewHolder {
                    val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.stores_list_item, parent, false)
                    return StoresViewHolder(view)
                }
            }

        listOfFollowingStores?.adapter = adapter
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