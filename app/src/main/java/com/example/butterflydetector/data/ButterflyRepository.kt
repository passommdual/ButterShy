package com.example.butterflydetector.data

import android.content.Context

class ButterflyRepository(context: Context) {
    private val butterflyDao = ButterflyDatabase.getDatabase(context).butterflyDao()

    suspend fun getAll() = butterflyDao.getAll()

    suspend fun getBySpecies(species: String) = butterflyDao.getBySpecies(species)

    suspend fun searchByName(query: String) = butterflyDao.searchByName(query)

    suspend fun getFavorites() = butterflyDao.getFavorites()

    suspend fun getAllSpecies() = butterflyDao.getAllSpecies()
    suspend fun insertAll(list: List<ButterflyEntity>) = butterflyDao.insertAll(list)

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) = butterflyDao.updateFavorite(id, isFavorite)
}
