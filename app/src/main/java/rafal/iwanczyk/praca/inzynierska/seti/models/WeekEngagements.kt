package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable

data class WeekEngagements (
    val assignedTo: String = "",
    var documentID: String = "",
    val mondayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val tuesdayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val wednesdayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val thursdayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val fridayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val saturdayEngagements: ArrayList<RegularEngagement> = ArrayList(),
    val sundayEngagements: ArrayList<RegularEngagement> = ArrayList()
    //val weeklyEngagementsArray: Array<ArrayList<RegularEngagement>> = Array(7){ArrayList()},
    //val weeklyEngagementsArray: Array<ArrayList<RegularEngagement>> = Array<ArrayList<RegularEngagement>> (7){ArrayList<RegularEngagement>()}
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
        parcel.createTypedArrayList(RegularEngagement.CREATOR)!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(assignedTo)
        parcel.writeString(documentID)
        parcel.writeTypedList(mondayEngagements)
        parcel.writeTypedList(tuesdayEngagements)
        parcel.writeTypedList(wednesdayEngagements)
        parcel.writeTypedList(thursdayEngagements)
        parcel.writeTypedList(fridayEngagements)
        parcel.writeTypedList(saturdayEngagements)
        parcel.writeTypedList(sundayEngagements)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeekEngagements> {
        override fun createFromParcel(parcel: Parcel): WeekEngagements {
            return WeekEngagements(parcel)
        }

        override fun newArray(size: Int): Array<WeekEngagements?> {
            return arrayOfNulls(size)
        }
    }
}