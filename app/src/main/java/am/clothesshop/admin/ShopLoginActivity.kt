package am.clothesshop.admin

import am.clothesshop.global.CheckConnection
import am.clothesshop.global.NoConnectionActivity
import am.clothesshop.R
import am.clothesshop.user.LoginActivity
import am.clothesshop.user.ProductsActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_shop_login.*

class ShopLoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_login)

        checkConnection()
        login()
        activityListener()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            changeActivityClear(NoConnectionActivity::class.java)
        }
    }

    private fun login() {
        shop_login_login_button.setOnClickListener {
            checkConnection()

            val email: String = shop_login_email.text.toString()
            val password: String = shop_login_password.text.toString()

            when {
                email.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please enter the shop email",
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
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Successfully logged in",
                                    Toast.LENGTH_LONG
                                ).show()
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

    private fun changeActivity(to: Class<*>) {
        startActivity(Intent(this, to))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun activityListener() {
        login_user_account.setOnClickListener {
            changeActivity(LoginActivity::class.java)
        }
        register_new_shop.setOnClickListener {
            changeActivity(ShopRegistrationActivity::class.java)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}