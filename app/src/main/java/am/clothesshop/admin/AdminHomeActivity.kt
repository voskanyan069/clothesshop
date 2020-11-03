package am.clothesshop.admin

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.user.NotificationsActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_admin_home.*
import kotlinx.android.synthetic.main.admin_bottom_menu.*

class AdminHomeActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val root: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val storesRef: DatabaseReference = root.reference.child("Stores")
    private val currentStoreRef: DatabaseReference = storesRef.child(mAuth.currentUser?.uid.toString())
    private val currentStoreSearchRef: DatabaseReference = currentStoreRef.child("Search")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        checkConnection()
        search()
        bottomMenu()
    }

    private fun search() {
        search_bar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchAction(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchAction(searchText: String) {
        checkConnection()
        currentStoreSearchRef.removeValue()
        filter(searchText)
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

//        displayStores(currentUserSearchRef)
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

        currentStoreSearchRef.child(item.key!!).setValue(hashMap)
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun bottomMenu() {
        admin_menu_add.setOnClickListener {
            startActivity(Intent(this, AdminAddClothActivity::class.java))
        }
        admin_menu_messenger.setOnClickListener {
            startActivity(Intent(this, AdminMessengerActivity::class.java))
        }
        admin_menu_notification.setOnClickListener {
            startActivity(Intent(this, AdminNotificationsActivity::class.java))
        }
        admin_menu_account.setOnClickListener {
            startActivity(Intent(this, AdminAccountSettingsActivity::class.java))
        }
    }
}