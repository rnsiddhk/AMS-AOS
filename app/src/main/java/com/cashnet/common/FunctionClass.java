package com.cashnet.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class FunctionClass {

	// 총 파일크기.(바이트 기준)
	public static int totalSize;

	// 다운로드 받은 파일 크기.(바이트 기준)
	public static int downloadedSize = 0;

	public static Bitmap getBitmap(View v) {

		v.setDrawingCacheEnabled(true);
		// measure의 width와 height를 임시 보관합니다.
		int tempMeasuredWidth = v.getMeasuredWidth();
		int tempMeasuredHeight = v.getMeasuredHeight();
		v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

		// drawingcache enable
		v.buildDrawingCache(true);
		// 해당 레이아웃을 비트맵으로 생성 합니다.
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Bitmap.Config.RGB_565);

		// drawingcache disable
		v.setDrawingCacheEnabled(false); // clear drawing cache
		v.destroyDrawingCache();
		// view에 전환한 bitmap이미지를 그려줍니다. 해당 작업을 하지 않을 시 생성한 이미지뷰에 이미지가 들어가지 않습니다.
		Canvas c = new Canvas(b);
		v.draw(c);

		// 보관해두었던 measure의 설정값으로 measure를 초기 설정으로 돌려줍니다. 해당 작업을 해주지 않으면 스크롤이
		// 정상적으로 동작하지 않습니다.
		v.measure(MeasureSpec.makeMeasureSpec(tempMeasuredWidth,
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				tempMeasuredHeight, MeasureSpec.EXACTLY));
		v.layout(0, 0, tempMeasuredWidth, tempMeasuredHeight);

		return b;
	}

	/**
	 * 스트링 널 여부
	 */
	public static boolean isNull(String str) {

		return str == null ? true : false;

	}

	/**
	 * 스트링 널 여부
	 *
	 * @param param
	 * @return true-null, false-null 아님
	 */
	public static boolean isNullOrBlank(String param) {

		if (isNull(param) || param.trim().length() == 0 || param.equals("null")) {

			return true;

		}

		return false;
	}

	/**
	 * 스트링 널 공백으로 치환함수
	 *
	 * @param param
	 * @return true-null, false-null 아님
	 */
	public static String isNullOrBlankReturn(String param) {

		if (isNull(param) || param.trim().length() == 0 || "null".equals(param)) {

			return "";

		}

		return param;
	}

	/**
	 * 소프트웨어 키보드를 1초 후에 숨김
	 *
	 * @param act
	 *            해당 액티비티
	 */
	public static void doHideKeyBoard(final Activity act) {
		try {
			InputMethodManager imm = (InputMethodManager) act
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(),
					0);
		} catch (Exception e) {
		}
	}

	public static class EditKeyboardActionListener implements
			OnEditorActionListener {

		EditText curEdt; // 현재 EditText

		EditText nexEdt; // 다음 EditText

		int curEditorInfo; // EditorInfo 정보 (EditorInfo.IME_ACTION_NEXT 등)
		int nexEditorInfo; // 옮겨갈 EditText의 EditorInfo 정보
		// (EditorInfo.IME_ACTION_NEXT 등)

		Context ctx; // 현재 Context (Activity)

		public EditKeyboardActionListener(EditText curEdt, int curEditorInfo,
										  Context ctx) {

			this.ctx = ctx;
			this.curEdt = curEdt;
			this.curEditorInfo = curEditorInfo;
			this.curEdt.setImeOptions(curEditorInfo);
			this.curEdt.setFilters(new InputFilter[] {});

		}

		public EditKeyboardActionListener(EditText curEdt, EditText nexEdt,
										  int curEditorInfo, Context ctx) {

			this.ctx = ctx;
			this.curEdt = curEdt;
			this.nexEdt = nexEdt;
			this.curEditorInfo = curEditorInfo;
			this.curEdt.setImeOptions(curEditorInfo);
			this.curEdt.setFilters(new InputFilter[] {});

		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

			if (actionId == EditorInfo.IME_ACTION_SEARCH) {

//				if (ctx.getClass().equals(ZipcodeSearchNewPopupView.class)) {
//					ZipcodeSearchNewPopupView.class.cast(ctx).searchAddress();
//				}
//				if (ctx.getClass().equals(ZipcodeSearchOldPopupView.class)) {
//					ZipcodeSearchOldPopupView.class.cast(ctx).searchAddress();
//				}

				return true;
			}

			if (actionId == EditorInfo.IME_ACTION_NEXT) {
				nexEdt.requestFocus();
				return true;
			}

			return false;
		}
	}

	/**
	 * 서명한 이미지 스트링 변환 메소드
	 *
	 *            bitmap
	 * */
	public static String saveSignView(Bitmap bm) {

		String strSign = "";

		try {

			if (bm != null) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				try {

					bm.compress(Bitmap.CompressFormat.JPEG, 40, outStream);

					byte[] bArray = outStream.toByteArray();

					strSign = Base64.encodeToString(bArray, Base64.DEFAULT);

					// Log.e("서명값", strSign);

				} catch (Exception e) {
					// TODO: handle exception
					Log.e("서명값", e.toString());
				}
			}

		} catch (Exception e) {
			// TODO: handle exception

			Log.e("서명값", e.toString());
		}

		return strSign;
	}

	/**
	 * 네트워크 상태 체크
	 *
	 * @return true - 네트워크 연결됨, false - 네트워크 연결안됨
	 */
	public static boolean getNetworkState(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isMobile =  cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		boolean isWifi =  cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

		if (!isMobile && !isWifi) {
			return false;
		}
		return true;
	}

	/**
	 * 스크린샷 메소드
	 *
	 * */
	public static void screenshot(Bitmap bm, String name) {

		try {
			File path = new File("/sdcard/CaptureTest");

			if (!path.isDirectory()) {
				path.mkdirs();
			}

			FileOutputStream out = new FileOutputStream("/sdcard/CaptureTest/"
					+ name + ".jpg");
			bm.compress(Bitmap.CompressFormat.JPEG, 25, out);

			out.close();

		} catch (FileNotFoundException e) {
			// TODO: handle exception
			Log.d("FileNotFoundException:", e.getMessage());

		} catch (IOException e) {
			// TODO: handle exception

		} catch (Exception e) {

		}
	}

	/**
	 *  숫자에 화폐단위 처럼 콤마를 넣어준다.
	 * */
	public static String getFormatDEC(String number)
	{
		DecimalFormat dec = new DecimalFormat("##,###,###");
		if(!number.trim().equals(""))
		{
			number = dec.format(Long.valueOf(number));
		}
		return number;

	}
}