package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable

data class NonRecurringEngagement (
    val documentID: String = "",
    var owner: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val name: String = "",
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val note: String = ""
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(documentID)
        parcel.writeString(owner)
        parcel.writeStringList(assignedTo)
        parcel.writeString(name)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
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