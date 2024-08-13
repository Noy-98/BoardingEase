package com.example.boardingease

import java.io.Serializable

data class TenantsReservedDataStructureDB(
    val reserved_id: String = "", // Primary key or unique identifier
    val landlord_lastname: String = "",
    val room_number: String = "",
    val number_of_borders: String = "",
    val status: String = "",
    val price: String = "",
    val tenants_first_name: String = "",
    val tenants_last_name: String = "",
    val tenants_contact_number: String = "",
    val concern: String = "",
    var valid_docs_image_url: String? = null,
    var g_cash_ss_image_url: String? = null
): Serializable
