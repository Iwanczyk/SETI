package rafal.iwanczyk.praca.inzynierska.seti.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
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
        if(my_profile_et_login.text.toString() != mUserDetails.login){ //TODO ADD VERIFICATION IF LOGIN CAN BE USED
            userHashMap[Constants.LOGIN] = my_profile_et_login.text.toString()
            anyChangesMade = true
        }

        if(anyChangesMade) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }

        hideProgressDialog()
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
}