package com.amgregoire.mangafeed.v2.service

import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException

class NetworkLogService
{
    fun logRequest(request: Request)
    {
        val sb = StringBuilder()

        sb.append("\nOutgoing request")
        sb.append(String.format("\nurl: %s", request.url.toString()))
        sb.append(String.format("\nheaders: %s", request.headers.toString().trim()))

        if (request.body != null) sb.append(String.format("\nrequest: %s", bodyToString(request)))

        Logger.debug(sb.toString())
    }

    fun logResponse(response: Response)
    {
        val sb = StringBuilder()
        val request = response.request

        sb.append("\nIncoming Response [${response.code}]")
        sb.append(String.format("\nurl: %s", request.url.toString()))
        sb.append(String.format("\nheaders: %s", request.headers.toString().trim()))

        if (request.body != null) sb.append(String.format("\nrequest: %s", bodyToString(request)))
        if (response.body != null) sb.append("\nresponse: ${responseToString(response.peekBody(Long.MAX_VALUE))}")

        Logger.debug(sb.toString())
    }

    private fun bodyToString(request: Request): String
    {
        return try
        {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        }
        catch (e: IOException)
        {
            "Failed to retrieve Body"
        }
    }

    private fun responseToString(response: ResponseBody): String
    {
        return try
        {
            String(response.bytes())
        }
        catch (e: IOException)
        {
            "Failed to retrieve Response body"
        }
    }
}