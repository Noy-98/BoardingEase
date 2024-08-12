package com.example.boardingease

import java.io.Serializable

data class BoardingDataStructureDB(
    val b_id: String = "", // Primary key or unique identifier
    val landlord_lastname: String = "",
    val room_number: String = "",
    val number_of_borders: String = "",
    val status: String = "",
    val price: String = "",
    val contact_number: String = "",
    val address: String = "",
    var unitPictureUrl: String? = null
) : Serializable