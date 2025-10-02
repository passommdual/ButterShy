package com.example.butterflydetector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "butterflies")
data class ButterflyEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val imageFile: String,   // e.g. "butterfly_abaeis_nicippe.jpg"
    val description: String,
    val habitat: String,
    val wingspan: String,
    val flightPeriod: String,
    val isFavorite: Boolean = false  // Added isFavorite field for favorites functionality
)
