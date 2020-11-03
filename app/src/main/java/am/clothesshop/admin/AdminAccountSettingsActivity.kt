package am.clothesshop.admin

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.user.NotificationsActivity
import am.clothesshop.user.LoginActivity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.account_action_dialog.view.*
import kotlinx.android.synthetic.main.activity_admin_account_settings.*
import kotlinx.android.synthetic.main.admin_bottom_menu.*
import java.lang.Exception

class AdminAccountSettingsActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private val currentStore = mAuth.currentUser
    private val root = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val profileImageRef = storage.child("profile_image")

    private val currentStoreRef = root.child("Stores").child(currentStore?.uid.toString())
    private var profileImageUri: Uri? = null
    private val thisProfileImageRef = profileImageRef.child("${currentStore?.uid.toString()}.png")
    private var downloadUrl: String? = null
    private var urlTask: Task<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_account_settings)

        checkConnection()
        pickImage()
        setProfile()
        saveProfile()
        accountActionDialog()
        bottomMenu()
    }

    private fun pickImage() {
        account_settings_profile_image.setOnClickListener {
            CropImage.activity().setAspectRatio(1,1).start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            try {
                val resultUri: CropImage.ActivityResult = CropImage.getActivityResult(data)
                profileImageUri = resultUri.uri
                val uploadTask = thisProfileImageRef.putFile(profileImageUri!!)

                uploadTask
                    .addOnSuccessListener {
                        Toast.makeText(this, "Image was successfully uploaded", Toast.LENGTH_SHORT).show()
                        urlTask = thisProfileImageRef.downloadUrl
                        while (!urlTask!!.isSuccessful);
                        downloadUrl = urlTask!!.result.toString()

                        Picasso.get().load(downloadUrl).into(account_settings_profile_image)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun setProfile() {
        if (currentStore?.photoUrl != null) {
            Picasso.get().load(mAuth.currentUser?.photoUrl).into(account_settings_profile_image)
        }
        val accountListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                account_settings_username.setText(snapshot.child("name").value.toString())
                account_settings_description.setText(snapshot.child("description").value.toString())
                account_settings_address.setText(snapshot.child("address").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        currentStoreRef.addValueEventListener(accountListener)
    }

    private fun saveProfile() {
        account_settings_save_button.setOnClickListener {
            checkConnection()
            saveUsername()
            saveDescription()
            saveAddress()
            saveProfileImage()
        }
    }

    private fun saveProfileImage() {
        if (urlTask != null) {
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setPhotoUri(urlTask!!.result)
                .build()

            currentStore
                ?.updateProfile(userProfileChangeRequest)
                ?.addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            currentStoreRef.child("profile_image").setValue(downloadUrl)
                        }
                        it.isCanceled -> Toast.makeText(
                            this,
                            it.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveUsername() {
        val newUsername: String = account_settings_username.text.toString()

        if (newUsername.replace(" ", "", true) != "") {
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build()

            currentStore?.updateEmail(newUsername + "@clothesshop.am")
            currentStore?.updateProfile(userProfileChangeRequest)
            currentStoreRef.child("name").setValue(newUsername)
        }
    }

    private fun saveDescription() {
        val newDescription: String = account_settings_description.text.toString()

        if (newDescription.replace(" ", "", true) != "") {
            currentStoreRef.child("description").setValue(newDescription)
        }
    }

    private fun saveAddress() {
        val newAddress: String = account_settings_description.text.toString()

        if (newAddress.replace(" ", "", true) != "") {
            currentStoreRef.child("address").setValue(newAddress)
        }
    }

    private fun accountActionDialog() {
        account_settings_menu.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.account_action_dialog, null)
            val builder = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
            val dialog = builder.show()

            dialogView.cancel_account_action_dialog.setOnClickListener {
                Toast.makeText(
                    this,
                    "Canceled",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            dialogView.logout_account_action_dialog.setOnClickListener {
                checkConnection()

                Toast.makeText(
                    this,
                    "Logged Out",
                    Toast.LENGTH_SHORT
                ).show()

                mAuth.signOut()
                moveToLogin()

                dialog.dismiss()
            }
            dialogView.delete_account_action_dialog.setOnClickListener {
                checkConnection()

                Toast.makeText(
                    this,
                    "Account has been deleted",
                    Toast.LENGTH_SHORT
                ).show()

                currentStoreRef.removeValue()
                root.child("Messenger").child(currentStore?.uid.toString()).removeValue()
                mAuth.currentUser?.delete()

                dialog.dismiss()
                moveToLogin()
            }
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

    private fun moveToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun bottomMenu() {
        admin_menu_products.setOnClickListener {
            startActivity(Intent(this, AdminHomeActivity::class.java))
        }
        admin_menu_add.setOnClickListener {
            startActivity(Intent(this, AdminAddClothActivity::class.java))
        }
        admin_menu_notification.setOnClickListener {
            startActivity(Intent(this, AdminNotificationsActivity::class.java))
        }
        admin_menu_messenger.setOnClickListener {
            startActivity(Intent(this, AdminMessengerActivity::class.java))
        }
    }
}