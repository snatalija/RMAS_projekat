package com.example.projekat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Pretpostavljamo da postoji klasa `MyObject` koja predstavlja objekte na mapi
data class MyObject(
    val id: String,
    val name: String,
    val type: String,
    val attributes: Map<String, String>,
    val creationDate: Long,
    val lastInteractionDate: Long
)

class ObjectViewModel : ViewModel() {
    private val _allObjects = MutableLiveData<List<MyObject>>()
    val allObjects: LiveData<List<MyObject>> = _allObjects

    private val _filteredObjects = MutableLiveData<List<MyObject>>()
    val filteredObjects: LiveData<List<MyObject>> = _filteredObjects

    init {
        // Simuliramo učitavanje objekata. U stvarnosti, ovo bi moglo biti učitavanje sa servera ili baze podataka
        loadObjects()
    }

    private fun loadObjects() {
        viewModelScope.launch {
            // Simulacija podataka. U stvarnoj aplikaciji ovo bi moglo doći iz baze podataka ili API-ja
            _allObjects.value = listOf(
                MyObject("1", "Dance Club A", "Club", mapOf("Capacity" to "200"), 1651246800000, 1651246800000),
                MyObject("2", "Dance Club B", "Club", mapOf("Capacity" to "150"), 1651246800000, 1651246800000)
                // Dodajte više objekata ovde
            )
            _filteredObjects.value = _allObjects.value
        }
    }

    fun applyFilter(filterText: String) {
        viewModelScope.launch {
            _filteredObjects.value = _allObjects.value?.filter { obj ->
                obj.name.contains(filterText, ignoreCase = true) ||
                        obj.type.contains(filterText, ignoreCase = true) ||
                        obj.attributes.values.any { it.contains(filterText, ignoreCase = true) }
            }
        }
    }
}
