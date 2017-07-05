package com.cashnet.menu;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.LockableScrollView;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.util.ScreenUtil;
import com.cashnet.vo.BasicVO;

/**
 * 기기정보
 * see machine_info_view.xml
 * @since 2016-08-24
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class MachineInfoActivity extends BaseActivtiy implements OnClickListener{

	// 상단 타이틀바 객체
	private View top_view;

	// 기기정보 레이아웃, 장애내역 레이아웃
	private LinearLayout layout_fir, layout_sec;

	// 잠금 스크롤
	private LockableScrollView scroll;

	private JSONObject jsonRes;

	private String[] strTag ={"지사/채널", "기기번호", "기기명", "기기종류", "기기위치", "회선업체",
			"간판업체", "부스업체", "주간장애", "야간장애", "장애메세지", "설치점", "관리번호", "기기메모"};

	private Context ctx;

	// 기기번호
	private String ORG_CD;

	// 메모 객체
	private EditText edt_memo;

	// 메모
	private String atm_memo = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.machine_info_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		ORG_CD = getIntent().getExtras().getString("ORG_CD", "");

		// 뷰 객체 생성
		initView();

		SearchTask searchTask =  new SearchTask();
		searchTask.execute();
	}

	// 뷰 객체 생성 메소드
	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("기기정보");

		// 잠금 스크롤
		scroll = (LockableScrollView) findViewById(R.id.scroll);

		edt_memo = (EditText) findViewById(R.id.edt_memo);
		// 메모작성시 레이아웃 스크롤 잠금기능
		edt_memo.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				if (v.getId() == R.id.edt_memo) {
					scroll.setEnableScrolling(false);
				}else{
					scroll.setEnableScrolling(true);
				}
				return false;
			}
		});


		// OTP
		((Button) findViewById(R.id.btn_otp)).setOnClickListener(this);

		// 메모수정
		((Button) findViewById(R.id.btn_edit)).setOnClickListener(this);

		// 메모 저장
		((Button) findViewById(R.id.btn_save)).setOnClickListener(this);

		// 취소
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);


	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()) {

			case R.id.btn_otp:
				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				break;

			case R.id.btn_edit:
				((LinearLayout) findViewById(R.id.ll_memo)).setVisibility(View.VISIBLE);
				break;

			case R.id.btn_save:

				// 특이사항 입력 공백 값 체크
				if (!FunctionClass.isNullOrBlank(edt_memo.getText().toString().trim())) {
					atm_memo = edt_memo.getText().toString().trim();
					InputDataTask inputDataTask = new InputDataTask();
					inputDataTask.execute();
				} else {
					Toast.makeText(ctx, getString(R.string.str_input_memo_msg), Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.btn_cancel:
				finish();
				break;
		}
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		JSONArray isuueArr;
		JSONObject info;
		String[] str_info = new String[14];
		try {

			info = json.getJSONObject("atms").getJSONObject("info");

			// 기기 정보 셋팅
			str_info[0] = info.getString("BRANCH_GB_NM");				// 지사/채널
			str_info[1] = info.getString("ORG_CD");						// 기기번호
			str_info[2] = info.getString("ATM_NM");						// 기기명
			str_info[3] = info.getString("ATM_KD_NM");					// 기기종류
			str_info[4] = info.getString("ADDR_RMK");					// 기기위치
			str_info[5] = info.getString("LINE_ENT_NM");				// 회선업체
			str_info[6] = FunctionClass.isNullOrBlankReturn(info.getString("SIGN_ENT_NM"));				// 간판업체
			str_info[7] = info.getString("BOOTH_MAKER_NM");				// 부스업체
			str_info[8] = info.getString("DAY_ADMIN_NM");				// 주간장애
			str_info[9] = info.getString("NIGHT_ADMIN_NM");				// 야간장애
			str_info[10] = info.getString("DOWN_CD1_NM") + "(" + info.getString("DOWN_CD1") + ")"; // 장애메세지
			str_info[11] = info.getString("OPTEL_NO");					// 설치점
			str_info[12] = FunctionClass.isNullOrBlankReturn(info.getString("RCD_NO2"));					// 관리번호
			str_info[13] = FunctionClass.isNullOrBlankReturn(info.getString("ATM_MEMO")); // 기기메모

			// 메모가 있는 경우 메모내용 셋팅
			if (!FunctionClass.isNullOrBlank(info.getString("ATM_MEMO"))) {
				edt_memo.setText(info.getString("ATM_MEMO"));
				((LinearLayout) findViewById(R.id.ll_memo)).setVisibility(View.VISIBLE);
			}

			// 기기 정보 레이아웃
			layout_fir = (LinearLayout) findViewById(R.id.ll_list);

			// 장애내역 레이아웃
			layout_sec = (LinearLayout) findViewById(R.id.ll_issue_list);

			int px = (int)ScreenUtil.convertDpToPixel(30f, this);

			// 기기정보 관련 객체 동적으로 생성하기
			for (int i = 0; i < str_info.length; i++) {

				LinearLayout ll_contain = new LinearLayout(this);
				LinearLayout.LayoutParams params =
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px*2, 0);

				params.setMargins(5, 15, 5, 5);

				TextView txt_1 = new TextView(this);
				txt_1.setText(strTag[i]);
				txt_1.setGravity(Gravity.CENTER);
				txt_1.setTextSize(18f);
				txt_1.setBackgroundResource(R.drawable.gradient_gray_linear);

				TextView txt_2 = new TextView(this);
				txt_2.setText(str_info[i]);
				txt_2.setTextSize(18f);
				txt_2.setGravity(Gravity.LEFT);

				LinearLayout.LayoutParams txt_1_params =
						new LinearLayout.LayoutParams(0, px, 1);
				txt_1_params.setMargins(5, 0, 0, 0);

				LinearLayout.LayoutParams txt_2_params =
						new LinearLayout.LayoutParams(0, px*2, 3);
				txt_2_params.setMargins(10, 0, 0, 0);


				ll_contain.addView(txt_1, txt_1_params);
				ll_contain.addView(txt_2, txt_2_params);
				layout_fir.addView(ll_contain, params);
			}

			// 장애내역 관련 객체 동적으로 생성하기
			isuueArr = json.getJSONObject("atms").getJSONArray("list");
			for (int i = 0; i < isuueArr.length(); i++) {


				TextView txtView = new TextView(this);
//				txtView.setText("2016/08/24 08:27:49 지폐입금부 이상(지폐 잔류 포함)");
				txtView.setText(isuueArr.getJSONObject(i).getString("DOWN_TIME") + " "
				+ isuueArr.getJSONObject(i).getString("DOWN_ATM_NM") + " "
				+ FunctionClass.isNullOrBlankReturn(isuueArr.getJSONObject(i).getString("DOWN_REG_NM")));
				txtView.setGravity(Gravity.LEFT);
				txtView.setTextSize(14f);

				LinearLayout.LayoutParams txt_1_params =
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px, 0);
				txt_1_params.setMargins(15, 0, 0, 0);

				layout_sec.addView(txtView, txt_1_params);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(MachineInfoActivity.this);
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
			if (!FunctionClass.getNetworkState(MachineInfoActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("org_cd", ORG_CD);         // 기기번호


					Log.e("기기정보 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "atms");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("기기정보 조회 결과 >>" , jsonRes.toString());

					if (jsonRes == null || 0 > jsonRes.length()) {
						return "null";

					}else {

						return "success";

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

				Toast.makeText(ctx, getString(R.string.str_search_fail),Toast.LENGTH_LONG).show();

			} else {

				setData(jsonRes);
				// 성공시 데이터 결과 셋팅 및 처리
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if(dialog != null) dialog.dismiss();
		}
	}


	// 메모 입력 결과 처리 메소드
	private void setResultView(){

		try {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setMessage("메모가 등록 되었습니다.");
			ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 메모 입력 Task
	private class InputDataTask extends AsyncTask<String, Integer, String> {

		private JSONObject jsonSend;

		private JSONArray jsonArray;

		private JSONObject jsonContain;

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(MachineInfoActivity.this);
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
			if (!FunctionClass.getNetworkState(MachineInfoActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("org_cd", ORG_CD);      		  	// 기기번호
					jsonSend.put("atm_memo", atm_memo);      		// 메모

					Log.e("메모 입력 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "modifyMemo");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("메모 입력 결과 >>" , jsonRes.toString());

					if (jsonRes.getJSONObject("modifyMemo").getString("code").equalsIgnoreCase("0")) {
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

				Toast.makeText(ctx, getString(R.string.str_input_fail),Toast.LENGTH_LONG).show();

			} else {

				// 성공시 데이터 결과 셋팅 및 처리
				setResultView();
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if(dialog != null) dialog.dismiss();
		}
	}
}
