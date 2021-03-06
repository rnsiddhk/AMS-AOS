package com.cashnet.menu;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.main.BaseActivtiy;

import java.util.Calendar;

/**
 * 기기별 종합 집계
 * see machine_info_view.xml
 * @since 2016-08-24
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */

public class TotalMachineActivity extends BaseActivtiy implements OnClickListener{

	// 상단 타이틀바 객체
	private View top_view;

	// 기준일자
	private TextView txt_date;

	// 날짜 문자열 변수 월, 일
	private String sMonth = "", sDay = "";

	// 기준일
	private String str_date = "";

	private Calendar calendar;

	private JSONObject jsonRes;

	private Context ctx;

	private String type = "0";

	// 기기번호
	private String ORG_CD = "";

	private SearchTask searchTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.total_machine_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		initView();

		clickEvent();
	}

	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("기기별 종합 집계");

		// 전일마감
		((Button) findViewById(R.id.btn_01)).setOnClickListener(this);

		// 전월동일
		((Button) findViewById(R.id.btn_02)).setOnClickListener(this);

		// 전월말일
		((Button) findViewById(R.id.btn_03)).setOnClickListener(this);

		// 전년동일
		((Button) findViewById(R.id.btn_04)).setOnClickListener(this);

		// 기준일자
		txt_date = (TextView) findViewById(R.id.txt_date);
		txt_date.setOnClickListener(this);

		calendar = Calendar.getInstance();

		// 기준일자
		txt_date = (TextView) findViewById(R.id.txt_date);
		txt_date.setOnClickListener(this);
		sMonth =  (calendar.get(Calendar.MONTH) + 1) < 10 ?  "0" +  (calendar.get(Calendar.MONTH) + 1):  (calendar.get(Calendar.MONTH) + 1)+ "";
		sDay = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?  "0" + (calendar.get(Calendar.DAY_OF_MONTH)-1) : (calendar.get(Calendar.DAY_OF_MONTH)-1) + "";
		txt_date.setText(calendar.get(Calendar.YEAR) + sMonth + sDay);
		str_date = txt_date.getText().toString().trim();

		// 기기번호
		ORG_CD = getIntent().getExtras().getString("ORG_CD", "");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		super.onClick(v);

		switch (v.getId()) {

			// 전일마감
			case R.id.btn_01:
				type = "0";
				setButton();
				((Button) findViewById(R.id.btn_01)).setTextColor(Color.BLACK);
				((Button) findViewById(R.id.btn_01)).setBackgroundResource(R.color.White);
				clickEvent();
				break;

			// 전월동일
			case R.id.btn_02:
				type = "1";
				setButton();
				((Button) findViewById(R.id.btn_02)).setTextColor(Color.BLACK);
				((Button) findViewById(R.id.btn_02)).setBackgroundResource(R.color.White);
				clickEvent();
				break;

			// 전월말일
			case R.id.btn_03:
				type = "2";
				setButton();
				((Button) findViewById(R.id.btn_03)).setTextColor(Color.BLACK);
				((Button) findViewById(R.id.btn_03)).setBackgroundResource(R.color.White);
				clickEvent();
				break;

			// 전년동일
			case R.id.btn_04:
				type = "3";
				setButton();
				((Button) findViewById(R.id.btn_04)).setTextColor(Color.BLACK);
				((Button) findViewById(R.id.btn_04)).setBackgroundResource(R.color.White);
				clickEvent();
				break;

			// 기준일자
			case R.id.txt_date:
				showDialog();
				break;
		}
	}

	// 버튼 글자색, 바탕색 변경
	private void setButton(){

		((Button) findViewById(R.id.btn_01)).setTextColor(Color.WHITE);
		((Button) findViewById(R.id.btn_01)).setBackgroundResource(R.color.Gray);
		((Button) findViewById(R.id.btn_02)).setTextColor(Color.WHITE);
		((Button) findViewById(R.id.btn_02)).setBackgroundResource(R.color.Gray);
		((Button) findViewById(R.id.btn_03)).setTextColor(Color.WHITE);
		((Button) findViewById(R.id.btn_03)).setBackgroundResource(R.color.Gray);
		((Button) findViewById(R.id.btn_04)).setTextColor(Color.WHITE);
		((Button) findViewById(R.id.btn_04)).setBackgroundResource(R.color.Gray);
	}

	// 조회 클릭 이벤트
	private void clickEvent(){

		searchTask = new SearchTask();
		searchTask.execute();
	}

	// 날짜 선택 DatePicker Dialog
	private void showDialog(){

		DatePickerDialog pickerDialog = new DatePickerDialog(TotalMachineActivity.this,  new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {

				sMonth = (monthOfYear+1) < 10 ?  "0" + (monthOfYear+1) : (monthOfYear+1) + "";

				sDay = dayOfMonth < 10 ?  "0" + (dayOfMonth-1) : (dayOfMonth-1) + "";

				txt_date.setText(year + sMonth + sDay);
				str_date = year + sMonth + sDay;

			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), (calendar.get(Calendar.DAY_OF_MONTH)-1));

		pickerDialog.show();
	}
	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		try {

			// 일 총 수수료 금액
			((TextView) findViewById(R.id.txt_day_mn)).setText(FunctionClass.getFormatDEC(json.getJSONObject("statAtm").getString("DAY_FEE_TOT_AMT")));

			// 월 평균 건수
			((TextView) findViewById(R.id.txt_mon_cnt)).setText(json.getJSONObject("statAtm").getString("AVR_TOT_CNT"));

			// 월 총 수수료 금액
			((TextView) findViewById(R.id.txt_mon_mn)).setText(FunctionClass.getFormatDEC(json.getJSONObject("statAtm").getString("MON_FEE_TOT_AMT")));

			// 월 평균 재고 일수
			((TextView) findViewById(R.id.txt_avg_md)).setText(json.getJSONObject("statAtm").getString("M_S_DAY_CNT") + "일");

			// 연 평균 재고 일수
			((TextView) findViewById(R.id.txt_avg_year)).setText(json.getJSONObject("statAtm").getString("Y_S_DAY_CNT") + "일");

			// 월 평균 과다장입금액
			((TextView) findViewById(R.id.txt_mon_over)).setText(FunctionClass.getFormatDEC(json.getJSONObject("statAtm").getString("M_OVER_SIJE_AMT")) + "원");

			// 연 평균 과다장입금액
			((TextView) findViewById(R.id.txt_year_over)).setText(FunctionClass.getFormatDEC(json.getJSONObject("statAtm").getString("Y_OVER_SIJE_AMT")) + "원");

			// 월 평균 현금부족건수
			((TextView) findViewById(R.id.txt_mon_lower)).setText(json.getJSONObject("statAtm").getString("L_CASH_CNT") + "건");

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String>{

		private JSONObject jsonSend;
		private JSONObject jsonContain;
		private JSONArray jsonArray;

		private String url = "";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(TotalMachineActivity.this);
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
			if (!FunctionClass.getNetworkState(TotalMachineActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();
					jsonSend.put("gijun_il", str_date);				// 기준일
					jsonSend.put("org_cd", ORG_CD);					// 기기번호
					jsonSend.put("type", type);						//  날짜 플래그

					jsonContain = new JSONObject();
					jsonContain.put("method", "statAtm");			// API 메소드 명
					jsonContain.put("params", jsonSend);			// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);
					Log.e("일/월별 종합 집계 params >>" , jsonSend.toString());

					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("일/월별 종합 집계 결과 >>" , jsonRes.toString());

					if (jsonRes == null || jsonRes.getJSONObject("statAtm") == null) {
						return "null";

					}else{
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
