package com.cashnet.menu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CommonListItem;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.common.SimpleAdapter;
import com.cashnet.main.BaseActivtiy;

/**
 * 장애 유형별 건수
 * see total_issue_status_view.xml
 * @since 2016-08-25
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class TotalIssueStatusActivity extends BaseActivtiy implements OnClickListener{

	private View top_view;

	// 기준일자
	private TextView txt_date;

	// 날짜 문자열 변수 월, 일
	private String sMonth = "", sDay = "";

	// 기준일
	private String str_date = "";

	private JSONObject jsonRes;

	private SimpleAdapter adapter;

	private SearchTask searchTask;

	private Calendar calendar;

	// 일 상태 값 0 : 전일마감, 1 : 전월동일, 2 : 전월말일, 3 : 전년동일
	private String type = "0";

	private Context ctx;

	private SGPreference user_info;

	private String BRANCH_GB = "", TEAM_GB = "";

	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.total_issue_status_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();

		clickEvent();
	}

	// 뷰 객체 생성
	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("장애 유형별 건수");

		// 지사구분
		BRANCH_GB = getIntent().getExtras().getString("BRANCH_GB", "");

		// 채널 구분
		TEAM_GB = getIntent().getExtras().getString("TEAM_GB", "");

		// 전일마감
		((Button) findViewById(R.id.btn_01)).setOnClickListener(this);

		// 전월동일
		((Button) findViewById(R.id.btn_02)).setOnClickListener(this);

		// 전월말일
		((Button) findViewById(R.id.btn_03)).setOnClickListener(this);

		// 전년동일
		((Button) findViewById(R.id.btn_04)).setOnClickListener(this);

		// 기준일
		txt_date = (TextView) findViewById(R.id.txt_date);

		calendar = Calendar.getInstance();

		// 기준일자
		txt_date = (TextView) findViewById(R.id.txt_date);
		txt_date.setOnClickListener(this);
		sMonth =  (calendar.get(Calendar.MONTH) + 1) < 10 ?  "0" +  (calendar.get(Calendar.MONTH) + 1):  (calendar.get(Calendar.MONTH) + 1)+ "";
		sDay = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?  "0" + (calendar.get(Calendar.DAY_OF_MONTH)) : (calendar.get(Calendar.DAY_OF_MONTH)) + "";
		txt_date.setText(calendar.get(Calendar.YEAR) + sMonth + sDay);
		str_date = txt_date.getText().toString().trim();

		adapter = new SimpleAdapter(this, R.layout.simple_list_item);

		((ListView) findViewById(R.id.lv_issue)).setAdapter(adapter);

		// pull to refresh 객체 생성 및 이벤트 구현
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {

				clickEvent();

				swipeRefreshLayout.setRefreshing(false);

			}
		});

	}

	// 조회 클릭 이벤트
	private void clickEvent(){

		searchTask = new SearchTask();
		searchTask.execute();
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

			// 기준일
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


	// 날짜 선택 DatePicker Dialog
	private void showDialog(){

		DatePickerDialog pickerDialog = new DatePickerDialog(TotalIssueStatusActivity.this,  new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				sMonth = (monthOfYear+1) < 10 ?  "0" + (monthOfYear+1) : (monthOfYear+1) + "";

				sDay = dayOfMonth < 10 ?  "0" + (dayOfMonth) : (dayOfMonth) + "";

				txt_date.setText(year + sMonth + sDay);
				str_date = year + sMonth + sDay;

			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), (calendar.get(Calendar.DAY_OF_MONTH)));

		pickerDialog.show();
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		JSONArray jsonArray;
		try {

			jsonArray =  json.getJSONObject("statOb").getJSONArray("list");

			// 리스트 아이템 초기화
			if (adapter.mItems != null){
				adapter.mItems.clear();
			}

			for (int i = 0; i < jsonArray.length(); i++){
				adapter.mItems.add(new CommonListItem(jsonArray.getJSONObject(i).getString("DOWN_ATM_NM"),
						jsonArray.getJSONObject(i).getString("FAILURE_COUNT")));
			}

			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String>{

		private JSONObject jsonSend;
		private JSONObject jsonContain;
		private JSONArray jsonArray;


		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(TotalIssueStatusActivity.this);
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
			if (!FunctionClass.getNetworkState(TotalIssueStatusActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();
					jsonSend.put("user_id", user_info.getValue("USER_ID", ""));				// 사용자 아이디
					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));		// 사용자 그룹
					jsonSend.put("branch_gb", BRANCH_GB);									// 지사구분
					jsonSend.put("team_gb", TEAM_GB);										// 채널 구분
					jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));		// 사용자 직책
					jsonSend.put("gijun_il", str_date);										// 기준일
					jsonSend.put("type", type);												//  날짜 플래그

					jsonContain = new JSONObject();
					jsonContain.put("method", "statOb");			// API 메소드 명
					jsonContain.put("params", jsonSend);			// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);
					Log.e("장애 유형별 건수 params >>" , jsonSend.toString());

					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("장애 유형별 건수 결과 >>" , jsonRes.toString());

					if (jsonRes == null || jsonRes.getJSONObject("statOb").getJSONArray("list").length() <= 0) {
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