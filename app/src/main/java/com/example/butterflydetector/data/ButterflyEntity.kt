package com.example.butterflydetector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "butterflies")
data class ButterflyEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val imageFile: String,   // "butterfly_abaeis_nicippe" (drawable resource name without extension)
    val description: String,
    val habitat: String,
    val wingspan: String,
    val flightPeriod: String,
    val isFavorite: Boolean = false
)
