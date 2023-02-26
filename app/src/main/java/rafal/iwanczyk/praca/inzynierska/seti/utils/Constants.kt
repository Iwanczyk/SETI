package rafal.iwanczyk.praca.inzynierska.seti.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERS: String = "Users"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val AVATAR_IMAGE_CHOSEN = "Avatar"
    const val BACKGROUND_IMAGE_CHOSEN = "Background"
    const val NAME: String = "name"
    const val LOGIN: String = "login"
    const val IMAGE: String = "image"
    const val BACKGROUND: String = "background"
    const val EMAIL: String = "email"

    const val WEEKPLAN: String = "weekPlan"
    const val REGULAR_ENGAGEMENT = "regularEngagement"
    const val REGULAR_ENGAGEMENT_LIST_POSITION = "regular_engagement_list_position"
    const val DISPLAYED_DAY_OF_WEEK = "displayed_day_of_week"

    const val ASSIGNED_TO: String = "assignedTo"

    const val NON_RECURRING_ENGAGEMENT: String = "NonRecurringEngagement"
    const val NON_RECURRING_ENGAGEMENTS: String = "NonRecurringEngagements"

    const val START_DATE: String = "startDate"
    const val END_DATE: String = "endDate"



    fun showImageChooser(activity: Activity){
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}