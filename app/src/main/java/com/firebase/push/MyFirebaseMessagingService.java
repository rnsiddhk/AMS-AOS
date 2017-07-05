package com.firebase.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cashnet.R;
import com.cashnet.main.Intro;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.UnsupportedEncodingException;


/**
 * Created by Won on 2016-08-10.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static PowerManager.WakeLock sCpuWakeLock;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.e("From: " , remoteMessage.getFrom());
//        Log.e("Message Body: " , remoteMessage.getData().get("message")); // Debug version
        Log.e("Message Body: " , remoteMessage.getNotification().getBody()); // Release version


        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "");

        sCpuWakeLock.acquire();


        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }

        //Calling method to generate notification
//        sendNotification(remoteMessage.getData().get("message"));   // Debug version
        sendNotification(remoteMessage.getNotification().getBody());  // Release version
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, Intro.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        byte[] byte_text = null;
        String temp = "";
        try {
            byte_text =  messageBody.getBytes("iso-8859-1");
            temp = new String(byte_text , "utf-8");
        }catch (Exception ex){

        }

        String temp_test = messageBody; // 테스트
        String [] charSet = {"utf-8","euc-kr","ksc5601","iso-8859-1","x-windows-949", "MS949"};

        for (int i=0; i<charSet.length; i++) {
            for (int j=0; j<charSet.length; j++) {
                try {
                    Log.e("로그확인 ", "[" + charSet[i] +"," + charSet[j] +"] = " + new String(temp_test.getBytes(charSet[i]), charSet[j]));
                } catch (UnsupportedEncodingException e) {
                    Log.e("UnsupportedEncoding >> " , e.toString());
                }
            }
        }


        // 소리 지정(기본 설정)
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("장애 메세지")
                .setSmallIcon(R.drawable.icon_notic)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int s = (int) System.currentTimeMillis() ;

        notificationManager.notify(s, notificationBuilder.build());
    }
}
