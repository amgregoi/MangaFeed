package com.amgregoire.mangafeed.v2.network

import okhttp3.Request
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class CallDelegate<TIn, TOut>(
        protected val proxy: Call<TIn>
) : Call<TOut>
{
    override fun execute(): Response<TOut> = throw NotImplementedError()
    final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)
    final override fun clone(): Call<TOut> = cloneImpl()

    override fun cancel() = proxy.cancel()
    override fun request(): Request = proxy.request()
    override fun isExecuted() = proxy.isExecuted
    override fun isCanceled() = proxy.isCanceled

    abstract fun enqueueImpl(callback: Callback<TOut>)
    abstract fun cloneImpl(): Call<TOut>
}

class ResultCall<T>(proxy: Call<T>) : CallDelegate<T, Result<T>>(proxy)
{
    override fun execute(): Response<Result<T>>
    {
        val result = mapTo(proxy.execute())
        return Response.success(result)
    }

    private fun mapTo(response: Response<T>): Result<T>
    {
        val code = response.code()
        val result = if (code in 200 until 300)
        {
            val body = response.body()
            if (body != null) Result.Success(body)
            else Result.Failure<T>(NullPointerException(), code)
        }
        else
        {
            Result.Failure<T>(NullPointerException(), code)
        }

        return result
    }

    override fun enqueueImpl(callback: Callback<Result<T>>) = proxy.enqueue(object : Callback<T>
    {
        override fun onResponse(call: Call<T>, response: Response<T>)
        {
            val code = response.code()
            val result = if (code in 200 until 300)
            {
                val body = response.body()
                if (body != null) Result.Success(body)
                else Result.Failure<T>(NullPointerException(), code)
            }
            else
            {
                Result.Failure<T>(NullPointerException(), code)
            }

            callback.onResponse(this@ResultCall, Response.success(result))
        }

        override fun onFailure(call: Call<T>, t: Throwable)
        {
            val result = Result.Failure<T>(t)
            callback.onResponse(this@ResultCall, Response.success(result))
        }
    })

    override fun cloneImpl() = ResultCall(proxy.clone())
}

class ResultAdapter(
        private val type: Type
) : CallAdapter<Type, Call<Result<Type>>>
{
    override fun responseType() = type
    override fun adapt(call: Call<Type>): Call<Result<Type>> = ResultCall(call)
}

class ResultCallFactory : CallAdapter.Factory()
{
    override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ) = when (getRawType(returnType))
    {
        Call::class.java ->
        {
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            when (getRawType(callType))
            {
                Result::class.java ->
                {
                    val resultType = getParameterUpperBound(0, callType as ParameterizedType)
                    ResultAdapter(resultType)
                }
                else -> null
            }
        }
        else -> null
    }
}