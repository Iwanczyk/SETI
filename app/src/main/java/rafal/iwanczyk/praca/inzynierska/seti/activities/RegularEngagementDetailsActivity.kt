package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
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

                if(Locale.getDefault().displayLanguage == "English"){
                    btn_start_time_regular_engagement_details.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format((cal.time))
                }else{
                    btn_start_time_regular_engagement_details.text = SimpleDateFormat("HH:mm").format((cal.time))
                }
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

                if(Locale.getDefault().displayLanguage == "English") {
                    btn_end_time_regular_engagement_details.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format((cal.time))
                }else{
                    btn_end_time_regular_engagement_details.text = SimpleDateFormat("HH:mm").format((cal.time))
                }
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
        btn_start_time_regular_engagement_details.text = mRegularEngagement.startTime
        btn_end_time_regular_engagement_details.text = mRegularEngagement.endTime
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.regular_engagement_details_options, menu)
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
        if(Locale.getDefault().displayLanguage == "English"){
            if(validateAddingNewRegularEngagementEN()){
                insertEditedRegularEngagement()
            }
        }else{
            if(validateAddingNewRegularEngagement()){
                insertEditedRegularEngagement()
            }
        }
    }

    private fun insertEditedRegularEngagement(){
        //If the regular engagement is edited within the same day or it has to be transferred to the another day
        if(mSelectedDay == mRegularEngagement.day){
            mRegularEngagement.name = et_title_of_regular_engagement_details.text.toString()
            mRegularEngagement.day = mSelectedDay
            mRegularEngagement.startTime = btn_start_time_regular_engagement_details.text.toString()
            mRegularEngagement.endTime = btn_end_time_regular_engagement_details.text.toString()
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
            btn_start_time_regular_engagement_details.text.toString(),
            btn_end_time_regular_engagement_details.text.toString(),
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

    private fun validateAddingNewRegularEngagement(): Boolean{
        return validateRequiredFields()
                && validateStartEndTimeFilledProperly()
                && validateStartEndTimeWithOtherEngagements()
    }

    private fun validateAddingNewRegularEngagementEN(): Boolean{
        return validateRequiredFieldsEN()
                && validateStartEndTimeFilledProperlyEN()
                && validateStartEndTimeWithOtherEngagementsEN()
    }

    private fun validateRequiredFields(): Boolean{
        return if(et_title_of_regular_engagement_details.text!!.isNotBlank()
            && btn_start_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))
            && btn_end_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateStartEndTimeFilledProperly(): Boolean{
        val startTimeArray = btn_start_time_regular_engagement_details.text.toString().split(":")
        val endTimeArray = btn_end_time_regular_engagement_details.text.toString().split(":")

        if(btn_start_time_regular_engagement_details.text.toString()
            == btn_end_time_regular_engagement_details.text.toString()){
            showErrorSnackBar(resources.getString(R.string.start_time_and_end_time_the_same_alert))
            return false
        }else if(btn_end_time_regular_engagement_details.text.toString() == "00:00"){
            showErrorSnackBar(resources.getString(R.string.engagement_ends_at_midnight_alert))
            return false
        }else if((btn_start_time_regular_engagement_details.text.toString() == "00:00"
                    && btn_end_time_regular_engagement_details.text.toString() != "00:00")){
            return true
        }else if((endTimeArray[0].toInt() * 60 + endTimeArray[1].toInt()) -
            (startTimeArray[0].toInt() * 60 + startTimeArray[1].toInt()) <= 0){
            showErrorSnackBar(resources.getString(R.string.end_time_before_start_time_alert))
            return false
        }else{
            return true
        }
    }

    private fun validateStartEndTimeWithOtherEngagements(): Boolean{

        when(mSelectedDay){
            resources.getStringArray(R.array.DaysOfWeek)[0] -> tmpListRegularEngagements = mWeekPlan.mondayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[1] -> tmpListRegularEngagements = mWeekPlan.tuesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[2] -> tmpListRegularEngagements = mWeekPlan.wednesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[3] -> tmpListRegularEngagements = mWeekPlan.thursdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[4] -> tmpListRegularEngagements = mWeekPlan.fridayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[5] -> tmpListRegularEngagements = mWeekPlan.saturdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[6] -> tmpListRegularEngagements = mWeekPlan.sundayEngagements
        }

        val startTimeArray = btn_start_time_regular_engagement_details.text.toString().split(":")
        val endTimeArray = btn_end_time_regular_engagement_details.text.toString().split(":")

        val startTime = startTimeArray[0].toInt() * 60 + startTimeArray[1].toInt()
        val endTime = endTimeArray[0].toInt() * 60 + endTimeArray[1].toInt()

        for(i in tmpListRegularEngagements) {

            if (i == mRegularEngagement) {
                continue
            }else{
                if (startTime >= (i.startTime.substring(0, 2).toInt() * 60 + i.startTime.substring(3, 5).toInt())
                    && startTime <= (i.endTime.substring(0, 2).toInt() * 60 + i.endTime.substring(3,5).toInt())
                ) {
                    showErrorSnackBar("Start time interrupts regular engagement ${i.name}!")
                    return false
                } else if (endTime >= (i.startTime.substring(0, 2).toInt() * 60 + i.startTime.substring(3, 5).toInt())
                    && endTime <= (i.endTime.substring(0, 2).toInt() * 60 + i.endTime.substring(3,5).toInt())
                ) {
                    showErrorSnackBar("End time interrupts regular engagement ${i.name}")
                    return false
                } else if (i.startTime.substring(0, 2).toInt() * 60 + i.startTime.substring(3, 5).toInt() in startTime..endTime
                ) {
                    showErrorSnackBar("Regular engagement duration interrupts with ${i.name}")
                    return false
                }
            }
        }
        return true
    }

    private fun validateRequiredFieldsEN(): Boolean{
        return if(et_title_of_regular_engagement_details.text!!.isNotBlank()
            && btn_start_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9] [A-Z][A-Z]"))
            && btn_end_time_regular_engagement_details.text.contains(Regex("[0-9][0-9]:[0-9][0-9] [A-Z][A-Z]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateStartEndTimeFilledProperlyEN(): Boolean{

        var additionalStartAMPMvalue = 0
        var additionalEndAMPMvalue = 0

        if(btn_start_time_regular_engagement_details.text.toString().substring(0,2) != "12"
            && btn_start_time_regular_engagement_details.text.toString().substring(6) == "PM"){
            additionalStartAMPMvalue = 12
        }

        if(btn_end_time_regular_engagement_details.text.toString().substring(0,2) != "12"
            && btn_end_time_regular_engagement_details.text.toString().substring(6) == "PM"){
            additionalEndAMPMvalue = 12
        }

        var startTime = 0

        if(btn_start_time_regular_engagement_details.text.toString().substring(0,2) == "12"
            &&  btn_start_time_regular_engagement_details.text.toString().substring(6) == "AM"){
            startTime = btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt()
        }else{
            startTime = btn_start_time_regular_engagement_details.text.toString().substring(0,2).toInt()*60 +
                    btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt() +
                    additionalStartAMPMvalue*60

        }

        var endTime = 0

        if(btn_end_time_regular_engagement_details.text.toString().substring(0,2) == "12"
            &&  btn_end_time_regular_engagement_details.text.toString().substring(6) == "AM"){
            endTime = btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt()
        }else{
            endTime = btn_end_time_regular_engagement_details.text.toString().substring(0,2).toInt()*60 +
                    btn_end_time_regular_engagement_details.text.toString().substring(3,5).toInt() + additionalEndAMPMvalue*60
        }

        return if(startTime == endTime){
            showErrorSnackBar(resources.getString(R.string.start_time_and_end_time_the_same_alert))
            false
        }else if(endTime - startTime <= 0){
            showErrorSnackBar(resources.getString(R.string.end_time_before_start_time_alert))
            false
        }else{
            true
        }
    }

    private fun validateStartEndTimeWithOtherEngagementsEN(): Boolean{

        var additionalStartAMPMvalue = 0
        var additionalEndAMPMvalue = 0

        if(btn_start_time_regular_engagement_details.text.toString().substring(0,2) != "12"
            && btn_start_time_regular_engagement_details.text.toString().substring(6) == "PM"){
            additionalStartAMPMvalue = 12
        }

        if(btn_end_time_regular_engagement_details.text.toString().substring(0,2) != "12"
            && btn_end_time_regular_engagement_details.text.toString().substring(6) == "PM"){
            additionalEndAMPMvalue = 12
        }

        var startTime = 0

        if(btn_start_time_regular_engagement_details.text.toString().substring(0,2) == "12"
            &&  btn_start_time_regular_engagement_details.text.toString().substring(6) == "AM"){
            startTime = btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt()
        }else{
            startTime = btn_start_time_regular_engagement_details.text.toString().substring(0,2).toInt()*60 +
                    btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt() +
                    additionalStartAMPMvalue*60
        }

        var endTime = 0

        if(btn_end_time_regular_engagement_details.text.toString().substring(0,2) == "12"
            &&  btn_end_time_regular_engagement_details.text.toString().substring(6) == "AM"){
            endTime = btn_start_time_regular_engagement_details.text.toString().substring(3,5).toInt()
        }else{
            endTime = btn_end_time_regular_engagement_details.text.toString().substring(0,2).toInt()*60 +
                    btn_end_time_regular_engagement_details.text.toString().substring(3,5).toInt() + additionalEndAMPMvalue*60
        }

        when(mSelectedDay){
            resources.getStringArray(R.array.DaysOfWeek)[0] -> tmpListRegularEngagements = mWeekPlan.mondayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[1] -> tmpListRegularEngagements = mWeekPlan.tuesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[2] -> tmpListRegularEngagements = mWeekPlan.wednesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[3] -> tmpListRegularEngagements = mWeekPlan.thursdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[4] -> tmpListRegularEngagements = mWeekPlan.fridayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[5] -> tmpListRegularEngagements = mWeekPlan.saturdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[6] -> tmpListRegularEngagements = mWeekPlan.sundayEngagements
        }

        for(i in tmpListRegularEngagements){

            if(i == mRegularEngagement){
                continue
            }else{
            var tmpAdditionalAMPMstartTime = 0
            var tmpAdditionalAMPMendTime = 0
            var tmpStartTime = 0
            var tmpEndTime = 0

            if(i.startTime.substring(0,2) != "12"
                && i.startTime.substring(6) == "PM"){
                tmpAdditionalAMPMstartTime = 12
            }

            if(i.endTime.substring(0,2) != "12"
                && i.endTime.substring(6) == "PM"){
                tmpAdditionalAMPMendTime = 12
            }

            if(i.startTime.substring(0,2) == "12"
                &&  i.startTime.substring(6) == "AM"){
                tmpStartTime = i.startTime.substring(3,5).toInt()
            }else{
                tmpStartTime = i.startTime.substring(0,2).toInt()*60 +
                        i.startTime.substring(3,5).toInt() +
                        tmpAdditionalAMPMstartTime*60
            }

            if(i.endTime.substring(0,2) == "12"
                &&  i.endTime.substring(6) == "AM"){
                tmpEndTime = i.endTime.substring(3,5).toInt()
            }else{
                tmpEndTime = i.endTime.substring(0,2).toInt()*60 +
                        i.endTime.substring(3,5).toInt() + tmpAdditionalAMPMendTime*60
            }

            if(startTime >= (tmpStartTime)
                && startTime <= (tmpEndTime)){
                showErrorSnackBar(resources.getString(R.string.start_time_interrupts_another_engagement_alert)+i.name)
                return false
            }else if(endTime >= (tmpStartTime)
                && endTime <= (tmpEndTime)){
                showErrorSnackBar(resources.getString(R.string.end_time_interrupts_another_engagement_alert)+i.name)
                return false
            }else if(tmpStartTime in startTime..endTime){
                showErrorSnackBar(resources.getString(R.string.regular_engagement_duration_interrupts_alert)+i.name)
                return false
            }
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
        tts?.speak("Day of week: ${mRegularEngagement.day}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("Start time: ${mRegularEngagement.startTime}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("End time: ${mRegularEngagement.endTime}", TextToSpeech.QUEUE_ADD, null, "")

        if(mRegularEngagement.note.isNotBlank()){
            tts?.speak("Additional notes: ${mRegularEngagement.note}", TextToSpeech.QUEUE_ADD, null, "")
        }else{
            tts?.speak("Additional notes: None", TextToSpeech.QUEUE_ADD, null, "")
        }

        tts?.speak("Type of the engagement: ${mRegularEngagement.typeOfEngagement}", TextToSpeech.QUEUE_ADD, null, "")

        if(mRegularEngagement.lectureRoom.isNotBlank()){
            tts?.speak("Lecture room number: ${mRegularEngagement.lectureRoom}", TextToSpeech.QUEUE_ADD, null, "")
        }
        if(mRegularEngagement.buildingNumber.isNotBlank()){
            tts?.speak("Lecture building number: ${mRegularEngagement.buildingNumber}", TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(tts != null){
            tts?.stop()
            tts?.shutdown()
        }
    }
}