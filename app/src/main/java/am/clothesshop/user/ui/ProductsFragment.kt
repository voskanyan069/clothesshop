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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_products.*


class ProductsFragment : Fragment() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val databaseRoot: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = databaseRoot.reference.child("Users")
    private val clothesRef: DatabaseReference = databaseRoot.reference.child("Clothes")
    private var currentUserRef: DatabaseReference? = null
    private var currentUserFavoritesRef: DatabaseReference? = null

    private var lottieAnimationView: LottieAnimationView? = null
    private var listOfProducts: RecyclerView? = null

    private var liked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_products, container, false)
        init(root)
        checkConnection()
        displayClothes(clothesRef)

        return root
    }

    private fun init(root: View) {
        lottieAnimationView = root.findViewById(R.id.lottie_loading_product)
        listOfProducts = root.findViewById(R.id.list_of_products)

        if (mAuth.currentUser != null) {
            currentUserRef = usersRef.child(mAuth.currentUser?.uid.toString())
            currentUserFavoritesRef = currentUserRef?.child("Favorite Products")
        }

        lottieAnimationView?.visibility = View.VISIBLE
        lottieAnimationView?.loop(true)
        lottieAnimationView?.playAnimation()
    }

    private fun displayClothes(query: DatabaseReference) {
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this.context, 2)
        listOfProducts?.layoutManager = layoutManager

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
                                if (snapshot.hasChild(getRef(position).key.toString())) {
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
                    currentUserFavoritesRef?.addValueEventListener(likeListener)

                    lottieAnimationView?.visibility = View.INVISIBLE
                    lottieAnimationView?.loop(false)
                    lottieAnimationView?.pauseAnimation()
                    lottieAnimationView?.cancelAnimation()

                    holder.itemView.setOnClickListener {
                        val visitProductKey = getRef(position).key

                        val intent = Intent(
                            this@ProductsFragment.context,
                            ThisProductActivity::class.java
                        )
                        intent.putExtra("visitProductKey", visitProductKey)
                        startActivity(intent)
                        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

        listOfProducts?.adapter = adapter
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