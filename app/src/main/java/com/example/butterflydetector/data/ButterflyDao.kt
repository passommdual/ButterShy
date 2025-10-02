package com.example.butterflydetector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ButterflyDao {

    @Query("SELECT * FROM butterflies ORDER BY name ASC")
    fun getAllFlow(): Flow<List<ButterflyEntity>>  // Added Flow for reactive updates

    @Query("SELECT * FROM butterflies ORDER BY name ASC")
    suspend fun getAll(): List<ButterflyEntity>

    @Query("SELECT * FROM butterflies WHERE species = :species ORDER BY name ASC")
    suspend fun getBySpecies(species: String): List<ButterflyEntity>  // Renamed from getByFamily to getBySpecies

    @Query("SELECT * FROM butterflies WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchByName(query: String): List<ButterflyEntity>

    @Query("SELECT * FROM butterflies WHERE isFavorite = 1 ORDER BY name ASC")
    suspend fun getFavorites(): List<ButterflyEntity>  // Added query for favorites

    @Query("SELECT DISTINCT species FROM butterflies ORDER BY species ASC")
    suspend fun getAllSpecies(): List<String>  // Added query to get all unique species

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(butterflies: List<ButterflyEntity>)

    @Update
    suspend fun update(butterfly: ButterflyEntity)  // Added update method for favorites

    @Query("UPDATE butterflies SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)  // Added method to toggle favorites
}
