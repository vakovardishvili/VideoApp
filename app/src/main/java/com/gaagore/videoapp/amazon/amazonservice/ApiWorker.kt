package com.gaagore.videoapp.amazon.amazonservice

import com.gaagore.videoapp.amazon.amazonservice.ServiceConsts.ACCESS_TOKEN
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

object ApiWorker {
    private var mClient: OkHttpClient? = null
    private var mGsonConverter: GsonConverterFactory? = null

    /**
     * Don't forget to remove Interceptors (or change Logging Level to NONE)
     * in production! Otherwise people will be able to see your request and response on Log Cat.
     */
    val client: OkHttpClient
        @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
        get() {
            if (mClient == null) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.HEADERS

                val headerInterceptor = object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        var request = chain.request()
                        request = request.newBuilder()
                            .addHeader("Account-Id", "eIT0jPlSQ2VEpbAEcgHEkrTlQW83")
                            .addHeader(
                                "Session-Token",
                                ACCESS_TOKEN
                            )
                            .method(request.method, request.body)
                            .build()
                        return chain.proceed(request)
                    }

                }


                val httpBuilder = OkHttpClient.Builder()
                httpBuilder
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(interceptor)
                mClient = httpBuilder.build()

            }
            return mClient!!
        }


    val gsonConverter: GsonConverterFactory
        get() {
            if (mGsonConverter == null) {
                mGsonConverter = GsonConverterFactory
                    .create(
                        GsonBuilder()
                            .setLenient()
                            .disableHtmlEscaping()
                            .create()
                    )
            }
            return mGsonConverter!!
        }
}