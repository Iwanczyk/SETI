package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_create_regual_engagement.*
import kotlinx.android.synthetic.main.activity_regular_engagement_details.*
import kotlinx.android.synthetic.main.activity_regular_engagement_details.et_building_number_of_engagement_details
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.meta.When

class RegularEngagementDetailsActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var mRegularEngagement: RegularEngagement
    private lateinit var mWeekPlan: WeekEngagements
    private var mRegularEngagementListPosition: Int = -1
    private var mDisplayedDayOfWeek: String = ""
    private var mSelectedDay: String = ""
    private var mSelectedTypeOfEngagement: String = ""
    private lateinit var tmpListRegularEngagements: ArrayList<RegularEngagement>
    private var startTimeEdited: String = ""
    private var endTimeEdited: String = ""
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regular_engagement_details)

        tts = TextToSpeech(this, this)

        getIntentData()
        setupActionBar()
        populateRegularEngagementDetailsUI()

        //Day list dropdown
        val dayList: MutableList<String> = resources.getStringArray(R.array.DaysOfWeek).toMutableList()
        val dayListAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, dayList)
        dropdown_list_day_of_week_details.adapter = dayListAdapter

        dropdown_list_day_of_week_details.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mSelectedDay = dropdown_list_day_of_week_details.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //Engagement type dropdown
        val engagementTypeList: MutableList<String> = resources.getStringArray(R.array.TypeOfEngagement).toMutableList()
        val engagementTypeListAdapter : ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, engagementTypeList)
        dropdown_list_type_of_engagement_details.adapter = engagementTypeListAdapter

        dropdown_list_type_of_engagement_details.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                mSelectedTypeOfEngagement = dropdown_list_type_of_engagement_details.getItemAtPosition(position).toString()

                if(dropdown_list_type_of_engagement_details.getItemAtPosition(position).toString()
                    == resources.getString(R.string.study)){
                    et_lecture_room_number_of_engagement_details.visibility = View.VISIBLE
                    et_building_number_of_engagement_details.visibility = View.VISIBLE
                }else{
                    et_lecture_room_number_of_engagement_details.visibility = View.GONE
                    et_building_number_of_engagement_details.visibility = View.GONE
                    et_lecture_room_number_of_engagement_details.setText("")
                    et_building_number_of_engagement_details.setText("")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //Start time button
        btn_start_time_regular_engagement_details.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{
                    timePicker: TimePicker, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                startTimeEdited = timeFormatter.format(cal.time).toString()
                btn_start_time_regular_engagement_details.text = startTimeEdited
            }
            if(Locale.getDefault().displayLanguage == "English"){
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(
                    Calendar.MINUTE),false).show()
            }else{
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(
                    Calendar.MINUTE),true).show()
            }
        }

        //End time button
        btn_end_time_regular_engagement_details.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{
                    timePicker: TimePicker, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                endTimeEdited = timeFormatter.format(cal.time).toString()
                btn_end_time_regular_engagement_details.text = endTimeEdited

            }
            if(Locale.getDefault().displayLanguage == "English"){
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),false).show()
            }else{
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),true).show()
            }
        }

        tv_cancel_edition_regular_engagement.setOnClickListener {
            cancelEditingRegularEngagement()
        }

        btn_save_edited_regular_engagement.setOnClickListener {
            saveEditedRegularEngagement()
        }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.REGULAR_ENGAGEMENT)){
            mRegularEngagement = intent.getParcelableExtra<RegularEngagement>(Constants.REGULAR_ENGAGEMENT)!!
        }
        if(intent.hasExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION)){
            mRegularEngagementListPosition = intent.getIntExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION, -1)
        }
        if(intent.hasExtra(Constants.WEEKPLAN)){
            mWeekPlan = intent.getParcelableExtra<WeekEngagements>(Constants.WEEKPLAN)!!
        }
        if(intent.hasExtra(Constants.DISPLAYED_DAY_OF_WEEK)){
            mDisplayedDayOfWeek = intent.getStringExtra(Constants.DISPLAYED_DAY_OF_WEEK)!!
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_regular_engagement_details)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = mRegularEngagement.name
        }
        toolbar_regular_engagement_details.setNavigationOnClickListener { onBackPressed() }
    }

    private fun populateRegularEngagementDetailsUI(){
        et_title_of_regular_engagement_details.setText(mRegularEngagement.name)
        et_day_of_week_regular_engagement_details.setText(mRegularEngagement.day)
        btn_start_time_regular_engagement_details.text = timeFormatter.format(mRegularEngagement.startTime)
        btn_end_time_regular_engagement_details.text = timeFormatter.format(mRegularEngagement.endTime)
        et_note_of_engagement_details.setText(mRegularEngagement.note)
        et_type_of_regular_engagement_details.setText(mRegularEngagement.typeOfEngagement)
        if(mRegularEngagement.typeOfEngagement == resources.getString(R.string.study)){
            et_lecture_room_number_of_engagement_details.visibility = View.VISIBLE
            et_lecture_room_number_of_engagement_details.setText(mRegularEngagement.lectureRoom)
            et_building_number_of_engagement_details.visibility = View.VISIBLE
            et_building_number_of_engagement_details.setText(mRegularEngagement.buildingNumber)
        }else{
            et_lecture_room_number_of_engagement_details.visibility = View.GONE
            et_building_number_of_engagement_details.visibility = View.GONE
        }

        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.regular_engagement_details_options, menu)

        val menuSize = menu!!.size()
        for (i in 0 until menuSize){
            val holder = menu.getItem(i)
            val spannable = SpannableString(
                menu.getItem(i).title.toString()
            )
            spannable.setSpan(ForegroundColorSpan(resources.getColor(R.color.primary_text_color)),0,spannable.length,0)
            holder.title = spannable
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
         R.id.action_edit_regular_engagement -> {
             enableEditingRegularEngagement()
             return true
         }
         R.id.action_delete_regular_engagement -> {
             alertDialogForDeleteRegularEngagement(mRegularEngagement.name)
             return true
         }
        R.id.action_speak_out_regular_engagement -> {
            speakOut()
            return true
        }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableEditingRegularEngagement(){
        et_title_of_regular_engagement_details.visibility = View.VISIBLE
        et_title_of_regular_engagement_details.isEnabled = true
        et_day_of_week_regular_engagement_details.visibility = View.GONE
        dropdown_list_day_of_week_details.visibility = View.VISIBLE
        btn_start_time_regular_engagement_details.isEnabled = true
        btn_start_time_regular_engagement_details.isClickable = true
        btn_end_time_regular_engagement_details.isEnabled = true
        btn_end_time_regular_engagement_details.isClickable = true
        et_note_of_engagement_details.isEnabled = true
        et_type_of_regular_engagement_details.visibility = View.GONE
        dropdown_list_type_of_engagement_details.visibility = View.VISIBLE
        et_lecture_room_number_of_engagement_details.isEnabled = true
        et_building_number_of_engagement_details.isEnabled = true
        btn_save_edited_regular_engagement.visibility = View.VISIBLE
        tv_cancel_edition_regular_engagement.visibility = View.VISIBLE
    }

    private fun cancelEditingRegularEngagement(){
        et_title_of_regular_engagement_details.visibility = View.GONE
        et_title_of_regular_engagement_details.isEnabled = false
        et_day_of_week_regular_engagement_details.visibility = View.VISIBLE
        dropdown_list_day_of_week_details.visibility = View.GONE
        btn_start_time_regular_engagement_details.isEnabled = false
        btn_start_time_regular_engagement_details.isClickable = false
        btn_end_time_regular_engagement_details.isEnabled = false
        btn_end_time_regular_engagement_details.isClickable = false
        et_note_of_engagement_details.isEnabled = false
        et_type_of_regular_engagement_details.visibility = View.VISIBLE
        dropdown_list_type_of_engagement_details.visibility = View.GONE
        et_lecture_room_number_of_engagement_details.isEnabled = false
        et_building_number_of_engagement_details.isEnabled = false
        btn_save_edited_regular_engagement.visibility = View.GONE
        tv_cancel_edition_regular_engagement.visibility = View.GONE

        populateRegularEngagementDetailsUI()
    }

    private fun alertDialogForDeleteRegularEngagement(regularEngagementName: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.delete_regular_engagement) + " $regularEngagementName")
        builder.setMessage(resources.getString(R.string.delete_regular_engagement_confirmation))
        builder.setIcon(R.drawable.ic_alert_dialog)

        builder.setPositiveButton(resources.getString(R.string.yes)){ dialogInterface, which ->
            dialogInterface.dismiss()
            deleteRegularEngagement()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteRegularEngagement(){
        when(mRegularEngagement.day){
            resources.getStringArray(R.array.DaysOfWeek)[0] -> {
                mWeekPlan.mondayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[1] -> {
                mWeekPlan.tuesdayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[2] -> {
                mWeekPlan.wednesdayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[3] -> {
                mWeekPlan.thursdayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[4] -> {
                mWeekPlan.fridayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[5] -> {
                mWeekPlan.saturdayEngagements.removeAt(mRegularEngagementListPosition)
            }
            resources.getStringArray(R.array.DaysOfWeek)[6] -> {
                mWeekPlan.sundayEngagements.removeAt(mRegularEngagementListPosition)
            }
        }
        showProgressDialog()
        FirestoreClass().createUpdateRegularEngagement(this@RegularEngagementDetailsActivity, mWeekPlan)
    }

    private fun saveEditedRegularEngagement(){
        if(validateFields() && validateTime() && validateCollisionWithOtherRegularEngagements()){
            insertEditedRegularEngagement()
        }

    }

    private fun insertEditedRegularEngagement(){
        //If the regular engagement is edited within the same day or it has to be transferred to the another day
        if(mSelectedDay == mRegularEngagement.day){
            mRegularEngagement.name = et_title_of_regular_engagement_details.text.toString()
            mRegularEngagement.day = mSelectedDay
            mRegularEngagement.startTime = timeFormatter.parse(startTimeEdited)!!.time
            mRegularEngagement.endTime = timeFormatter.parse(endTimeEdited)!!.time
            mRegularEngagement.note = et_note_of_engagement_details.text.toString()
            mRegularEngagement.typeOfEngagement = mSelectedTypeOfEngagement
            mRegularEngagement.lectureRoom = et_lecture_room_number_of_engagement_details.text.toString()
            mRegularEngagement.buildingNumber = et_building_number_of_engagement_details.text.toString()

            when(mSelectedDay){
                resources.getStringArray(R.array.DaysOfWeek)[0] -> {
                    mWeekPlan.mondayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[1] -> {
                    mWeekPlan.tuesdayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[2] -> {
                    mWeekPlan.wednesdayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[3] -> {
                    mWeekPlan.thursdayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[4] -> {
                    mWeekPlan.fridayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[5] -> {
                    mWeekPlan.saturdayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
                resources.getStringArray(R.array.DaysOfWeek)[6] -> {
                    mWeekPlan.sundayEngagements[mRegularEngagementListPosition] = mRegularEngagement
                }
            }
        }else{
            val newEditedRegularEngagement = RegularEngagement(
            getCurrentUserID(),
            et_title_of_regular_engagement_details.text.toString(),
            mSelectedDay,
            timeFormatter.parse(startTimeEdited)!!.time,
                timeFormatter.parse(endTimeEdited)!!.time,
            et_note_of_engagement_details.text.toString(),
            mSelectedTypeOfEngagement,
            et_lecture_room_number_of_engagement_details.text.toString(),
            et_building_number_of_engagement_details.text.toString()
            )

            when(mRegularEngagement.day){
                resources.getStringArray(R.array.DaysOfWeek)[0] -> {
                    mWeekPlan.mondayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[1] -> {
                    mWeekPlan.tuesdayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[2] -> {
                    mWeekPlan.wednesdayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[3] -> {
                    mWeekPlan.thursdayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[4] -> {
                    mWeekPlan.fridayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[5] -> {
                    mWeekPlan.saturdayEngagements.removeAt(mRegularEngagementListPosition)
                }
                resources.getStringArray(R.array.DaysOfWeek)[6] -> {
                    mWeekPlan.sundayEngagements.removeAt(mRegularEngagementListPosition)
                }
            }

            when(mSelectedDay){
                resources.getStringArray(R.array.DaysOfWeek)[0] -> {
                    mWeekPlan.mondayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[1] -> {
                    mWeekPlan.tuesdayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[2] -> {
                    mWeekPlan.wednesdayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[3] -> {
                    mWeekPlan.thursdayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[4] -> {
                    mWeekPlan.fridayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[5] -> {
                    mWeekPlan.saturdayEngagements.add(newEditedRegularEngagement)
                }
                resources.getStringArray(R.array.DaysOfWeek)[6] -> {
                    mWeekPlan.sundayEngagements.add(newEditedRegularEngagement)
                }
            }
        }
       showProgressDialog()
       FirestoreClass().createUpdateRegularEngagement(this, mWeekPlan)
    }

    fun regularEngagementChangeSuccessful(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun validateFields(): Boolean{
        return if(et_title_of_regular_engagement_details.text!!.isNotBlank()
            && btn_start_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))
            && btn_end_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateTime(): Boolean{
        return if(timeFormatter.parse(startTimeEdited)!!.before(timeFormatter.parse(endTimeEdited)!!)){
            true
        }else{
            showErrorSnackBar("Start Time must be before End Time!")
            false
        }
    }

    private fun validateCollisionWithOtherRegularEngagements(): Boolean{
        when(mSelectedDay){
            resources.getStringArray(R.array.DaysOfWeek)[0] ->
                tmpListRegularEngagements = mWeekPlan.mondayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[1] ->
                tmpListRegularEngagements = mWeekPlan.tuesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[2] ->
                tmpListRegularEngagements = mWeekPlan.wednesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[3] ->
                tmpListRegularEngagements = mWeekPlan.thursdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[4] ->
                tmpListRegularEngagements = mWeekPlan.fridayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[5] ->
                tmpListRegularEngagements = mWeekPlan.saturdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[6] ->
                tmpListRegularEngagements = mWeekPlan.sundayEngagements
        }

        val startTimeLong = timeFormatter.parse(startTimeEdited)!!.time
        val endTimeLong = timeFormatter.parse(endTimeEdited)!!.time

        for(i in tmpListRegularEngagements){
            if(startTimeLong >= i.startTime && startTimeLong <= i.endTime){
                showErrorSnackBar("Start Time interrupts regular engagement: ${i.name}")
                return false
            }else if(endTimeLong >= i.startTime && endTimeLong <= i.endTime){
                showErrorSnackBar("End Time interrupts regular engagement: ${i.name}")
                return false
            }else if(i.startTime in startTimeLong..endTimeLong){
                showErrorSnackBar("Engagement duration interrupts with: ${i.name}")
                return false
            }
        }
        return true
    }

    //Text to speech
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

    private fun speakOut(){
        tts?.speak(mRegularEngagement.name, TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.day_of_week)}: ${mRegularEngagement.day}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.start_time)}: ${timeFormatter.format(mRegularEngagement.startTime)}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.end_time)}: ${timeFormatter.format(mRegularEngagement.endTime)}", TextToSpeech.QUEUE_ADD, null, "")

        if(mRegularEngagement.note.isNotBlank()){
            tts?.speak("${resources.getString(R.string.additional_note)}: ${mRegularEngagement.note}", TextToSpeech.QUEUE_ADD, null, "")
        }else{
            tts?.speak("${resources.getString(R.string.additional_note)}: ${resources.getString(R.string.none)}", TextToSpeech.QUEUE_ADD, null, "")
        }

        tts?.speak("${resources.getString(R.string.type_of_the_engagement)}: ${mRegularEngagement.typeOfEngagement}", TextToSpeech.QUEUE_ADD, null, "")

        if(mRegularEngagement.lectureRoom.isNotBlank()){
            tts?.speak("${resources.getString(R.string.lecture_room_number)}: ${mRegularEngagement.lectureRoom}", TextToSpeech.QUEUE_ADD, null, "")
        }
        if(mRegularEngagement.buildingNumber.isNotBlank()){
            tts?.speak("${resources.getString(R.string.lecture_building_number)}: ${mRegularEngagement.buildingNumber}", TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(tts != null){
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun displayHighContrastTheme(){

        when(resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_NO ->{
                showToast(this, "For high contrast mode please turn on dark mode on the device")
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                ll_regular_engagement_details.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                cv_regular_engagement_details.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                et_title_of_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                et_day_of_week_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_start_time_regular_engagement_details.setBackgroundColor(resources.getColor(R.color.background_button_disability_color))
                btn_end_time_regular_engagement_details.setBackgroundColor(resources.getColor(R.color.background_button_disability_color))
                tv_start_time_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_start_time_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_end_time_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_end_time_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                et_note_of_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                et_type_of_regular_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                et_lecture_room_number_of_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                et_building_number_of_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_save_edited_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
            }
        }
    }
}