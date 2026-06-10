package com.rainclass.app.navigation

import androidx.compose.runtime.Composable
import com.rainclass.core.network.cookie.PersistentCookieStore
import com.rainclass.core.navigation3.RainNavHost
import com.rainclass.core.navigation3.RainRoute
import com.rainclass.core.navigation3.rainEntry
import com.rainclass.feature.courses.ui.CoursesScreen
import com.rainclass.feature.courses.viewmodel.CoursesViewModel
import com.rainclass.feature.exam.ui.ExamProgressScreen
import com.rainclass.feature.exam.ui.ExamStatusScreen
import com.rainclass.feature.exam.viewmodel.ExamStatusViewModel
import com.rainclass.feature.exam.viewmodel.ExamViewModel
import com.rainclass.feature.home.ui.HomeScreen
import com.rainclass.feature.home.viewmodel.HomeViewModel
import com.rainclass.feature.homework.ui.HomeworkDetailScreen
import com.rainclass.feature.homework.ui.HomeworkListScreen
import com.rainclass.feature.homework.viewmodel.HomeworkViewModel
import com.rainclass.feature.login.ui.LoginScreen
import com.rainclass.feature.login.viewmodel.LoginViewModel
import com.rainclass.feature.settings.ui.SettingsScreen
import com.rainclass.feature.settings.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(startDestination: RainRoute = Login) {
  val cookieStore: PersistentCookieStore = koinInject()

  RainNavHost(startRoute = startDestination) {
    rainEntry<Login> { _, navigator ->
      val vm: LoginViewModel = koinViewModel()
      LoginScreen(viewModel = vm, onLoginSuccess = {
        navigator.replaceAll(Home)
      })
    }

    rainEntry<Home> { _, navigator ->
      val vm: HomeViewModel = koinViewModel()
      HomeScreen(
        viewModel = vm,
        onNavigateToCourses = { navigator.navigate(Courses) },
        onNavigateToStatus = { navigator.navigate(Status) },
        onNavigateToSettings = { navigator.navigate(Settings) },
        onLogout = {
          cookieStore.clearAll()
          navigator.replaceAll(Login)
        }
      )
    }

    rainEntry<Courses> { _, navigator ->
      val vm: CoursesViewModel = koinViewModel()
      CoursesScreen(
        viewModel = vm,
        onBackClick = { navigator.pop() },
        onCourseClick = { cid -> navigator.navigate(HomeworkList(cid)) }
      )
    }

    rainEntry<HomeworkList> { route, navigator ->
      val vm: HomeworkViewModel = koinViewModel()
      HomeworkListScreen(
        cid = route.cid,
        viewModel = vm,
        onBackClick = { navigator.pop() },
        onHomeworkClick = { cid, leafId -> navigator.navigate(HomeworkDetail(cid, leafId)) }
      )
    }

    rainEntry<HomeworkDetail> { route, navigator ->
      val vm: HomeworkViewModel = koinViewModel()
      HomeworkDetailScreen(
        cid = route.cid,
        leafId = route.leafId,
        viewModel = vm,
        onBackClick = { navigator.pop() },
        onStartExam = { cid, examId ->
          navigator.navigate(ExamProgress(cid, examId))
        }
      )
    }

    rainEntry<ExamProgress> { route, navigator ->
      val vm: ExamViewModel = koinViewModel()
      ExamProgressScreen(
        cid = route.cid,
        examId = route.examId,
        isResume = route.isResume,
        viewModel = vm,
        onBackClick = { navigator.pop() }
      )
    }

    rainEntry<Status> { _, navigator ->
      val vm: ExamStatusViewModel = koinViewModel()
      ExamStatusScreen(
        viewModel = vm,
        onBackClick = { navigator.pop() },
        onResume = { cid, examId ->
          navigator.navigate(ExamProgress(cid, examId, isResume = true))
        }
      )
    }

    rainEntry<Settings> { _, navigator ->
      val vm: SettingsViewModel = koinViewModel()
      SettingsScreen(viewModel = vm, onBackClick = { navigator.pop() })
    }
  }
}
