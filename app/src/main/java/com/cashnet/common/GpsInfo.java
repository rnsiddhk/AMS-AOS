package com.cashnet.common;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/***
 *  GPS 데이터 서비스
 *  Created by JJH on 2016-09-05.
 */
public class GpsInfo extends Service {

    // GPS 상태값
    boolean isGetLocation = false;

    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 1000;

    // GPS Manager 객첵
    protected LocationManager locationManager;

    // 커스텀 Thread 클래스
    private ServiceThread serviceThread;

    /***
     *  위치 정보 갱신하는 메소드
     */
    public void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                this.isGetLocation = true;
                // 네트워크 정보로 부터 위치값 가져오기

            // 퍼미션 체크
            if (checkPermission()) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  퍼미션 체크
     * */
    private boolean checkPermission(){

        // 마시멜로 이전 버전 권한 체크 필요없음
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else {

            return true;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     *  최초 생성되었을 때 한번 실행
     * */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     *  백그라운드에서 실행되는 동작들이 들어가는 곳
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("onStartCommand >> ", "여기 실행중?");
        ServiceHandler serviceHandler = new ServiceHandler();
        serviceThread = new ServiceThread(serviceHandler);
        serviceThread.start();
        return START_NOT_STICKY ;
    }

    /**
     *  서비스가 종료될 때 할 작업
     * */
    @Override
    public void onDestroy() {
        Log.e("onDestroy >> ", "여기 실행중?");
        serviceThread.stopForever();
        serviceThread = null;

    }

    /**
     *  Location Listener 콜백 메소드
     * */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (checkPermission()) {

                if (location != null) {
                    // GPS Provider 데이터인지
                    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                        // 2016-09-05 JJH
                        // 데이터 전송 로직 넣을 예정
                        // 사용자 ID, 위도, 경도, 시간
                        writeText(location.getLatitude(), location.getLongitude());

                    }// NextWork Provider 인지
                    else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {

                        writeText(location.getLatitude(), location.getLongitude());

                    } else {
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     *  테스트용 텍스트 파일 쓰는 메소드(위치 정보 기록)
     * */
    private void writeText(double lat, double lon){
        String dirPath = "/sdcard/Test";
        File file = new File(dirPath);
        File logFile = new File("/sdcard/Test/gps.txt");

        if (!file.exists()){
            file.mkdir();
        }

        Calendar calendar = Calendar.getInstance();

        // long으로 가져올 때
        long now = calendar.getTimeInMillis();
        // 문자열로 가져올 때
        String str = calendar.getTime().toString();

        // 텍스트 파일에 출력될 문자열
        String strGpsInfo = str + " 위도 >> " + lat + ", 경도 >> " + lon + "\n";

        try{

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(strGpsInfo);
            buf.newLine();
            buf.close();

            Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();

        } catch(IOException e){}
    }

    /***
     *  위치정보 갱신하는 핸들러
     */
   class ServiceHandler extends android.os.Handler{

        @Override
        public void handleMessage(Message msg) {
            getLocation();
        }
    }
}
