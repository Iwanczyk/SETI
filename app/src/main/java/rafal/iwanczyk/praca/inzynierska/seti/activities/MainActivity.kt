package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this)
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
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
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

    //Updating UI when user data changed in MyProfile activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun updateNavigationUserDetails(user: User){

        Glide
            .with(this@MainActivity)
            .load(user.image)
            .fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        //Set up drawer background using Glide
        Glide
            .with(this@MainActivity)
            .load(user.background)
            .fitCenter()
            //.placeholder(R.drawable.ic_user_place_holder)
            .into(object :
                CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    ly_nav_user_background.setBackgroundResource(R.drawable.nav_header_main_background)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    ly_nav_user_background.background = resource
                }

            })

        tv_login.text = user.login
        tv_username.text = user.name
    }
}