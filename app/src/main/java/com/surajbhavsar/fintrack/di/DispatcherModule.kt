package com.surajbhavsar.fintrack.di

import com.surajbhavsar.fintrack.core.common.DefaultDispatcher
import com.surajbhavsar.fintrack.core.common.DispatcherProvider
import com.surajbhavsar.fintrack.core.common.IoDispatcher
import com.surajbhavsar.fintrack.core.common.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides @IoDispatcher fun provideIo(): CoroutineDispatcher = Dispatchers.IO

    @Provides @DefaultDispatcher fun provideDefault(): CoroutineDispatcher = Dispatchers.Default

    @Provides @MainDispatcher fun provideMain(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = object : DispatcherProvider {
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
        override val main = Dispatchers.Main
    }
}
