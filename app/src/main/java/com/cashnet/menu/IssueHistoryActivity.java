package com.cashnet.menu;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.main.BaseActivtiy;

/**
 * 장애이력
 * see issue_history_view.xml
 * @since 2016-08-22
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */

public class IssueHistoryActivity extends BaseActivtiy{

	// 상단 타이틀바 객체
	private View top_view;

	// 기본 리스트뷰 어댑터
	private ArrayAdapter<String> adapter;

	// 조회 결과 담을 JSON 객체
	private JSONObject jsonRes;

	private ArrayList<String> search_AL = new ArrayList<String>();

	// android-support-v4 라이브러리에서 제공하느, Pull to Refresh 기능
	private SwipeRefreshLayout swipeRefreshLayout;

	private Context ctx;

	private String ORG_CD = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.issue_history_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		ORG_CD = getIntent().getExtras().getString("ORG_CD", "");

		initView();

		SearchTask searchTask = new SearchTask();
		searchTask.execute();

	}

	private void initView(){

		// 상단 타이틀바 작성
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("장애이력");

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, search_AL);
		((ListView) findViewById(R.id.lv_history)).setAdapter(adapter);

		// pull to refresh 객체 생성 및 이벤트 구현
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {

				// 조회 기능 추가 예정
				swipeRefreshLayout.setRefreshing(false);
			}
		});

		ctx = this;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		JSONArray jsonArray;

		try {

			if (adapter != null){
				adapter.clear();
				search_AL.clear();
			}

			jsonArray = json.getJSONObject("obHistory").getJSONArray("list");

			for (int i = 0; i < jsonArray.length(); i++){
				search_AL.add(jsonArray.getJSONObject(i).getString("DOWN_TIME") + " " +
						jsonArray.getJSONObject(i).getString("DOWN_ATM_NM"));
			}

			((TextView) findViewById(R.id.txt_count)).setText("총 건수 : " + jsonArray.length());
			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String>{

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(IssueHistoryActivity.this);
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
			if (!FunctionClass.getNetworkState(IssueHistoryActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();


					jsonSend.put("org_cd", ORG_CD);         // 기기번호


					Log.e("상세 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "obHistory");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("긴급 조회 결과 >>" , jsonRes.toString());

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

			} else {	// 성공시 데이터 결과 셋팅 및 처리
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
