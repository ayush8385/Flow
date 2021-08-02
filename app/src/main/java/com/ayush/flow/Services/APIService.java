package com.ayush.flow.Services;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Authorization:key=AAAAfpLDbYU:APA91bEXZzRy_bxGEp9iJcSjLw8cJ66NmU237M7nLMib0kd_ewgoeH7ScVuQ0dtG7nXuB2JO1u3YYrAgpAvI9bRheWZLQ56BX0xrfLEoCQARRtdtD1r1jGpDu7RIqUmijYgtVedCkLUv",
            "Content-Type:application/json"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
