package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable

data class RegularEngagement (
    val assignedTo: String = "",
    val name: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val note: String = "",
    val typeOfEngagement: String = "",
    val lectureRoom: String = "",
    val buildingNumber: String = ""
        ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(assignedTo)
        parcel.writeString(name)
        parcel.writeString(day)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(note)
        parcel.writeString(typeOfEngagement)
        parcel.writeString(lectureRoom)
        parcel.writeString(buildingNumber)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<RegularEngagement> {
        override fun createFromParcel(parcel: Parcel): RegularEngagement {
            return RegularEngagement(parcel)
        }

        override fun newArray(size: Int): Array<RegularEngagement?> {
            return arrayOfNulls(size)
        }
    }

}