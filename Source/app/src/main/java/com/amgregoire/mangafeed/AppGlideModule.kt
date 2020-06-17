//package com.amgregoire.mangafeed
//
//import android.content.Context
//import com.amgregoire.mangafeed.v2.service.Logger
//import com.bumptech.glide.Glide
//import com.bumptech.glide.GlideBuilder
//import com.bumptech.glide.Registry
//import com.bumptech.glide.annotation.GlideModule
//import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
//import com.bumptech.glide.load.DecodeFormat
//import com.bumptech.glide.load.engine.executor.GlideExecutor.newDiskCacheExecutor
//import com.bumptech.glide.load.engine.executor.GlideExecutor.newSourceExecutor
//import com.bumptech.glide.load.model.GlideUrl
//import com.bumptech.glide.module.AppGlideModule
//import com.bumptech.glide.request.RequestOptions
//import okhttp3.OkHttpClient
//import java.io.InputStream
//import java.util.concurrent.TimeUnit
//
//
//@GlideModule
//class AppGlideModule : AppGlideModule()
//{
//    @Override
//    override fun applyOptions(context: Context, builder: GlideBuilder)
//    {
//        builder.setDiskCacheExecutor(newDiskCacheExecutor())
//        builder.setResizeExecutor(newSourceExecutor())
//        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
//    }
//
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry)
//    {
//        Logger.error("Good morning fable")
//        val client = OkHttpClient.Builder()
//                .connectTimeout(20, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS)
//                .build()
//
//        val factory: OkHttpUrlLoader.Factory = OkHttpUrlLoader.Factory(client)
//        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
//    }
//}