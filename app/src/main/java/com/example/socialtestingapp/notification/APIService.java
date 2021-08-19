package com.example.socialtestingapp.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Authorization: key=AAAAtNCDcyg:APA91bEHGOwQYgVa_RbAdubBlFZgaoc8ekw1EzhzKL0_W56gEnfTmhKmy7QArMLnot9byQuHoKThdxq0PBq_JHzVNgLXYN6T6sSyWPX0x6-rLsOXNP0TJApkwnHSvgODaIPTC6G7S45c",
                    "Content-Type: application/json"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
