package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable

data class RegularEngagement (
    val assignedTo: String = "",
    var name: String = "",
    var day: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var note: String = "",
    var typeOfEngagement: String = "",
    var lectureRoom: String = "",
    var buildingNumber: String = ""
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