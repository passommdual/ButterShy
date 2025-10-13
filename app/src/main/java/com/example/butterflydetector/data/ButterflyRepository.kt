package com.example.butterflydetector.data

import android.content.Context

class ButterflyRepository(context: Context) {
    private val butterflyDao = ButterflyDatabase.getDatabase(context).butterflyDao()

    suspend fun getAll() = butterflyDao.getAll()

    suspend fun getBySpecies(species: String) = butterflyDao.getBySpecies(species)  // Renamed from getByFamily

    suspend fun searchByName(query: String) = butterflyDao.searchByName(query)

    suspend fun getFavorites() = butterflyDao.getFavorites()  // Added favorites method

    suspend fun getAllSpecies() = butterflyDao.getAllSpecies()  // Added method to get all species

    suspend fun insertAll(list: List<ButterflyEntity>) = butterflyDao.insertAll(list)

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) = butterflyDao.updateFavorite(id, isFavorite)  // Added toggle favorite method
}
