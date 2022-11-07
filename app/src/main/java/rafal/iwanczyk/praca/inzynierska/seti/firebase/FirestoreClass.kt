package rafal.iwanczyk.praca.inzynierska.seti.firebase

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import rafal.iwanczyk.praca.inzynierska.seti.activities.MainActivity
import rafal.iwanczyk.praca.inzynierska.seti.activities.SignInActivity
import rafal.iwanczyk.praca.inzynierska.seti.activities.SignUpActivity
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
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

    fun signInUser(activity: Activity){
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
                        activity.updateNavigationUserDetails(loggedInUser)
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

}