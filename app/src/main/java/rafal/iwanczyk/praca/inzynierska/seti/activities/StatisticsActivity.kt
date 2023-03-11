package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_statistics.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsActivity : BaseActivity() {
    var studyTimeList: ArrayList<Long> = ArrayList()
    var workTimeList: ArrayList<Long> = ArrayList()
    var otherRecurringEngagementsTimeList: ArrayList<Long> = ArrayList()
    var nonRecurringEngagementsStatsList: ArrayList<Int> = ArrayList()
    private lateinit var displayedData: LocalDate
    private val startMonthFormatter = DateTimeFormatter.ofPattern("MM/yyyy")
    private val endMonthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val displayMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private var monthStartDate: String = ""
    private var monthEndDate: String = ""
    private var longestEngagementTitle: String = ""
    private var shortestEngagementTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        setupActionBar()

        getUserStatistics()
        setupDisplayDate()

        val statTypeList: MutableList<String> = resources.getStringArray(R.array.StatsType).toMutableList()
        val statTypeListAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, statTypeList)
        dropdown_list_chose_type_of_stats.adapter = statTypeListAdapter

        dropdown_list_chose_type_of_stats.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(dropdown_list_chose_type_of_stats.getItemAtPosition(position).toString() == statTypeList[0]){
                    ll_stats_regular_engagements.visibility = View.VISIBLE
                    ll_stats_non_recurring_engagements.visibility = View.GONE

                    rb_stats_regular_engagements_study.isChecked = true
                }else{
                    ll_stats_non_recurring_engagements.visibility = View.VISIBLE
                    ll_stats_regular_engagements.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                ll_stats_non_recurring_engagements.visibility = View.GONE
                ll_stats_regular_engagements.visibility = View.GONE
            }
        }

        rg_stats_regular_engagements.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.rb_stats_regular_engagements_study -> {
                    showToast(this, "STUDY")
                    //showStudyStats()
                }
                R.id.rb_stats_regular_engagements_work -> {
                    showToast(this,"WORK")
                    //showWorkStats()
                }
                R.id.rb_stats_regular_engagements_other -> {
                    showToast(this, "OTHER")
                    //showOtherRegularEngagementsStats()
                }
            }
        }

        btn_stats_previous_month.setOnClickListener {
            displayedData = displayedData.minusMonths(1)
            monthStartDate = "01/${displayedData.format(startMonthFormatter)}"
            monthEndDate = displayedData.plusMonths(1).minusDays((displayedData.dayOfMonth).toLong()).format(endMonthFormatter).toString()
            tv_stats_non_recurring_engagement_month.text = displayedData.format(displayMonthFormatter).toString()

            FirestoreClass().getNonRecurringEngagementStats(this,
                dateFormatter.parse(monthStartDate)!!.time, dateFormatter.parse(monthEndDate)!!.time)
        }

        btn_stats_next_month.setOnClickListener {
            displayedData = displayedData.plusMonths(1)
            monthStartDate = "01/${displayedData.format(startMonthFormatter)}"
            monthEndDate = displayedData.plusMonths(1).minusDays((displayedData.dayOfMonth).toLong()).format(endMonthFormatter).toString()
            tv_stats_non_recurring_engagement_month.text = displayedData.format(displayMonthFormatter).toString()

            FirestoreClass().getNonRecurringEngagementStats(this,
                dateFormatter.parse(monthStartDate)!!.time, dateFormatter.parse(monthEndDate)!!.time)
        }
    }

    private fun setupDisplayDate(){
        displayedData = LocalDate.now()
        monthStartDate = "01/${displayedData.format(startMonthFormatter)}"
        monthEndDate = displayedData.plusMonths(1).minusDays((displayedData.dayOfMonth).toLong()).format(endMonthFormatter).toString()

        FirestoreClass().getNonRecurringEngagementStats(this,
            dateFormatter.parse(monthStartDate)!!.time, dateFormatter.parse(monthEndDate)!!.time)
    }

    private fun showNonRecurringEngagementsStats(){
        tv_stats_non_recurring_engagements.text = "" +
                "${resources.getString(R.string.number_of_owned_non_recurring_engagements)}\n" +
                "${nonRecurringEngagementsStatsList[0]}\n\n" +
                "${resources.getString(R.string.number_of_assigned_non_recurring_engagements)}\n" +
                "${nonRecurringEngagementsStatsList[1]}\n\n" +
                "${resources.getString(R.string.longest_non_recurring_engagement_title)}\n" +
                "$longestEngagementTitle\n\n" +
                "${resources.getString(R.string.shortest_non_recurring_engagement_title)}\n" +
                "$shortestEngagementTitle"
    }

    private fun showStudyStats(){
        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${studyTimeList[0]/60000} min (${studyTimeList[0]/360000} h)" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${studyTimeList[1]/60000} min (${studyTimeList[1]/360000} h)" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${studyTimeList[2]/60000} min (${studyTimeList[2]/360000} h)" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${studyTimeList[3]/60000} min (${studyTimeList[3]/360000} h)" +
                "${resources.getString(R.string.friday)}:\n" +
                "${studyTimeList[4]/60000} min (${studyTimeList[4]/360000} h)" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${studyTimeList[5]/60000} min (${studyTimeList[5]/360000} h)" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${studyTimeList[6]/60000} min (${studyTimeList[6]/360000} h)"
    }

    private fun showWorkStats(){
        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${workTimeList[0]/60000} min (${workTimeList[0]/360000} h)" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${workTimeList[1]/60000} min (${workTimeList[1]/360000} h)" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${workTimeList[2]/60000} min (${workTimeList[2]/360000} h)" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${workTimeList[3]/60000} min (${workTimeList[3]/360000} h)" +
                "${resources.getString(R.string.friday)}:\n" +
                "${workTimeList[4]/60000} min (${workTimeList[4]/360000} h)" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${workTimeList[5]/60000} min (${workTimeList[5]/360000} h)" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${workTimeList[6]/60000} min (${workTimeList[6]/360000} h)"
    }

    private fun showOtherRegularEngagementsStats(){
        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${workTimeList[0]/60000} min (${otherRecurringEngagementsTimeList[0]/360000} h)" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${otherRecurringEngagementsTimeList[1]/60000} min (${otherRecurringEngagementsTimeList[1]/360000} h)" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${otherRecurringEngagementsTimeList[2]/60000} min (${otherRecurringEngagementsTimeList[2]/360000} h)" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${otherRecurringEngagementsTimeList[3]/60000} min (${otherRecurringEngagementsTimeList[3]/360000} h)" +
                "${resources.getString(R.string.friday)}:\n" +
                "${otherRecurringEngagementsTimeList[4]/60000} min (${otherRecurringEngagementsTimeList[4]/360000} h)" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${otherRecurringEngagementsTimeList[5]/60000} min (${otherRecurringEngagementsTimeList[5]/360000} h)" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${otherRecurringEngagementsTimeList[6]/60000} min (${otherRecurringEngagementsTimeList[6]/360000} h)"
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_statistics)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.statistics)
        }

        toolbar_statistics.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getUserStatistics(){

    }

    fun getNonRecurringEngagementStats(list: ArrayList<Int>, longestEngagement: String, shortestEngagement: String){
        nonRecurringEngagementsStatsList = list
        longestEngagementTitle = longestEngagement
        shortestEngagementTitle = shortestEngagement
        showNonRecurringEngagementsStats()
    }

    fun getRecurringEngagementStatsStudy(list: ArrayList<Long>){
        studyTimeList = list
    }

    fun getRecurringEngagementStatsWork(list: ArrayList<Long>){
        workTimeList = list
    }

    fun getRecurringEngagementStatsOther(list: ArrayList<Long>){
        otherRecurringEngagementsTimeList = list
    }
}