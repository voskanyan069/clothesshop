package am.clothesshop.user

import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_this_product.*
import kotlinx.android.synthetic.main.bottom_menu.*

class ThisProductActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val clothesRef: DatabaseReference = root.reference.child("Clothes")
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserFavoritesRef: DatabaseReference = currentUserRef.child("Favorite Products")
    private var thisProductRef: DatabaseReference? = null

    private var liked = false
    private val thisProductMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_this_product)

        val thisProductKey = intent.getStringExtra("visitProductKey")
        val thisProductName = intent.getStringExtra("visitProductName")

        checkConnection()
        toolbarInit(thisProductName.toString())

        thisProductRef = clothesRef.child(thisProductKey.toString())

        if (thisProductKey != null) {
            getProductInfo(thisProductKey)
            like(thisProductKey)
        }
    }

    private fun toolbarInit(thisProductName: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = thisProductName
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun getProductInfo(thisProductKey: String) {
        checkConnection()

        val productListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("Users").child(mAuth.currentUser?.uid.toString()).child("Favorite Products").hasChild(thisProductKey)) {
                    liked = true
                    Picasso.get().load(R.drawable.like_fill).into(this_product_like)
                } else {
                    Picasso.get().load(R.drawable.like_border).into(this_product_like)
                }

                val productName = dataSnapshot.child("Clothes").child(thisProductKey).child("name").value.toString()
                val productDescription = dataSnapshot.child("Clothes").child(thisProductKey).child("description").value.toString()
                val productPrice = dataSnapshot.child("Clothes").child(thisProductKey).child("price").value.toString()
                val productSize = dataSnapshot.child("Clothes").child(thisProductKey).child("size").value.toString()
                val productImage = dataSnapshot.child("Clothes").child(thisProductKey).child("image").value.toString()
                val productStore = dataSnapshot.child("Clothes").child(thisProductKey).child("store").value.toString()
                val productAddress = dataSnapshot.child("Clothes").child(thisProductKey).child("address").value.toString()
                val productType = dataSnapshot.child("Clothes").child(thisProductKey).child("type").value.toString()

                thisProductMap["name"] = productName
                thisProductMap["description"] = productDescription
                thisProductMap["price"] = productPrice
                thisProductMap["size"] = productSize
                thisProductMap["image"] = productImage
                thisProductMap["store"] = productStore
                thisProductMap["address"] = productAddress
                thisProductMap["type"] = productType

//                this_product_name.text = productName
                this_product_description.text = productDescription
                this_product_price_size.text = "$productPrice | $productSize"
                this_product_type.text = productType
                this_product_store.text = "$productStore | $productAddress"
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        root.reference.addValueEventListener(productListener)
    }

    private fun like(thisProductKey: String) {
        this_product_like.setOnClickListener {
            checkConnection()

            if (liked) {
                liked = false
                currentUserFavoritesRef.child(thisProductKey).removeValue()
                Picasso.get().load(R.drawable.like_border).into(this_product_like)
            } else {
                lottie_like_this_product.visibility = View.VISIBLE
                lottie_like_this_product.playAnimation()
                Handler().postDelayed({
                    lottie_like_this_product.pauseAnimation()
                    lottie_like_this_product.cancelAnimation()
                    lottie_like_this_product.visibility = View.INVISIBLE
                }, lottie_like_this_product.duration)

                liked = true
                currentUserFavoritesRef.child(thisProductKey).setValue(thisProductMap)
                currentUserFavoritesRef.child(thisProductKey).child("liked").setValue("true")
                Picasso.get().load(R.drawable.like_fill).into(this_product_like)
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}