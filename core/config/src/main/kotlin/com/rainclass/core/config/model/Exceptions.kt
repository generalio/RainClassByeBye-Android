package com.rainclass.core.config.model

class UnauthenticatedException(message: String = "登录已过期，请重新登录") : Exception(message)
