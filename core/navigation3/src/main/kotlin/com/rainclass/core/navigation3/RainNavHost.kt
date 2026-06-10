package com.rainclass.core.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

val LocalRainNavigator = staticCompositionLocalOf<RainNavigator> {
    error("RainNavigator 未提供，请在 RainNavHost 内使用")
}

typealias RainEntryProviderScope = EntryProviderScope<NavKey>

/**
 * 项目级 Navigation 3 容器。
 *
 * 使用方式：
 *
 * ```
 * RainNavHost(startRoute = HomeRoute) {
 *     rainEntry<HomeRoute> { route, navigator ->
 *         HomeScreen(
 *             onOpenDetail = { id -> navigator.navigate(DetailRoute(id)) }
 *         )
 *     }
 *
 *     rainEntry<DetailRoute> { route, navigator ->
 *         DetailScreen(
 *             id = route.id,
 *             onBack = { navigator.pop() }
 *         )
 *     }
 * }
 * ```
 *
 * `rainEntry<T>` 会创建 Navigation 3 的 NavEntry，并把 route 参数和
 * RainNavigator 一起传给页面。页面跳转统一使用 RainNavigator，不直接操作
 * Navigation 3 的 backStack。
 */
@Composable
fun RainNavHost(
    startRoute: RainRoute,
    modifier: Modifier = Modifier,
    entries: RainEntryProviderScope.() -> Unit
) {
    val backStack = rememberNavBackStack(startRoute)
    val resultBus = remember { RainNavigationResultBus() }
    val navigator = remember(backStack, resultBus) {
        RainNavigator(backStack = backStack, resultBus = resultBus)
    }

    CompositionLocalProvider(
        LocalRainNavigator provides navigator,
        LocalRainNavigationResultBus provides resultBus
    ) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = { navigator.pop() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider(builder = entries)
        )
    }
}

/**
 * Navigation 3 entry 的项目级薄封装。
 *
 * 新增页面时，在 RainNavHost 的 entries 中注册：
 *
 * ```
 * rainEntry<MyRoute> { route, navigator ->
 *     MyScreen(
 *         id = route.id,
 *         onNext = { navigator.navigate(NextRoute) },
 *         onBack = { navigator.pop() }
 *     )
 * }
 * ```
 *
 * 这里的 `route` 就是入栈时传入的 route 实例，字段可直接作为页面参数使用。
 */
inline fun <reified T : RainRoute> RainEntryProviderScope.rainEntry(
    noinline content: @Composable (route: T, navigator: RainNavigator) -> Unit
) {
    entry<T> { route ->
        content(route, LocalRainNavigator.current)
    }
}

/**
 * 在 RainNavHost 子树内快捷获取项目级导航器。
 *
 * 大多数页面推荐通过 `rainEntry` 的参数接收 navigator；如果页面内部的
 * 子组件需要主动跳转，可以调用：
 *
 * ```
 * val navigator = rememberRainNavigator()
 * navigator.navigate(DetailRoute(id))
 * ```
 */
@Composable
fun rememberRainNavigator(): RainNavigator = LocalRainNavigator.current
