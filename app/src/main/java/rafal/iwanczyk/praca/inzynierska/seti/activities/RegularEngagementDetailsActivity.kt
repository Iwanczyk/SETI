package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class RegularEngagementDetailsActivity : AppCompatActivity() {

    private lateinit var mRegularEngagement: RegularEngagement
    private var mRegularEngagementListPosition: Int = -1
    private var mSelectedDay: String = ""
    private var mSelectedTypeOfEngagement: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regular_engagement_details)

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
            println("SAVED EDITED REGULAR ENGAGEMENT")
        }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.REGULAR_ENGAGEMENT)){
            mRegularEngagement = intent.getParcelableExtra<RegularEngagement>(Constants.REGULAR_ENGAGEMENT)!!
        }
        if(intent.hasExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION)){
            mRegularEngagementListPosition = intent.getIntExtra(Constants.REGULAR_ENGAGEMENT_LIST_POSITION, -1)
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableEditingRegularEngagement(){
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
            //deleteRegularEngagement()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteRegularEngagement(){

    }

    private fun saveEditedRegularEngagement(){

    }
}