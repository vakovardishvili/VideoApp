package com.gaagore.videoapp.amazon.amazonservice

import com.gaagore.videoapp.amazon.MeetingInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface Api {
    @Headers(
        "Account-Id: eIT0jPlSQ2VEpbAEcgHEkrTlQW83",
        "Session-Token: ${ServiceConsts.ACCESS_TOKEN}"
    )
    @FormUrlEncoded
    @POST("api/v1/meeting/access")
    fun getSessionData(
        @Field("meeting") title: String?,
    ): Call<MeetingInfo>

    companion object {
        var BASE_URL = "https://api-meeting-eeihggz33a-uc.a.run.app/"
        private val client = OkHttpClient()

        fun create(): Api {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(ApiWorker.gsonConverter)
                .client(ApiWorker.client)
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(Api::class.java)
        }
    }
}