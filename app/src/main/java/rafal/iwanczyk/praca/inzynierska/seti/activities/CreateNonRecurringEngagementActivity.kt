package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TimePicker
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_create_non_recurring_engagement.*
import kotlinx.android.synthetic.main.activity_create_regual_engagement.*
import kotlinx.android.synthetic.main.activity_non_recurring_engagements.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CreateNonRecurringEngagementActivity : BaseActivity() {

    val c = Calendar.getInstance()
    private var startDate: String = ""
    private var startTime: String = ""
    private var endDate: String = ""
    private var endTime: String = ""
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_non_recurring_engagement)

        setupActionBar()

        //START DATE TIME BUTTON
        btn_pick_start_date_time_non_recurring_engagement.setOnClickListener {
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

                            btn_pick_start_date_time_non_recurring_engagement.text = "$startDate $startTime"
                        },hour,minute, false)
                    }else{
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            val sHour = if(hour < 10) "0$hour" else "$hour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            startTime = "$sHour:$sMinute"

                            btn_pick_start_date_time_non_recurring_engagement.text = "$startDate $startTime"
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

        //END DATE BUTTON
        btn_pick_end_date_time_non_recurring_engagement.setOnClickListener {
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

                            btn_pick_end_date_time_non_recurring_engagement.text = "$endDate $endTime"
                        },hour,minute, false)
                    }else{
                        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{
                                view, hour, minute ->
                            val sHour = if(hour < 10) "0$hour" else "$hour"
                            val sMinute = if(minute < 10) "0$minute" else "$minute"
                            endTime = "$sHour:$sMinute"

                            btn_pick_end_date_time_non_recurring_engagement.text = "$endDate $endTime"
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

        btn_add_non_recurring_engagement.setOnClickListener {
            if(validateFields() && validateDateTime()){
                addNonRecurringEngagement()
            }
        }

        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_create_non_recurring_engagement)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.add_new_non_recurring_engagement)
        }
        toolbar_create_non_recurring_engagement.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateFields(): Boolean{
        return if(et_title_of_non_recurring_engagement.text!!.isBlank()
            || !btn_pick_start_date_time_non_recurring_engagement.text
                .contains(Regex("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9] [0-9][0-9]:[0-9][0-9]"))
            || !btn_pick_end_date_time_non_recurring_engagement.text
                .contains(Regex("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9] [0-9][0-9]:[0-9][0-9]"))){
            showErrorSnackBar("Please fill all requested fields (title, start date/time, end date/time")
            false
        }else{
            true
        }
    }

    private fun validateDateTime(): Boolean{
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

    private fun addNonRecurringEngagement(){
        val assignedToInitialList: ArrayList<String> = ArrayList()
        assignedToInitialList.add(getCurrentUserID())


        val newEngagement = NonRecurringEngagement(
        "",
        getCurrentUserID(),
        assignedToInitialList,
        et_title_of_non_recurring_engagement.text.toString(),
        dateFormatter.parse(startDate)!!.time,
        timeFormatter.parse(startTime)!!.time,
        dateFormatter.parse(endDate)!!.time,
            timeFormatter.parse(endTime)!!.time,
        et_note_of_non_recurring_engagement.text.toString(),
        )

        showProgressDialog()
        FirestoreClass().addNonRecurringEngagements(this, newEngagement)
    }

    fun addNonRecurringEngagementSuccessful(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun addNonRecurringEngagementFailed(){
        hideProgressDialog()
        showErrorSnackBar("Creating non-recurring engagement failed!")
    }

    fun displayHighContrastTheme(){

        when(resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_NO ->{
                showToast(this, resources.getString(R.string.high_contrast_mode_only_on_darkmode))
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                ll_create_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                cv_create_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_disability_color))
                et_title_of_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_start_date_create_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_pick_start_date_time_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                tv_end_date_create_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_pick_end_date_time_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                et_title_of_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))
                btn_add_non_recurring_engagement.setTextColor(resources.getColor(R.color.text_color_disability))

                et_title_of_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_fields_disability_color))
                btn_pick_start_date_time_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_button_disability_color))
                btn_pick_end_date_time_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_button_disability_color))
                et_note_of_non_recurring_engagement.setBackgroundColor(resources.getColor(R.color.background_fields_disability_color))

            }
        }

        }
}