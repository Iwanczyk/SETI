package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MenuItem
import android.widget.RadioButton
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_REGULAR_ENGAGEMENT_CODE: Int = 12
        const val EDIT_REGULAR_ENGAGEMENT_CODE: Int = 13
    }

    lateinit var adapter :RegularEngagementsAdapter
    private val calendar: Calendar = Calendar.getInstance()
    private val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    lateinit var mWeekPlan: WeekEngagements
    private var mSelectedDay: String = ""
    private lateinit var mSharedPreferences: SharedPreferences

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.SETI_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if(tokenUpdated){
            showProgressDialog()
            FirestoreClass().loadUserData(this, true)
        }else{
            //Jeśli nie zadziała to spróbować  public void onSuccess(InstanceIdResult instanceIdResult) {
            //           String newToken = instanceIdResult.getToken();
            //           Log.e("newToken",newToken);
            //
            //     }

            /* OLD:
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
            }
            //290 11:24
             */
            FirebaseMessaging.getInstance()
                .token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult)
                }
            /*
            FirebaseMessaging.getInstance()
                .token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult)
                }

             */
        }

        FirestoreClass().loadUserData(this, true)

        //Setup buttons of days
        daysOfWeekButtonsSetup()

        //Adding new regular engagements
        fab_create_regular_engagement.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateRegularEngagementActivity::class.java)
            intent.putExtra(Constants.WEEKPLAN, mWeekPlan)
            startActivityForResult(intent, CREATE_REGULAR_ENGAGEMENT_CODE)
        }

        //Days radio group listener
        rg_days_of_week.setOnCheckedChangeListener { group, checkedId ->
            mSelectedDay = findViewById<RadioButton>(checkedId).text.toString()
            changeDaysOfWeekAdapter(checkedId)
        }

        tts = TextToSpeech(this, this)
    }

    override fun onResume() {
        super.onResume()
        FirestoreClass().getWeekPlan(this)
    }

    //Text to speech language initialization
    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            if(Locale.getDefault().displayLanguage == "English"){
                val result = tts!!.setLanguage(Locale.ENGLISH)
            }else{
                val result = tts!!.setLanguage(Locale.getDefault())
            }
        }else{
            showErrorSnackBar("Text to speech initialization failed")
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

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            R.id.nav_non_recurring_engagements -> {
                startActivity(Intent(this, NonRecurringEngagementsActivity::class.java))
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
        }else if((resultCode == Activity.RESULT_OK && requestCode == CREATE_REGULAR_ENGAGEMENT_CODE)
                || (resultCode == Activity.RESULT_OK && requestCode == EDIT_REGULAR_ENGAGEMENT_CODE)){
            FirestoreClass().getWeekPlan(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun updateNavigationUserDetails(user: User, readWeekPlanRegularEngagements: Boolean){
        hideProgressDialog()
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
        when(currentDay){
            Calendar.MONDAY -> {
                rb_monday.isChecked = true
                mSelectedDay = rb_monday.text.toString()
            }
            Calendar.TUESDAY -> {
                rb_tuesday.isChecked = true
                mSelectedDay = rb_tuesday.text.toString()
            }
            Calendar.WEDNESDAY -> {
                rb_wednesday.isChecked = true
                mSelectedDay = rb_wednesday.text.toString()
            }
            Calendar.THURSDAY -> {
                rb_thursday.isChecked = true
                mSelectedDay = rb_thursday.text.toString()
            }
            Calendar.FRIDAY -> {
                rb_friday.isChecked = true
                mSelectedDay = rb_friday.text.toString()
            }
            Calendar.SATURDAY -> {
                rb_saturday.isChecked = true
                mSelectedDay = rb_saturday.text.toString()
            }
            Calendar.SUNDAY -> {
                rb_sunday.isChecked = true
                mSelectedDay = rb_sunday.text.toString()
            }
        }
    }

    fun populateWeekPlanToUI(weekPlan: WeekEngagements){
        hideProgressDialog()

        mWeekPlan = weekPlan

        rv_regular_engagements_list.layoutManager = LinearLayoutManager(this)
        rv_regular_engagements_list.setHasFixedSize(true)

        val formatter = if(Locale.getDefault().displayLanguage == "English"){
            DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        }else{
            DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        }

        try{
            when(currentDay){
                Calendar.MONDAY -> {
                    mWeekPlan.mondayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.mondayEngagements)
                }
                Calendar.TUESDAY -> {
                    mWeekPlan.tuesdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.tuesdayEngagements)
                }
                Calendar.WEDNESDAY -> {
                    mWeekPlan.wednesdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.wednesdayEngagements)
                }
                Calendar.THURSDAY -> {
                    mWeekPlan.thursdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.thursdayEngagements)
                }
                Calendar.FRIDAY -> {
                    mWeekPlan.fridayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.fridayEngagements)
                }
                Calendar.SATURDAY -> {
                    mWeekPlan.saturdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.saturdayEngagements)
                }
                Calendar.SUNDAY -> {
                    mWeekPlan.sundayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, weekPlan.sundayEngagements)
                }
            }
            }catch (e :Exception){
                if(Locale.getDefault().displayLanguage == "English"){
                    convertFrom24hFormatTo12h()
                }else{
                    convertFrom12hFormatTo24h()
                }
            populateWeekPlanToUI(mWeekPlan)
            }
        rv_regular_engagements_list.adapter = adapter

        adapter.setOnClickListener(object: RegularEngagementsAdapter.OnClickListener{
            override fun onClick(position: Int, model: RegularEngagement) {
                val intent = Intent(this@MainActivity, RegularEngagementDetailsActivity::class.java)
                intent.putExtra(Constants.WEEKPLAN, mWeekPlan)
                intent.putExtra(Constants.REGULAR_ENGAGEMENT, model)
                intent.putExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION, position)
                intent.putExtra(Constants.DISPLAYED_DAY_OF_WEEK, mSelectedDay)
                startActivityForResult(intent, EDIT_REGULAR_ENGAGEMENT_CODE)
            }
        })

        adapter.setOnLongClickListener(object: RegularEngagementsAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: RegularEngagement) {
                speakOut(model.name, model.startTime, model.endTime, model.lectureRoom, model.buildingNumber)
            }

        })
    }

    private fun changeDaysOfWeekAdapter(checkedId: Int) {
        rv_regular_engagements_list.layoutManager = LinearLayoutManager(this)
        rv_regular_engagements_list.setHasFixedSize(true)

        val formatter = if(Locale.getDefault().displayLanguage == "English"){
            DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        }else{
            DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        }

        try{
            when(checkedId){
                R.id.rb_monday -> {
                    mWeekPlan.mondayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.mondayEngagements)}
                R.id.rb_tuesday -> {
                    mWeekPlan.tuesdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.tuesdayEngagements)
                }
                R.id.rb_wednesday -> {
                    mWeekPlan.wednesdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.wednesdayEngagements)
                }
                R.id.rb_thursday -> {
                    mWeekPlan.thursdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.thursdayEngagements)
                }
                R.id.rb_friday -> {
                    mWeekPlan.fridayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.fridayEngagements)
                }
                R.id.rb_saturday -> {
                    mWeekPlan.saturdayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.saturdayEngagements)
                }
                R.id.rb_sunday -> {
                    mWeekPlan.sundayEngagements.sortBy { LocalTime.parse(it.startTime, formatter) }
                    adapter = RegularEngagementsAdapter(this, mWeekPlan.sundayEngagements)
                }
            }
        }catch (e: Exception){
            if(Locale.getDefault().displayLanguage == "English"){
                convertFrom24hFormatTo12h()
            }else{
                convertFrom12hFormatTo24h()
            }
            changeDaysOfWeekAdapter(checkedId)
        }


        rv_regular_engagements_list.adapter = adapter

        adapter.setOnClickListener(object: RegularEngagementsAdapter.OnClickListener{
            override fun onClick(position: Int, model: RegularEngagement) {
                val intent = Intent(this@MainActivity, RegularEngagementDetailsActivity::class.java)
                intent.putExtra(Constants.WEEKPLAN, mWeekPlan)
                intent.putExtra(Constants.REGULAR_ENGAGEMENT, model)
                intent.putExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION, position)
                intent.putExtra(Constants.DISPLAYED_DAY_OF_WEEK, mSelectedDay)
                startActivityForResult(intent, EDIT_REGULAR_ENGAGEMENT_CODE)
            }
        })
        
        adapter.setOnLongClickListener(object: RegularEngagementsAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: RegularEngagement) {
                speakOut(model.name, model.startTime, model.endTime, model.lectureRoom, model.buildingNumber)
            }

        })
    }

    private fun convertFrom12hFormatTo24h(){
        val formatter12H = DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        val formatter24H = DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        for (i in mWeekPlan.mondayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.tuesdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.wednesdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.thursdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.fridayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.saturdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekPlan.sundayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        //populateWeekPlanToUI(mWeekPlan)
    }

    private fun convertFrom24hFormatTo12h(){
        val formatter12H = DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        val formatter24H = DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        for (i in mWeekPlan.mondayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.tuesdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.wednesdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.thursdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.fridayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.saturdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekPlan.sundayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        //populateWeekPlanToUI(mWeekPlan)
    }

    private fun speakOut(nameOfEngagement: String, startTime: String, endTime: String,
                         lectureRoomNumber: String, buildingNumber: String){
        tts?.speak(nameOfEngagement, TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("Start time: $startTime", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("End time: $endTime", TextToSpeech.QUEUE_ADD, null, "")

        if(lectureRoomNumber.isNotBlank()){
            tts?.speak("Lecture room: $lectureRoomNumber", TextToSpeech.QUEUE_ADD, null, "")
        }
        if(buildingNumber.isNotBlank()){
            tts?.speak("Lecture building number: $buildingNumber", TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog()
        FirestoreClass().loadUserData(this@MainActivity, true)
    }

    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog()
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(tts != null){
            tts?.stop()
            tts?.shutdown()
        }
    }
}