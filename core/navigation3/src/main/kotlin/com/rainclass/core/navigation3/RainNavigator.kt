package com.rainclass.core.navigation3

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * 项目级导航器，只负责修改 Navigation 3 的 back stack。
 *
 * 页面不直接操作 backStack，统一通过这里 navigate/pop，后续要加日志、
 * 埋点、登录拦截或模块间结果通信时，只需要在这一层扩展。
 *
 * 常用操作：
 *
 * ```
 * navigator.navigate(DetailRoute(id))       // 普通入栈，打开新页面
 * navigator.navigateSingleTop(HomeRoute)    // 栈顶不是目标页面时才入栈
 * navigator.pop()                           // 弹出当前页面
 * navigator.popToRoot()                     // 回到根页面
 * navigator.popTo<HomeRoute>()              // 回到最近的 HomeRoute
 * navigator.replaceTop(EditRoute(id))       // 替换当前页面
 * navigator.replaceAll(LoginRoute)          // 清空返回栈并打开登录页
 * navigator.navigateBackWithResult(result)  // 返回上一页并发送一次性结果
 * ```
 */
class RainNavigator internal constructor(
  private val backStack: NavBackStack<NavKey>,
  private val resultBus: RainNavigationResultBus
) {
  val currentRoute: RainRoute
    get() = backStack.last() as RainRoute

  val canPop: Boolean
    get() = backStack.size > 1

  /**
   * 普通入栈。
   *
   * 等价于“打开一个新页面”：新 route 会追加到返回栈末尾，系统返回键
   * 或 `pop()` 会回到上一个页面。
   */
  fun navigate(route: RainRoute) {
    backStack.add(route)
  }

  /**
   * 单顶入栈。
   *
   * 当栈顶已经是同一个 route 时不重复添加，适合底部导航、重复点击按钮、
   * 或避免连续打开同一详情页的场景。
   */
  fun navigateSingleTop(route: RainRoute) {
    if (backStack.lastOrNull() != route) {
      backStack.add(route)
    }
  }

  /**
   * 替换当前栈顶页面。
   *
   * 常用于“提交后进入结果页”这类流程，用户返回时会回到当前页面的上一级，
   * 而不是回到被替换掉的页面。
   */
  fun replaceTop(route: RainRoute) {
    if (backStack.isNotEmpty()) {
      backStack.removeLastOrNull()
    }
    backStack.add(route)
  }

  /**
   * 清空当前返回栈并打开目标页面。
   *
   * 用于登录成功、退出登录这类流程切换，等价于 Navigation 2 中
   * navigate(route) { popUpTo(...) { inclusive = true } } 的使用场景。
   */
  fun replaceAll(route: RainRoute) {
    backStack.clear()
    backStack.add(route)
  }

  /**
   * 弹出当前页面。
   *
   * 根页面不会被弹出，返回 false 表示当前已经在根页面。
   */
  fun pop(): Boolean {
    return if (canPop) {
      backStack.removeLastOrNull()
      true
    } else {
      false
    }
  }

  /**
   * 一直弹栈到根页面。
   */
  fun popToRoot() {
    while (backStack.size > 1) {
      backStack.removeLastOrNull()
    }
  }

  /**
   * 按条件回退到最近的指定页面。
   *
   * 示例：
   *
   * ```
   * navigator.popTo { route -> route is DetailRoute && route.id == id }
   * ```
   */
  fun popTo(matcher: (RainRoute) -> Boolean): Boolean {
    val index = backStack.indexOfLast { route ->
      route is RainRoute && matcher(route)
    }
    if (index < 0) return false

    while (backStack.lastIndex > index) {
      backStack.removeLastOrNull()
    }
    return true
  }

  /**
   * 回退到最近的指定 route 类型。
   */
  inline fun <reified T : RainRoute> popTo(): Boolean = popTo { it is T }

  /**
   * 返回上一页并发送一次性结果。
   *
   * 上一个页面可使用 `RainResultEffect<ResultType> { ... }` 接收。
   */
  fun navigateBackWithResult(result: Any): Boolean {
    resultBus.send(result)
    return pop()
  }
}
