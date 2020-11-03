package am.clothesshop.global

import am.clothesshop.R
import am.clothesshop.user.ProductsActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_no_connection.*

class NoConnectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        checkConnection()
    }

    private fun checkConnection() {
        try_again_to_connect.setOnClickListener {
            if (CheckConnection().isNetworkAvailable(this) ||
                CheckConnection().isInternetAvailable()) {
                val intent = Intent(this, ProductsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}