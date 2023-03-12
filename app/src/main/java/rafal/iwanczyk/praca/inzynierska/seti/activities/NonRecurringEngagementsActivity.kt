package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_non_recurring_engagement_members.*
import kotlinx.android.synthetic.main.activity_non_recurring_engagements.*
import kotlinx.android.synthetic.main.item_member.view.*
import kotlinx.android.synthetic.main.item_non_recurring_engagement.view.*
import kotlinx.android.synthetic.main.main_content.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.adapters.NonRecurringEngagementsAdapter
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class NonRecurringEngagementsActivity : BaseActivity(), TextToSpeech.OnInitListener {

    companion object{
        const val ADD_NON_RECURRING_ENGAGEMENT_CODE: Int = 14
        const val EDIT_NON_RECURRING_ENGAGEMENT_CODE: Int = 15
    }

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }
    private lateinit var displayStartData: LocalDate
    private lateinit var displayEndData: LocalDate
    private var mEngagementsList: ArrayList<NonRecurringEngagement> = ArrayList()

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_non_recurring_engagements)

        setupActionBar()
        setupDisplayedDates()
        tts = TextToSpeech(this, this)

        FirestoreClass().getNonRecurringEngagements(this,
            dateFormatter.parse(formatter.format(displayStartData).toString())!!.time,
            dateFormatter.parse(formatter.format(displayEndData).toString())!!.time)

        btn_previous_date_non_recurring_engagements.setOnClickListener {
            displayEndData = displayStartData
            displayStartData = displayStartData.minusDays(7)
            tv_date_display_non_recurring_engagements.text = 
                "${displayStartData.format(formatter)} - ${displayEndData.format(formatter)}"

            FirestoreClass().getNonRecurringEngagements(this,
                dateFormatter.parse(formatter.format(displayStartData).toString())!!.time,
                dateFormatter.parse(formatter.format(displayEndData).toString())!!.time)
        }

        btn_next_date_non_recurring_engagements.setOnClickListener {
            displayStartData = displayEndData
            displayEndData = displayEndData.plusDays(7)
            tv_date_display_non_recurring_engagements.text =
                "${displayStartData.format(formatter)} - ${displayEndData.format(formatter)}"

            FirestoreClass().getNonRecurringEngagements(this,
                dateFormatter.parse(formatter.format(displayStartData).toString())!!.time,
                dateFormatter.parse(formatter.format(displayEndData).toString())!!.time)
        }

        fab_create_non_recurring_engagement.setOnClickListener {
            val intent = Intent(this@NonRecurringEngagementsActivity,
                CreateNonRecurringEngagementActivity::class.java)
            startActivityForResult(intent, ADD_NON_RECURRING_ENGAGEMENT_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == ADD_NON_RECURRING_ENGAGEMENT_CODE
            || resultCode == Activity.RESULT_OK && requestCode == EDIT_NON_RECURRING_ENGAGEMENT_CODE){

            FirestoreClass().getNonRecurringEngagements(this,
                dateFormatter.parse(formatter.format(displayStartData).toString())!!.time,
                dateFormatter.parse(formatter.format(displayEndData).toString())!!.time)
        }
    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_non_recurring_engagements_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.non_recurring_engagements)
        }
        toolbar_non_recurring_engagements_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupDisplayedDates(){
        displayStartData = LocalDate.now()
        displayEndData = LocalDate.now().plusDays(7)

        tv_date_display_non_recurring_engagements.text =
            "${displayStartData.format(formatter)} - ${displayEndData.format(formatter)}"
    }

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

    fun populateNonRecurringEngagementsToUI(nonRecurringEngagementsList: ArrayList<NonRecurringEngagement>){
        mEngagementsList = nonRecurringEngagementsList

        rv_non_recurring_engagements_list.layoutManager = LinearLayoutManager(this)
        rv_non_recurring_engagements_list.setHasFixedSize(true)
        val adapter = NonRecurringEngagementsAdapter(this, mEngagementsList)
        rv_non_recurring_engagements_list.adapter = adapter

        adapter.setOnClickListener(object: NonRecurringEngagementsAdapter.OnClickListener{
            override fun onClick(position: Int, model: NonRecurringEngagement) {
                val intent = Intent(this@NonRecurringEngagementsActivity,
                    NonRecurringEngagementDetailsActivity::class.java)
                intent.putExtra(Constants.NON_RECURRING_ENGAGEMENT, model)
                startActivityForResult(intent, EDIT_NON_RECURRING_ENGAGEMENT_CODE)
            }
        })

        adapter.setOnLongClickListener(object: NonRecurringEngagementsAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: NonRecurringEngagement) {
                speakOut(model.name, dateFormatter.format(model.startDate), timeFormatter.format(model.startTime),
                    dateFormatter.format(model.endDate), timeFormatter.format(model.endTime))
            }

        })

        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)
    }

    private fun speakOut(nameOfEngagement: String, startDate: String, startTime: String,
                         endDate: String, endTime: String){
        tts?.speak(nameOfEngagement, TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.start_date)}: $startDate", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.start_time)}: $startTime", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.end_date)}: $endDate", TextToSpeech.QUEUE_ADD, null, "")
        tts?.speak("${resources.getString(R.string.end_time)}: $endTime", TextToSpeech.QUEUE_ADD, null, "")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(tts != null){
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun displayHighContrastTheme(){
        ll_non_recurring_engagements_activity.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        cv_non_recurring_engagements_activity.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        btn_previous_date_non_recurring_engagements.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_date_display_non_recurring_engagements.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_next_date_non_recurring_engagements.setTextColor(resources.getColor(R.color.text_color_disability))

        val itemCount = rv_non_recurring_engagements_list.adapter!!.itemCount

        for (i in 0 until itemCount){
            val holder = rv_non_recurring_engagements_list.findViewHolderForAdapterPosition(i)
            if (holder != null){
                holder.itemView.tv_item_non_recurring_engagement_start_date.setTextColor(resources.getColor(R.color.text_color_disability))
                holder.itemView.tv_item_non_recurring_engagement_start_time.setTextColor(resources.getColor(R.color.text_color_disability))
                holder.itemView.tv_item_non_recurring_engagement_title.setTextColor(resources.getColor(R.color.text_color_disability))
                holder.itemView.tv_item_non_recurring_engagement_end_date.setTextColor(resources.getColor(R.color.text_color_disability))
                holder.itemView.tv_item_non_recurring_engagement_end_time.setTextColor(resources.getColor(R.color.text_color_disability))
            }
        }
    }
}