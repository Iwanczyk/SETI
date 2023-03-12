package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_statistics.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Duration
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

        getRegularEngagementStatistics() //Get regular engagements statistics
        setupDisplayDate() //Setup current month displayed & get non-recurring engagement statistics

        val statTypeList: MutableList<String> = resources.getStringArray(R.array.StatsType).toMutableList()
        val statTypeListAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item, statTypeList)
        dropdown_list_chose_type_of_stats.adapter = statTypeListAdapter

        dropdown_list_chose_type_of_stats.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(dropdown_list_chose_type_of_stats.getItemAtPosition(position).toString() == statTypeList[0]){
                    ll_stats_regular_engagements.visibility = View.VISIBLE
                    ll_stats_non_recurring_engagements.visibility = View.GONE

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
                    showStudyStats()
                }
                R.id.rb_stats_regular_engagements_work -> {
                    showWorkStats()
                }
                R.id.rb_stats_regular_engagements_other -> {
                    showOtherRegularEngagementsStats()
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

        FirestoreClass().checkIfUserNeedsHighContrastTheme(this)
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
        val manWeekTime = studyTimeList[0]+studyTimeList[1]+studyTimeList[2]+studyTimeList[3]+studyTimeList[4]
        val weekendTime = studyTimeList[5]+studyTimeList[6]

        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${studyTimeList[0]/60000/60} h ${(studyTimeList[0]/60000)%60} min\n\n" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${studyTimeList[1]/60000/60} h ${(studyTimeList[1]/60000)%60} min\n\n" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${studyTimeList[2]/60000/60} h ${(studyTimeList[2]/60000)%60} min\n\n" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${studyTimeList[3]/60000/60} h ${(studyTimeList[3]/60000)%60} min\n\n" +
                "${resources.getString(R.string.friday)}:\n" +
                "${studyTimeList[4]/60000/60} h ${(studyTimeList[4]/60000)%60} min\n\n" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${studyTimeList[5]/60000/60} h ${(studyTimeList[5]/60000)%60} min\n\n" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${studyTimeList[6]/60000/60} h ${(studyTimeList[6]/60000)%60} min\n\n" +
                "${resources.getString(R.string.man_week)}:\n" +
                "${manWeekTime/60000/60} h ${(manWeekTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.weekend)}:\n" +
                "${weekendTime/60000/60} h ${(weekendTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.full_week)}:\n" +
                "${(manWeekTime + weekendTime)/60000/60} h ${((manWeekTime + weekendTime)/60000)%60} min"
    }

    private fun showWorkStats(){
        val manWeekTime = workTimeList[0]+workTimeList[1]+workTimeList[2]+workTimeList[3]+workTimeList[4]
        val weekendTime = workTimeList[5]+workTimeList[6]

        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${workTimeList[0]/60000/60} h ${(workTimeList[0]/60000)%60} min\n\n" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${workTimeList[1]/60000/60} h ${(workTimeList[1]/60000)%60} min\n\n" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${workTimeList[2]/60000/60} h ${(workTimeList[2]/60000)%60} min\n\n" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${workTimeList[3]/60000/60} h ${(workTimeList[3]/60000)%60} min\n\n" +
                "${resources.getString(R.string.friday)}:\n" +
                "${workTimeList[4]/60000/60} h ${(workTimeList[4]/60000)%60} min\n\n" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${workTimeList[5]/60000/60} h ${(workTimeList[5]/60000)%60} min\n\n" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${workTimeList[6]/60000/60} h ${(workTimeList[6]/60000)%60} min\n\n" +
                "${resources.getString(R.string.man_week)}:\n" +
                "${manWeekTime/60000/60} h ${(manWeekTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.weekend)}:\n" +
                "${weekendTime/60000/60} h ${(weekendTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.full_week)}:\n" +
                "${(manWeekTime + weekendTime)/60000/60} h ${((manWeekTime + weekendTime)/60000)%60} min"
    }

    private fun showOtherRegularEngagementsStats(){
        val manWeekTime = otherRecurringEngagementsTimeList[0]+otherRecurringEngagementsTimeList[1]+otherRecurringEngagementsTimeList[2]+otherRecurringEngagementsTimeList[3]+otherRecurringEngagementsTimeList[4]
        val weekendTime = otherRecurringEngagementsTimeList[5]+otherRecurringEngagementsTimeList[6]

        tv_stats_regular_engagements.text = "${resources.getString(R.string.monday)}:\n" +
                "${otherRecurringEngagementsTimeList[0]/60000/60} h ${(otherRecurringEngagementsTimeList[0]/60000)%60} min\n\n" +
                "${resources.getString(R.string.tuesday)}:\n" +
                "${otherRecurringEngagementsTimeList[1]/60000/60} h ${(otherRecurringEngagementsTimeList[1]/60000)%60} min\n\n" +
                "${resources.getString(R.string.wednesday)}:\n" +
                "${otherRecurringEngagementsTimeList[2]/60000/60} h ${(otherRecurringEngagementsTimeList[2]/60000)%60} min\n\n" +
                "${resources.getString(R.string.thursday)}:\n" +
                "${otherRecurringEngagementsTimeList[3]/60000/60} h ${(otherRecurringEngagementsTimeList[3]/60000)%60} min\n\n" +
                "${resources.getString(R.string.friday)}:\n" +
                "${otherRecurringEngagementsTimeList[4]/60000/60} h ${(otherRecurringEngagementsTimeList[4]/60000)%60} min\n\n" +
                "${resources.getString(R.string.saturday)}:\n" +
                "${otherRecurringEngagementsTimeList[5]/60000/60} h ${(otherRecurringEngagementsTimeList[5]/60000)%60} min\n\n" +
                "${resources.getString(R.string.sunday)}:\n" +
                "${otherRecurringEngagementsTimeList[6]/60000/60} h ${(otherRecurringEngagementsTimeList[6]/60000)%60} min\n\n" +
                "${resources.getString(R.string.man_week)}:\n" +
                "${manWeekTime/60000/60} h ${(manWeekTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.weekend)}:\n" +
                "${weekendTime/60000/60} h ${(weekendTime/60000)%60} min\n\n" +
                "${resources.getString(R.string.full_week)}:\n" +
                "${(manWeekTime + weekendTime)/60000/60} h ${((manWeekTime + weekendTime)/60000)%60} min"
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

    private fun getRegularEngagementStatistics(){
        FirestoreClass().getRegularEngagementStats(this)
    }

    fun getNonRecurringEngagementStats(list: ArrayList<Int>, longestEngagement: String, shortestEngagement: String){
        nonRecurringEngagementsStatsList = list
        longestEngagementTitle = longestEngagement
        shortestEngagementTitle = shortestEngagement
        showNonRecurringEngagementsStats()
    }

    fun getRecurringEngagementStats(studyList: ArrayList<Long>, workList: ArrayList<Long>, otherList: ArrayList<Long>)
    {
        studyTimeList = studyList
        workTimeList = workList
        otherRecurringEngagementsTimeList = otherList
    }

    fun displayHighContrastTheme(){
        ll_statistics.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        ll_stats_regular_engagements.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        ll_stats_non_recurring_engagements.setBackgroundColor(resources.getColor(R.color.background_disability_color))
        rb_stats_regular_engagements_study.setTextColor(resources.getColor(R.color.text_color_disability))
        rb_stats_regular_engagements_work.setTextColor(resources.getColor(R.color.text_color_disability))
        rb_stats_regular_engagements_other.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_stats_regular_engagements.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_stats_previous_month.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_stats_non_recurring_engagement_month.setTextColor(resources.getColor(R.color.text_color_disability))
        btn_stats_next_month.setTextColor(resources.getColor(R.color.text_color_disability))
        tv_stats_non_recurring_engagements.setTextColor(resources.getColor(R.color.text_color_disability))
    }

}