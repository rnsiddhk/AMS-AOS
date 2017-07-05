package com.firebase.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JJH on 2016-08-10.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.e(" Refreshed token: " , refreshedToken);

        // 토큰 서버에 등록하는 부분 추가 예정
        // 2016.08.30 JJH
        //sendRegistrationToServer(refreshedToken);

    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}