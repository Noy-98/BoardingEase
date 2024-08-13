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
    val rules_and_regulations: String = "",
    val g_cash_name: String = "",
    val g_cash_number: String = "",
    var permitImageUrl: String? = null,
    var unitPictureUrl: String? = null
) : Serializable