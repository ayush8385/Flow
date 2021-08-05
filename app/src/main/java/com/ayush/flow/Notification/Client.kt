package com.ayush.flow.Notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Client {

//    companion object{
//        private val retrofit by lazy {
//            Retrofit.Builder()
//                .baseUrl(BASE__URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//        }
//
//        val api by lazy {
//            retrofit.create(APIService::class.java)
//        }
//    }

    object Client{
        private var retrofit: Retrofit?=null

        fun getClient(url:String):Retrofit?{
            if(retrofit==null){
                retrofit=Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }

    }
}