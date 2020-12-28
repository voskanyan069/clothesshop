package am.clothesshop.admin

import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_shop_registration.*

class ShopRegistrationActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val storesRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Stores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_registration)

        checkConnection()
        createNewShop()
        moveToLogin()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            changeActivityClear(NoConnectionActivity::class.java)
        }
    }

    private fun createNewShop() {
        shop_registration_sign_in_button.setOnClickListener {
            checkConnection()

            val email: String = shop_registration_email.text.toString()
            val name: String = shop_registration_name.text.toString()
            val description: String = shop_registration_description.text.toString()
            val address: String = shop_registration_address.text.toString()
            val password: String = shop_registration_password.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the shop name",
                        Toast.LENGTH_LONG
                    ).show()
                }
                description.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the shop description",
                        Toast.LENGTH_LONG
                    ).show()
                }
                address.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the shop address",
                        Toast.LENGTH_LONG
                    ).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the shop password",
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

                                val hashMap = HashMap<String, String>()

                                hashMap["name"] = name
                                hashMap["description"] = description
                                hashMap["address"] = address
                                hashMap["followers"] = "0"
                                hashMap["image"] = "null"

                                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()
                                mAuth
                                    .currentUser
                                    ?.updateProfile(userProfileChangeRequest)

                                storesRef.child(mAuth.currentUser?.uid.toString()).setValue(hashMap)
                                changeActivityClear(AdminHomeActivity::class.java)
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

    private fun changeActivityClear(to: Class<*>) {
        val intent = Intent(this, to)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun moveToLogin() {
        shop_already_have_an_account.setOnClickListener {
            startActivity(Intent(this, ShopLoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}