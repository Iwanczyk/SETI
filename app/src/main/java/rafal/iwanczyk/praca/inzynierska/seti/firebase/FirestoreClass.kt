package rafal.iwanczyk.praca.inzynierska.seti.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
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
            }
    }

    fun getCurrentUserID(): String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    /*
    fun validateIfLoginCanBeUsed(login: String): Boolean{

        var isLoginFree: Boolean = false

        mFireStore.collection(Constants.USERS)
            .whereEqualTo("login",login)
            .get()
            .addOnSuccessListener{
             document ->
                if(document.documents.isEmpty()) {
                    isLoginFree = true
                }
            }.addOnFailureListener {
                Log.e("LoginCheck", "Error while checking login")
            }
        return isLoginFree
    }

     */

    suspend fun validateIfLoginCanBeUsed(login: String): Boolean {
            return getDataFromFirestore(login).isEmpty()
    }

    private suspend fun getDataFromFirestore(login: String): List<DocumentSnapshot>{
        val documents =  mFireStore.collection(Constants.USERS).whereEqualTo("login",login).get().await()
        return documents.documents
    }

}