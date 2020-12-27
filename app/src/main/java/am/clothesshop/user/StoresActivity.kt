package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.global.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_stores.*
//import kotlinx.android.synthetic.main.activity_stores.search_button
import kotlinx.android.synthetic.main.activity_stores.search_bar
import kotlinx.android.synthetic.main.bottom_menu.*

class StoresActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = root.reference.child("Users")
    private val storesRef: DatabaseReference = root.reference.child("Stores")
    private val currentUserRef: DatabaseReference = usersRef.child(mAuth.currentUser?.uid.toString())
    private val currentUserSearchRef: DatabaseReference = currentUserRef.child("Stores Search")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stores)

        lottie_loading_stores.visibility = View.VISIBLE
        lottie_loading_stores.playAnimation()
        lottie_loading_stores.loop(true)

        checkConnection()
        displayStores(storesRef)
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

    private fun displayStores(query: DatabaseReference) {
        list_of_stores.layoutManager = LinearLayoutManager(this)

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

                    lottie_loading_stores.visibility = View.INVISIBLE
                    lottie_loading_stores.loop(false)
                    lottie_loading_stores.pauseAnimation()
                    lottie_loading_stores.cancelAnimation()

                    holder.itemView.setOnClickListener {
                        val visitStoreKey = getRef(position).key

                        val intent = Intent(this@StoresActivity, ThisStoreActivity::class.java)
                        intent.putExtra("visitStoreKey", visitStoreKey)
                        startActivity(intent)
                    }
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

        list_of_stores.adapter = adapter
        adapter.startListening()
    }

    private fun filter(searchText: String) {
        checkConnection()

        val storesListener = object : ValueEventListener {
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
        val query: Query = storesRef.orderByKey()
        query.addListenerForSingleValueEvent(storesListener)

        displayStores(currentUserSearchRef)
    }

    private fun check(item: DataSnapshot, searchText: String) {
        if (
            item.child("name").value.toString().toLowerCase().contains(searchText.toLowerCase()) ||
            item.child("description").value.toString().toLowerCase().contains(searchText.toLowerCase()) ||
            item.child("address").value.toString().toLowerCase().contains(searchText.toLowerCase())
        ) {
            setItem(item)
        }
    }

    private fun setItem(item: DataSnapshot) {
        val hashMap = hashMapOf<String, String>()

        val storeName = item.child("name").value.toString()
        val storeDescription = item.child("description").value.toString()
        val storeImage = item.child("image").value.toString()
        val storeFollowers = item.child("followers").value.toString()
        val storeAddress = item.child("address").value.toString()

        hashMap["name"] = storeName
        hashMap["description"] = storeDescription
        hashMap["image"] = storeImage
        hashMap["followers"] = storeFollowers
        hashMap["address"] = storeAddress

        currentUserSearchRef.child(item.key!!).setValue(hashMap)
    }

    private fun search() {
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

    private fun searchAction(searchText: String) {
        checkConnection()
        currentUserSearchRef.removeValue()
        filter(searchText)
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
        menu_favorite.setOnClickListener {
            startActivity(Intent(this, FavoriteProductsActivity::class.java))
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}