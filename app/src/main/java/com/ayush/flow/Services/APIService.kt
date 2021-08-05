package com.ayush.flow.Services

import com.ayush.flow.Notification.Constants.Companion.CONTENT_TYPE
import com.ayush.flow.Notification.Constants.Companion.SERVER_KEY
import com.ayush.flow.Notification.MyResponse
import com.ayush.flow.Notification.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Authorization:key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")

    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse>
}