package com.example.boardingease

import java.io.Serializable

data class TenantsReservedDataStructureDB(
    val tenants_uid: String = "", // Primary key or unique identifier
    val landlord_lastname: String = "",
    val room_number: String = "",
    val number_of_borders: String = "",
    val status: String = "",
    val price: String = "",
    var valid_id_image_url: String? = null,
    var barangay_clearance_image_url: String? = null
): Serializable
