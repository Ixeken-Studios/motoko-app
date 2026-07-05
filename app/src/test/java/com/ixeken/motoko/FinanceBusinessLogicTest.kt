package com.ixeken.motoko

import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.domain.model.Subscription
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.usecase.SubscriptionCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para validar las reglas de negocio críticas de finanzas y suscripciones de Motoko.
 */
class FinanceBusinessLogicTest {

    @Test
    fun testSubscriptionProportionality() {
        val monthlySub = Subscription(
            id = 1,
            accountId = 100,
            name = "Spotify",
            amount = 10.0,
            wallet = WalletType.BANK,
            billingPeriod = BillingPeriod.MONTHLY,
            startDate = System.currentTimeMillis(),
            iconName = "music",
            category = "Entertainment"
        )

        val annualSub = Subscription(
            id = 2,
            accountId = 100,
            name = "Amazon Prime",
            amount = 120.0,
            wallet = WalletType.BANK,
            billingPeriod = BillingPeriod.ANNUAL,
            startDate = System.currentTimeMillis(),
            iconName = "shopping-cart",
            category = "Services"
        )

        // Spotify: $10/mes -> Anualizado: $120. Mensual: $10.
        assertEquals(10.0, SubscriptionCalculator.calculateMonthlyExpense(monthlySub), 0.0)
        assertEquals(120.0, SubscriptionCalculator.calculateAnnualExpense(monthlySub), 0.0)

        // Amazon Prime: $120/año -> Proporcional mensual: $10. Anual: $120.
        assertEquals(10.0, SubscriptionCalculator.calculateMonthlyExpense(annualSub), 0.0)
        assertEquals(120.0, SubscriptionCalculator.calculateAnnualExpense(annualSub), 0.0)

        // Suma total mensual esperada: Spotify ($10) + Amazon Prime ($10) = $20.0
        val totalMonthly = SubscriptionCalculator.calculateTotalMonthlyExpense(listOf(monthlySub, annualSub))
        assertEquals(20.0, totalMonthly, 0.0)

        // Suma total anual esperada: Spotify ($120) + Amazon Prime ($120) = $240.0
        val totalAnnual = SubscriptionCalculator.calculateTotalAnnualExpense(listOf(monthlySub, annualSub))
        assertEquals(240.0, totalAnnual, 0.0)
    }

    @Test
    fun testNegativeBalanceAndNoIncomeLogic() {
        // En ausencia total de ingresos, solo tenemos transacciones de gastos (isIncome = false)
        val transactions = listOf(
            Transaction(
                id = 1, accountId = 1, title = "Supermarket", amount = 45.5,
                isIncome = false, wallet = WalletType.CASH, category = "Food",
                iconName = "shopping-bag", timestamp = System.currentTimeMillis(),
                note = null, receiptPath = null
            ),
            Transaction(
                id = 2, accountId = 1, title = "Bus ticket", amount = 2.5,
                isIncome = false, wallet = WalletType.CASH, category = "Transport",
                iconName = "bus", timestamp = System.currentTimeMillis(),
                note = null, receiptPath = null
            )
        )

        // Simular el cálculo de balance neto localmente (isIncome = true suma, isIncome = false resta)
        val netBalance = transactions.sumOf { if (it.isIncome) it.amount else -it.amount }

        // El balance neto debe ser negativo
        assertEquals(-48.0, netBalance, 0.0)
    }

    @Test
    fun testSubscriptionOccurrencesGeneration() {
        val calendar = java.util.Calendar.getInstance()
        // June 23, 2026 12:00:00
        calendar.set(2026, java.util.Calendar.JUNE, 23, 12, 0, 0)
        val startDate = calendar.timeInMillis

        // Set "now" to August 24, 2026 15:00:00
        calendar.set(2026, java.util.Calendar.AUGUST, 24, 15, 0, 0)
        val now = calendar.timeInMillis

        // Proyectar ocurrencias mensuales
        val monthlyOccurrences = mutableListOf<Long>()
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startDate }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val targetDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

        while (cal.timeInMillis <= now) {
            monthlyOccurrences.add(cal.timeInMillis)
            cal.add(java.util.Calendar.MONTH, 1)
            val maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            cal.set(java.util.Calendar.DAY_OF_MONTH, java.lang.Math.min(targetDay, maxDay))
        }

        // Debería haber 3 cobros: 23 de Junio, 23 de Julio, 23 de Agosto
        assertEquals(3, monthlyOccurrences.size)

        // Verificar la primera ocurrencia (23 de Junio de 2026)
        cal.timeInMillis = monthlyOccurrences[0]
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JUNE, cal.get(java.util.Calendar.MONTH))
        assertEquals(23, cal.get(java.util.Calendar.DAY_OF_MONTH))

        // Verificar la segunda ocurrencia (23 de Julio de 2026)
        cal.timeInMillis = monthlyOccurrences[1]
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.JULY, cal.get(java.util.Calendar.MONTH))
        assertEquals(23, cal.get(java.util.Calendar.DAY_OF_MONTH))

        // Verificar la tercera ocurrencia (23 de Agosto de 2026)
        cal.timeInMillis = monthlyOccurrences[2]
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.AUGUST, cal.get(java.util.Calendar.MONTH))
        assertEquals(23, cal.get(java.util.Calendar.DAY_OF_MONTH))
    }
}
