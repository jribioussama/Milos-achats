package com.example.milos_achats.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.milos_achats.data.confirmedKey
import com.example.milos_achats.data.confirmedKitchenKey
import com.example.milos_achats.data.confirmedServerKey
import com.example.milos_achats.data.getWeekInfo
import com.example.milos_achats.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OrdersStatus(
    val barConfirmed:     Boolean,
    val kitchenConfirmed: Boolean,
    val serverConfirmed:  Boolean,
)

class HomeViewModel(private val repository: ProductRepository) : ViewModel() {

    private val weekInfo    = getWeekInfo()
    private val editableIdx = weekInfo.days.indexOfFirst { it.isEditable }
    private val editableDay = if (editableIdx >= 0) weekInfo.days[editableIdx] else null
    private val monthName   = weekInfo.monthHeader.split(" ").first()

    val formattedDate: String = editableDay?.let { "${it.fullName} ${it.dayNumber} $monthName" } ?: ""

    val ordersStatus: StateFlow<OrdersStatus> = repository
        .observeCheckStates()
        .map { states ->
            OrdersStatus(
                barConfirmed     = editableIdx >= 0 && states[confirmedKey(editableIdx)] == true,
                kitchenConfirmed = editableIdx >= 0 && states[confirmedKitchenKey(editableIdx)] == true,
                serverConfirmed  = editableIdx >= 0 && states[confirmedServerKey(editableIdx)] == true,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersStatus(false, false, false))

    // Conservé pour compatibilité avec le CTA gérant sur la HP
    val isOrderConfirmed: StateFlow<Boolean> = ordersStatus
        .map { it.barConfirmed }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    class Factory(private val repository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
