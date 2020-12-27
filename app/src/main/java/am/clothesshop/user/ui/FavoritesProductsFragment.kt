package am.clothesshop.user.ui

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.Clothes
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.global.ProductViewHolder
import am.clothesshop.user.ThisProductActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_favorite_products.*

class FavoritesProductsFragment : Fragment() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = databaseRoot.reference.child("Users")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFavoritesRef: DatabaseReference = currentUserRef.child("Favorite Products")

    private var lottieAnimationView: LottieAnimationView? = null
    private var listOfFavoriteProducts: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_favorites_products, container, false)
        init(root)
        checkConnection()
        displayFavoriteClothes(currentUserFavoritesRef)

        return root
    }

    private fun init(root: View) {
        lottieAnimationView = root.findViewById(R.id.lottie_loading_favorite)
        listOfFavoriteProducts = root.findViewById(R.id.list_of_favorite_products)

        lottieAnimationView?.visibility = View.VISIBLE
        lottieAnimationView?.playAnimation()
        lottieAnimationView?.loop(true)
    }

    private fun displayFavoriteClothes(query: DatabaseReference) {
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this.context, 2)
        listOfFavoriteProducts?.layoutManager = layoutManager

        val options: FirebaseRecyclerOptions<Clothes?> = FirebaseRecyclerOptions.Builder<Clothes>()
            .setQuery(query, Clothes::class.java)
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
                    query.addValueEventListener(likeListener)

                    lottieAnimationView?.visibility = View.INVISIBLE
                    lottieAnimationView?.loop(false)
                    lottieAnimationView?.pauseAnimation()
                    lottieAnimationView?.cancelAnimation()

                    holder.itemView.setOnClickListener {
                        try {
                            val visitProductKey = getRef(position).key

                            val intent = Intent(
                                this@FavoritesProductsFragment.context,
                                ThisProductActivity::class.java
                            )
                            intent.putExtra("visitProductKey", visitProductKey)
                            intent.putExtra("visitProductName", model.getName())
                            startActivity(intent)
                            activity?.overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        } catch (ignored: IndexOutOfBoundsException) {}
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

        listOfFavoriteProducts?.adapter = adapter
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