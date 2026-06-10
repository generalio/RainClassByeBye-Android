package com.rainclass.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rainclass.feature.courses.CoursesScreen
import com.rainclass.feature.courses.CoursesViewModel
import com.rainclass.feature.exam.ExamProgressScreen
import com.rainclass.feature.exam.ExamViewModel
import com.rainclass.feature.home.HomeScreen
import com.rainclass.feature.home.HomeViewModel
import com.rainclass.feature.homework.HomeworkDetailScreen
import com.rainclass.feature.homework.HomeworkListScreen
import com.rainclass.feature.homework.HomeworkViewModel
import com.rainclass.feature.login.LoginScreen
import com.rainclass.feature.login.LoginViewModel
import com.rainclass.feature.settings.SettingsScreen
import com.rainclass.feature.settings.SettingsViewModel
import com.rainclass.core.network.cookie.PersistentCookieStore
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
                onNavigateToStatus = { /* TODO */ },
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

        composable<Settings> {
            val vm: SettingsViewModel = koinViewModel()
            SettingsScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }
    }
}
