package am.clothesshop.admin

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.user.NotificationsActivity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.gabrielbb.cutout.CutOut
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_admin_add_cloth.*
import kotlinx.android.synthetic.main.admin_bottom_menu.*


class AdminAddClothActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private val currentStoreUid = mAuth.currentUser?.uid

    private val root = FirebaseDatabase.getInstance().reference
    private val clothesRef = root.child("Clothes")
    private val currentStoreRef = root.child("Stores").child(currentStoreUid!!)

    private val storage = FirebaseStorage.getInstance().reference
    private val storeImageRef = storage.child("cloth_image/${currentStoreUid}")

    private var clothImageUri: Uri? = null
    private val thisClothImageRef = storeImageRef.child("${System.currentTimeMillis()}.png")
    private var downloadUrl: String? = null
    private var urlTask: Task<Uri>? = null

    private var storeName: String? = null
    private var storeAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_cloth)

        checkConnection()
        setSpinners()
        getStore()
        pickImage()
        publishProduct()
        bottomMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE.toInt()) {
            when (resultCode) {
                RESULT_OK -> {
                    clothImageUri = CutOut.getUri(data)
                    Picasso.get().load(clothImageUri).into(add_cloth_image)
                    add_cloth_image.setPadding(0, 0, 0, 0)
                    Toast.makeText(this, "Image was successfully uploaded", Toast.LENGTH_SHORT).show()
                }
                CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE.toInt() -> {
                    Toast.makeText(this, CutOut.getError(data).toString(), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    add_cloth_image.setPadding(50, 50, 50, 50)
                }
            }
        }
    }

    private fun publishProduct() {
        add_cloth_add_button.setOnClickListener {
            checkConnection()
            val pushMap = HashMap<String, String>()
            publishImage()

            if (
                add_cloth_name.text != null &&
                add_cloth_description.text != null &&
                add_cloth_price.text != null &&
                downloadUrl != null
            ) {
                pushMap["image"] = downloadUrl.toString()
                pushMap["name"] = add_cloth_name.text.toString()
                pushMap["description"] = add_cloth_description.text.toString()
                pushMap["price"] = add_cloth_price.text.toString()
                pushMap["type"] = add_cloth_type.selectedItem.toString()
                pushMap["size"] = add_cloth_size.selectedItem.toString()
                pushMap["store"] = storeName.toString()
                pushMap["address"] = storeAddress.toString()

                currentStoreRef.child("Clothes").push().setValue(pushMap)
                clothesRef.push().setValue(pushMap)
                Toast.makeText(this, "Cloth are successfully published", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        add_cloth_image.setOnClickListener {
//            CropImage.activity().setAspectRatio(1, 1).start(this)
            CutOut.activity().intro().start(this)
        }
    }

    private fun publishImage() {
        val uploadTask = thisClothImageRef.putFile(clothImageUri!!)

        uploadTask
            .addOnSuccessListener {
                urlTask = thisClothImageRef.downloadUrl
                while (!urlTask!!.isSuccessful);
                downloadUrl = urlTask!!.result.toString()

                Picasso.get().load(downloadUrl).into(add_cloth_image)
                add_cloth_image.setPadding(0, 0, 0, 0)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCanceledListener {
                Toast.makeText(this, "Image uploading canceled", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getStore() {
        val storeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storeName = snapshot.child("name").value.toString()
                storeAddress = snapshot.child("address").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        currentStoreRef.addValueEventListener(storeListener)
    }

    private fun setSpinners() {
        ArrayAdapter.createFromResource(
            this,
            R.array.type_array,
            R.layout.spinner_list_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_list_item)
            add_cloth_type.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.size_array,
            R.layout.spinner_list_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_list_item)
            add_cloth_size.adapter = adapter
        }
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
        admin_menu_products.setOnClickListener {
            startActivity(Intent(this, AdminHomeActivity::class.java))
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