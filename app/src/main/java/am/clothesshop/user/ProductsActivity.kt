package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.admin.AdminHomeActivity
import am.clothesshop.global.*
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_products.*
import kotlinx.android.synthetic.main.activity_products.search_bar
import kotlinx.android.synthetic.main.bottom_menu.*
import kotlinx.android.synthetic.main.search_price_dialog.view.*
import java.lang.IndexOutOfBoundsException


class ProductsActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val clothesRef: DatabaseReference = root.reference.child("Clothes")
    private var currentUserRef: DatabaseReference? = null
    private var currentUserSearchRef: DatabaseReference? = null
    private var currentUserFavoritesRef: DatabaseReference? = null

    private var priceMin: String = 0.toString()
    private var priceMax: String = 999999999.toString()
    private var liked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        if (mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            currentUserRef = usersRef.child(mAuth.currentUser?.uid.toString())
            currentUserSearchRef = currentUserRef!!.child("Products Search")
            currentUserFavoritesRef = currentUserRef?.child("Favorite Products")
        }

        lottie_loading_product.visibility = View.VISIBLE
        lottie_loading_product.playAnimation()
        lottie_loading_product.loop(true)

        checkConnection()
        isAccountAdmin()
        displayClothes(clothesRef)
        search()
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

    private fun isAccountAdmin() {
        val adminListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (
                    !snapshot.child("Users").child(mAuth.currentUser?.uid.toString()).exists() &&
                    snapshot.child("Stores").child(mAuth.currentUser?.uid.toString()).exists()
                ) {
                    startActivity(Intent(this@ProductsActivity, AdminHomeActivity::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        root.reference.addValueEventListener(adminListener)
    }

    private fun displayClothes(query: DatabaseReference) {
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        list_of_products.layoutManager = layoutManager

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

                    lottie_loading_product.visibility = View.INVISIBLE
                    lottie_loading_product.loop(false)
                    lottie_loading_product.pauseAnimation()
                    lottie_loading_product.cancelAnimation()

                    holder.itemView.setOnClickListener {
                        val visitProductKey = getRef(position).key

                        val intent = Intent(this@ProductsActivity, ThisProductActivity::class.java)
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

        list_of_products.adapter = adapter
        adapter.startListening()
    }

    private fun filter(searchText: String) {
        checkConnection()

        val productListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val snapshotIterator: Iterable<DataSnapshot> =
                    dataSnapshot.children
                val iterator: Iterator<DataSnapshot> =
                    snapshotIterator.iterator()

                while (iterator.hasNext()) {
                    val item = iterator.next()
                    check(item, searchText)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        val query: Query = clothesRef.orderByKey()
        query.addListenerForSingleValueEvent(productListener)

        displayClothes(currentUserSearchRef!!)
    }

    private fun check(item: DataSnapshot, searchText: String) {
        if (
            item.child("name").value.toString().toLowerCase().contains(searchText.toLowerCase()) ||
            item.child("description").value.toString().toLowerCase().contains(searchText.toLowerCase()) ||
            item.child("store").value.toString().toLowerCase().contains(searchText.toLowerCase())
        ) {
            setItem(item)
        }
    }

    private fun setItem(item: DataSnapshot) {
        val hashMap = hashMapOf<String, String>()

        val productName = item.child("name").value.toString()
        val productDescription = item.child("description").value.toString()
        val productImage = item.child("image").value.toString()
        val productStore = item.child("store").value.toString()
        val productAddress = item.child("address").value.toString()
        val productPrice = item.child("price").value.toString()
        val productSize = item.child("size").value.toString()
        val productType = item.child("type").value.toString()

        hashMap["name"] = productName
        hashMap["description"] = productDescription
        hashMap["image"] = productImage
        hashMap["store"] = productStore
        hashMap["address"] = productAddress
        hashMap["price"] = productPrice
        hashMap["size"] = productSize
        hashMap["type"] = productType

        currentUserSearchRef?.child(item.key!!)?.setValue(hashMap)
    }

    private fun search() {
//        search_price.setOnClickListener {
//            priceAction()
//        }
//        search_button.setOnClickListener {
//            searchAction()
//        }
        search_bar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchAction(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun priceAction() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.search_price_dialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val dialog = builder.show()

        dialogView.dialog_min_price.text = Editable.Factory.getInstance().newEditable(priceMin)
        dialogView.dialog_max_price.text = Editable.Factory.getInstance().newEditable(priceMax)

        dialogView.set_price_dialog.setOnClickListener {
            priceMin = dialogView.dialog_min_price.text.toString()
            priceMax = dialogView.dialog_max_price.text.toString()

            if (priceMin == null.toString() || priceMin == "") {
                priceMin = 0.toString()
            }
            if (priceMax == null.toString() || priceMax == "") {
                priceMax = 999999999.toString()
            }

            try {
                if (priceMin.toInt() > priceMax.toInt()) {
                    Toast.makeText(
                        this,
                        "Please enter the correct price",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    "Please enter the correct price",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            dialog.dismiss()

            currentUserSearchRef?.removeValue()
//            filter(s = s)
        }

        dialogView.cancel_price_dialog.setOnClickListener {
            priceMin = 0.toString()
            priceMax = 999999999.toString()

            dialog.dismiss()

            Toast.makeText(
                this,
                "Canceled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun searchAction(searchText: String) {
        checkConnection()
        currentUserSearchRef?.removeValue()
        filter(searchText)
    }

    private fun bottomMenu() {
        menu_stores.setOnClickListener {
            startActivity(Intent(this, StoresActivity::class.java))
        }
        menu_notifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }
        menu_messenger.setOnClickListener {
            startActivity(Intent(this, MessengerActivity::class.java))
        }
        menu_favorite.setOnClickListener {
            startActivity(Intent(this, FavoriteProductsActivity::class.java))
        }
    }
}