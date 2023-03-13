package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_non_recurring_engagement_details.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class NonRecurringEngagementDetailsActivity : BaseActivity(), TextToSpeech.OnInitListener {

    companion object{
        const val ADD_EDIT_TEAM_MEMBERS_CODE: Int = 16
    }

    private lateinit var mNonRecurringEngagement: NonRecurringEngagement
    val c = Calendar.getInstance()
    var startDate: String = ""
    var startTime: String = ""
    var endDate: String = ""
    var endTime: String = ""
    var formatterDateTime: DateTimeFormatter? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_non_recurring_engagement_details)

        tts = TextToSpeech(this, this)

        getIntentData()
        setupActionBar()
        populateNonRecurringEngagementDetailsUI()

        formatterDateTime = if(Locale.getDefault().displayLanguage == "English")
        {
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
        }else{
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        }

        btn_pick_start_date_time_non_recurring_engagement_details.setOnClickListener {
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)

            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    val sMonthOfYear =
                        if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                    startDate = "$sDayOfMonth/$sMonthOfYear/$year"

                    val tpd = if(Locale.getDefault().displayLanguage == "English"){
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            var timeSet = ""
                            var tmpSelectedHour = hour

                            if (hour > 12) {
                                tmpSelectedHour -= 12;
                                timeSet = "PM";
                            } else if (hour == 0) {
                                tmpSelectedHour += 12;
                                timeSet = "AM";
                            } else if (hour == 12){
                                timeSet = "PM";
                            }else{
                                timeSet = "AM";
                            }

                            val sHour = if(tmpSelectedHour < 10) "0$tmpSelectedHour" else "$tmpSelectedHour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            startTime = "$sHour:$sMinute $timeSet"

                            btn_pick_start_date_time_non_recurring_engagement_details.text = "$startDate $startTime"
                        },hour,minute, false)
                    }else{
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            val sHour = if(hour < 10) "0$hour" else "$hour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            startTime = "$sHour:$sMinute"

                            btn_pick_start_date_time_non_recurring_engagement_details.text = "$startDate $startTime"
                        },hour,minute, true)
                    }
                    tpd.show()
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        btn_pick_end_date_time_non_recurring_engagement_details.setOnClickListener {
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)

            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    val sMonthOfYear =
                        if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                    endDate = "$sDayOfMonth/$sMonthOfYear/$year"

                    val tpd = if(Locale.getDefault().displayLanguage == "English"){
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            var timeSet = ""
                            var tmpSelectedHour = hour

                            if (hour > 12) {
                                tmpSelectedHour -= 12;
                                timeSet = "PM";
                            } else if (hour == 0) {
                                tmpSelectedHour += 12;
                                timeSet = "AM";
                            } else if (hour == 12){
                                timeSet = "PM";
                            }else{
                                timeSet = "AM";
                            }

                            val sHour = if(tmpSelectedHour < 10) "0$tmpSelectedHour" else "$tmpSelectedHour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            endTime = "$sHour:$sMinute $timeSet"

                            btn_pick_end_date_time_non_recurring_engagement_details.text = "$endDate $endTime"
                        },hour,minute, false)
                    }else{
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            val sHour = if(hour < 10) "0$hour" else "$hour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            endTime = "$sHour:$sMinute"

                            btn_pick_end_date_time_non_recurring_engagement_details.text = "$endDate $endTime"
                        },hour,minute, true)
                    }
                    tpd.show()
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        tv_assigned_members.setOnClickListener {
            val intent = Intent(this@NonRecurringEngagementDetailsActivity, NonRecurringEngagementMembersActivity::class.java)
            intent.putExtra(Constants.NON_RECURRING_ENGAGEMENT, mNonRecurringEngagement)
            startActivityForResult(intent, ADD_EDIT_TEAM_MEMBERS_CODE)
            //startActivity(intent)
        }

        btn_save_edited_non_recurring_engagement.setOnClickListener {
            saveEditedNonRecurringEngagement()
        }

        tv_cancel_edition_non_recurring_engagement.setOnClickListener {
            cancelEditingNonRecurringEngagement()
        }
    }

    override fun onResume() {
        super.onResume()

        populateNonRecurringEngagementDetailsUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == ADD_EDIT_TEAM_MEMBERS_CODE){
            mNonRecurringEngagement = data!!.getParcelableExtra<NonRecurringEngagement>(Constants.NON_RECURRING_ENGAGEMENT)!!
            }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.NON_RECURRING_ENGAGEMENT)){
            mNonRecurringEngagement = intent.getParcelableExtra<NonRecurringEngagement>(Constants.NON_RECURRING_ENGAGEMENT)!!
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_non_recurring_engagement_details)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = mNonRecurringEngagement.name
        }
        toolbar_non_recurring_engagement_details.setNavigationOnClickListener { onBackPressed() }
    }

    private fun populateNonRecurringEngagementDetailsUI(){
        et_title_of_non_recurring_engagement_edit.setText(mNonRecurringEngagement.name)
        btn_pick_start_date_time_non_recurring_engagement_details.text = "" +
                "${dateFormatter.format(mNonRecurringEngagement.startDate)} \n${timeFormatter.format(mNonRecurringEngagement.startTime)}"
        btn_pick_end_date_time_non_recurring_engagement_details.text ="" +
                "${dateFormatter.format(mNonRecurringEngagement.endDate)} \n${timeFormatter.format(mNonRecurringEngagement.endTime)}"
        et_note_of_non_recurring_engagement_details.setText(mNonRecurringEngagement.note)

        FirestoreClass().getAssignedMembersListDetails(this, mNonRecurringEngagement.assignedTo)
        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)
    }

    fun setupMembersToUI(membersList: ArrayList<User>){
        var assignedLogins: String = ""
        for(i in membersList){
            assignedLogins = i.login + ", " + assignedLogins
        }
        tv_assigned_members_logins.text = assignedLogins
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.non_recurring_engagement_details_options, menu)

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
            R.id.action_edit_non_recurring_engagement -> {
                enableEditingNonRecurringEngagement()
                return true
            }
            R.id.action_delete_non_recurring_engagement -> {
                alertDialogForDeleteNonRecurringEngagement(mNonRecurringEngagement.name)
                return true
            }
            R.id.action_speak_out_non_recurring_engagement -> {
                speakOut()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
        tts?.speak(mNonRecurringEngagement.name, TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.start_date)}: ${dateFormatter.format(mNonRecurringEngagement.startDate)}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.start_time)}: ${timeFormatter.format(mNonRecurringEngagement.startTime)}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.end_date)}: ${dateFormatter.format(mNonRecurringEngagement.endDate)}", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.end_time)}: ${timeFormatter.format(mNonRecurringEngagement.endTime)}", TextToSpeech.QUEUE_ADD, null, "")
        if(mNonRecurringEngagement.note.isNotBlank()){
            tts?.speak("${resources.getString(R.string.additional_note)}: ${mNonRecurringEngagement.note}", TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    private fun enableEditingNonRecurringEngagement(){
        et_title_of_non_recurring_engagement_edit.visibility = View.VISIBLE
        btn_pick_start_date_time_non_recurring_engagement_details.isEnabled = true
        btn_pick_start_date_time_non_recurring_engagement_details.isClickable = true
        btn_pick_end_date_time_non_recurring_engagement_details.isEnabled = true
        btn_pick_end_date_time_non_recurring_engagement_details.isClickable = true
        et_note_of_non_recurring_engagement_details.isEnabled = true
        btn_save_edited_non_recurring_engagement.visibility = View.VISIBLE
        tv_cancel_edition_non_recurring_engagement.visibility = View.VISIBLE
    }

    private fun cancelEditingNonRecurringEngagement(){
        et_title_of_non_recurring_engagement_edit.visibility = View.GONE
        btn_pick_start_date_time_non_recurring_engagement_details.isEnabled = false
        btn_pick_start_date_time_non_recurring_engagement_details.isClickable = false
        btn_pick_end_date_time_non_recurring_engagement_details.isEnabled = false
        btn_pick_end_date_time_non_recurring_engagement_details.isClickable = false
        et_note_of_non_recurring_engagement_details.isEnabled = false
        btn_save_edited_non_recurring_engagement.visibility = View.GONE
        tv_cancel_edition_non_recurring_engagement.visibility = View.GONE
        populateNonRecurringEngagementDetailsUI()

    }

    private fun alertDialogForDeleteNonRecurringEngagement(nonRecurringEngagementName: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.delete_non_recurring_engagement) + " $nonRecurringEngagementName")
        builder.setMessage(resources.getString(R.string.delete_regular_engagement_confirmation))
        builder.setIcon(R.drawable.ic_alert_dialog)

        builder.setPositiveButton(resources.getString(R.string.yes)){ dialogInterface, which ->
            dialogInterface.dismiss()
            deleteNonRecurringEngagement()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteNonRecurringEngagement(){
        showProgressDialog()
        FirestoreClass().deleteNonRecurringEngagement(this, mNonRecurringEngagement.documentID)
    }

    private fun saveEditedNonRecurringEngagement(){
            if(validateFields() && validateDateTime()){
                insertEditedRegularEngagement()
            }
    }

    private fun insertEditedRegularEngagement(){
        val changesHashMap = HashMap<String, Any>()
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_NAME] = et_title_of_non_recurring_engagement_edit.text.toString()
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_START_DATE] = dateFormatter!!.parse(startDate).time
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_START_TIME] = startTime
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_END_DATE] = dateFormatter!!.parse(startDate).time
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_END_TIME] = endTime
        changesHashMap[Constants.NON_RECURRING_ENGAGEMENT_NOTE] = et_note_of_non_recurring_engagement_details.text.toString()

        FirestoreClass().updateNonRecurringEngagement(this, mNonRecurringEngagement.documentID, changesHashMap)
    }

    private fun validateFields():Boolean{
        return if(et_title_of_non_recurring_engagement_edit.text!!.isBlank()
            || !btn_pick_start_date_time_non_recurring_engagement_details.text
                .contains(Regex("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9] [0-9][0-9]:[0-9][0-9]"))
            || !btn_pick_end_date_time_non_recurring_engagement_details.text
                .contains(Regex("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9] [0-9][0-9]:[0-9][0-9]"))){
            showErrorSnackBar("Please fill all requested fields (title, start date/time, end date/time")
            false
        }else{
            true
        }
    }

    private fun validateDateTime():Boolean{
        val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
            SimpleDateFormat("hh:mm a")
        }else{
            SimpleDateFormat("HH:mm")
        }

        return if(dateFormatter.parse(startDate)!!.before(dateFormatter.parse(endDate))){
            true
        }else if(startDate == endDate){
            if(timeFormatter.parse(startTime)!!.before(timeFormatter.parse(endTime))){
                true
            }else{
                showErrorSnackBar("End Time can't be before Start Time!")
                false
            }
        }else{
            showErrorSnackBar("End Date can't be before Start Date!")
            false
        }
    }


    fun nonRecurringEngagementChangeSuccessful(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun nonRecurringEngagementChangeFailed(){
        hideProgressDialog()
        showErrorSnackBar("Creating non-recurring engagement failed!")
    }

    fun displayHighContrastTheme(){

        when(resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_NO ->{
                showToast(this, resources.getString(R.string.high_contrast_mode_only_on_darkmode))
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                ll_non_recurring_engagement_details2.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                ll_non_recurring_engagement_details.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                cv_non_recurring_engagement_details.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                et_title_of_non_recurring_engagement_edit.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_start_date_non_recurring_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_end_date_non_recurring_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_pick_start_date_time_non_recurring_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_pick_end_date_time_non_recurring_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_assigned_members_logins.setTextColor(resources.getColor(R.color.text_color_disability))
                et_note_of_non_recurring_engagement_details.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_save_edited_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))

            }
        }

        }

}