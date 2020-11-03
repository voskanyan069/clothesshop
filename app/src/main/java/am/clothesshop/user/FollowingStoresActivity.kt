package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.global.*
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
import kotlinx.android.synthetic.main.activity_favorite_products.account_settings
import kotlinx.android.synthetic.main.activity_favorite_products.favorite_account_mail
import kotlinx.android.synthetic.main.activity_following_stores.*
import kotlinx.android.synthetic.main.bottom_menu.*

class FollowingStoresActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFollowingRef: DatabaseReference = currentUserRef.child("Following Stores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following_stores)

        lottie_loading_following.visibility = View.VISIBLE
        lottie_loading_following.playAnimation()
        lottie_loading_following.loop(true)

        checkConnection()
        setAccount()
        displayFollowingStores()
        bottomMenu()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun displayFollowingStores() {

        favorite_products.setOnClickListener {
            startActivity(Intent(this@FollowingStoresActivity, FavoriteProductsActivity::class.java))
        }

        list_of_favorite_stores.layoutManager = LinearLayoutManager(this)

        val options: FirebaseRecyclerOptions<Stores?> = FirebaseRecyclerOptions.Builder<Stores>()
            .setQuery(currentUserFollowingRef, Stores::class.java)
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
                        val visitStoreKey = getRef(position).key

                        val intent = Intent(this@FollowingStoresActivity, ThisStoreActivity::class.java)
                        intent.putExtra("visitStoreKey", visitStoreKey)
                        startActivity(intent)
                    }

                    lottie_loading_following.visibility = View.INVISIBLE
                    lottie_loading_following.loop(false)
                    lottie_loading_following.pauseAnimation()
                    lottie_loading_following.cancelAnimation()
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

        list_of_favorite_stores.adapter = adapter
        adapter.startListening()
    }

    private fun setAccount() {
        favorite_account_mail.text = mAuth.currentUser?.displayName.toString()

        account_settings.setOnClickListener {
            accountSettings()
        }
    }

    private fun accountSettings() {
        startActivity(Intent(this, AccountSettingsActivity::class.java))
    }

    private fun bottomMenu() {
        menu_products.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        menu_messenger.setOnClickListener {
            startActivity(Intent(this, MessengerActivity::class.java))
        }
        menu_notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        menu_stores.setOnClickListener {
            startActivity(Intent(this, StoresActivity::class.java))
        }
    }
}