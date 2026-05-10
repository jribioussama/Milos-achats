package com.example.milos_achats.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.milos_achats.data.WeekInfo
import com.example.milos_achats.data.confirmedKey
import com.example.milos_achats.data.confirmedKitchenKey
import com.example.milos_achats.data.confirmedServerKey
import com.example.milos_achats.data.getWeekInfo
import com.example.milos_achats.data.repository.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OrdersStatus(
    val barConfirmed:     Boolean,
    val kitchenConfirmed: Boolean,
    val serverConfirmed:  Boolean,
)

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    // Se recalcule toutes les 60 secondes pour détecter le basculement à 02h00
    private val weekInfoFlow: StateFlow<WeekInfo> = flow {
        while (true) {
            emit(getWeekInfo())
            delay(60_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), getWeekInfo())

    val formattedDate: StateFlow<String> = weekInfoFlow.map { info ->
        val idx   = info.days.indexOfFirst { it.isEditable }
        val day   = if (idx >= 0) info.days[idx] else null
        val month = info.monthHeader.split(" ").first()
        day?.let { "${it.fullName} ${it.dayNumber} $month" } ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val ordersStatus: StateFlow<OrdersStatus> = combine(
        repository.observeCheckStates(),
        weekInfoFlow,
    ) { states, info ->
        val idx = info.days.indexOfFirst { it.isEditable }
        OrdersStatus(
            barConfirmed     = idx >= 0 && states[confirmedKey(idx)] == true,
            kitchenConfirmed = idx >= 0 && states[confirmedKitchenKey(idx)] == true,
            serverConfirmed  = idx >= 0 && states[confirmedServerKey(idx)] == true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersStatus(false, false, false))

    val isOrderConfirmed: StateFlow<Boolean> = ordersStatus
        .map { it.barConfirmed }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
