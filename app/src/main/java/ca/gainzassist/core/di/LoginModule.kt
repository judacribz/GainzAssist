package ca.gainzassist.core.di

import ca.gainzassist.feature.login.data.repository.FirebaseLoginRepository
import ca.gainzassist.feature.login.domain.repository.LoginRepository
import ca.gainzassist.feature.login.domain.usecase.LoginWithEmailUseCase
import ca.gainzassist.feature.login.domain.usecase.SignOutUseCase
import ca.gainzassist.feature.login.domain.usecase.ValidateLoginInputUseCase
import ca.gainzassist.feature.login.presentation.viewmodel.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    single<LoginRepository> { FirebaseLoginRepository() }
    factory { ValidateLoginInputUseCase() }
    factory { LoginWithEmailUseCase(loginRepository = get()) }
    factory { SignOutUseCase(loginRepository = get()) }
    viewModel {
        LoginViewModel(
            validateLoginInputUseCase = get(),
            loginWithEmailUseCase = get()
        )
    }
}
