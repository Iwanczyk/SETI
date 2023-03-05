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

    const val NON_RECURRING_ENGAGEMENT_NAME: String = "name"
    const val NON_RECURRING_ENGAGEMENT_START_DATE: String = "startDate"
    const val NON_RECURRING_ENGAGEMENT_START_TIME: String = "startTime"
    const val NON_RECURRING_ENGAGEMENT_END_DATE: String = "endDate"
    const val NON_RECURRING_ENGAGEMENT_END_TIME: String = "endTime"
    const val NON_RECURRING_ENGAGEMENT_NOTE: String = "note"

    const val ID: String = "id"

    const val SETI_PREFERENCES = "SETIPrefs"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAAXOnZqfk:APA91bHw2WrL7ZYEpc8mzjke0m8YGEGS7-VD5yRCUm6MQTF8MfBMQwP3ynyVU8EVWv9uRw59u-yvIxcRRI-CQPRkVmSXF5qquu0xVYVc_8XG4j_akhDdDFNytQ8cvEo1MJKeQRVrS6BQ"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    fun showImageChooser(activity: Activity){
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}