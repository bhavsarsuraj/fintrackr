package com.surajbhavsar.fintrack.di

import com.surajbhavsar.fintrack.data.preferences.PreferencesRepositoryImpl
import com.surajbhavsar.fintrack.data.repository.AuthRepositoryImpl
import com.surajbhavsar.fintrack.data.repository.BudgetRepositoryImpl
import com.surajbhavsar.fintrack.data.repository.ExpenseRepositoryImpl
import com.surajbhavsar.fintrack.data.repository.RecurringRuleRepositoryImpl
import com.surajbhavsar.fintrack.domain.repository.AuthRepository
import com.surajbhavsar.fintrack.domain.repository.BudgetRepository
import com.surajbhavsar.fintrack.domain.repository.ExpenseRepository
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import com.surajbhavsar.fintrack.domain.repository.RecurringRuleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds @Singleton
    abstract fun bindRecurringRuleRepository(impl: RecurringRuleRepositoryImpl): RecurringRuleRepository

    @Binds @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
