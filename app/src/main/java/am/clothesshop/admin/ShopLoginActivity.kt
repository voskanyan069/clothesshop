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
        moveToUserLogin()
        moveToRegister()
    }

    private fun checkConnection() {
        if (!CheckConnection().isNetworkAvailable(this) &&
            !CheckConnection().isInternetAvailable()) {
            val intent = Intent(this, NoConnectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun login() {
        shop_login_login_button.setOnClickListener {
            checkConnection()

            val email: String = shop_login_email.text.toString()
            val password: String = shop_login_password.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter the shop email",
                    Toast.LENGTH_LONG
                ).show()
            } else if (password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter the shop password",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Successfully logged in",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(Intent(this, AdminHomeActivity::class.java))
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

    private fun moveToUserLogin() {
        login_user_account.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun moveToRegister() {
        register_new_shop.setOnClickListener {
            startActivity(Intent(this, ShopRegistrationActivity::class.java))
        }
    }
}