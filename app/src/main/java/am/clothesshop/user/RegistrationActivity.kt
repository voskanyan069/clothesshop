package am.clothesshop.user

import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.io.File

class RegistrationActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        checkConnection()
        createNewUser()
        moveToLogin()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
//            val intent = Intent(this, NoConnectionActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
            changeActivityClear(NoConnectionActivity::class.java)
        }
    }

    private fun createNewUser() {
        registration_sign_in_button.setOnClickListener {
            checkConnection()

            val displayName: String = registration_name.text.toString()
            val email: String = "$displayName@clothesshop.am"
            val password: String = registration_password.text.toString()

            when {
                displayName.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the account name",
                        Toast.LENGTH_LONG
                    ).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the account password",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Account successfully created",
                                    Toast.LENGTH_LONG
                                ).show()

                                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build()

                                mAuth
                                    .currentUser
                                    ?.updateProfile(userProfileChangeRequest)

                                val currentUserRef = usersRef
                                    .child(mAuth.currentUser?.uid.toString())

                                val hashMap = HashMap<String, String>()

                                hashMap["profile_image"] = "null"
                                hashMap["username"] = displayName
                                hashMap["uid"] = mAuth.currentUser?.uid.toString()

                                currentUserRef.setValue(hashMap)

                                changeActivityClear(DrawerActivity::class.java)
        //                            val intent = Intent(this, DrawerActivity::class.java)
        //                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //                            startActivity(intent)
        //                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error: ${it.exception.toString()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun moveToLogin() {
        already_have_an_account.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun changeActivityClear(to: Class<*>) {
        val intent = Intent(this, to)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}