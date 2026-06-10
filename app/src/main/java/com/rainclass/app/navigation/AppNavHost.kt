package com.rainclass.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rainclass.core.network.cookie.PersistentCookieStore
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
fun AppNavHost(startDestination: Any = Login) {
    val navController = rememberNavController()
    val cookieStore: PersistentCookieStore = koinInject()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Login> {
            val vm: LoginViewModel = koinViewModel()
            LoginScreen(viewModel = vm, onLoginSuccess = {
                navController.navigate(Home) { popUpTo(Login) { inclusive = true } }
            })
        }

        composable<Home> {
            val vm: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = vm,
                onNavigateToCourses = { navController.navigate(Courses) },
                onNavigateToStatus = { navController.navigate(Status) },
                onNavigateToSettings = { navController.navigate(Settings) },
                onLogout = {
                    cookieStore.clearAll()
                    navController.navigate(Login) { popUpTo(Home) { inclusive = true } }
                }
            )
        }

        composable<Courses> {
            val vm: CoursesViewModel = koinViewModel()
            CoursesScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onCourseClick = { cid -> navController.navigate(HomeworkList(cid)) }
            )
        }

        composable<HomeworkList> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeworkList>()
            val vm: HomeworkViewModel = koinViewModel()
            HomeworkListScreen(
                cid = route.cid,
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onHomeworkClick = { cid, leafId -> navController.navigate(HomeworkDetail(cid, leafId)) }
            )
        }

        composable<HomeworkDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeworkDetail>()
            val vm: HomeworkViewModel = koinViewModel()
            HomeworkDetailScreen(
                cid = route.cid,
                leafId = route.leafId,
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onStartExam = { cid, examId ->
                    navController.navigate(ExamProgress(cid, examId))
                }
            )
        }

        composable<ExamProgress> { backStackEntry ->
            val route = backStackEntry.toRoute<ExamProgress>()
            val vm: ExamViewModel = koinViewModel()
            ExamProgressScreen(
                cid = route.cid,
                examId = route.examId,
                isResume = route.isResume,
                viewModel = vm,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Status> {
            val vm: ExamStatusViewModel = koinViewModel()
            ExamStatusScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onResume = { cid, examId ->
                    navController.navigate(ExamProgress(cid, examId, isResume = true))
                }
            )
        }

        composable<Settings> {
            val vm: SettingsViewModel = koinViewModel()
            SettingsScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
    }
}
