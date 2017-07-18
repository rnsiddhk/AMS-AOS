package com.cashnet.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FileDownloadClass;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.common.VersionCheck;
import com.cashnet.menu.MenuActivity;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * 로그인 메인화면
 * see main.xml
 * @since 2016-08-17
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class Main extends Activity implements OnClickListener {

	// 로그인, 패스워드
	private EditText edt_id, edt_pwd;

	// 로그인 버튼
	private Button btn_login;

	// ID 저장 체크박스
	private CheckBox chk_id;

	private Context ctx;

	private JSONObject jsonRes = null;	// 결과 Json

	private SGPreference user_info;

	// 사용자 아이디, 사용자 패스워드
	private String user_id = "", user_pw = "";

	// 패키지정보 객체
	private PackageInfo pInfo;

	// 버전 확인 결과변수
	private String result = "";

	public static ProgressDialog mProgress;

	// 다운로드 취소
	public static Boolean finish_chk = false;

	// 버전체크 확인
	private int newVersion = 0;

	private  TelephonyManager telephony;

	private String strTokken = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		ctx = this;

		// 뷰 객체 생성
		initView();

		user_info = new SGPreference(ctx);


		// 아이디 값이 저장되어 있을 시 처리 로직
		if (!user_info.getValue("REMEMBER_ID","").equalsIgnoreCase("")) {
			edt_id.setText(user_info.getValue("REMEMBER_ID",""));
			chk_id.setChecked(true);
		}

		// Wi-Fi 모델 기기 필터링
		try {
			telephony = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
			Log.e("디바이스 ID",  telephony.getDeviceId() + "");

			// 사용자 로그인 ID
			user_info.put("LOGIN_ID", "");

			user_info.put("DEVICE_ID", telephony.getDeviceId());					// 기기 ID

			mProgress = new ProgressDialog(ctx);

		}catch (Exception e) {

			Toast.makeText(ctx, "전화 기능이 없는 기기는 사용이 불가합니다!",Toast.LENGTH_LONG).show();
			finish();
		}

		// 전송 사진을 저장하기 위한 폴더 생성, 앱 설치시 최초 한번 생성
		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(dirPath, "bgf");
		if(!file.exists())
			file.mkdirs();
	}

	@Override
	protected void onResume() {
		super.onResume();

		VersionCheckTask versionCheckTask = new VersionCheckTask();
		versionCheckTask.execute();

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	// 뷰 객체 생성
	void initView(){

		edt_id = (EditText) findViewById(R.id.edt_id);
		edt_pwd = (EditText) findViewById(R.id.edt_pwd);

		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);

		chk_id = (CheckBox) findViewById(R.id.chk_id);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		// 로그인 이벤트 처리
		if(v.getId() == R.id.btn_login){

			// 네트워크 상태 체크
			if (FunctionClass.getNetworkState(ctx)){

				// 최신버전인 경우  로그인 로직
				if (result.equalsIgnoreCase("new")){
					// 공백체크
					if (!FunctionClass.isNullOrBlank(edt_id.getText().toString()) && !FunctionClass.isNullOrBlank(edt_id.getText().toString())){

						user_info.put("TOKKEN", FirebaseInstanceId.getInstance().getToken());	// 사용자 기기 토근 : FCM 용

						// FCM 등록 기기 아이디가 없을 경우 처리 로직
						if(!FunctionClass.isNullOrBlank(user_info.getValue("TOKKEN",""))){
							user_id  = edt_id.getText().toString().trim();
							user_pw  = edt_pwd.getText().toString().trim();

							LoginTask loginTask = new LoginTask();
							loginTask.execute();
						}else{
							Toast.makeText(Main.this, getString(R.string.str_fail_app_id_msg), Toast.LENGTH_SHORT).show();
						}

					}else{

						Toast.makeText(Main.this, getString(R.string.str_input_msg), Toast.LENGTH_SHORT).show();
					}
				}else{// 최신버전이 아닌경우 로그인 시도시 에러 메세지
					Toast.makeText(Main.this, getString(R.string.str_update_msg), Toast.LENGTH_SHORT).show();
				}


			}else{
				Toast.makeText(Main.this, getString(R.string.str_network_err), Toast.LENGTH_SHORT).show();
			}
		}
	}


	// 로그인 결과 처리하는 메소드
	private void setData(JSONObject json){

		try {

			// REG_ST : 상태값 1:정상, 9:해제
			if (json.getJSONObject("login").getString("REG_ST").equalsIgnoreCase("1")){
				user_info.put("USER_ID", json.getJSONObject("login").getString("USER_ID"));		// 사용자 아이디
				user_info.put("PASSWORD", user_pw);		// 패스워드
				user_info.put("USER_NM", json.getJSONObject("login").getString("USER_NM"));		// 사용자명
				user_info.put("USER_GROUP", json.getJSONObject("login").getString("USER_GROUP"));	// 사용자 그룹
				user_info.put("BRANCH_GB", json.getJSONObject("login").getString("BRANCH_GB"));	// 지사구분 : JISA_GB
				user_info.put("USER_GRADE", json.getJSONObject("login").getString("USER_GRADE"));	// 사용자 직책
				user_info.put("USER_ENT", FunctionClass.isNullOrBlankReturn(json.getJSONObject("login").getString("USER_ENT")));		// 업체 코드 :  USER_GROUP 값이 0->본사 1->지사 인 경우는 값이 없음
				user_info.put("SECOND_PASSWD", json.getJSONObject("login").getString("SECOND_PASSWD"));	// 세컨 비밀번호 (시재정보 확인시 사용)

				if (chk_id.isChecked()){
					user_info.put("REMEMBER_ID", json.getJSONObject("login").getString("USER_ID"));		// 사용자 아이디
				}else{
					user_info.put("REMEMBER_ID", "");
				}

				// 결과 처리 후 메뉴 메인 화면으로 이동
				Intent intent =  new Intent (Main.this, MenuActivity.class);
				startActivity(intent);
				finish();
			}else{
				// 해제 값인 경우 처리 프로세스
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 로그인 AsyncTask
	private class LoginTask extends AsyncTask<String, Integer, String>{

		private JSONObject jsonSend;		// 파라메터 Json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(Main.this);
			dialog.setMessage(getString(R.string.str_login_msg));
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();

			// 키보드 가리기
			FunctionClass.doHideKeyBoard(Main.this);
		}

		@Override
		protected String doInBackground(String... result) {
			// TODO Auto-generated method stub

			// 네트워크 연결이 안되었을 때
			if (!FunctionClass.getNetworkState(Main.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();
					jsonSend.put("user_id", user_id);									// 사용자 아이디
					jsonSend.put("user_passwd", user_pw);								// 사용자 비밀번호
					jsonSend.put("device_id", user_info.getValue("DEVICE_ID", ""));		// 기기ID
					jsonSend.put("device_type", "31");									// 기기 종류 : 신규 설치 안드로이드 폰(FCM 푸쉬 이용하기 위해 구분) -> "31"
					jsonSend.put("app_reg_id", user_info.getValue("TOKKEN", ""));		// FCM 토큰값
					jsonSend.put("noti_expire_yn", "N");								// 기기 토큰값 만료 여부 : 정상 -> "N", 만료 -> "Y"

					jsonContain = new JSONObject();
					jsonContain.put("method", "login");									// API 메소드 명
					jsonContain.put("params", jsonSend);								// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);
					Log.e("로그인 params >>" , jsonSend.toString());

					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("로그인 결과 >>" , jsonRes.toString());

					if (jsonRes == null || 0 > jsonRes.length()) {
						return "null";

					}else if(jsonRes.getJSONObject("login").getString("permit").equalsIgnoreCase("Y")){

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
				Toast.makeText(Main.this, getString(R.string.str_network_err), Toast.LENGTH_SHORT).show();
			} // 결과 없음
			else if (result.equalsIgnoreCase("null")){
				Toast.makeText(Main.this,           getString(R.string.str_login_fail), Toast.LENGTH_SHORT).show();
			} // 에러 처리
			else if (result.equalsIgnoreCase("fail")){
				Toast.makeText(Main.this, getString(R.string.str_login_fail), Toast.LENGTH_SHORT).show();
			} else {	// 성공시 데이터 결과 셋팅 및 처리
				Toast.makeText(Main.this, getString(R.string.str_login_success), Toast.LENGTH_SHORT).show();
				setData(jsonRes);
			}
		}
	}

	// 버전체크 AsyncTask
	private class VersionCheckTask extends AsyncTask<String, Integer, String>{

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(Main.this);
			dialog.setMessage(getString(R.string.str_version_msg));
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);
						}
					});
			dialog.show();

		}

		@Override
		protected String doInBackground(String... str) {
			// TODO Auto-generated method stub

			// 네트워크 연결이 안되었을 때
			if (!FunctionClass.getNetworkState(Main.this)) {

				return "net";

			}else{
				// 수정
				try {

					// 현재 패키지 정보 얻어옴.
					pInfo = getPackageManager().getPackageInfo("com.cashnet", PackageManager.GET_META_DATA);

					Log.e("App현재 버전>> ", pInfo.versionCode +"");

					// 최신버전 XML 파싱..
					VersionCheck versionCheck = new VersionCheck();
					newVersion = versionCheck.getVersion();

					Log.e("버전체크 결과 >> ", newVersion +"");

					// 새버전 체크
					if (0 < newVersion) {
						// 설치된 버전보다 업그레이드할 버전이 높으면
 						if(pInfo.versionCode < newVersion){
							result = "upgrade";
						}else{
							result = "new";
						}
					}
					// 버전체크 Exception 되었을 때 재시도 루틴
					else{
						result = "version_fail";
					}

					return result;

				} catch (Exception e) {
					// TODO: handle exception
					return "fail";
				}
			}

		}

		// 작업경과 표시를 위한 호출, 프로그래스 바에 진행 상태 표시
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		// 쓰래드의 핸들러 역할
		// 값 셋팅
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if(dialog != null)   dialog.dismiss(); // 프로그래스 종료

			try {
				// 네트워크 문제시
				if (result.equalsIgnoreCase("net")) {
					Toast.makeText(ctx, getString(R.string.str_network_err),Toast.LENGTH_LONG).show();
				}
				// 업데이트
				else if (result.equalsIgnoreCase("upgrade")) {
					Toast.makeText(ctx, "업데이트",Toast.LENGTH_LONG).show();;
					ApkDownloadTask apkDownloadTask = new ApkDownloadTask();
					apkDownloadTask.execute();
				}
				// 최신버전
				else if (result.equalsIgnoreCase("new")) {
					Toast.makeText(ctx, getString(R.string.str_version_new_msg),Toast.LENGTH_LONG).show();;
				}else{
					Toast.makeText(ctx, getString(R.string.str_version_fail_msg),Toast.LENGTH_LONG).show();
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub

			super.onCancelled();

			if(dialog != null)   dialog.dismiss(); // 프로그래스 종료
		}
	}
	// 버전체크 AsyncTask
	private class ApkDownloadTask extends AsyncTask<String, Integer, String>{

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(Main.this);
			dialog.setMessage(getString(R.string.str_apk_msg));
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);
						}
					});
			dialog.show();

		}

		@Override
		protected String doInBackground(String... str) {
			// TODO Auto-generated method stub

			// 네트워크 연결이 안되었을 때
			if (!FunctionClass.getNetworkState(Main.this)) {

				return "net";

			}else{
				// 수정
				try {
					try {

						finish_chk = false;
						// 파일 다운로드 (이게 정식)
						String result = FileDownloadClass.APKfileDownload(getString(R.string.str_apk_home));
						return result;

					} catch (Exception e) {
						return "fail";
					}

				} catch (Exception e) {
					// TODO: handle exception
					return "fail";
				}
			}

		}

		// 쓰래드의 핸들러 역할
		// 값 셋팅
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if(dialog != null)   dialog.dismiss(); // 프로그래스 종료

			try {
				// 네트워크 문제시
				if (result.equalsIgnoreCase("net")) {
					Toast.makeText(ctx, getString(R.string.str_network_err),Toast.LENGTH_LONG).show();
				}
				// 다운로드 성공
				else if (result.equalsIgnoreCase("Finish")) {
					new AlertDialog.Builder(ctx)
							.setMessage(getString(R.string.str_down_success_msg))
							.setPositiveButton("확인",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,	int which) {

											File apkFile = new File("/mnt/sdcard/MobileAMS/ams.apk");
											Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
											webLinkIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
											startActivity(webLinkIntent);
											finish();
										}
									}).setCancelable(false).show();
				}
				// 다운로드 취소
				else if (result.equalsIgnoreCase("Stop")) {
					FileDownloadClass.deleteFile("/mnt/sdcard/MobileAMS/ams.apk");
				}
				// 다운로드 실패
				else{
					new AlertDialog.Builder(ctx)
							.setMessage(result)
							.setPositiveButton("확인",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,	int which) {
										}
									}).setCancelable(false).show();
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub

			super.onCancelled();

			if(dialog != null)   dialog.dismiss(); // 프로그래스 종료
		}
	}
}