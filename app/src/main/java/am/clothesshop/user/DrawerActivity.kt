package am.clothesshop.user

import am.clothesshop.R
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class DrawerActivity : AppCompatActivity(), View.OnClickListener {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navView: NavigationView
    private lateinit var navHeaderView: View
    private lateinit var navHeaderProfileImage: CircleImageView
    private lateinit var navHeaderUsername: TextView
    private lateinit var navHeaderEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (mAuth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navController = findNavController(R.id.nav_host_fragment)

        init()
        setMenuHeaderInformation()
        onAccountClick()

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_products, R.id.nav_stores, R.id.nav_recommendations, R.id.nav_notifications,
                R.id.nav_messenger, R.id.nav_favorites, R.id.nav_following
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun init() {
        navView = findViewById(R.id.nav_view)
        navHeaderView = navView.getHeaderView(0)
        navHeaderProfileImage = navHeaderView.findViewById(R.id.account_profile_image)
        navHeaderUsername = navHeaderView.findViewById(R.id.account_profile_name)
        navHeaderEmail = navHeaderView.findViewById(R.id.account_profile_email)
    }

    private fun setMenuHeaderInformation() {
        updateProfileImage()
        navHeaderUsername.text = mAuth.currentUser?.displayName
        navHeaderEmail.text = mAuth.currentUser?.email
    }

    private fun onAccountClick() {
        navHeaderProfileImage.setOnClickListener(this)
        navHeaderUsername.setOnClickListener(this)
        navHeaderEmail.setOnClickListener(this)
    }

    private fun updateProfileImage() {
        if (mAuth.currentUser?.photoUrl == null) {
            Picasso.get().load(R.drawable.profile_image_placeholder).into(navHeaderProfileImage)
        } else {
            Picasso.get().load(mAuth.currentUser?.photoUrl).into(navHeaderProfileImage)
        }
    }

    override fun onClick(v: View?) {
        startActivity(Intent(this, AccountSettingsActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}