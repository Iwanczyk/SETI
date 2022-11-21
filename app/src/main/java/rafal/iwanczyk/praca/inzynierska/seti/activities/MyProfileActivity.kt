package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.dialog_delete_account.*
import kotlinx.coroutines.launch
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.firebase.FirestoreClass
import rafal.iwanczyk.praca.inzynierska.seti.models.User
import rafal.iwanczyk.praca.inzynierska.seti.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private lateinit var mUserDetails: User
    private var mSelectedImageFileURI: Uri? = null
    private var mSelectedImage: String = ""
    private var mSelectedImageAvatarURI: Uri? = null
    private var mSelectedImageAvatarURL: String = ""
    private var mSelectedImageBackgroundURI: Uri? = null
    private var mSelectedImageBackgroundURL: String = ""
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        //Check if storage access permission is granted
        my_profile_iv_user_image.setOnClickListener {
            mSelectedImage = Constants.AVATAR_IMAGE_CHOSEN

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        my_profile_iv_user_background_image.setOnClickListener {
            mSelectedImage = Constants.BACKGROUND_IMAGE_CHOSEN

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update.setOnClickListener {
            if(mSelectedImageFileURI != null){
                uploadUserImage()
            }else {
                showProgressDialog()
                updateUserProfileData()
            }
        }

        btn_delete_account.setOnClickListener {
            showDeleteAccountDialog()
        }

        sw_my_profile_edit_email.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                my_profile_et_email.visibility = View.VISIBLE
                my_profile_et_current_password.visibility = View.VISIBLE
                tv_my_profile_edit_sensitive_data.visibility = View.VISIBLE
            }else{
                my_profile_et_email.visibility = View.GONE
                my_profile_et_email.setText(mUserDetails.email)
                my_profile_et_current_password.setText("")
                    if(!sw_my_profile_edit_password.isChecked){
                        tv_my_profile_edit_sensitive_data.visibility = View.GONE
                        my_profile_et_current_password.visibility = View.GONE
                    }
            }
        }

        sw_my_profile_edit_password.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                my_profile_et_current_password.visibility = View.VISIBLE
                my_profile_et_new_password.visibility = View.VISIBLE
                my_profile_et_repeat_new_password.visibility = View.VISIBLE
                tv_my_profile_edit_sensitive_data.visibility = View.VISIBLE
            }else{
                my_profile_et_new_password.visibility = View.GONE
                my_profile_et_repeat_new_password.visibility = View.GONE

                my_profile_et_current_password.setText("")
                my_profile_et_new_password.setText("")
                my_profile_et_repeat_new_password.setText("")

                    if(!sw_my_profile_edit_email.isChecked){
                        tv_my_profile_edit_sensitive_data.visibility = View.GONE
                        my_profile_et_current_password.visibility = View.GONE
                    }
            }
        }

    }

    //Ask for permission and handle the result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            }
        }else{
            showToast(this, resources.getString(R.string.permission_denied))
        }
    }

    //get the chosen image from the gallery and load it into correct place
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){

            mSelectedImageFileURI = data.data

            if(mSelectedImage == Constants.AVATAR_IMAGE_CHOSEN){
                mSelectedImageAvatarURI = mSelectedImageFileURI
            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileURI)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(my_profile_iv_user_image)
            }catch (e : IOException){
                e.printStackTrace()
            }
            }else if(mSelectedImage == Constants.BACKGROUND_IMAGE_CHOSEN){
                mSelectedImageBackgroundURI = mSelectedImageFileURI
                try {
                    Glide
                        .with(this@MyProfileActivity)
                        .load(mSelectedImageFileURI)
                        .centerCrop()
                        .placeholder(R.drawable.nav_header_main_background)
                        .into(my_profile_iv_user_background_image)
                }catch (e : IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    //Upload user Image to Firebase Storage
    private fun uploadUserImage(){
        showProgressDialog()

        //User avatar changed
        if(mSelectedImageAvatarURI != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE${mUserDetails.login}"+System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this, mSelectedImageAvatarURI))

            sRef.putFile(mSelectedImageAvatarURI!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URL", uri.toString())

                        mSelectedImageAvatarURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener{
                    exception ->
                showErrorSnackBar(exception.message.toString())
                hideProgressDialog()
            }
        }

        //User background changed
        if(mSelectedImageBackgroundURI != null){
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_BACKGROUND${mUserDetails.login}"+System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this, mSelectedImageBackgroundURI))

            sRef.putFile(mSelectedImageBackgroundURI!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URL", uri.toString())

                    mSelectedImageBackgroundURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener{
                    exception ->
                showErrorSnackBar(exception.message.toString())
                hideProgressDialog()
            }
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false
        auth = FirebaseAuth.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        lifecycleScope.launch{
            //Edit basic data
            if(mSelectedImageAvatarURL.isNotEmpty() && mSelectedImageAvatarURL != mUserDetails.image){
                userHashMap[Constants.IMAGE] = mSelectedImageAvatarURL
                anyChangesMade = true
            }
            if(mSelectedImageBackgroundURL.isNotEmpty() && mSelectedImageBackgroundURL != mUserDetails.background){
                userHashMap[Constants.BACKGROUND] = mSelectedImageBackgroundURL
                anyChangesMade = true
            }
            if(my_profile_et_name.text.toString() != mUserDetails.name){
                userHashMap[Constants.NAME] = my_profile_et_name.text.toString()
                anyChangesMade = true
            }
            if(my_profile_et_login.text.toString() != mUserDetails.login){
                if(FirestoreClass().validateIfLoginCanBeUsed(my_profile_et_login.text.toString())){
                    userHashMap[Constants.LOGIN] = my_profile_et_login.text.toString()
                    anyChangesMade = true
                }else{
                    showErrorSnackBar(resources.getString(R.string.login_already_used))
                }
            }

            //Edit email and password
            if(sw_my_profile_edit_email.isChecked
                && validatePasswordField(my_profile_et_current_password.text.toString())
                && my_profile_et_email.text.toString() != mUserDetails.email){
                userHashMap[Constants.EMAIL] = my_profile_et_email.text.toString()

                val credential = EmailAuthProvider
                    .getCredential(mUserDetails.email, my_profile_et_current_password.text.toString())

                firebaseUser!!.reauthenticate(credential)
                    .addOnCompleteListener {
                            task ->
                        if (task.isSuccessful){
                            firebaseUser.updateEmail(my_profile_et_email.text.toString())
                                .addOnCompleteListener {
                                        task ->
                                    if (task.isSuccessful) {
                                        Log.d("Email updated", "User email address updated.")
                                    }
                                }.addOnFailureListener {
                                    Log.e("Email update error", "User email address NOT updated.")
                                }
                        }
                    }.addOnFailureListener {
                        showErrorSnackBar("Authorization failed")
                    }

                anyChangesMade = true
            }
            if(sw_my_profile_edit_password.isChecked
                && validateEditPasswordFields(my_profile_et_current_password.text.toString(),
                    my_profile_et_new_password.text.toString(), my_profile_et_repeat_new_password.text.toString())){

                val credential = EmailAuthProvider
                    .getCredential(my_profile_et_email.text.toString(), my_profile_et_current_password.text.toString())

                firebaseUser!!.reauthenticate(credential)
                    .addOnCompleteListener {
                            task ->
                        if (task.isSuccessful){
                        firebaseUser.updatePassword(my_profile_et_new_password.text.toString())
                            .addOnCompleteListener {
                                    task ->
                                if (task.isSuccessful) {
                                    Log.d("Password updated", "User password updated.")
                                }
                            }.addOnFailureListener {  Log.e("Password NOT updated", "User password NOT updated.") }
                        }
                    }.addOnFailureListener { showErrorSnackBar("Authorization failed") }

                anyChangesMade = true
            }

            //TODO ADD EMAIL EDIT AND PASSWORD EDIT + RESETTING PASSWORD IN SING IN ACTIVITY

            if(anyChangesMade) {
                FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
            }

            hideProgressDialog()
        }
    }

    private fun validateEditPasswordFields(currentPassword: String, newPassword: String,
                                           repeatPassword: String): Boolean{
        return if(currentPassword.isBlank() || newPassword.isBlank() || repeatPassword.isBlank()){
            showErrorSnackBar(resources.getString(R.string.please_enter_your_password))
            false
        }else if(newPassword != repeatPassword){
            showErrorSnackBar(resources.getString(R.string.passwords_not_matching))
            false
        }else{
            true
        }
    }

    private fun validatePasswordField(password: String): Boolean{
        return if(password.isBlank()){
            showErrorSnackBar(resources.getString(R.string.please_enter_your_password))
            false
        }else{
            true
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){

        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_icon_24dp)
            actionBar.title = resources.getString(R.string.nav_my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User){

        showProgressDialog()
        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(my_profile_iv_user_image)

        my_profile_et_login.setText(user.login)
        my_profile_et_name.setText(user.name)
        my_profile_et_email.setText(user.email)

        Glide
            .with(this@MyProfileActivity)
            .load(user.background)
            .centerCrop()
            .placeholder(R.drawable.nav_header_main_background)
            .into(my_profile_iv_user_background_image)

        hideProgressDialog()
    }

    private fun showDeleteAccountDialog(){
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_account)

        dialog.tv_cancel_delete.setOnClickListener {
            dialog.dismiss()
        }

        dialog.tv_confirm_delete.setOnClickListener {
            showProgressDialog()
            FirestoreClass().deleteUserAccount(mUserDetails)
            hideProgressDialog()
            val intent = Intent(this, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}