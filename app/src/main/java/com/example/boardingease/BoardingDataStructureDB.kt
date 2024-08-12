package com.example.boardingease

import android.os.Parcel
import android.os.Parcelable

data class BoardingDataStructureDB(
    var landlord_lastname: String? = null,
    var room_number: String? = null,
    var number_of_borders: String? = null,
    var status: String? = null,
    var price: String? = null,
    var contact_number: String? = null,
    var address: String? = null,
    var unitPictureUrl: String? = null,
    var b_id: String? = null
): Parcelable {
    constructor(parcel: Parcel): this(
        // Read values from parcel
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // Write values to parcel
        parcel.writeString(landlord_lastname)
        parcel.writeString(room_number)
        parcel.writeString(number_of_borders)
        parcel.writeString(status)
        parcel.writeString(price)
        parcel.writeString(contact_number)
        parcel.writeString(address)
        parcel.writeString(unitPictureUrl)
        parcel.writeString(b_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoardingDataStructureDB> {
        override fun createFromParcel(parcel: Parcel): BoardingDataStructureDB {
            return BoardingDataStructureDB(parcel)
        }

        override fun newArray(size: Int): Array<BoardingDataStructureDB?> {
            return arrayOfNulls(size)
        }
    }
}
