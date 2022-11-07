package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.nav_header_main.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().signInUser(this)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        //Showing/Closing the drawer
        toolbar_main_activity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(drawer_layout!!.isDrawerOpen(GravityCompat.START)){
            drawer_layout!!.closeDrawer(GravityCompat.START)
        }else{
            drawer_layout!!.openDrawer(GravityCompat.START)
        }
    }

    //Closing the drawer with Back button
    override fun onBackPressed() {
        if(drawer_layout!!.isDrawerOpen(GravityCompat.START)){
            drawer_layout!!.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    //Selecting menu options
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile -> {
               // startActivityForResult(Intent(this,
               //     MyProfileActivity::class.java),
               //     MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_settings -> {
                //startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        drawer_layout!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User){

        hideProgressDialog()

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        tv_login.text = user.login
        tv_username.text = user.name
    }
}