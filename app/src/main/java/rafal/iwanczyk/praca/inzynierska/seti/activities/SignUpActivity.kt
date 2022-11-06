package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.User

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //Showing activity in fullscreen mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setupActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
        }
        toolbar_sign_up_activity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun registerUser(){
        val login: String = et_login?.text.toString().trim()
        val email: String = et_email?.text.toString()
        val password: String = et_password?.text.toString().trim()
        val repeatedPassword: String = et_repeat_password?.text.toString().trim()

        if(validateForm(login, email, password, repeatedPassword)){
            showProgressDialog()
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid,login,registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString())
                        hideProgressDialog()
                    }
                }
        }
    }

    private fun validateForm(login:String, email:String, password:String, repeatedPassword: String): Boolean{
        return (validateEmptyForm(login, email, password, repeatedPassword)
                && validatePasswords(password, repeatedPassword)
                && checkIfLoginCanBeUsed(login))
    }

    private fun validateEmptyForm(login:String, email:String,
                                  password:String, repeatedPassword: String): Boolean{
        return when {
            TextUtils.isEmpty(login)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_login))
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_email))
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_password))
                false
            }
            TextUtils.isEmpty(repeatedPassword)->{
                showErrorSnackBar(resources.getString(R.string.please_repeat_your_password))
                false
            }else -> {
                true
            }
        }
    }

    private fun validatePasswords(password: String, repeatedPassword: String): Boolean{
        return if(password == repeatedPassword){
            true
        }else{
            showErrorSnackBar(resources.getString(R.string.passwords_not_matching))
            false
        }
    }

    private fun checkIfLoginCanBeUsed(login: String): Boolean{

        println("CHECKIFLOGINCANBEUSED: ${FirestoreClass().validateIfLoginCanBeUsed(login)}")
        /*
        return if (FirestoreClass().validateIfLoginCanBeUsed(login)){
            true
        }else{
            showErrorSnackBar("Login is already used")
            false
        }

         */

        return false
    }

    fun userRegisteredSuccess(){
        showToast(this, resources.getString(R.string.registration_success))
        hideProgressDialog()
    }
}