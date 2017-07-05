package com.cashnet.menu;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CustomGallery;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 파출수납 상세(등록 기능)
 * see collect_money_detail_view.xml
 * @since 2016-08-25
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class CollectMoneyDetailActivity extends BaseActivtiy implements OnClickListener{

	// 상단 타이틀바 뷰 객체
	private View top_view;

	// 입력 금액 결과 값, 입력 금액
	private String result = "", tot_amt = "";

	// 전달된 데이터 VO 객체
	private BasicVO dataVO;

	private Context ctx;

	private SGPreference user_info;

	// JSON 결과 객체
	private JSONObject jsonRes;

	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.collect_money_detail_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();
	}

	// 뷰 객체 생성
	private void initView(){

		// 상단 타이틀바 작성
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("파출수납 등록");

		dataVO = getIntent().getExtras().getParcelable("DATA");

		Log.e("DataVO >>", dataVO.getDATA01());
		Log.e("DataVO >>", dataVO.getDATA02());
		Log.e("DataVO >>", dataVO.getDATA03());
		Log.e("DataVO >>", dataVO.getDATA04());
		Log.e("DataVO >>", dataVO.getDATA05());


		// 업체명
		((TextView) findViewById(R.id.txt_nm)).setText(dataVO.getDATA02());

		// 기준일
		((TextView) findViewById(R.id.txt_date)).setText(dataVO.getDATA03());

		// 수납예정금액
		((TextView) findViewById(R.id.txt_est_mn)).setText(makeStringComma(dataVO.getDATA04()));

		// 입금예정금액
		((TextView) findViewById(R.id.edt_est_mn)).setText(makeStringComma(dataVO.getDATA05()));

		// 이벤트 등록
		// 동일
		((Button) findViewById(R.id.btn_same)).setOnClickListener(this);

		// 저장
		((Button) findViewById(R.id.btn_save)).setOnClickListener(this);

		// 취소
//		((Button) findViewById(R.id.btn_image)).setOnClickListener(this);
//
//		((Button) findViewById(R.id.btn_image2)).setOnClickListener(this);

		// 입급예정금액
		// 숫자 입력시 콤마(,) 표시
		((EditText) findViewById(R.id.edt_est_mn)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

				if (!s.toString().equals(result)) {
					result = makeStringComma(s.toString().replaceAll(",", "")); // 에딧텍스트의 값을 변환하여, result에 저장.
					((EditText) findViewById(R.id.edt_est_mn)).setText(result); 		  // 결과 텍스트 셋팅.
					((EditText) findViewById(R.id.edt_est_mn)).setSelection(result.length()); // 커서를 끝으로 보냄
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

			@Override
			public void afterTextChanged(Editable arg0) {}
		});
	}

	private String makeStringComma(String str) {
		if (str.length() == 0)
			return "";
		long value = Long.parseLong(str);
		DecimalFormat format = new DecimalFormat("###,###");
		return format.format(value);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()) {

			// 동일
			case R.id.btn_same:
				((EditText) findViewById(R.id.edt_est_mn)).setText(((TextView) findViewById(R.id.txt_est_mn)).getText().toString());
				break;

			// 저장
			case R.id.btn_save:
				if (!FunctionClass.isNullOrBlank(((EditText) findViewById(R.id.edt_est_mn)).getText().toString().trim())){
					tot_amt = ((EditText) findViewById(R.id.edt_est_mn)).getText().toString().trim().replace(",","");

					InsertDataTask insertDataTask = new InsertDataTask();
					insertDataTask.execute();
				}else{
					Toast.makeText(ctx, getString(R.string.str_input_msg), Toast.LENGTH_SHORT).show();
				}

				break;

			// 취소
//			case R.id.btn_image:
//				intent = new Intent(ctx, CustomGalleryActivity.class);
//				intent.putExtra("action", "M");
//				startActivityForResult(intent, 1);
//				break;
//
//			case R.id.btn_image2:
//				intent = new Intent(ctx, CustomGalleryActivity.class);
//				intent.putExtra("action", "S");
//				startActivityForResult(intent, 2);
//				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

			String[] all_path = data.getStringArrayExtra("all_path");

			ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				Log.e("이미지 path >> ", string);
				dataT.add(item);
			}
		}else if (requestCode == 2 && resultCode == Activity.RESULT_OK){

			String single_path = data.getStringExtra("single_path");
			Log.e("이미지 path >> ", single_path);
		}
	}

	// 데이터 결과 정리 메소드
	private void setData(){

		try {

			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setMessage(getString(R.string.str_success_collect_msg));
			ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();


		}catch (Exception e){

		}
	}

	// 파출수납 등록 Task
	private class InsertDataTask extends AsyncTask<String, Integer, String> {

		private JSONObject jsonSend;
		private JSONObject jsonContain;
		private JSONArray jsonArray;
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(CollectMoneyDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(CollectMoneyDetailActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("tot_amt", tot_amt);    						// 입금예정금액
					jsonSend.put("upt_amt", user_info.getValue("USER_ID", ""));	// 수납등록자
					jsonSend.put("gijun_il", dataVO.getDATA03());				// 기준일
					jsonSend.put("org_cd", dataVO.getDATA01());					// 기기번호


					Log.e("파출수납 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "saveGoods");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("파출수납 결과 >>" , jsonRes.toString());

					// 등록성공 코드 0
					if (jsonRes == null || !jsonRes.getJSONObject("saveGoods").getString("code").equalsIgnoreCase("0")) {
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
				setData();
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
