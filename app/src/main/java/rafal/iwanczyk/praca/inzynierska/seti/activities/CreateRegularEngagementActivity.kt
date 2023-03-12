package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_create_regual_engagement.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

class CreateRegularEngagementActivity : BaseActivity() {

    private var mSelectedDay: String = ""
    private var mSelectedTypeOfEngagement: String = ""
    lateinit var mWeekEngagements: WeekEngagements
    private lateinit var tmpListRegularEngagements: ArrayList<RegularEngagement>
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }
    private var startTime: String = ""
    private var endTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_regual_engagement)

        setupActionBar()

        //Get user's week plan
        if(intent.hasExtra(Constants.WEEKPLAN)){
            mWeekEngagements = intent.getParcelableExtra<WeekEngagements>(Constants.WEEKPLAN)!!
        }

        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)

        //Day list dropdown
        val dayList: MutableList<String> = resources.getStringArray(R.array.DaysOfWeek).toMutableList()
        val dayListAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, dayList)
        dropdown_list_chose_day_of_week.adapter = dayListAdapter

        dropdown_list_chose_day_of_week.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long) {
                mSelectedDay = dropdown_list_chose_day_of_week.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //Engagement type dropdown
        val engagementTypeList: MutableList<String> = resources.getStringArray(R.array.TypeOfEngagement).toMutableList()
        val engagementTypeListAdapter : ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, engagementTypeList)
        dropdown_list_chose_type_of_engagement.adapter = engagementTypeListAdapter

        dropdown_list_chose_type_of_engagement.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                mSelectedTypeOfEngagement = dropdown_list_chose_type_of_engagement.getItemAtPosition(position).toString()

                if(dropdown_list_chose_type_of_engagement.getItemAtPosition(position).toString()
                    == resources.getString(R.string.study)){
                    et_lecture_room_number_of_engagement.visibility = View.VISIBLE
                    et_building_number_of_engagement.visibility = View.VISIBLE
                }else{
                    et_lecture_room_number_of_engagement.visibility = View.GONE
                    et_building_number_of_engagement.visibility = View.GONE
                    et_lecture_room_number_of_engagement.setText("")
                    et_building_number_of_engagement.setText("")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //Start time button
        btn_pick_start_time_regular_engagement.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{
                timePicker: TimePicker, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                startTime = timeFormatter.format(cal.time).toString()
                btn_pick_start_time_regular_engagement.text = startTime

            }
            if(Locale.getDefault().displayLanguage == "English"){
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),false).show()
            }else{
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
            }
        }

        //End time button
        btn_pick_end_time_regular_engagement.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener{
                    timePicker: TimePicker, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                endTime = timeFormatter.format(cal.time).toString()
                btn_pick_end_time_regular_engagement.text = endTime
            }
            if(Locale.getDefault().displayLanguage == "English"){
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),false).show()
            }else{
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),true).show()
            }
        }

        //Button create regular engagement
        btn_add_regular_engagement.setOnClickListener {
            if(validateFields() && validateTime() && validateCollisionWithOtherRegularEngagements()){
                addRegularEngagement()
            }
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_create_regular_engagement)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.add_new_regular_engagement)
        }

        toolbar_create_regular_engagement.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateFields(): Boolean{
        return if(et_title_of_engagement.text!!.isNotBlank()
            && btn_pick_start_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))
            && btn_pick_end_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateTime(): Boolean{
        return if(timeFormatter.parse(startTime)!!.before(timeFormatter.parse(endTime)!!)){
            true
        }else{
            showErrorSnackBar("Start Time must be before End Time!")
            false
        }
    }

    private fun validateCollisionWithOtherRegularEngagements(): Boolean{
        when(mSelectedDay){
            resources.getStringArray(R.array.DaysOfWeek)[0] ->
                tmpListRegularEngagements = mWeekEngagements.mondayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[1] ->
                tmpListRegularEngagements = mWeekEngagements.tuesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[2] ->
                tmpListRegularEngagements = mWeekEngagements.wednesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[3] ->
                tmpListRegularEngagements = mWeekEngagements.thursdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[4] ->
                tmpListRegularEngagements = mWeekEngagements.fridayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[5] ->
                tmpListRegularEngagements = mWeekEngagements.saturdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[6] ->
                tmpListRegularEngagements = mWeekEngagements.sundayEngagements
        }

        val startTimeLong = timeFormatter.parse(startTime)!!.time
        val endTimeLong = timeFormatter.parse(endTime)!!.time

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


    private fun addRegularEngagement(){
        val newRegularEngagement = RegularEngagement(
            getCurrentUserID(),
            et_title_of_engagement.text.toString(),
            mSelectedDay,
            timeFormatter.parse(startTime)!!.time,
            timeFormatter.parse(endTime)!!.time,
            et_note_of_engagement.text.toString(),
            mSelectedTypeOfEngagement,
            et_lecture_room_number_of_engagement.text.toString(),
            et_building_number_of_engagement.text.toString()
        )

        when(mSelectedDay){
            "Monday" -> mWeekEngagements.mondayEngagements.add(newRegularEngagement)
            "Tuesday" -> mWeekEngagements.tuesdayEngagements.add(newRegularEngagement)
            "Wednesday" -> mWeekEngagements.wednesdayEngagements.add(newRegularEngagement)
            "Thursday" -> mWeekEngagements.thursdayEngagements.add(newRegularEngagement)
            "Friday" -> mWeekEngagements.fridayEngagements.add(newRegularEngagement)
            "Saturday" -> mWeekEngagements.saturdayEngagements.add(newRegularEngagement)
            "Sunday" -> mWeekEngagements.sundayEngagements.add(newRegularEngagement)
        }

        FirestoreClass().createUpdateRegularEngagement(this, mWeekEngagements)
    }

    fun regularEngagementCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun displayHighContrastTheme(){
        ll_create_regular_engagement.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        cv_create_regular_engagement.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        et_title_of_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_start_time_create_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_pick_start_time_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_end_time_create_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_pick_end_time_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        et_note_of_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        et_lecture_room_number_of_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        et_building_number_of_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_add_regular_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
    }

}