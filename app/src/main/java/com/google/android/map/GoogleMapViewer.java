package com.google.android.map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.MarkerItem;
import com.cashnet.common.SGPreference;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.menu.LocationSearchListActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  구글맵 뷰어
 *  see google_map_viewer
 * @since 2016-08-29
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * Created by JJH on 2016-08-29.
 */
public class GoogleMapViewer extends BaseActivtiy implements OnMapReadyCallback, View.OnClickListener{

    // 좌표 객체
    private  LatLng POINT;

    // 구글맵 객체
    private GoogleMap googleMap;

    // 마커 레이아웃
    private View pinView, pinPointView;

    // 마커 말풍선
    private TextView txt_contents;

    // 내위치 버튼
    private Button btn_my_location;

    // 위치 조회 버튼
    private Button btn_location_search;

    private Context ctx;

    // 조회된 내용 저장할 JSON 객체
    private JSONObject jsonRes;

    // 위경도 저장 객체
    private LatLng[] latlng;

    // 시작 및 내위치 위도, 경동 변수
    private double startLat, startLng;

    // 목적지 위도, 경도 변수
    private double endLat, endLng;

    private LocationManager locationManager;

    private int gpsSearchTime = 300; // GPS 탐색시간 :  300밀리세크 * 100 = 30초

    private ProgressDialog mProgress = null;

    // 경로 조회 Task
    private SearchTask searchTask;

    // 위치 조회 Task
    private LocationTask locationTask;

    // 인텐트에서 전달된 액티비티 구분 값
    // POINT : 위치보기, ROUTE : 경로보기, PROXIMITY : 근접기기 보기
    private String actGB = "";

    // 지구의 반지름
    private static final double EARTHRADIUS = 6366198;

    // 북동쪽 좌표, 남서쪽 좌표
    private LatLng northEast, southWest;

    // 기기 이름
    private String atmNM;

    private SGPreference user_info;

    // 기기번호
    private String ORG_CD = "";

    // 시작 위도, 경도
    private String str_lat = "", str_lng = "";

    private Intent intent;

    // 폴리라인 Array 객체
    private List<Polyline> polyLines = new ArrayList<Polyline>();

    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;


        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }


        // 위치보기시 맵 초기화시 포인트 찍기
        if (actGB.equalsIgnoreCase("POINT")) {

            POINT = new LatLng(endLat, endLng);

            addMarker(POINT, atmNM);

            // 시작 위치의 Zoom Level 설정
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POINT, 17));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

            // 마커 드래그 이벤트 리스너
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    Log.e("onMarkerDrag >>", marker.getPosition() + "");
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }

                @Override
                public void onMarkerDragEnd(final Marker marker) {
                    LatLng temp = marker.getPosition();
                    Log.e("변환전 주소", "위도 : " + Double.toString(temp.latitude).substring(0, 10) + " 경도 : " + Double.toString(temp.longitude).substring(0, 11));

                    str_lat = Double.toString(temp.latitude).substring(0, 10);
                    str_lng = Double.toString(temp.longitude).substring(0, 11);

                    AlertDialog.Builder ab = new AlertDialog.Builder(ctx);
                    ab.setMessage("기기 위치를 수정하시겠습니까?");
                    ab.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            InputDataTask inputDataTask = new InputDataTask();
                            inputDataTask.execute();
                        }
                    });
                    ab.setNegativeButton("취소", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            marker.setPosition(POINT);
                        }
                    });
                    ab.show();

                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.google_map_viewer);

        super.onCreate(savedInstanceState);

        ctx = this;

        user_info = new SGPreference(ctx);

        // 액티비티 플래그
        actGB = getIntent().getExtras().getString("ACT_GB", "POINT");
        ORG_CD = getIntent().getExtras().getString("ORG_CD", "");

        initView();

        if (!actGB.equalsIgnoreCase("PROXIMITY")){
            endLat = getIntent().getExtras().getDouble("Y_GRID", 0.0);
            endLng = getIntent().getExtras().getDouble("X_GRID", 0.0);
            atmNM = getIntent().getExtras().getString("ATM_NAME","");
        }

        // 맵 플레그먼트 셋팅
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 위치보기 기능은 조회 제외
        // 현재 위치정보 가져오기
        if (actGB.equalsIgnoreCase("ROUTE") || actGB.equalsIgnoreCase("PROXIMITY")) {
            btn_my_location.performClick();
        }

    }

    // 객체 생성 메소드
    private void initView(){

        // 타이틀바 이름 정하기
        if (actGB.equalsIgnoreCase("ROUTE")) {
            ((TextView) (((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("경로보기");
        }else if (actGB.equalsIgnoreCase("PROXIMITY")){
            ((TextView) (((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("주변기기");
        }else{
            ((TextView) (((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("지도보기");
        }

        // 마커 레이아웃 커스텀
        pinView = LayoutInflater.from(this).inflate(R.layout.pin_layout, null);

        // 마커 포인트 레이아웃 커스텀
        pinPointView = LayoutInflater.from(this).inflate(R.layout.pin_point_layout, null);

        // 마커 말풍선
        txt_contents = (TextView) pinView.findViewById(R.id.txt_contents);

        // 경로보기 화면
        // 내위치 버튼 셋팅 및 활성화
        if (actGB.equalsIgnoreCase("ROUTE") || actGB.equalsIgnoreCase("PROXIMITY")) {
            // 내위치 버튼
            btn_my_location = (Button) findViewById(R.id.btn_my_location);
            btn_my_location.setVisibility(View.VISIBLE);
            btn_my_location.setOnClickListener(this);

            // GPS 매니저
            locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        }

        // 2016-10-17 JJH 차후 오픈예정
        // 위치 조회 기능
        else{

            // 위치 조회 버튼
            btn_location_search = (Button) findViewById(R.id.btn_location_search);
            btn_location_search.setVisibility(View.VISIBLE);
            btn_location_search.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        super.onClick(v);

        switch (v.getId()){

            // 내 위치 버튼
            case R.id.btn_my_location:

                if (checkPermission()) {
                    locationManager.removeUpdates(locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);     // 1초 자동갱신으로 바꿈
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener); // 1초 자동갱신으로 바꿈
                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, locationListener); // 1초 자동갱신으로 바꿈

                    locationTask = new LocationTask();
                    locationTask.execute("location");
                }
                break;

            // 위치 조회 버튼
            case R.id.btn_location_search:

                intent = new Intent(GoogleMapViewer.this, LocationSearchListActivity.class);
                intent.putExtra("ORG_CD" , ORG_CD);
                startActivity(intent);
                finish();
                break;
        }

    }

    // 마시멜로 버전 권한 체크 지도 관련 Runtime 권한 체크 메소드
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

    // 지도 반경 구하는 메소드
    private static LatLng move(LatLng startLL, double toNorth, double toEast) {

        Log.e("첫 좌표", startLL.latitude  + "," + startLL.longitude);
        double lonDiff = meterToLongitude(toEast, startLL.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startLL.latitude + latDiff, startLL.longitude + lonDiff);
    }

    // 특정 거리 경도 구하기
    private static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        return Math.toDegrees(rad);
    }

    // 특정 거리 위도 구하기
    private static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        return Math.toDegrees(rad);
    }

    // 테스트용
    private void  setMarkerItems() {

        ArrayList<MarkerItem> sampleList = new ArrayList();

        sampleList.add(new MarkerItem(37.4802045, 126.8838206, "15:14:39"));

        sampleList.add(new MarkerItem(37.4800068, 126.883458, "15:30:39"));

        sampleList.add(new MarkerItem(37.479887, 126.8824707, "15:31:37"));

        sampleList.add(new MarkerItem(37.4794788, 126.8822086, "15:32:43"));

        sampleList.add(new MarkerItem(37.4789559, 126.8822377, "15:33:29"));

        sampleList.add(new MarkerItem(37.47842685790394, 126.88163484199886, "15:34:24"));

        sampleList.add(new MarkerItem(37.477988024286134, 126.88223287718723, "15:35:30"));

        sampleList.add(new MarkerItem(37.4769904, 126.8829483, "15:36:42"));

        sampleList.add(new MarkerItem(37.4771716, 126.8828922, "15:37:32"));

        sampleList.add(new MarkerItem(37.47734570623741, 126.88332099328356, "15:38:52"));

        sampleList.add(new MarkerItem(37.4771577, 126.8829105, "15:40:49"));

        sampleList.add(new MarkerItem(37.47774197549374, 126.88276495648239, "15:41:37"));

        LatLng[] test_latlng = new LatLng[sampleList.size()];
        for (int i = 0; i < test_latlng.length; i++){
            test_latlng[i] = new LatLng(sampleList.get(i).getLat(), sampleList.get(i).getLon());
        }


        for (MarkerItem markerItem : sampleList) {
            addMarker(markerItem);
        }

        // 임시
        googleMap.addPolyline(new PolylineOptions()
                .add(test_latlng)
                .width(20)
                .color(Color.BLUE)
                .geodesic(true));
    }

    // 마커 추가 메소드 (근접기기 전용)
    private Marker addMarker(MarkerItem markerItem) {

        LatLng position = new LatLng(markerItem.getLat(), markerItem.getLon());

        txt_contents.setText(markerItem.getContents());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(ctx, pinView)));

        return googleMap.addMarker(markerOptions);

    }

    // 마커 추가 메소드 (위치 찾기 전용)
    private Marker addMarker(LatLng position, String name) {

        txt_contents.setText(name);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.draggable(true);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(ctx, pinView)));
        return googleMap.addMarker(markerOptions);

    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
    /**
     * 위치확인을 위한 콜백메서드 locationTask 에서 좌표 찾으면 들어옴
     */
    public LocationListener locationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {

            if (checkPermission()){

                if ( location != null) {

                    // GPS Provider 데이터인지
                    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                            || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                            || location.getProvider().equals(LocationManager.PASSIVE_PROVIDER) ) {

                        locationManager.removeUpdates(locationListener); // 위치확인 종료
                        locationTask.cancel(true);
                        mProgress.dismiss(); // 다이얼로그 종료

                        startLat = location.getLatitude();
                        startLng = location.getLongitude();

                        // 경로 찾기, 주변기기 찾기
                        if (actGB.equalsIgnoreCase("ROUTE") || actGB.equalsIgnoreCase("PROXIMITY")) {

                            POINT = new LatLng(startLat, startLng);
                            northEast = move(POINT, 500, 500);
                            southWest = move(POINT, -500, -500);
                            searchTask = new SearchTask();
                            searchTask.execute();
                        }

                    } else {
                        Toast.makeText(ctx, "GPS 연결실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    // 기기 위치 수정 Task
    private class InputDataTask extends AsyncTask<String, Integer, String> {

        private JSONObject jsonSend;

        private JSONArray jsonArray;

        private JSONObject jsonContain;

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            dialog = new ProgressDialog(GoogleMapViewer.this);
            dialog.setMessage(getString(R.string.str_input_loading_msg));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(true);
                        }
                    });
            dialog.show();

        }

        @Override
        protected String doInBackground(String... result) {
            // TODO Auto-generated method stub

            // 네트워크 연결이 안되었을 때
            if (!FunctionClass.getNetworkState(GoogleMapViewer.this)) {

                return "net";

            }else{

                try {

                    jsonSend = new JSONObject();

                    jsonSend.put("org_cd", ORG_CD);  			// 기기번호
                    jsonSend.put("x_grid", str_lng);      		// 경도
                    jsonSend.put("y_grid", str_lat); 	    	// 위도

                    Log.e("기기위치 수정 입력 params >>" , jsonSend.toString());

                    jsonContain = new JSONObject();
                    jsonContain.put("method", "modifyLocation");// API 메소드 명
                    jsonContain.put("params", jsonSend);		// 파라메터

                    jsonArray = new JSONArray();
                    jsonArray.put(jsonContain);

                    // POST 방식 호출
                    jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

                    Log.e("기기위치 수정 입력 결과 >>" , jsonRes.toString());

                    if (jsonRes.getJSONObject("modifyLocation").getString("code").equalsIgnoreCase("0")) {
                        return "success";
                    }else{
                        return "fail";
                    }

                } catch (Exception e) {
                    // TODO: handle exception

                    return "fail";
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if(dialog != null) dialog.dismiss();

            // 네트워크 연결상태 문제
            if (result.equalsIgnoreCase("net")) {
                Toast.makeText(ctx, getString(R.string.str_network_err),Toast.LENGTH_LONG).show();
            } // 결과 없음
            else if (result.equalsIgnoreCase("null")){
                Toast.makeText(ctx, getString(R.string.str_no_result),Toast.LENGTH_LONG).show();
            } // 에러 처리
            else if (result.equalsIgnoreCase("fail")){

                Toast.makeText(ctx, getString(R.string.str_fail_modify_location_msg),Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(ctx, getString(R.string.str_success_modify_location_msg),Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            if(dialog != null) dialog.dismiss();
        }
    }


    // 조회된 결과 처리 메소드
    // 구글맵에 선을 긋는 작업
    private void setRouteData(JSONObject json){

        // route(경로)
        JSONArray jsonRoute = null;

        // 경도, 위도 임시저장 객체
        ArrayList<Double> strTempX, strTempY;

        // 마커 생성을 위한 리스트 객체
        ArrayList<MarkerItem> sampleList = new ArrayList();

        // 37.4816464,126.8829358(가산역), 37.4993786,126.8665636
//        sampleList.add(new MarkerItem(37.4816464,126.8829358, "시작"));
//        sampleList.add(new MarkerItem(37.4993786,126.8665636, "도착"));

        googleMap.clear();

        sampleList.add(new MarkerItem(startLat, startLng, "시작"));
        sampleList.add(new MarkerItem(endLat, endLng, "도착"));

        // 폴리라인이 이미 그려져 있을 경우 삭제 하는 로직
        if (polyLines != null){

            for (Polyline line : polyLines){
                line.remove();
            }
            polyLines.clear();
        }


        // 마커 생성
        for (MarkerItem markerItem : sampleList) {
            addMarker(markerItem);
        }

        try {

            // 전체 JSON에서 route(경로) Array만 분리
            jsonRoute = json.getJSONArray("route");

            // point(경로 구간) JsonArray 배열 만들기
            JSONArray[] jsonPoint = new JSONArray[jsonRoute.length()];

            // 분리된 route(경로) 에서 point(경로 구간)분리
            for(int i = 0; i < jsonRoute.length(); i++) {
                jsonPoint[i] = jsonRoute.getJSONObject(i).getJSONArray("point");
            }

            // 위도 경도 임시 저장 변수
            strTempX = new ArrayList<Double>(); // 경도
            strTempY = new ArrayList<Double>(); // 위도

            // panorama(위도, 경도) 분리
            for (int i = 0; i < jsonRoute.length(); i++) {
                for (int j = 0; j < jsonPoint[i].length(); j++) {

                    // 위도 경도 값 추출
                    // 키 값의 시작과 끝은 빼고 나머지 값 추출
//					jsonPoint[i].getJSONObject(j).getString("key").equalsIgnoreCase("start") << 위,경도 값이 없음
//				    jsonPoint[i].getJSONObject(j).getString("key").equalsIgnoreCase("end")
                    // 	panorama 키값이 확인 : 실제 위도, 경도 값이 들어 있는 부분
                    if (jsonPoint[i].getJSONObject(j).has("panorama")) {
                        strTempX.add (Double.parseDouble(jsonPoint[i].getJSONObject(j).getJSONObject("panorama").getString("lng"))); // 경도
                        strTempY.add (Double.parseDouble(jsonPoint[i].getJSONObject(j).getJSONObject("panorama").getString("lat"))); // 위도
                    }
                }
            }
            Log.e("출발지 경도 좌표 >> ", strTempX + " 길이 : >>" + strTempX.size() );

            Log.e("출발지 위도 좌표 >> ", strTempY + " 길이 : >>" + strTempY.size() );

            // 배열 크기는 시작과 끝 지점의 위경도 값이 추가된 크기로 생성
            latlng = new LatLng[strTempX.size()+2];

            // 테스트 데이터
            // 37.4816464,126.8829358(가산역), 37.4993786,126.8665636(구로성심병원)
//            latlng[0] = new LatLng(37.4816464,126.8829358);
//            latlng[latlng.length-1] = new LatLng(37.4993786,126.8665636);
            // 테스트 데이터

            // 폴리라인을 그리기 위해 처음과 끝 LatLng 배열에 좌표값을 넣어준다
            latlng[0] = new LatLng(startLat, startLng);
            latlng[latlng.length-1] = new LatLng(endLat, endLng);

            // 경로를 좌표값으로 변환하는 작업
            for (int i = 0; i < strTempX.size(); i++){

                // 경로 좌표 정리
                latlng[i+1] = new LatLng(strTempY.get(i), strTempX.get(i));


                // 중간 경로마다에 마커 찍기
//                googleMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(strTempY.get(i), strTempX.get(i)))                     // 마커 위치
//                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(GoogleMapViewer.this, pinPointView))));

            }

            // 추출된 좌표로 경로(선) 만들기
            // ex) .add() 메소드 사용법
            // .add( new LatLng(37.4993786,126.8665636), new LatLng(37.4993786,126.8665636), ..... )  :  좌표 하나씩 넣을 경우 new 연산
            // .add( latlng ) :   LatLng[] 좌표를 배열로 넣을 경우 객체를 그대로 넣으면 된다.
            // .addAll()   : ArrayList<LatLng> 컬렉션으로 해도 가능
            // .geodesic(true) : 측지 세그먼트, 아래 사이트에서 확인
            // https://developers.google.com/maps/documentation/android-api/shapes?hl=ko << 참조 사이트
            polyLines.add(googleMap.addPolyline(new PolylineOptions()
                    .add(latlng)
                    .width(20)
                    .color(Color.BLUE)
                    .geodesic(true)));


            // 출발 좌표로 카메라 이동(중간지점 화면 이동)
            // 경유지 좌표가 35개 이상일 경우 Zoom Level을 낮게 줌
            if (35 < strTempX.size()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng[(latlng.length / 2) + 1], 7));
                // Zoom Level 12, duration 2000 = 2초
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(7), 2000, null);
            }else{
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng[(latlng.length / 2) + 1], 15));

                // Zoom Level 12, duration 2000 = 2초
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            }

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Exception >> ", e.toString());
        }
    }

    // 조회된 결과 처리 메소드
    // 구글맵에 선을 긋는 작업
    private void setProximityData(JSONObject json){

        // route(경로)
        JSONArray jsonArray;
        MarkerItem item;

        try {

            jsonArray = json.getJSONObject("nearAtms").getJSONArray("list");

            googleMap.clear();

            item = new MarkerItem(POINT.latitude, POINT.longitude, "현위치");

            addMarker(item);

            for (int i = 0; i < jsonArray.length(); i++){
                item = new MarkerItem(Double.parseDouble(jsonArray.getJSONObject(i).getString("Y_GRID")),
                        Double.parseDouble(jsonArray.getJSONObject(i).getString("X_GRID")),
                        jsonArray.getJSONObject(i).getString("ATM_NM")
                                + "\n긴급 : " + jsonArray.getJSONObject(i).getString("URGENT_COUNT") + "건, "
                                + "장애 : " + jsonArray.getJSONObject(i).getString("FAILURE_COUNT") + "건");

                addMarker(item);
            }



            // 출발 좌표로 카메라 이동(중간지점 화면 이동)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POINT, 17));

            // Zoom Level 16, duration 2000 = 2초
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Exception >> ", e.toString());
        }
    }

    // 조회 Task
    private class SearchTask extends AsyncTask<String, Integer, String> {

        private String url = "";

        private ProgressDialog dialog;

        private String err_msg = "";

        private JSONObject jsonSend;

        private JSONObject jsonContain;

        private JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            dialog = new ProgressDialog(GoogleMapViewer.this);
            dialog.setMessage(getString(R.string.str_loading_msg));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(true);
                        }
                    });
            dialog.show();
        }

        @Override
        protected String doInBackground(String... result) {
            // TODO Auto-generated method stub

            // 네트워크 연결이 안되었을 때
            if (!FunctionClass.getNetworkState(GoogleMapViewer.this)) {

                return "net";

            }else{

                try {

                    // 경로 탐색 API (네이버)
                    // 37.4816464,126.8829358(가산역), 37.4993786,126.8665636(구로성심병원)
                    // 값 입력시 strat= 경도,위도 destination = 경도, 위도 << 순서대로 넣어야 데이터가 나옴
                    if (actGB.equalsIgnoreCase("ROUTE")) {
                        url = "http://map.naver.com/findroute2/findCarRoute.nhn?via=&call=route2&output=json&car=0&mileage=12.4&start=" + startLng + "," + startLat + "&destination=" + endLng + "," + endLat+ "&search=2";  // 성공 주소

//                      url = "http://www.google.com/maps?f=d&hl=en&saddr=37.4802045,126.8838206&daddr=37.4993786,126.8665636&ie=UTF8&0&om=0&output=kml"; // 구글경로 안내 (테스트)
//                      url = "http://map.naver.com/findroute2/findCarRoute.nhn?via=&call=route2&output=json&car=0&mileage=12.4&start=" + startLon + "," + startLat + "&destination=" + 126.8665636 + "," + 37.4993786 + "&search=2";  // 성공 주소
//                      url = "http://map.naver.com/findroute2/findCarRoute.nhn?via=&call=route2&output=json&car=0&mileage=12.4&start=37.4816464,126.8829358&destination=37.4993786, 126.8665636&search=2";  // 실패 주소
//                      url = "http://map.naver.com/findroute2/findCarRoute.nhn?via=&call=route2&output=json&car=0&mileage=12.4&start=128.0%2C37.0%2C%20&destination=128.0%2C37.5%2C%20&search=2";    // 예시 주소
                    }   // 근접기기 조회
                    else if (actGB.equalsIgnoreCase("PROXIMITY")) {

                        jsonSend = new JSONObject();
                        jsonSend.put("user_id", user_info.getValue("USER_ID", ""));               // 사용자 ID
                        jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));         // 사용자 그룹
                        jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));         // 사용자 직책
                        jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB", ""));           // 지사구분

                        //  right : northEast.longitude (X축), top : northEast.latitude (Y축)
                        //  left ->  southWest.longitude (X축), bottom : southWest.latitude (Y축)
                        jsonSend.put("left", southWest.longitude);             // (X축 - 거리) 남서쪽 경도 좌표
                        jsonSend.put("right", northEast.longitude);            // (X축 + 거리) 북동쪽 경도 좌표
                        jsonSend.put("top", northEast.latitude);               // (Y축 - 거리) 북동쪽 위도 좌표
                        jsonSend.put("bottom", southWest.latitude);            // (Y축 + 거리) 남서쪽 위도 좌표

                        Log.e("주변기기 조회 params >>" , jsonSend.toString());

                        jsonContain = new JSONObject();
                        jsonContain.put("method", "nearAtms");				// API 메소드 명
                        jsonContain.put("params", jsonSend);	        	// 파라메터

                        jsonArray = new JSONArray();
                        jsonArray.put(jsonContain);

                        // POST 방식 호출
                        jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

                        Log.e("주변기기 조회 결과 >>" , jsonRes.toString());

                        if (jsonRes == null) {
                            return "null";
                        }else {
                            return "success";
                        }
                    }

                    // GET 방식 호출
                    jsonRes = JsonClient.connect(url);

                    Log.e("jsonRes 결과 >> ", jsonRes.toString());

                    // 결과 값 유무 확인
                    if (jsonRes == null || 0 > jsonRes.length() ) {
                        return "null";
                    }else{

                        // 경로 API
                        // 결과중 에러값 처리 로직
                        // 키값 중 error 값 확인
                        if (!jsonRes.has("error")) {
                            return "success";
                        } else {
                            // 출발지가 자동차로 갈 수 없는 장소입니다. 다시 설정해 주세요. (반환된 메세지값 출력)
                            err_msg = jsonRes.getJSONObject("error").getString("error_message");
                            return "fail";
                        }

                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    err_msg = "조회중 오류가 발생하였습니다. \n 다시 시도 하시기 바랍니다.";
                    return "fail";
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if(dialog != null) dialog.dismiss();

            // 네트워크 연결상태 문제
            if (result.equalsIgnoreCase("net")) {
                Toast.makeText(ctx, getString(R.string.str_network_err),Toast.LENGTH_LONG).show();
            } // 결과 없음
            else if (result.equalsIgnoreCase("null")){
                Toast.makeText(ctx, getString(R.string.str_no_result),Toast.LENGTH_LONG).show();
            } // 에러 처리
            else if (result.equalsIgnoreCase("fail")){
                Toast.makeText(ctx, err_msg,Toast.LENGTH_LONG).show();
            } else {

                // 성공시 데이터 결과 셋팅 및 처리
                if (actGB.equalsIgnoreCase("ROUTE")) {  // 경로찾기 결과 정리
                    setRouteData(jsonRes);
                }else if(actGB.equalsIgnoreCase("PROXIMITY")){  // 주변기기 결과 정리
                    setProximityData(jsonRes);
                }
            }
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            if(dialog != null) dialog.dismiss();
        }
    }

    // GPS 자기 위치 확인 메소드
    // 네트워크 활성화
    public void getNetWorkGps() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new AlertDialog.Builder(ctx).setTitle("GPS 활성화")
                    .setMessage("환경설정 > 위치 및 보안 > GPS위성 사용을 체크 후 재시도해주십시오.")
                    .setPositiveButton("위치 및 보안 화면으로", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //GPS 환경설정 연결
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {}} ).show();
        }
        //활성화 안됐을 경우 환경설정으로 유도
        else if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            new AlertDialog.Builder(ctx).setTitle("네트워크 활성화").setMessage("무선 네트워크를 활성화하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //네트워크 환경설정 연결
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
            }).show();
            return;
        }
    }

    private void getNetWorkAndGps(String title, String msg) {

        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("네트워크 탐색", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getNetWorkGps();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) {} }).show();

    }
    //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    //■■■■■■비동기로 처리할 네트워크 탐색 클래스■■■■■■
    //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public class LocationTask extends AsyncTask<String, Integer, String>
    {

        // 비동기 작업이 시작되기 전에 호출되면 UI스레드에서 실행
        //mProgress
        protected void onPreExecute()
        {
            mProgress = new ProgressDialog(ctx);
            mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgress.setMessage("GPS신호를 찾는 중...");

            mProgress.setCancelable(false);
            mProgress.setProgress(0);
            mProgress.setButton("취소", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (checkPermission()) {
                        locationManager.removeUpdates(locationListener);
                    }
                    cancel(true);
                }
            });
            mProgress.show();
        }

        // 백그라운드 작업을 수행하며 분리된 스레드에서 실행
        protected String doInBackground(String... params)
        {
            try
            {
                for(int i = 0 ; i <= 100 ; i++)
                {
                    publishProgress(i);
                    Thread.sleep(gpsSearchTime);

                    if(i == 100)
                        return "fail";
                }
            } catch(Exception ex)
            {
                return null;
            }
            return "ok";
        }

        // 작업 경과 표시를 위하 호출. 프로그래시브 바에 진행 상태를 표시.
        protected void onProgressUpdate(Integer... progress)
        {
            mProgress.setProgress(progress[0]);
        }

        // 백그라운드 작업이 끝난 후 UI쓰레드에서 실행
        protected void onPostExecute(String result)
        {
            mProgress.dismiss();

            //GPS 수신 실패
            Log.e("result", result);
            if(result.equals("fail") || result == null)
            {
                if (checkPermission()) {
                    locationManager.removeUpdates(locationListener); // GPS수신 중지
                }

                getNetWorkAndGps("GPS수신 실패", "GPS수신에 실패하였습니다. 재시도하시겠습니까?");

            } //# //GPS 수신 실패
        }

        // cancel 메서드로 작업을 취소했을 때 호출되며 UI스레드에서 실행
        protected void onCancelled()
        {
            super.onCancelled();
            cancel(true);
        }
    }//# 비동기로 처리할 위치확인 클래스
}