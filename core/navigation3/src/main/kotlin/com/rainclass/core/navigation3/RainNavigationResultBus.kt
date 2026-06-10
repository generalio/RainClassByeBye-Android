package com.rainclass.core.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

class RainNavigationResultBus internal constructor() {
  @PublishedApi
  internal val events = MutableSharedFlow<Any>(extraBufferCapacity = 1)

  fun send(result: Any) {
    events.tryEmit(result)
  }

  suspend inline fun <reified T : Any> collect(noinline onResult: (T) -> Unit) {
    events.filterIsInstance<T>().collect { onResult(it) }
  }
}

val LocalRainNavigationResultBus = staticCompositionLocalOf<RainNavigationResultBus> {
  error("RainNavigationResultBus 未提供，请在 RainNavHost 内使用")
}

/**
 * 监听其他页面返回的一次性结果。
 *
 * 使用示例：
 *
 * ```
 * RainResultEffect<PickResult> { result ->
 *     viewModel.onPicked(result.id)
 * }
 * ```
 *
 * 发送结果：
 *
 * ```
 * navigator.navigateBackWithResult(PickResult(id))
 * ```
 */
@Composable
inline fun <reified T : Any> RainResultEffect(noinline onResult: (T) -> Unit) {
  val resultBus = LocalRainNavigationResultBus.current
  LaunchedEffect(resultBus) {
    resultBus.collect(onResult)
  }
}
