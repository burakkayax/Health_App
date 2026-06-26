package com.saglik.core.common.result

data class AppError(
    val message: String,
    val cause: Throwable? = null,
)
