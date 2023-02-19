package rafal.iwanczyk.praca.inzynierska.seti.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import rafal.iwanczyk.praca.inzynierska.seti.activities.*
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.models.WeekEngagements
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants

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

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Data updated successfully!")

                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
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

    fun addUpdateDeleteNonRecurringEngagements(activity: Activity, nonRecurringEngagement: NonRecurringEngagement)
    {
        mFireStore.collection(Constants.NON_RECURRING_ENGAGEMENTS).document().set(nonRecurringEngagement, SetOptions.merge())
            .addOnSuccessListener {
                //TODO add
            }.addOnFailureListener {
                e -> //TODO add
            }
    }
    }