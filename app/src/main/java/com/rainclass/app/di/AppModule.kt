package com.rainclass.app.di

import com.rainclass.core.database.AppDatabase
import com.rainclass.core.config.datastore.SettingsDataStore
import com.rainclass.core.network.NetworkModule
import com.rainclass.core.network.cookie.PersistentCookieStore
import com.rainclass.feature.courses.model.api.CoursesApi
import com.rainclass.feature.courses.model.repository.CoursesRepository
import com.rainclass.feature.courses.viewmodel.CoursesViewModel
import com.rainclass.feature.exam.model.api.ExamApi
import com.rainclass.feature.exam.model.api.ExamTokenApi
import com.rainclass.core.network.llm.LlmClient
import com.rainclass.feature.exam.model.repository.ExamRunner
import com.rainclass.feature.exam.model.repository.LLMSolver
import com.rainclass.feature.exam.viewmodel.ExamStatusViewModel
import com.rainclass.feature.exam.viewmodel.ExamViewModel
import com.rainclass.feature.home.model.api.HomeApi
import com.rainclass.feature.home.model.repository.HomeRepository
import com.rainclass.feature.home.viewmodel.HomeViewModel
import com.rainclass.feature.homework.model.api.HomeworkApi
import com.rainclass.feature.homework.model.repository.HomeworkRepository
import com.rainclass.feature.homework.viewmodel.HomeworkViewModel
import com.rainclass.feature.login.model.api.LoginApi
import com.rainclass.feature.login.model.repository.LoginRepository
import com.rainclass.feature.login.viewmodel.LoginViewModel
import com.rainclass.feature.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
  single { NetworkModule.provideCookieStore(androidContext()) }
  single { AppDatabase.getInstance(androidContext()) }
  single { SettingsDataStore(androidContext()) }

  single<LoginApi> {
    NetworkModule.provideRainClassRetrofit(get()).create(LoginApi::class.java)
  }
  single<HomeApi> {
    NetworkModule.provideRainClassRetrofit(get()).create(HomeApi::class.java)
  }
  single<CoursesApi> {
    NetworkModule.provideRainClassRetrofit(get()).create(CoursesApi::class.java)
  }
  single<HomeworkApi> {
    NetworkModule.provideRainClassRetrofit(get()).create(HomeworkApi::class.java)
  }
  single<ExamTokenApi> {
    NetworkModule.provideRainClassRetrofit(get()).create(ExamTokenApi::class.java)
  }
  single<ExamApi> {
    NetworkModule.provideExamRetrofit(get()).create(ExamApi::class.java)
  }

  single {
    val cookieStore = get<PersistentCookieStore>()
    LoginRepository(get(), NetworkModule.provideCookieClient(cookieStore))
  }
  single { HomeRepository(get()) }
  single { CoursesRepository(get()) }
  single { HomeworkRepository(get()) }

  viewModel { LoginViewModel(get()) }
  viewModel { HomeViewModel(get()) }
  viewModel { CoursesViewModel(get()) }
  viewModel { HomeworkViewModel(get()) }
  viewModel {
    ExamViewModel(
      examRunnerFactory = {
        val settingsDataStore = get<SettingsDataStore>()
        val currentSettings = runBlocking { settingsDataStore.settingsFlow.first() }
        val solver = LLMSolver(
          llmClient = LlmClient(currentSettings.requestTimeoutSeconds),
          settings = currentSettings
        )
        ExamRunner(get(), get(), solver, get(), currentSettings)
      }
    )
  }
  viewModel { ExamStatusViewModel(get()) }
  viewModel { SettingsViewModel(get()) }
}
