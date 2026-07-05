package com.ixeken.motoko.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.domain.model.Subscription
import com.ixeken.motoko.domain.repository.FinanceRepository
import com.ixeken.motoko.domain.usecase.SubscriptionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.ixeken.motoko.R as AppR
import com.composables.icons.lucide.R

/**
 * Estado visual para la pantalla de Suscripciones.
 */
sealed interface SubscriptionsUiState {
    object Loading : SubscriptionsUiState
    data class Success(
        val subscriptions: List<SubscriptionItem> = emptyList(),
        val monthlyTotal: Double = 0.0,
        val annualTotal: Double = 0.0
    ) : SubscriptionsUiState
    data class Error(val message: String) : SubscriptionsUiState
}

/**
 * ViewModel que gestiona los datos de negocio y estado de Suscripciones.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val activeAccountIdFlow: Flow<Long> = preferences.activeAccountId
        .flatMapLatest { id ->
            if (id != null) {
                flowOf(id)
            } else {
                repository.getAccounts().map { accounts ->
                    accounts.firstOrNull()?.id ?: 1L
                }
            }
        }

    private fun mapIconNameToDrawable(iconName: String?): Int {
        return when (iconName) {
            "Pizza" -> com.composables.icons.lucide.R.drawable.lucide_ic_pizza
            "Soup" -> com.composables.icons.lucide.R.drawable.lucide_ic_soup
            "Apple" -> com.composables.icons.lucide.R.drawable.lucide_ic_apple
            "Coffee" -> com.composables.icons.lucide.R.drawable.lucide_ic_coffee
            "Wine" -> com.composables.icons.lucide.R.drawable.lucide_ic_wine
            "Beer" -> com.composables.icons.lucide.R.drawable.lucide_ic_beer
            "Utensils" -> com.composables.icons.lucide.R.drawable.lucide_ic_utensils
            "LeafyGreen" -> com.composables.icons.lucide.R.drawable.lucide_ic_leafy_green
            "Train" -> com.composables.icons.lucide.R.drawable.lucide_ic_train_front
            "Car" -> com.composables.icons.lucide.R.drawable.lucide_ic_car
            "Bus" -> com.composables.icons.lucide.R.drawable.lucide_ic_bus
            "Bike" -> com.composables.icons.lucide.R.drawable.lucide_ic_bike
            "Plane" -> com.composables.icons.lucide.R.drawable.lucide_ic_plane
            "Ship" -> com.composables.icons.lucide.R.drawable.lucide_ic_ship
            "Fuel" -> com.composables.icons.lucide.R.drawable.lucide_ic_fuel
            "Truck" -> com.composables.icons.lucide.R.drawable.lucide_ic_truck
            "Navigation" -> com.composables.icons.lucide.R.drawable.lucide_ic_navigation
            "House" -> com.composables.icons.lucide.R.drawable.lucide_ic_house
            "Plug" -> com.composables.icons.lucide.R.drawable.lucide_ic_plug
            "Wifi" -> com.composables.icons.lucide.R.drawable.lucide_ic_wifi
            "Lamp" -> com.composables.icons.lucide.R.drawable.lucide_ic_lamp
            "Key" -> com.composables.icons.lucide.R.drawable.lucide_ic_key
            "Wrench" -> com.composables.icons.lucide.R.drawable.lucide_ic_wrench
            "Hammer" -> com.composables.icons.lucide.R.drawable.lucide_ic_hammer
            "Warehouse" -> com.composables.icons.lucide.R.drawable.lucide_ic_warehouse
            "Ticket" -> com.composables.icons.lucide.R.drawable.lucide_ic_ticket
            "Gamepad" -> com.composables.icons.lucide.R.drawable.lucide_ic_gamepad_2
            "Music" -> com.composables.icons.lucide.R.drawable.lucide_ic_music
            "Film" -> com.composables.icons.lucide.R.drawable.lucide_ic_film
            "Headphones" -> com.composables.icons.lucide.R.drawable.lucide_ic_headphones
            "Tv" -> com.composables.icons.lucide.R.drawable.lucide_ic_tv
            "Trophy" -> com.composables.icons.lucide.R.drawable.lucide_ic_trophy
            "Medal" -> com.composables.icons.lucide.R.drawable.lucide_ic_medal
            "ShoppingCart" -> com.composables.icons.lucide.R.drawable.lucide_ic_shopping_cart
            "ShoppingBag" -> com.composables.icons.lucide.R.drawable.lucide_ic_shopping_bag
            "Gift" -> com.composables.icons.lucide.R.drawable.lucide_ic_gift
            "Package" -> com.composables.icons.lucide.R.drawable.lucide_ic_package
            "Gem" -> com.composables.icons.lucide.R.drawable.lucide_ic_gem
            "Scissors" -> com.composables.icons.lucide.R.drawable.lucide_ic_scissors
            "Heart" -> com.composables.icons.lucide.R.drawable.lucide_ic_heart
            "Hospital" -> com.composables.icons.lucide.R.drawable.lucide_ic_hospital
            "Pill" -> com.composables.icons.lucide.R.drawable.lucide_ic_pill
            "Stethoscope" -> com.composables.icons.lucide.R.drawable.lucide_ic_stethoscope
            "Dumbbell" -> com.composables.icons.lucide.R.drawable.lucide_ic_dumbbell
            "Thermometer" -> com.composables.icons.lucide.R.drawable.lucide_ic_thermometer
            "GraduationCap" -> com.composables.icons.lucide.R.drawable.lucide_ic_graduation_cap
            "Book" -> com.composables.icons.lucide.R.drawable.lucide_ic_book_open
            "School" -> com.composables.icons.lucide.R.drawable.lucide_ic_school
            "Pen" -> com.composables.icons.lucide.R.drawable.lucide_ic_pen
            "Laptop" -> com.composables.icons.lucide.R.drawable.lucide_ic_laptop
            "Monitor" -> com.composables.icons.lucide.R.drawable.lucide_ic_monitor
            "Phone" -> com.composables.icons.lucide.R.drawable.lucide_ic_phone
            "Printer" -> com.composables.icons.lucide.R.drawable.lucide_ic_printer
            "Globe" -> com.composables.icons.lucide.R.drawable.lucide_ic_globe
            "Mountain" -> com.composables.icons.lucide.R.drawable.lucide_ic_mountain
            "Waves" -> com.composables.icons.lucide.R.drawable.lucide_ic_waves
            "Umbrella" -> com.composables.icons.lucide.R.drawable.lucide_ic_umbrella
            "Sun" -> com.composables.icons.lucide.R.drawable.lucide_ic_sun
            "Tent" -> com.composables.icons.lucide.R.drawable.lucide_ic_tent
            "Sailboat" -> com.composables.icons.lucide.R.drawable.lucide_ic_sailboat
            "MapPin" -> com.composables.icons.lucide.R.drawable.lucide_ic_map_pin
            "User" -> com.composables.icons.lucide.R.drawable.lucide_ic_user
            "Users" -> com.composables.icons.lucide.R.drawable.lucide_ic_users
            "Baby" -> com.composables.icons.lucide.R.drawable.lucide_ic_baby
            "Cat" -> com.composables.icons.lucide.R.drawable.lucide_ic_cat
            "Dog" -> com.composables.icons.lucide.R.drawable.lucide_ic_dog
            "Fish" -> com.composables.icons.lucide.R.drawable.lucide_ic_fish
            "Folder" -> com.composables.icons.lucide.R.drawable.lucide_ic_folder_code
            "Camera" -> com.composables.icons.lucide.R.drawable.lucide_ic_camera
            "Shield" -> com.composables.icons.lucide.R.drawable.lucide_ic_shield
            "Lock" -> com.composables.icons.lucide.R.drawable.lucide_ic_lock
            "Star" -> com.composables.icons.lucide.R.drawable.lucide_ic_star
            "Zap" -> com.composables.icons.lucide.R.drawable.lucide_ic_zap
            "Flame" -> com.composables.icons.lucide.R.drawable.lucide_ic_flame
            "Rocket" -> com.composables.icons.lucide.R.drawable.lucide_ic_rocket
            "Flower" -> com.composables.icons.lucide.R.drawable.lucide_ic_flower
            "Settings" -> com.composables.icons.lucide.R.drawable.lucide_ic_settings
            "Palette" -> com.composables.icons.lucide.R.drawable.lucide_ic_palette
            "Type" -> com.composables.icons.lucide.R.drawable.lucide_ic_type
            "FileText" -> com.composables.icons.lucide.R.drawable.lucide_ic_file_text
            "Refresh" -> com.composables.icons.lucide.R.drawable.lucide_ic_refresh_cw
            "UserPlus" -> com.composables.icons.lucide.R.drawable.lucide_ic_user_plus
            "Search" -> com.composables.icons.lucide.R.drawable.lucide_ic_search
            "Plus" -> com.composables.icons.lucide.R.drawable.lucide_ic_plus
            "Briefcase" -> com.composables.icons.lucide.R.drawable.lucide_ic_briefcase
            "Dollar" -> com.composables.icons.lucide.R.drawable.lucide_ic_circle_dollar_sign
            else -> com.composables.icons.lucide.R.drawable.lucide_ic_circle_dollar_sign
        }
    }

    val uiState: StateFlow<SubscriptionsUiState> = activeAccountIdFlow
        .flatMapLatest { accountId ->
            combine(
                repository.getSubscriptions(accountId),
                preferences.categoryIcons
            ) { subsList, categoryIconsMap ->
                val monthlyTotal = SubscriptionCalculator.calculateTotalMonthlyExpense(subsList)
                val annualTotal = SubscriptionCalculator.calculateTotalAnnualExpense(subsList)

                val mappedSubs = subsList.map { sub ->
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = sub.startDate }
                    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
                    val suffix = when (day) {
                        1, 21, 31 -> "st"
                        2, 22 -> "nd"
                        3, 23 -> "rd"
                        else -> "th"
                    }
                    val periodStr = when (sub.billingPeriod) {
                        BillingPeriod.MONTHLY -> "Every $day$suffix"
                        BillingPeriod.ANNUAL -> {
                            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                            "Every ${sdf.format(java.util.Date(sub.startDate))}"
                        }
                    }

                    val finalIcon = categoryIconsMap[sub.category] ?: sub.iconName
                    SubscriptionItem(
                        nameStr = sub.name,
                        amount = String.format(java.util.Locale.US, "%.2f", sub.amount),
                        dateStr = periodStr,
                        iconRes = mapIconNameToDrawable(finalIcon),
                        domainSubscription = sub.copy(iconName = finalIcon)
                    )
                }

                SubscriptionsUiState.Success(
                    subscriptions = mappedSubs,
                    monthlyTotal = monthlyTotal,
                    annualTotal = annualTotal
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionsUiState.Loading
        )
}
