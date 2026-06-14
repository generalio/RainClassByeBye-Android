package com.rainclass.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

private enum class MainTab(
  val title: String,
  val icon: ImageVector
) {
  Courses("我的课程", Icons.Default.School),
  Status("任务状态", Icons.AutoMirrored.Filled.Assignment),
  Profile("个人", Icons.Default.Person)
}

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
      MainShell(
        onCourseClick = { cid -> navigator.navigate(HomeworkList(cid)) },
        onResume = { cid, examId ->
          navigator.navigate(ExamProgress(cid, examId, isResume = true))
        },
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

@Composable
private fun MainShell(
  onCourseClick: (Long) -> Unit,
  onResume: (Long, Long) -> Unit,
  onNavigateToSettings: () -> Unit,
  onLogout: () -> Unit
) {
  var selectedTab by rememberSaveable { mutableStateOf(MainTab.Courses) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0),
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
      ) {
        MainTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Icon(tab.icon, contentDescription = tab.title) },
            label = { Text(tab.title) }
          )
        }
      }
    }
  ) { padding ->
    val contentModifier = Modifier
      .fillMaxSize()
      .padding(padding)
      .consumeWindowInsets(padding)

    when (selectedTab) {
      MainTab.Courses -> {
        val vm: CoursesViewModel = koinViewModel()
        CoursesScreen(
          viewModel = vm,
          modifier = contentModifier,
          onBackClick = null,
          onCourseClick = onCourseClick
        )
      }

      MainTab.Status -> {
        val vm: ExamStatusViewModel = koinViewModel()
        ExamStatusScreen(
          viewModel = vm,
          modifier = contentModifier,
          onBackClick = null,
          onResume = onResume
        )
      }

      MainTab.Profile -> {
        val vm: HomeViewModel = koinViewModel()
        HomeScreen(
          viewModel = vm,
          modifier = contentModifier,
          onNavigateToSettings = onNavigateToSettings,
          onLogout = onLogout
        )
      }
    }
  }
}
