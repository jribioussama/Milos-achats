package com.example.milos_achats.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.milos_achats.data.checkKey
import com.example.milos_achats.data.confirmedServerKey
import com.example.milos_achats.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServerProductsViewModel(private val repository: ProductRepository) : ViewModel() {

    val checkStates: StateFlow<Map<String, Boolean>> = repository
        .observeCheckStates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun toggle(productId: String, dayIndex: Int, weekId: String) {
        val key = checkKey(productId, dayIndex, weekId)
        viewModelScope.launch {
            repository.toggle(key, checkStates.value[key] ?: false)
        }
    }

    fun confirmOrder(dayIndex: Int, weekId: String) {
        viewModelScope.launch { repository.setChecked(confirmedServerKey(dayIndex, weekId), true) }
    }

    fun unvalidateOrder(dayIndex: Int, weekId: String) {
        viewModelScope.launch { repository.setChecked(confirmedServerKey(dayIndex, weekId), false) }
    }

    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ServerProductsViewModel(repository) as T
    }
}
