package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.main.BaseActivtiy;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 시재정보 화면
 *
 * see currency_info_view.xml
 * @since 2016-08-22
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class CurrencyInfoActivity extends BaseActivtiy{

	private View top_view;

	private JSONObject jsonRes;

	private Context ctx;

	private String org_cd = "";

	private int[] gradeId = new int[] { R.drawable.icon_grade_0, R.drawable.icon_grade_1, R.drawable.icon_grade_2, R.drawable.icon_grade_3, R.drawable.icon_grade_4,
			R.drawable.icon_grade_5, R.drawable.icon_grade_6, R.drawable.icon_grade_7, R.drawable.icon_grade_8, R.drawable.icon_grade_9,
			R.drawable.icon_grade_10, R.drawable.icon_grade_11 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.currency_info_view);

		super.onCreate(savedInstanceState);

		initView();

		ctx = this;

		org_cd = getIntent().getExtras().getString("ORG_CD", "");

	}

	@Override
	protected void onResume() {
		super.onResume();

		SearchTask searchTask = new SearchTask();
		searchTask.execute();
	}

	// 뷰 객체 생성 및 셋팅
	private void initView(){

		// 상단 타이틀바 셋팅
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("시재정보");

		// 기기명, 기기번호, 현재잔액, 현송주기, 최근현송일, 전주출금액, 일평균, 현송계획
		((TextView) findViewById(R.id.txt_name)).setText("");
		((TextView) findViewById(R.id.txt_sn)).setText("");
		((TextView) findViewById(R.id.txt_money)).setText("");
		((TextView) findViewById(R.id.txt_cycle)).setText("");
		((TextView) findViewById(R.id.txt_recent_time)).setText("");
		((TextView) findViewById(R.id.txt_withdraw)).setText("");
		((TextView) findViewById(R.id.txt_avg)).setText("");
		((TextView) findViewById(R.id.txt_plan)).setText("");

	}

	// 조회 결과 셋팅
	private void setData(JSONObject json){

		int grade = 0;

		try {

			// 기기명, 기기번호, 현재잔액, 현송주기, 최근현송일, 전주출금액, 일평균, 현송계획
			((TextView) findViewById(R.id.txt_name)).setText(json.getJSONObject("goods").getString("ATM_NM"));
			((TextView) findViewById(R.id.txt_sn)).setText(json.getJSONObject("goods").getString("ORG_CD"));
			((TextView) findViewById(R.id.txt_money)).setText(json.getJSONObject("goods").getString("REMAIN_AMT").trim() + " (단위:만원)");
			((TextView) findViewById(R.id.txt_cycle)).setText(FunctionClass.isNullOrBlankReturn(json.getJSONObject("goods").getString("SIJE_PERIOD")));
			((TextView) findViewById(R.id.txt_recent_time)).setText(FunctionClass.isNullOrBlankReturn(json.getJSONObject("goods").getString("LAST_SIJE_DATE")));
			((TextView) findViewById(R.id.txt_withdraw)).setText(json.getJSONObject("goods").getString("PREV_AMT").trim() + " (단위:만원)");
			((TextView) findViewById(R.id.txt_avg)).setText(json.getJSONObject("goods").getString("DAY_AVR_AMT").trim() + " (단위:만원)");
			((TextView) findViewById(R.id.txt_plan)).setText(json.getJSONObject("goods").getString("SIJE_PLAN"));

			// 기기등급 이미지 치환
			grade = Integer.parseInt(json.getJSONObject("goods").getString("ATM_GRADE"));
			((TextView) findViewById(R.id.txt_grade)).setBackgroundResource(gradeId[grade]);

		}catch (Exception e){

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

			dialog = new ProgressDialog(CurrencyInfoActivity.this);
			dialog.setMessage(getString(R.string.str_loading_msg));
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}

		@Override
		protected String doInBackground(String... result) {
			// TODO Auto-generated method stub

			// 네트워크 연결이 안되었을 때
			if (!FunctionClass.getNetworkState(CurrencyInfoActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("org_cd", org_cd);             // 기기번호

					Log.e("시재 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "goods");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("시재 조회 결과 >>" , jsonRes.toString());

					if (jsonRes == null) {
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
}