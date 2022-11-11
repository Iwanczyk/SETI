package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_profile.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.User

class MyProfileActivity : BaseActivity() {

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

    }


    private fun setupActionBar(){

        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.nav_my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User){

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image)

        my_profile_et_login.setText(user.login)
        my_profile_et_name.setText(user.name)
        my_profile_et_email.setText(user.email)

        Glide
            .with(this@MyProfileActivity)
            .load(user.background)
            .centerCrop()
            .placeholder(R.drawable.nav_header_main_background)
            .into(my_profile_iv_user_background_image)


    }
}