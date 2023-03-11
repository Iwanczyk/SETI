package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class NonRecurringEngagement (
    var documentID: String = "",
    var owner: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val name: String = "",
    val startDate: Long = 0,
    val startTime: Long = 0,
    val endDate: Long = 0,
    val endTime: Long = 0,
    val note: String = "",
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentID)
        parcel.writeString(owner)
        parcel.writeStringList(assignedTo)
        parcel.writeString(name)
        parcel.writeLong(startDate)
        parcel.writeLong(startTime)
        parcel.writeLong(endDate)
        parcel.writeLong(endTime)
        parcel.writeString(note)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NonRecurringEngagement> {
        override fun createFromParcel(parcel: Parcel): NonRecurringEngagement {
            return NonRecurringEngagement(parcel)
        }

        override fun newArray(size: Int): Array<NonRecurringEngagement?> {
            return arrayOfNulls(size)
        }
    }

}