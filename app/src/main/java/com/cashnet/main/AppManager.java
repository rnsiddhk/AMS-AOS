package com.cashnet.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;



public class AppManager extends Application{

	private final String NICK = "kyPakr";
	private final String TAG = AppManager.class.getSimpleName();

	private static AppManager instance;

	public static final String SENDER_ID = "877855794844";  //자신의 Project ID 저런식으로 숫자로 구성됨. 홈피에 이 아이디 알아내는 방법이 있다.
	//public static final String SENDER_ID = "142918045721";  //자신의 Project ID 저런식으로 숫자로 구성됨. 홈피에 이 아이디 알아내는 방법이 있다.
	public static String gcmRegId = "";
	public static String gcmErrorId = "";
	public static boolean needRegister = true;
	public static String version = "";

	// server 정보
	public static final boolean isCommercial = true; // true:상용 false:테스트 구분
	//public static final String AMSIP_T = "218.148.22.232";  // 테스트 서버
	//public static final String AMSIP_T = "218.148.22.237";  // 2014/03/19 임시 테스트 서버
	//public static final String AMSIP_T = "192.168.30.37";  // 2014/05/07 사설 테스트 서버
	//public static final String AMSIP_T = "192.168.10.88";  // 2014/03/19 임시 테스트 서버
	public static final String AMSIP_T = "210.105.193.146";  // 2014/04/14
	//public static final String AMSIP_T = "10.200.1.88";  // 2014/04/14
	public static final String AMSIP_C = "210.105.193.180"; // 상용 서버
	//public static final int AMSPORT = 80; // HTTP(상용)
	public static final int AMSPORT = 443; // HTTP(테스트)
	//public static final int AMSPORT = 8080; // HTTP(테스트)
	public static final int TIMEOUT = 45000;

	// login 정보
	public static String loginid = "";
	public static String jsessionid = "";
	public static String menu = "MAIN";  // 로그인 후 이동할 메뉴 페이지





	public boolean					mSimpleImage			= false;
	public int						mhTmap					= 0;
	public boolean					mbLocationStart			= false;

	public AppManager() {
		instance = this;
	}

	/**
	 * 설치 후 첫 번째 실행 여부 설정
	 * true : 첫 실행    false : 첫 번째 실행 아님
	 */
	public void setFirstExecute(boolean bCheck){
		SharedPreferences prefUserAuth = getSharedPreferences("Excute", Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = prefUserAuth.edit();
		editor.putBoolean("isFirst", bCheck);
		editor.commit();
	}
	/**
	 * 설치 후 첫 번째 실행 여부 반환
	 * @return true : 첫 실행    false : 첫 번째 실행 아님
	 */
	public boolean isFirstExecute()
	{
		boolean bCheck = false;
		SharedPreferences prefUserAuth = getSharedPreferences("Excute", Context.MODE_WORLD_READABLE);
		bCheck = prefUserAuth.getBoolean("isFirst", true);
		return bCheck;
	}

	@Override
	public void onCreate()
	{
		Log.e("AppManager", " onCreate >> " );

		super.onCreate();
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
	}

	public static String getServerIP() {
		return AppManager.isCommercial?AMSIP_C:AMSIP_T;
	}

	public static String getDeviceID() {
		TelephonyManager telephony = ((TelephonyManager)instance.getSystemService(Context.TELEPHONY_SERVICE));
		Log.e("AppManager",  ">>>>>>>>>>>>> device-id = " + telephony.getDeviceId());
		return telephony.getDeviceId();
	}

	public static String getPhoneNumber() {
		String number = null;
		TelephonyManager tm = (TelephonyManager) instance.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getLine1Number() == null){
			Log.e("AppManager",  "전화 번호 null 이다 ");
		}
		else {
			if (tm.getLine1Number().contains("+82")) {
				number = "0" + tm.getLine1Number().substring(3);
			} else if (tm.getLine1Number().contains("010")) {
				number = tm.getLine1Number();
			}
		}
		return number;
	}
} 