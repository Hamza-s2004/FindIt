package com.example.findit.utils

/**
 * Tiny sealed wrapper used so fragments can react to API/DB outcomes
 * without throwing from inside coroutine bodies.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()
}
