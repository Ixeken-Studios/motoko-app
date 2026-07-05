package com.ixeken.motoko.domain.usecase

import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.domain.model.Subscription

/**
 * Calculador de gastos proporcionales para suscripciones recurrentes.
 */
object SubscriptionCalculator {

    /**
     * Proyecta el gasto mensual de una suscripción.
     * Si la suscripción es anual, se divide por 12.0 de forma automática.
     */
    fun calculateMonthlyExpense(subscription: Subscription): Double {
        return when (subscription.billingPeriod) {
            BillingPeriod.MONTHLY -> subscription.amount
            BillingPeriod.ANNUAL -> subscription.amount / 12.0
        }
    }

    /**
     * Proyecta el gasto anual de una suscripción.
     * Si la suscripción es mensual, se multiplica por 12.0 de forma automática.
     */
    fun calculateAnnualExpense(subscription: Subscription): Double {
        return when (subscription.billingPeriod) {
            BillingPeriod.MONTHLY -> subscription.amount * 12.0
            BillingPeriod.ANNUAL -> subscription.amount
        }
    }

    /**
     * Suma todos los gastos mensuales proporcionales de una lista de suscripciones.
     */
    fun calculateTotalMonthlyExpense(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { calculateMonthlyExpense(it) }
    }

    /**
     * Suma todos los gastos anuales proyectados de una lista de suscripciones.
     */
    fun calculateTotalAnnualExpense(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { calculateAnnualExpense(it) }
    }
}
