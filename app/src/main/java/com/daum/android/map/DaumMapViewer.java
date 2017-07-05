package com.daum.android.map;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cashnet.R;
import com.cashnet.main.BaseActivtiy;


import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;


/**
 * Created by JJH on 2016-08-26.
 *
 */
public class DaumMapViewer extends FragmentActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener{

    private static final MapPoint DEFAULT_MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.4020737, 127.1086766);
    private static final MapPoint CUSTOM_MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.537229, 127.005515);

    private MapPOIItem mDefaultMarker;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        setActivity(R.layout.daum_map_viewer);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.daum_map_viewer);

        mapView = (MapView)findViewById(R.id.map_view);

        mapView.setDaumMapApiKey("5d88e00f179cb765348f75d4e96fc5cf");
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);


        ((TextView)(((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("지도보기");

    }


    private void showAll() {

    }

//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("테스트 중입니다.");
        marker.setTag(0);
        marker.setMapPoint(DEFAULT_MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.


        mapView.addPOIItem(marker);
        mapView.selectPOIItem(marker, true);
        mapView.moveCamera(CameraUpdateFactory.newMapPoint(DEFAULT_MARKER_POINT));

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
