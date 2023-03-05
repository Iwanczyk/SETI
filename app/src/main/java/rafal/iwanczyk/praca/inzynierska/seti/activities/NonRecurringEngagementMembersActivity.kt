package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_non_recurring_engagement_members.*
import kotlinx.android.synthetic.main.activity_regular_engagement_details.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import kotlinx.android.synthetic.main.item_member.view.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.adapters.MemberListItemsAdapter
import rafal.iwanczyk.praca.inzynierska.seti.adapters.NonRecurringEngagementsAdapter
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class NonRecurringEngagementMembersActivity : BaseActivity(), TextToSpeech.OnInitListener {

    private lateinit var mNonRecurringEngagement: NonRecurringEngagement
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangeMade: Boolean = false
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_non_recurring_engagement_members)

        if(intent.hasExtra(Constants.NON_RECURRING_ENGAGEMENT)){
            mNonRecurringEngagement = intent.getParcelableExtra<NonRecurringEngagement>(Constants.NON_RECURRING_ENGAGEMENT)!!
        }

        setupActionBar()
        tts = TextToSpeech(this, this)

        FirestoreClass().getAssignedMembersListDetails(this, mNonRecurringEngagement.assignedTo)
        FirestoreClass().getOwnerOfNonRecurringEngagement(this, mNonRecurringEngagement.owner)
        hideProgressDialog()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_members_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.assigned_members)
        }
        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.members_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email = dialog.et_email_search_member.text.toString()

            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog()
                FirestoreClass().getMemberDetails(this, email)
            }else{
                showToast(this, resources.getString(R.string.please_enter_users_email))
            }

        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberDetails(user: User){
        mNonRecurringEngagement.assignedTo.add(user.id)
        FirestoreClass().assignMemberToNonRecurringEngagement(
            this@NonRecurringEngagementMembersActivity, mNonRecurringEngagement, user)
    }

    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        setupMembersList(mAssignedMembersList)
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

    fun setupMembersList(assignedMembersList: ArrayList<User>){
        mAssignedMembersList = assignedMembersList

        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this, assignedMembersList)
        rv_members_list.adapter = adapter

        adapter.setOnClickListener(object: MemberListItemsAdapter.OnClickListener{
            override fun onClick(position: Int, model: User) {
                //TODO dodać opcję usuwania
            }
        })

        adapter.setOnLongClickListener(object: MemberListItemsAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: User) {
                speakOut(model.login, model.name, model.email)
            }
        })
    }

    fun setupOwner(owner: User){
        Glide
            .with(this)
            .load(owner.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_owner_image)

        tv_owner_login.text = owner.login
        tv_owner_name.text = owner.name
        tv_owner_email.text = owner.email
    }

    private fun speakOut(login: String, name: String, email: String){
        tts?.speak("${resources.getString(R.string.selected_user)}:$login", TextToSpeech.QUEUE_ADD, null, "")
        if(name.isNotBlank())
        {
            tts?.speak("${resources.getString(R.string.name)}: $name", TextToSpeech.QUEUE_ADD, null, "")
        }
        tts?.speak("${resources.getString(R.string.email)}: $email", TextToSpeech.QUEUE_ADD, null, "")
        }

    override fun onDestroy() {
        super.onDestroy()
        if(tts != null){
            tts?.stop()
            tts?.shutdown()
        }
    }
}