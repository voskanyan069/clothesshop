package am.clothesshop.user

import am.clothesshop.R
import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.account_action_dialog.view.*
import kotlinx.android.synthetic.main.activity_account_settings.*
import java.lang.Exception


class AccountSettingsActivity : AppCompatActivity() {
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private val root = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val profileImageRef = storage.child("profile_image")

    private val currentUserRef = root.child("Users").child(currentUser?.uid.toString())
    private var profileImageUri: Uri? = null
    private val thisProfileImageRef = profileImageRef.child("${currentUser?.uid.toString()}.png")
    private var downloadUrl: String? = null
    private var urlTask: Task<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        checkConnection()
        pickImage()
        accountActionDialog()
        setProfile()
        saveProfile()
        onBack()
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
        if (currentUser?.photoUrl != null) {
            Picasso.get().load(mAuth.currentUser?.photoUrl).into(account_settings_profile_image)
        }
        account_settings_username.setText(currentUser?.displayName)
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

                currentUserRef.removeValue()
                root.child("Messenger").child(currentUser?.uid.toString()).removeValue()
                mAuth.currentUser?.delete()

                dialog.dismiss()
                moveToLogin()
            }
        }
    }

    private fun saveProfile() {
        account_settings_save_button.setOnClickListener {
            checkConnection()
            saveUsername()
            saveProfileImage()
        }
    }

    private fun saveUsername() {
        val newUsername: String = account_settings_username.text.toString()

        if (newUsername.replace(" ", "", true) != "") {
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build()

            currentUser?.updateEmail(newUsername + "@clothesshop.am")
            currentUserRef.child("username").setValue(newUsername)
            currentUser
                ?.updateProfile(userProfileChangeRequest)
                ?.addOnCompleteListener {
                    when {
                        it.isSuccessful -> finish()
                        it.isCanceled -> Toast.makeText(
                            this,
                            it.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveProfileImage() {
        if (urlTask != null) {
            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setPhotoUri(urlTask!!.result)
                .build()

            currentUser
                ?.updateProfile(userProfileChangeRequest)
                ?.addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            currentUserRef.child("profile_image").setValue(downloadUrl)
                            finish()
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

    private fun onBack() {
        account_settings_back_button.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        startActivity(Intent(this, DrawerActivity::class.java))
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

    private fun moveToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}