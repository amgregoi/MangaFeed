package com.amgregoire.mangafeed.v2.di.module

//import okhttp3.logging.HttpLoggingInterceptor
import android.app.Application
import com.amgregoire.mangafeed.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule
{

    /*
     * The method returns the Gson object
     * */
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()


    /*
     * The method returns the Cache object
     * */
    @Provides
    @Singleton
    fun provideCache(application: Application): Cache
    {
        val cacheSize: Long = 10 * 1024 * 1024; // 10 MB
        val httpCacheDirectory = File(application.getCacheDir(), "http-cache");
        return Cache(httpCacheDirectory, cacheSize);
    }


    /*
     * The method returns the Okhttp object
     * */
    @Provides
    @Singleton
    fun provideOkhttpClient(cache: Cache): OkHttpClient
    {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY;

        val httpClient = OkHttpClient.Builder();
        httpClient.cache(cache)
        httpClient.addInterceptor(logging)
        httpClient.connectTimeout(30, TimeUnit.SECONDS)
        httpClient.readTimeout(30, TimeUnit.SECONDS)
        return httpClient.build()
    }


    /*
     * The method returns the Retrofit object
     * */
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit
    {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BuildConfig.BASE_URL) // TODO :: update url
                .client(okHttpClient)
                .build()
    }

    /*
     * We need the MovieApiService module.
     * For this, We need the Retrofit object, Gson, Cache and OkHttpClient .
     * So we will define the providers for these objects here in this module.
     *
     * */

//    @Provides
//    @Singleton
//    MovieApiService provideMovieApiService(Retrofit retrofit)
//    {
//        return retrofit.create(MovieApiService.class);
//    }
}