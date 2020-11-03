package am.clothesshop.user

import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_this_store.*
import kotlinx.android.synthetic.main.bottom_menu.*

class ThisStoreActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val storesRef: DatabaseReference = root.reference.child("Stores")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFavoritesRef: DatabaseReference = currentUserRef.child("Following Stores")
    private var thisStoreRef: DatabaseReference? = null

    private var liked = false
    private var followers: Int? = null
    private val thisStoreMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_this_store)

        val thisStoreKey = intent.getStringExtra("visitStoreKey")
        thisStoreRef = storesRef.child(thisStoreKey.toString())

        if (thisStoreKey != null) {
            getStoreInfo(thisStoreKey)
            follow(thisStoreKey)
        }

        checkConnection()
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

    private fun getStoreInfo(thisStoreKey: String) {
        checkConnection()

        val storeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("Users").child(mAuth.currentUser!!.uid).child("Following Stores").hasChild(thisStoreKey)) {
                    liked = true
                    Picasso.get().load(R.drawable.like_fill).into(this_store_follow)
                } else {
                    Picasso.get().load(R.drawable.like_border).into(this_store_follow)
                }

                val storeName = dataSnapshot.child("Stores").child(thisStoreKey).child("name").value.toString()
                val storeDescription = dataSnapshot.child("Stores").child(thisStoreKey).child("description").value.toString()
                val storeImage = dataSnapshot.child("Stores").child(thisStoreKey).child("image").value.toString()
                val storeAddress = dataSnapshot.child("Stores").child(thisStoreKey).child("address").value.toString()
                val storeFollowers = dataSnapshot.child("Stores").child(thisStoreKey).child("followers").value.toString()

                followers = storeFollowers.toInt()

                thisStoreMap["name"] = storeName
                thisStoreMap["description"] = storeDescription
                thisStoreMap["image"] = storeImage
                thisStoreMap["address"] = storeAddress
                thisStoreMap["followers"] = storeFollowers

                this_store_name.text = storeName
                this_store_description.text = storeDescription
                this_store_address.text = storeAddress
                this_store_followers.text = storeFollowers
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        root.reference.addValueEventListener(storeListener)
    }

    private fun follow(thisStoreKey: String) {
        this_store_follow.setOnClickListener {
            checkConnection()

            if (liked) {
                currentUserFavoritesRef.child(thisStoreKey).removeValue()
                liked = false
                Picasso.get().load(R.drawable.like_border).into(this_store_follow)
                followers = followers?.minus(1)
                this_store_followers.text = followers.toString()
                Toast.makeText(this, "You cancel following this store", Toast.LENGTH_SHORT).show()
            } else {
                lottie_like_this_store.visibility = View.VISIBLE
                lottie_like_this_store.playAnimation()
                Handler().postDelayed({
                    lottie_like_this_store.pauseAnimation()
                    lottie_like_this_store.cancelAnimation()
                    lottie_like_this_store.visibility = View.INVISIBLE
                }, lottie_like_this_store.duration)

                val followedUser = HashMap<String, String>()

                currentUserFavoritesRef.child(thisStoreKey).setValue(thisStoreMap)
                currentUserFavoritesRef.child(thisStoreKey).child("liked").setValue("true")

                thisStoreRef
                    ?.child("Follower")
                    ?.push()
                    ?.child("username")
                    ?.setValue(mAuth.currentUser?.displayName.toString())
                liked = true
                Picasso.get().load(R.drawable.like_fill).into(this_store_follow)
                followers = followers?.plus(1)
                currentUserFavoritesRef.child(thisStoreKey).child("followers").setValue(followers.toString())
                this_store_followers.text = followers.toString()
                Toast.makeText(this, "You start following this store", Toast.LENGTH_SHORT).show()
            }
            thisStoreRef?.child("followers")?.setValue(followers.toString())
        }
    }

    private fun bottomMenu() {
        menu_products.setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        menu_stores.setOnClickListener {
            startActivity(Intent(this, StoresActivity::class.java))
        }
        menu_messenger.setOnClickListener {
            startActivity(Intent(this, MessengerActivity::class.java))
        }
        menu_favorite.setOnClickListener {
            startActivity(Intent(this, FavoriteProductsActivity::class.java))
        }
    }
}