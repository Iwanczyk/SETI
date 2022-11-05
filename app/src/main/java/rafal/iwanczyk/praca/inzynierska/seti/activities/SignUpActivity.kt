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

    private fun validateForm(name:String, email:String, password:String):Boolean{
        return when {
            TextUtils.isEmpty(name)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_name))
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_email))
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar(resources.getString(R.string.please_enter_your_phone))
                false
            }else -> {
                true
            }
        }
    }

    private fun registerUser(){
        val name: String = et_name?.text.toString().trim()
        val email: String = et_email?.text.toString()
        val password: String = et_password?.text.toString().trim()

        if(validateForm(name, email, password)){
            showToast(this, "Data correct")
        }
    }
}