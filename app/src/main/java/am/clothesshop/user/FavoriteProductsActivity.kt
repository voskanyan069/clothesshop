package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.global.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_favorite_products.*
import kotlinx.android.synthetic.main.bottom_menu.*

class FavoriteProductsActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFavoritesRef: DatabaseReference = currentUserRef.child("Favorite Products")

    private var liked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_products)

        lottie_loading_favorite.visibility = View.VISIBLE
        lottie_loading_favorite.playAnimation()
        lottie_loading_favorite.loop(true)

        checkConnection()
        setAccount()
        displayFavoriteClothes()
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

    private fun displayFavoriteClothes() {
        favorite_stores.setOnClickListener {
            startActivity(Intent(this@FavoriteProductsActivity, FollowingStoresActivity::class.java))
        }

        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        list_of_favorite_products.layoutManager = layoutManager

        val options: FirebaseRecyclerOptions<Clothes?> = FirebaseRecyclerOptions.Builder<Clothes>()
            .setQuery(currentUserFavoritesRef, Clothes::class.java)
            .build()

        val adapter: FirebaseRecyclerAdapter<Clothes?, ProductViewHolder> =
            object :
                FirebaseRecyclerAdapter<Clothes?, ProductViewHolder>(options) {

                override fun onBindViewHolder(
                    holder: ProductViewHolder,
                    position: Int,
                    model: Clothes
                ) {

                    holder.productName.text = model.getName()
                    holder.productDescription.text = model.getDescription()
                    holder.productPrice.text = model.getPrice()
                    holder.productStore.text = model.getStore()

                    val likeListener = object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            try {
                                if (snapshot.exists() && snapshot.hasChild(getRef(position).key!!)) {
                                    liked = true
                                    Picasso.get().load(R.drawable.like_fill)
                                        .into(holder.productLikeImage)
                                } else {
                                    Picasso.get().load(R.drawable.like_border)
                                        .into(holder.productLikeImage)
                                }
                            } catch (e: IndexOutOfBoundsException) {
                                Picasso.get().load(R.drawable.like_border).into(holder.productLikeImage)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    }
                    currentUserFavoritesRef.addValueEventListener(likeListener)

                    lottie_loading_favorite.visibility = View.INVISIBLE
                    lottie_loading_favorite.loop(false)
                    lottie_loading_favorite.pauseAnimation()
                    lottie_loading_favorite.cancelAnimation()

                    /*
                    holder.productLikeImage.setOnClickListener {
                        if (liked) {
                            currentUserFavoritesRef.child(getRef(position).key!!).removeValue()
                            liked = false
                            Picasso.get().load(R.drawable.like_border).into(holder.productLikeImage)
                        } else {
                            lottie_like_favorite.visibility = View.VISIBLE
                            lottie_like_favorite.playAnimation()
                            Handler().postDelayed({
                                lottie_like_favorite.pauseAnimation()
                                lottie_like_favorite.cancelAnimation()
                                lottie_like_favorite.visibility = View.INVISIBLE
                            }, lottie_like_favorite.duration)

                            val thisProductMap = hashMapOf<String, String>()

                            thisProductMap.put("name", model.getName())
                            thisProductMap.put("description", model.getDescription())
                            thisProductMap.put("price", model.getPrice())
                            thisProductMap.put("size", model.getSize())
                            thisProductMap.put("image", model.getImage())
                            thisProductMap.put("store", model.getStore())
                            thisProductMap.put("address", model.getAddress())
                            thisProductMap.put("type", model.getType())

                            currentUserFavoritesRef.child(getRef(position).key.toString()).setValue(thisProductMap)
                            currentUserFavoritesRef.child(getRef(position).key.toString()).child("liked").setValue("true")
                            liked = true
                            Picasso.get().load(R.drawable.like_fill).into(holder.productLikeImage)
                        }
                    }
                     */

                    holder.itemView.setOnClickListener {
                        val visitProductKey = getRef(position).key

                        val intent = Intent(this@FavoriteProductsActivity, ThisProductActivity::class.java)
                        intent.putExtra("visitProductKey", visitProductKey)
                        startActivity(intent)
                    }
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ProductViewHolder {
                    val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.products_list_item, parent, false)
                    return ProductViewHolder(view)
                }
            }

        list_of_favorite_products.adapter = adapter
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
        menu_stores.setOnClickListener {
            startActivity(Intent(this, StoresActivity::class.java))
        }
        menu_notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        menu_messenger.setOnClickListener {
            startActivity(Intent(this, MessengerActivity::class.java))
        }
    }
}