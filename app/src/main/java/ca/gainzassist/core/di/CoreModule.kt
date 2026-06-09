package ca.gainzassist.core.di

import ca.gainzassist.core.coroutines.DefaultDispatcherProvider
import ca.gainzassist.core.coroutines.DispatcherProvider
import org.koin.dsl.module

val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
