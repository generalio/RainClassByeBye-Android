package com.rainclass.core.navigation3

import androidx.navigation3.runtime.NavKey

/**
 * 项目内所有页面路由的统一基类。
 *
 * Navigation 3 不再使用字符串 route，页面就是一个可序列化的 NavKey。
 * 新增页面时按下面的方式定义：
 *
 * ```
 * @Serializable
 * data class DetailRoute(val id: Long) : RainRoute
 *
 * @Serializable
 * data object HomeRoute : RainRoute
 * ```
 *
 * data class 用于携带参数，data object 用于无参数页面。所有字段名和类型
 * 都是页面参数本身，跳转时直接构造 route 实例即可。
 */
interface RainRoute : NavKey
