package com.rainclass.app.di

import com.rainclass.core.database.AppDatabase
import com.rainclass.core.datastore.SettingsDataStore
import com.rainclass.core.domain.runner.ExamRunner
import com.rainclass.core.domain.solver.LLMSolver
import com.rainclass.core.domain.usecase.GetCoursesUseCase
import com.rainclass.core.domain.usecase.GetHomeworkUseCase
import com.rainclass.core.domain.usecase.GetUserInfoUseCase
import com.rainclass.core.network.LoginHelper
import com.rainclass.core.network.NetworkModule
import com.rainclass.core.network.cookie.PersistentCookieStore
import com.rainclass.feature.courses.CoursesViewModel
import com.rainclass.feature.exam.ExamStatusViewModel
import com.rainclass.feature.exam.ExamViewModel
import com.rainclass.feature.home.HomeViewModel
import com.rainclass.feature.homework.HomeworkViewModel
import com.rainclass.feature.login.LoginViewModel
import com.rainclass.feature.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Data layer
    single { NetworkModule.provideCookieStore(androidContext()) }
    single { NetworkModule.provideRainClassApi(get()) }
    single { NetworkModule.provideExamApi(get()) }
    single { AppDatabase.getInstance(androidContext()) }
    single { SettingsDataStore(androidContext()) }

    // Login helper
    single {
        val cookieStore = get<PersistentCookieStore>()
        val client = OkHttpClient.Builder().cookieJar(cookieStore).build()
        LoginHelper(get(), client)
    }

    // Use cases
    factory { GetUserInfoUseCase(get()) }
    factory { GetCoursesUseCase(get()) }
    factory { GetHomeworkUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { CoursesViewModel(get()) }
    viewModel { HomeworkViewModel(get()) }
    viewModel {
        ExamViewModel(
            examRunnerFactory = {
                val settingsDataStore = get<SettingsDataStore>()
                val currentSettings = runBlocking { settingsDataStore.settingsFlow.first() }
                val llmApi = NetworkModule.provideLLMApi(currentSettings.baseUrl, currentSettings.requestTimeoutSeconds)
                val solver = LLMSolver(llmApi, currentSettings)
                ExamRunner(get(), get(), solver, get(), currentSettings)
            }
        )
    }
    viewModel { ExamStatusViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
