package rafal.iwanczyk.praca.inzynierska.seti.firebase

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import io.grpc.Context
import kotlinx.android.synthetic.main.activity_non_recurring_engagement_members.*
import kotlinx.coroutines.tasks.await
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.activities.*
import rafal.iwanczyk.praca.inzynierska.seti.adapters.RegularEngagementsAdapter
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
                createWeekPlan(WeekEngagements(getCurrentUserID()))
            }.addOnFailureListener {
                Log.e("Registration", "Error while registering")
            }
    }

    fun getCurrentUserID(): String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""

        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    suspend fun validateIfLoginCanBeUsed(login: String): Boolean {
            return getDataFromFirestore(login).isEmpty()
    }

    private suspend fun getDataFromFirestore(login: String): List<DocumentSnapshot>{
        val documents =  mFireStore.collection(Constants.USERS).whereEqualTo("login",login).get().await()
        return documents.documents
    }

    fun loadUserData(activity: Activity, readWeekPlanRegularEngagements: Boolean = false){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                val loggedInUser = document.toObject(User::class.java)!!
                println("USER DATA: ${loggedInUser.xDD}")
                when(activity){
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readWeekPlanRegularEngagements)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }.addOnFailureListener {
                e ->
                when(activity){
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e("LogIn", "Error while logging")
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Data updated successfully!")

                when(activity){
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName,"Error while updating data.", e)
                }
            }

    fun deleteUserAccount(user :User){

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser!!.delete()
        FirebaseStorage.getInstance().getReferenceFromUrl(user.image).delete()
        FirebaseStorage.getInstance().getReferenceFromUrl(user.background).delete()
        FirebaseFirestore.getInstance().collection(Constants.USERS)
            .document(getCurrentUserID())
            .delete()
            .addOnSuccessListener { task -> Log.i("Firestore deletion","Firestore document deleted") }
            .addOnFailureListener { task -> Log.e("Firestore error","Firestore document NOT deleted") }
    }

    fun createUpdateRegularEngagement(activity: Activity, weekEngagements: WeekEngagements){
        mFireStore.collection(Constants.WEEKPLAN).document(weekEngagements.documentID)
            .set(weekEngagements)
            .addOnSuccessListener {
                Toast.makeText(activity, "Regular engagement added successfully!", Toast.LENGTH_SHORT).show()

                when(activity){
                    is CreateRegularEngagementActivity ->{
                        activity.regularEngagementCreatedSuccessfully()
                    }
                    is RegularEngagementDetailsActivity -> {
                        activity.regularEngagementChangeSuccessful()
                    }
                }

            }
    }


    //Automatically creates empty week plan for newly registered user
    private fun createWeekPlan(weekEngagements: WeekEngagements){
        mFireStore.collection(Constants.WEEKPLAN).document().set(weekEngagements, SetOptions.merge())
            .addOnSuccessListener {
                println("STWORZONO PLAN")
            }.addOnFailureListener {
                println("Błąd w trakcie tworzenia :((")
            }
    }

    fun getWeekPlan(activity: MainActivity){
        mFireStore.collection(Constants.WEEKPLAN).whereEqualTo(Constants.ASSIGNED_TO,getCurrentUserID())
            .get().addOnSuccessListener {
                document -> Log.i(activity.javaClass.simpleName,document.documents.toString())
                val weekPlan: WeekEngagements = document.documents[0].toObject(WeekEngagements::class.java)!!
                weekPlan.documentID = document.documents[0].id
                activity.populateWeekPlanToUI(weekPlan)
            }.addOnFailureListener {
                e -> activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while populating week plan!")
            }
    }

    fun addNonRecurringEngagements(activity: CreateNonRecurringEngagementActivity, nonRecurringEngagement: NonRecurringEngagement)
    {
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS).document().set(nonRecurringEngagement, SetOptions.merge())
            .addOnSuccessListener {
                activity.addNonRecurringEngagementSuccessful()
            }.addOnFailureListener {
                activity.addNonRecurringEngagementFailed()
            }
    }

    fun getNonRecurringEngagements(activity: NonRecurringEngagementsActivity, startDate: Long, endDate: Long){
        val engagementsList: ArrayList<NonRecurringEngagement> = ArrayList()
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document1 ->
                for (i in document1.documents) {
                    val nonRecurringEngagement = i.toObject(NonRecurringEngagement::class.java)!!
                    nonRecurringEngagement.documentID = i.id

                    if ((nonRecurringEngagement.startDate in startDate..endDate)
                        || (nonRecurringEngagement.endDate in startDate..endDate)
                        || (nonRecurringEngagement.startDate < startDate && nonRecurringEngagement.endDate > endDate)
                    ) {
                        engagementsList.add(nonRecurringEngagement)
                    }
                }
                engagementsList.sortBy { it.startDate }
                activity.populateNonRecurringEngagementsToUI(engagementsList)
            }
    }

    //Previous approach
  /*

    fun getNonRecurringEngagements(activity: NonRecurringEngagementsActivity, startDate: Long, endDate: Long){
        val ownedEngagementsList: ArrayList<NonRecurringEngagement> = ArrayList()
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .whereEqualTo("owner", getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document1 ->
                for(i in document1.documents){
                    val nonRecurringEngagement = i.toObject(NonRecurringEngagement::class.java)!!
                    nonRecurringEngagement.documentID = i.id

                    if((nonRecurringEngagement.startDate in startDate..endDate)
                        || (nonRecurringEngagement.endDate in startDate..endDate)
                        || (nonRecurringEngagement.startDate < startDate && nonRecurringEngagement.endDate > endDate)){
                        ownedEngagementsList.add(nonRecurringEngagement)
                    }
                }
                //tmpList.sortBy { it.startDate }
                //activity.populateNonRecurringEngagementsToUI(tmpList)
                getNonRecurringEngagementAssigned(activity, startDate, endDate, ownedEngagementsList)
    }
    }

    private fun getNonRecurringEngagementAssigned(activity: NonRecurringEngagementsActivity, startDate: Long, endDate: Long, engagementsList: ArrayList<NonRecurringEngagement>){

        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document1 ->
                for (i in document1.documents) {
                    val nonRecurringEngagement = i.toObject(NonRecurringEngagement::class.java)!!
                    nonRecurringEngagement.documentID = i.id

                    if ((nonRecurringEngagement.startDate in startDate..endDate)
                        || (nonRecurringEngagement.endDate in startDate..endDate)
                        || (nonRecurringEngagement.startDate < startDate && nonRecurringEngagement.endDate > endDate)
                    ) {
                        engagementsList.add(nonRecurringEngagement)
                    }
                }
                engagementsList.sortBy { it.startDate }
                activity.populateNonRecurringEngagementsToUI(engagementsList)
            }
    }

   */

    fun getMemberDetails(activity: NonRecurringEngagementMembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found!")
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Database error!")
            }
    }

    fun assignMemberToNonRecurringEngagement(activity: NonRecurringEngagementMembersActivity,
                                             engagement: NonRecurringEngagement, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = engagement.assignedTo

        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .document(engagement.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Error while updating members!")
            }
    }

    fun removeMemberFromNonRecurringEngagement(activity: NonRecurringEngagementMembersActivity,
                                             engagement: NonRecurringEngagement, position: Int){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = engagement.assignedTo

        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .document(engagement.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberRemovedSuccess(position)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Error while updating members!")
            }
    }

    fun updateNonRecurringEngagement(activity: NonRecurringEngagementDetailsActivity, documentID: String, changeHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .document(documentID) // Document ID
            .update(changeHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener{
                activity.nonRecurringEngagementChangeSuccessful()
            }.addOnFailureListener {
                activity.nonRecurringEngagementChangeFailed()
            }
    }

    fun deleteNonRecurringEngagement(activity: NonRecurringEngagementDetailsActivity, documentID: String){
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
        .document(documentID)
        .delete()
        .addOnSuccessListener {
            activity.nonRecurringEngagementChangeSuccessful()
        }.addOnFailureListener {
            activity.nonRecurringEngagementChangeFailed()
        }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
    mFireStore.collection(Constants.USERS)
        .whereIn(Constants.ID, assignedTo)
        .get()
        .addOnSuccessListener {
            document ->
            val usersList: ArrayList<User> = ArrayList()

            for(i in document.documents){
                val assignedUser = i.toObject(User::class.java)!!
                usersList.add(assignedUser)
            }
            if(activity is NonRecurringEngagementMembersActivity){
                activity.setupMembersList(usersList)
            }else if(activity is NonRecurringEngagementDetailsActivity){
                activity.setupMembersToUI(usersList)
            }
        }.addOnFailureListener {
            if(activity is NonRecurringEngagementMembersActivity) {
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Error while downloading assigned users!")
            }else if(activity is NonRecurringEngagementDetailsActivity){
                activity.hideProgressDialog()
                activity.showErrorSnackBar("Error while downloading assigned users!")
            }
        }
    }

    fun getOwnerOfNonRecurringEngagement(activity: NonRecurringEngagementMembersActivity, ownerID: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.ID, ownerID)
            .get()
            .addOnSuccessListener {
                document ->
                val owner = document.documents[0].toObject(User::class.java)!!
                activity.setupOwner(owner)
            }.addOnFailureListener {
                activity.showErrorSnackBar("Owner's data downloading failed!")
                activity.tv_created_by.text = "Created by: - failed to download the data :("
                activity.ll_owner.visibility = View.GONE
            }
    }


    fun getNonRecurringEngagementStats(activity: StatisticsActivity, startDate: Long, endDate: Long){
        var assignedEngagementNumber: Int = 0
        var ownedEngagementsNumber: Int = 0
        val nonRecurringStatsList: ArrayList<Int> = ArrayList()
        var tmpNonRecurringEngagement: NonRecurringEngagement? = null
        var longestEngagementTitle: String = ""
        var shortestEngagementTitle: String = ""

        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document1 ->
                for (i in document1.documents) {
                    val nonRecurringEngagement = i.toObject(NonRecurringEngagement::class.java)!!
                    nonRecurringEngagement.documentID = i.id

                    if ((nonRecurringEngagement.startDate in startDate..endDate)
                        || (nonRecurringEngagement.endDate in startDate..endDate)
                        || (nonRecurringEngagement.startDate < startDate && nonRecurringEngagement.endDate > endDate)
                    ) {
                        if(nonRecurringEngagement.owner == getCurrentUserID()){
                            ownedEngagementsNumber++
                        }else{
                            assignedEngagementNumber++
                        }

                        if(tmpNonRecurringEngagement != null){
                            longestEngagementTitle = if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                > (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)) {
                                nonRecurringEngagement.name
                            }else if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                == (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)
                                && (nonRecurringEngagement.endTime - nonRecurringEngagement.startTime)
                                > (tmpNonRecurringEngagement!!.endTime - tmpNonRecurringEngagement!!.startTime)){
                                nonRecurringEngagement.name
                            }else if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                == (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)
                                && (nonRecurringEngagement.endTime - nonRecurringEngagement.startTime)
                                == (tmpNonRecurringEngagement!!.endTime - tmpNonRecurringEngagement!!.startTime)){
                                nonRecurringEngagement.name + ", " + tmpNonRecurringEngagement!!.name
                            }
                            else{
                                tmpNonRecurringEngagement!!.name
                            }

                            shortestEngagementTitle = if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                < (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)) {
                                nonRecurringEngagement.name
                            }else if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                == (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)
                                && (nonRecurringEngagement.endTime - nonRecurringEngagement.startTime)
                                < (tmpNonRecurringEngagement!!.endTime - tmpNonRecurringEngagement!!.startTime)){
                                nonRecurringEngagement.name
                            }else if((nonRecurringEngagement.endDate - nonRecurringEngagement.startDate)
                                == (tmpNonRecurringEngagement!!.endDate - tmpNonRecurringEngagement!!.startDate)
                                && (nonRecurringEngagement.endTime - nonRecurringEngagement.startTime)
                                == (tmpNonRecurringEngagement!!.endTime - tmpNonRecurringEngagement!!.startTime)){
                                nonRecurringEngagement.name + ", " + tmpNonRecurringEngagement!!.name
                            }
                            else{
                                tmpNonRecurringEngagement!!.name
                            }
                        }
                    }
                    tmpNonRecurringEngagement = nonRecurringEngagement
                }
                nonRecurringStatsList.add(0, ownedEngagementsNumber)
                nonRecurringStatsList.add(1, assignedEngagementNumber)
                activity.getNonRecurringEngagementStats(nonRecurringStatsList, longestEngagementTitle, shortestEngagementTitle)
            }
    }


    fun getRegularEngagementStats(activity: StatisticsActivity){
        var tmpStudyTime: Long = 0
        var tmpWorkTime: Long = 0
        var tmpOtherTime: Long = 0
        val studyTimeList: ArrayList<Long> = ArrayList(7)
        val workTimeList: ArrayList<Long> = ArrayList(7)
        val otherTimeList: ArrayList<Long> = ArrayList(7)
        val studyString: String = if(Locale.getDefault().displayLanguage == "English") {
           "Study"
        }else{
            "Studia"
        }
        val workString: String = if(Locale.getDefault().displayLanguage == "English") {
            "Work"
        }else{
            "Praca"
        }

        mFireStore.collection(Constants.WEEKPLAN).whereEqualTo(Constants.ASSIGNED_TO,getCurrentUserID())
            .get().addOnSuccessListener {
                    document -> Log.i(activity.javaClass.simpleName,document.documents.toString())
                val weekPlan: WeekEngagements = document.documents[0].toObject(WeekEngagements::class.java)!!
                weekPlan.documentID = document.documents[0].id

                for(i in weekPlan.mondayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(0, tmpStudyTime)
                workTimeList.add(0, tmpWorkTime)
                otherTimeList.add(0, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.tuesdayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(1, tmpStudyTime)
                workTimeList.add(1, tmpWorkTime)
                otherTimeList.add(1, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.wednesdayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(2, tmpStudyTime)
                workTimeList.add(2, tmpWorkTime)
                otherTimeList.add(2, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.thursdayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(3, tmpStudyTime)
                workTimeList.add(3, tmpWorkTime)
                otherTimeList.add(3, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.fridayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(4, tmpStudyTime)
                workTimeList.add(4, tmpWorkTime)
                otherTimeList.add(4, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.saturdayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(5, tmpStudyTime)
                workTimeList.add(5, tmpWorkTime)
                otherTimeList.add(5, tmpOtherTime)
                tmpStudyTime = 0
                tmpWorkTime = 0
                tmpOtherTime = 0

                for(i in weekPlan.sundayEngagements){
                    if(i.typeOfEngagement == studyString) {
                        tmpStudyTime += (i.endTime - i.startTime)
                    }else if(i.typeOfEngagement == workString){
                        tmpWorkTime += (i.endTime - i.startTime)
                    }else{
                        tmpOtherTime += (i.endTime - i.startTime)
                    }
                }
                studyTimeList.add(6, tmpStudyTime)
                workTimeList.add(6, tmpWorkTime)
                otherTimeList.add(6, tmpOtherTime)

                activity.getRecurringEngagementStats(studyTimeList, workTimeList, otherTimeList)

            }.addOnFailureListener {
                Log.e(activity.javaClass.simpleName,"Error while downloading statistics!")
            }
    }

    fun checkIfUserNeedsHighContrastTheme(activity: Activity){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                val loggedInUser = document.toObject(User::class.java)!!
                if(loggedInUser.xDD == 1) {
                    when (activity) {
                        is MainActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is MyProfileActivity -> {
                           activity.displayHighContrastTheme()
                        }
                        is CreateRegularEngagementActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is CreateNonRecurringEngagementActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is RegularEngagementDetailsActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is NonRecurringEngagementDetailsActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is NonRecurringEngagementMembersActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is NonRecurringEngagementsActivity -> {
                            activity.displayHighContrastTheme()
                        }
                        is StatisticsActivity -> {
                            activity.displayHighContrastTheme()
                        }
                    }
                }
            }
    }

}