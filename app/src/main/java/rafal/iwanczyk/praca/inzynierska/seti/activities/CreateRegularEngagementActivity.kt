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
            /*
            if(Locale.getDefault().displayLanguage == "English"){
                try{
                    if(validateAddingNewRegularEngagementEN()){
                        addRegularEngagement()
                    }
                }catch (e: Exception){
                    convertFrom24hFormatTo12h()
                }finally {
                    if(validateAddingNewRegularEngagementEN()){
                        addRegularEngagement()
                    }
                }
            }else{
                try{
                if(validateAddingNewRegularEngagement()){
                    addRegularEngagement()
                }
                }catch (e: Exception){
                    convertFrom12hFormatTo24h()
                }finally {
                    if(validateAddingNewRegularEngagement()){
                        addRegularEngagement()
                    }
                }
            }
            */
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

    /*
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
        return if(et_title_of_engagement.text!!.isNotBlank()
            && btn_pick_start_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))
            && btn_pick_end_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateStartEndTimeFilledProperly(): Boolean{
        val startTimeArray = btn_pick_start_time_regular_engagement.text.toString().split(":")
        val endTimeArray = btn_pick_end_time_regular_engagement.text.toString().split(":")

        if(btn_pick_start_time_regular_engagement.text.toString()
            == btn_pick_end_time_regular_engagement.text.toString()){
            showErrorSnackBar(resources.getString(R.string.start_time_and_end_time_the_same_alert))
            return false
        }else if(btn_pick_end_time_regular_engagement.text.toString() == "00:00"){
            showErrorSnackBar(resources.getString(R.string.engagement_ends_at_midnight_alert))
            return false
        }else if((btn_pick_start_time_regular_engagement.text.toString() == "00:00"
                    && btn_pick_end_time_regular_engagement.text.toString() != "00:00")){
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
            "Monday" -> tmpListRegularEngagements = mWeekEngagements.mondayEngagements
            "Tuesday" -> tmpListRegularEngagements = mWeekEngagements.tuesdayEngagements
            "Wednesday" -> tmpListRegularEngagements = mWeekEngagements.wednesdayEngagements
            "Thursday" -> tmpListRegularEngagements = mWeekEngagements.thursdayEngagements
            "Friday" -> tmpListRegularEngagements = mWeekEngagements.fridayEngagements
            "Saturday" -> tmpListRegularEngagements = mWeekEngagements.saturdayEngagements
            "Sunday" -> tmpListRegularEngagements = mWeekEngagements.sundayEngagements
        }

        val startTimeArray = btn_pick_start_time_regular_engagement.text.toString().split(":")
        val endTimeArray = btn_pick_end_time_regular_engagement.text.toString().split(":")

        val startTime = startTimeArray[0].toInt() * 60 + startTimeArray[1].toInt()
        val endTime = endTimeArray[0].toInt() * 60 + endTimeArray[1].toInt()

        for(i in tmpListRegularEngagements){
            if(startTime >= (i.startTime.substring(0,2).toInt() * 60 + i.startTime.substring(3,5).toInt())
                && startTime <= (i.endTime.substring(0,2).toInt() * 60 + i.endTime.substring(3,5).toInt())){
                showErrorSnackBar("Start time interrupts regular engagement ${i.name}!")
                return false
            }else if(endTime >= (i.startTime.substring(0,2).toInt() * 60 + i.startTime.substring(3,5).toInt())
                && endTime <= (i.endTime.substring(0,2).toInt() * 60 + i.endTime.substring(3,5).toInt())){
                showErrorSnackBar("End time interrupts regular engagement ${i.name}")
                return false
            }else if(i.startTime.substring(0,2).toInt() * 60 + i.startTime.substring(3,5).toInt() in startTime..endTime){
                showErrorSnackBar("Regular engagement duration interrupts with ${i.name}")
                return false
            }
        }

        return true
    }

     */

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
    /*

    private fun validateRequiredFieldsEN(): Boolean{
        return if(et_title_of_engagement.text!!.isNotBlank()
            && btn_pick_start_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9] [A-Z][A-Z]"))
            && btn_pick_end_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9] [A-Z][A-Z]"))){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.fill_required_fields_alert))
            false
        }
    }

    private fun validateStartEndTimeFilledProperlyEN(): Boolean{

        var additionalStartAMPMvalue = 0
        var additionalEndAMPMvalue = 0

        if(btn_pick_start_time_regular_engagement.text.toString().substring(0,2) != "12"
            && btn_pick_start_time_regular_engagement.text.toString().substring(6) == "PM"){
            additionalStartAMPMvalue = 12
        }

        if(btn_pick_end_time_regular_engagement.text.toString().substring(0,2) != "12"
            && btn_pick_end_time_regular_engagement.text.toString().substring(6) == "PM"){
            additionalEndAMPMvalue = 12
        }

        var startTime = 0

        if(btn_pick_start_time_regular_engagement.text.toString().substring(0,2) == "12"
            &&  btn_pick_start_time_regular_engagement.text.toString().substring(6) == "AM"){
            startTime = btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt()
        }else{
            startTime = btn_pick_start_time_regular_engagement.text.toString().substring(0,2).toInt()*60 +
            btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt() +
            additionalStartAMPMvalue*60

        }

        var endTime = 0

        if(btn_pick_end_time_regular_engagement.text.toString().substring(0,2) == "12"
            &&  btn_pick_end_time_regular_engagement.text.toString().substring(6) == "AM"){
            endTime = btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt()
        }else{
            endTime = btn_pick_end_time_regular_engagement.text.toString().substring(0,2).toInt()*60 +
            btn_pick_end_time_regular_engagement.text.toString().substring(3,5).toInt() + additionalEndAMPMvalue*60
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

        if(btn_pick_start_time_regular_engagement.text.toString().substring(0,2) != "12"
            && btn_pick_start_time_regular_engagement.text.toString().substring(6) == "PM"){
            additionalStartAMPMvalue = 12
        }

        if(btn_pick_end_time_regular_engagement.text.toString().substring(0,2) != "12"
            && btn_pick_end_time_regular_engagement.text.toString().substring(6) == "PM"){
            additionalEndAMPMvalue = 12
        }

        var startTime = 0

        if(btn_pick_start_time_regular_engagement.text.toString().substring(0,2) == "12"
            &&  btn_pick_start_time_regular_engagement.text.toString().substring(6) == "AM"){
            startTime = btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt()
        }else{
            startTime = btn_pick_start_time_regular_engagement.text.toString().substring(0,2).toInt()*60 +
                    btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt() +
                    additionalStartAMPMvalue*60
        }

        var endTime = 0

        if(btn_pick_end_time_regular_engagement.text.toString().substring(0,2) == "12"
            &&  btn_pick_end_time_regular_engagement.text.toString().substring(6) == "AM"){
            endTime = btn_pick_start_time_regular_engagement.text.toString().substring(3,5).toInt()
        }else{
           endTime = btn_pick_end_time_regular_engagement.text.toString().substring(0,2).toInt()*60 +
                    btn_pick_end_time_regular_engagement.text.toString().substring(3,5).toInt() + additionalEndAMPMvalue*60
        }

        when(mSelectedDay){
            resources.getStringArray(R.array.DaysOfWeek)[0] -> tmpListRegularEngagements = mWeekEngagements.mondayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[1] -> tmpListRegularEngagements = mWeekEngagements.tuesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[2] -> tmpListRegularEngagements = mWeekEngagements.wednesdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[3] -> tmpListRegularEngagements = mWeekEngagements.thursdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[4] -> tmpListRegularEngagements = mWeekEngagements.fridayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[5] -> tmpListRegularEngagements = mWeekEngagements.saturdayEngagements
            resources.getStringArray(R.array.DaysOfWeek)[6] -> tmpListRegularEngagements = mWeekEngagements.sundayEngagements
        }

        for(i in tmpListRegularEngagements){
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
        return true
    }

     */

    fun regularEngagementCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /*
    private fun convertFrom12hFormatTo24h(){
        val formatter12H = DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        val formatter24H = DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        for (i in mWeekEngagements.mondayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.tuesdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.wednesdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.thursdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.fridayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.saturdayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
        for (i in mWeekEngagements.sundayEngagements){
            if(i.startTime.length == 8){
                i.startTime = formatter24H.format(LocalTime.parse(i.startTime, formatter12H)).toString()
            }
            if(i.endTime.length == 8){
                i.endTime = formatter24H.format(LocalTime.parse(i.endTime, formatter12H)).toString()
            }
        }
    }

    private fun convertFrom24hFormatTo12h(){
        val formatter12H = DateTimeFormatterBuilder().appendPattern("hh:mm a").toFormatter(Locale.ENGLISH)
        val formatter24H = DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter()
        for (i in mWeekEngagements.mondayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.tuesdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.wednesdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.thursdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.fridayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.saturdayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
        for (i in mWeekEngagements.sundayEngagements){
            if(i.startTime.length == 5){
                i.startTime = formatter12H.format(LocalTime.parse(i.startTime, formatter24H)).toString()
            }
            if(i.endTime.length == 5){
                i.endTime = formatter12H.format(LocalTime.parse(i.endTime, formatter24H)).toString()
            }
        }
    }

     */
}