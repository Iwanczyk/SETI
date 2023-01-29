package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

class CreateRegularEngagementActivity : BaseActivity() {

    private var mSelectedDay: String = ""
    private var mSelectedTypeOfEngagement: String = ""
    lateinit var mWeekEngagements: WeekEngagements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_regual_engagement)

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

                if(dropdown_list_chose_type_of_engagement.getItemAtPosition(position).toString() == "Study"){
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

                btn_pick_start_time_regular_engagement.text = SimpleDateFormat("HH:mm").format((cal.time))
            }
            if(Locale.getDefault().getDisplayLanguage() == "English"){
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

                btn_pick_end_time_regular_engagement.text = SimpleDateFormat("HH:mm").format((cal.time))
            }
            if(Locale.getDefault().getDisplayLanguage() == "English"){
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),false).show()
            }else{
                TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),true).show()
            }
        }

        //Button create regular engagement
        btn_add_regular_engagement.setOnClickListener {
            if(validateRequiredFields()){
                addRegularEngagement()
            }else{
                showErrorSnackBar("You have not filled all required fields (Title, start time, end time)")
            }
        }
    }

    private fun validateRequiredFields(): Boolean{
        return (et_title_of_engagement.text!!.isNotBlank()
                && btn_pick_start_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]"))
                && btn_pick_end_time_regular_engagement.text.contains(Regex("[0-9][0-9]:[0-9][0-9]")))
    }

    private fun addRegularEngagement(){
        val newRegularEngagement = RegularEngagement(
            getCurrentUserID(),
            et_title_of_engagement.text.toString(),
            mSelectedDay,
            btn_pick_start_time_regular_engagement.text.toString(),
            btn_pick_end_time_regular_engagement.text.toString(),
            et_note_of_engagement.text.toString(),
            mSelectedTypeOfEngagement,
            et_lecture_room_number_of_engagement.text.toString(),
            et_building_number_of_engagement.text.toString()
        )

        println("Created regular engagement: "+newRegularEngagement.toString())

        when(mSelectedDay){
            "Monday" -> mWeekEngagements.mondayEngagements.add(newRegularEngagement)
            "Tuesday" -> mWeekEngagements.tuesdayEngagements.add(newRegularEngagement)
            "Wednesday" -> mWeekEngagements.wednesdayEngagements.add(newRegularEngagement)
            "Thursday" -> mWeekEngagements.thursdayEngagements.add(newRegularEngagement)
            "Friday" -> mWeekEngagements.fridayEngagements.add(newRegularEngagement)
            "Saturday" -> mWeekEngagements.saturdayEngagements.add(newRegularEngagement)
            "Sunday" -> mWeekEngagements.sundayEngagements.add(newRegularEngagement)
        }

        FirestoreClass().createRegularEngagement(this, mWeekEngagements)
    }

    fun regularEngagementCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}