package com.example.boardingease

import java.io.Serializable

data class LandlordConfirmationDataStructureDB(
    val confirmation_id: String = "",
    val landlord_last_name: String = "",
    val tenants_last_name: String = "",
    val room_number: String = "",
    val status: String = "",
    val concern: String = ""
): Serializable
