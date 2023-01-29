package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.RadioButton
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.common.config.GservicesValue.value
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.adapters.RegularEngagementsAdapter
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.util.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_REGULAR_ENGAGEMENT_CODE: Int = 12
    }

    lateinit var adapter :RegularEngagementsAdapter
    private val calendar: Calendar = Calendar.getInstance()
    private val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    lateinit var mWeekPlan: WeekEngagements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this, true)

        //setup buttons of days
        daysOfWeekButtonsSetup()

        fab_create_regular_engagement.setOnClickListener {
            val intent = Intent(this, CreateRegularEngagementActivity::class.java)
            intent.putExtra(Constants.WEEKPLAN, mWeekPlan)
            startActivityForResult(intent, CREATE_REGULAR_ENGAGEMENT_CODE)
        }
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
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_REGULAR_ENGAGEMENT_CODE){
            println("ACTIVITY FOR RESULT USED")
            FirestoreClass().getWeekPlan(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun updateNavigationUserDetails(user: User, readWeekPlanRegularEngagements: Boolean){

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

        if(readWeekPlanRegularEngagements){
            showProgressDialog()
            FirestoreClass().getWeekPlan(this)
        }
    }

    private fun daysOfWeekButtonsSetup(){
        println("DAY: ${calendar.get(Calendar.DAY_OF_WEEK)}")

        when(currentDay){
            Calendar.MONDAY -> rb_monday.isChecked = true
            Calendar.TUESDAY -> rb_tuesday.isChecked = true
            Calendar.WEDNESDAY -> rb_wednesday.isChecked = true
            Calendar.THURSDAY -> rb_thursday.isChecked = true
            Calendar.FRIDAY -> rb_friday.isChecked = true
            Calendar.SATURDAY -> rb_saturday.isChecked = true
            Calendar.SUNDAY -> rb_sunday.isChecked = true
        }

    }

    fun populateWeekPlanToUI(weekPlan: WeekEngagements){
        hideProgressDialog()

        mWeekPlan = weekPlan

        rv_regular_engagements_list.layoutManager = LinearLayoutManager(this)
        rv_regular_engagements_list.setHasFixedSize(true)

        when(currentDay){
            Calendar.MONDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.mondayEngagements)
            Calendar.TUESDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.tuesdayEngagements)
            Calendar.WEDNESDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.wednesdayEngagements)
            Calendar.THURSDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.thursdayEngagements)
            Calendar.FRIDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.fridayEngagements)
            Calendar.SATURDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.saturdayEngagements)
            Calendar.SUNDAY -> adapter = RegularEngagementsAdapter(this, weekPlan.sundayEngagements)
        }
        rv_regular_engagements_list.adapter = adapter

        adapter.setOnClickListener(object: RegularEngagementsAdapter.OnClickListener{
            override fun onClick(position: Int, model: RegularEngagement) {
                //startActivity(Intent(this@MainActivity, RegularEngagementDetail::class.java))
            }
        })

    }
}