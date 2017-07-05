package com.cashnet.main;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.cashnet.R;

public class Intro extends Activity{

	private Handler handler;

	// 권한 종류 : 카메라, 메모리 읽기/쓰기, 전화, 위치, 셋팅, 네트워크 상태
	private String[] permissions = { Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.CALL_PHONE,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_NETWORK_STATE };

	private final static int  MY_PERMISSIONS = 100;

	private final String filter = "finishReceiver";
	private final IntentFilter ifilter = new IntentFilter(filter);
	private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Intro.this.finish();
		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		registerReceiver(finishReceiver, ifilter);

		checkPermission();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(finishReceiver);
	}

	private void runApp(){

		handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				Intent intent = new Intent(Intro.this, Main.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
		}, 1000);

	}

	/**
	 * Permission check.
	 */
	private void checkPermission() {

		// 마시멜로 이전 버전 권한 체크 필요없음
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			runApp();
			return;
		}

		// 권한 있는지 여부 체크
		// checkSelfPermission() 리턴 값이 0 이면 권한 있음. -1 이면 권한 없음
		// 권한 종류 : 카메라, 메모리 읽기/쓰기, 전화, 위치, 네트워크 상태
		if (checkSelfPermission(Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.CALL_PHONE)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
				!= PackageManager.PERMISSION_GRANTED) {

			// 권한 요청
			ActivityCompat.requestPermissions(this,
					permissions,
					MY_PERMISSIONS);


		}else{	// 권한이 이미 다 있으면 App 실행
			runApp();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub

		switch (requestCode) {
			case MY_PERMISSIONS:

				if (grantResults.length > 0) {
					// 권한 허가
					// 해당 권한을 사용해서 작업을 진행할 수 있습니다
					for (int i = 0; i < grantResults.length; i++) {
						if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
							Toast.makeText(this, "앱을 실행하기 위한 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
							// 셋팅 화면으로 이동 후 권한 얻기
//							 startActivity(new Intent(Settings.ACTION_SETTINGS));`
							return;
						}
					}
					runApp();
				}
				break;
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}