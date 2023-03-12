package rafal.iwanczyk.praca.inzynierska.seti.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id: String = "",
    val login: String = "",
    val email: String = "",
    val name: String = "",
    val image: String = "",
    val background: String = "",
    val fcmToken: String = "",
    //val isHighContrastNecessary: Boolean = false
    val xDD: Int = 0

        ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        //parcel.readBoolean()
    parcel.readInt()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(login)
        parcel.writeString(email)
        parcel.writeString(image)
        parcel.writeString(background)
        parcel.writeString(fcmToken)
        //parcel.writeBoolean(isHighContrastNecessary)
        parcel.writeInt(xDD)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }
        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}