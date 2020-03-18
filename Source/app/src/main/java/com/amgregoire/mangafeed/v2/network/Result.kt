package com.amgregoire.mangafeed.v2.network

import com.amgregoire.mangafeed.v2.exception.EmptyException
import retrofit2.Call

sealed class Result<T>
{
    data class Success<T>(val value: T) : Result<T>()
    class Failure<T>(val throwable: Throwable = EmptyException("Empty Exception"), val code: Int = 0) : Result<T>()
}

fun <I, O> Call<Result<I>>.result(mapTo: (I) -> O) = this.execute().body()?.run {
    when (this)
    {
        is Result.Success -> Result.Success(mapTo(this.value))
        is Result.Failure -> Result.Failure<O>(this.throwable, this.code)
    }
} ?: Result.Failure(NullPointerException())