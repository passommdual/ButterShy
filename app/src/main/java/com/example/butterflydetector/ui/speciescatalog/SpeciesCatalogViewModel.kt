package com.example.butterflydetector.ui.speciescatalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.butterflydetector.data.ButterflyEntity
import com.example.butterflydetector.data.ButterflyRepository
import kotlinx.coroutines.launch

class SpeciesCatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ButterflyRepository(application)

    private val _filteredButterflies = MutableLiveData<List<ButterflyEntity>>()
    val filteredButterflies: LiveData<List<ButterflyEntity>> = _filteredButterflies

    private val _speciesList = MutableLiveData<List<String>>()
    val speciesList: LiveData<List<String>> = _speciesList

    private var allButterflies: List<ButterflyEntity> = emptyList()

    init {
        loadButterflies()
        loadSpeciesList()
    }

    private fun loadButterflies() {
        viewModelScope.launch {
            allButterflies = repository.getAll()
            _filteredButterflies.value = allButterflies
        }
    }

    private fun loadSpeciesList() {
        viewModelScope.launch {
            _speciesList.value = repository.getAllSpecies()
        }
    }

    fun filterButterflies(selectedSpecies: List<String>, onlyFavorites: Boolean) {
        _filteredButterflies.value = allButterflies.filter { butterfly ->
            val speciesMatch = selectedSpecies.isEmpty() || butterfly.species in selectedSpecies
            val favoriteMatch = !onlyFavorites || butterfly.isFavorite
            speciesMatch && favoriteMatch
        }
    }

    fun toggleFavorite(butterfly: ButterflyEntity) {
        viewModelScope.launch {
            repository.updateFavorite(butterfly.id, !butterfly.isFavorite)
            loadButterflies() // refresh list
        }
    }
}
