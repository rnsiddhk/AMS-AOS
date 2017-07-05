package com.cashnet.menu;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CommonListItem;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 기기 선택
 * see total_machine_list_view.xml
 * @since 2016-08-25
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class TotalMachineListActivity extends BaseActivtiy {

	// 상단 타이틀바 객체
	private View top_view;

	private ListView lv_machine;

	// 리스트뷰 사용할 어댑터 객체
	private ArrayAdapter<String> adapter;

	// 데이터 리스트
	private ArrayList<String> search_AL;

	private ArrayList<BasicVO> data_AL = new ArrayList<BasicVO>();

	private Context ctx;

	// android-support-v4 라이브러리에서 제공하느, Pull to Refresh 기능
	private SwipeRefreshLayout swipeRefreshLayout;

	// 통신 결과 JSON 객체
	private JSONObject jsonRes;

	// 리스트 상단 구분 플래그
	private boolean top = true;

	// 리스트뷰 footer
	private View footer;

	// 페이징 변수
	private int PAGE_NUMBER = 25;

	// 시작 row와 끝 row
	private int PAGE_START = 1, PAGE_END = 25;

	// 페이지 처리 할 플래그
	private boolean page = false;

	private Intent intent;

	// 조회 Task
	private SearchTask searchTask;

	private SGPreference user_info;

	private String str_jisa  = "", str_ch = "";

	// 자동조회 플래그
	private static boolean flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.total_machine_list_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();

		eventClick();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!flag) {
			eventClick();
		}else{
			flag = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		flag = true;
	}

	// 뷰 객체 생성
	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("기기 선택");

		// 지사코드, 채널코드 입력 받기
		str_jisa = getIntent().getExtras().getString("BRANCH_GB","");
		str_ch = getIntent().getExtras().getString("TEAM_GB","");

		search_AL = new ArrayList<String>();

		// 어댑터 생성
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, search_AL);

		// 리스트뷰 생성 및 이벤트 등록
		lv_machine = (ListView) findViewById(R.id.lv_machine);
		lv_machine.setAdapter(adapter);
		lv_machine.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
									long arg3) {
				// TODO Auto-generated method stub

				intent = new Intent(ctx, TotalMachineActivity.class);
				intent.putExtra("ORG_CD", data_AL.get(position).getDATA01());		// 기기번호
				startActivity(intent);

			}
		});

		// pull to refresh 객체 생성 및 이벤트 구현
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {

				eventClick();

				// 조회 기능 추가 예정
				swipeRefreshLayout.setRefreshing(false);
			}
		});

		footer = getLayoutInflater().inflate(R.layout.footer_view, null, false);
	}

	// 자동 조회 하기 위한 메소드
	private void eventClick(){

		Log.e("eventClick?", "footer 삭제");
		top = true;
		PAGE_START =  1;
		PAGE_END =  25;

		if(lv_machine.getFooterViewsCount() == 1){
			Log.e("btn_seacrh 이거 삭제 됨?", "footer 삭제");
			lv_machine.removeFooterView(footer);
		}

		searchTask =  new SearchTask();
		searchTask.execute();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	private void setData(JSONObject json){
		JSONArray jsonArray;
		BasicVO dataVO;
		try {

			if (top) {
				if (adapter != null) {
					adapter.clear();
					data_AL.clear();
				}
				Log.e("이거 삭제 됨?", lv_machine.getFooterViewsCount() + "개");
				if(lv_machine.getFooterViewsCount() == 1){
					Log.e("setData 이거 삭제 됨?", "footer 삭제");
					lv_machine.removeFooterView(footer);
				}
			}

			jsonArray = json.getJSONObject("selectAtm").getJSONArray("list");

			for (int i = 0; i < jsonArray.length(); i++ ) {

				search_AL.add(jsonArray.getJSONObject(i).getString("ATM_NM"));

				dataVO = new BasicVO();
				dataVO.setDATA01(jsonArray.getJSONObject(i).getString("ORG_CD"));		// 기기번호

				data_AL.add(dataVO);
			}

			adapter.notifyDataSetChanged();

			if (PAGE_NUMBER <= jsonArray.length()){

				if (lv_machine.getFooterViewsCount()  == 0) {

					lv_machine.addFooterView(footer);
					top = false;
				}

				footer.findViewById(R.id.btn_footer).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						page = true;

						PAGE_START = PAGE_END + 1;
						PAGE_END = PAGE_END + 25;

						searchTask =  new SearchTask();
						searchTask.execute();
					}
				});

			}else{

				if (!top){
					lv_machine.removeFooterView(footer);
				}

			}
			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String> {

		private JSONObject jsonSend;
		private JSONObject jsonContain;
		private JSONArray jsonArray;
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(TotalMachineListActivity.this);
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
			if (!FunctionClass.getNetworkState(TotalMachineListActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("user_id", user_info.getValue("USER_ID", ""));              // 사용자 ID
					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
					jsonSend.put("branch_gb", str_jisa);    								 // 지사구분
					jsonSend.put("team_gb", str_ch);           							     // 채널 구분
					jsonSend.put("user_ent", user_info.getValue("USER_ENT", ""));            // 업체 코드
					jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));        // 사용자 직책

					jsonSend.put("startRow", PAGE_START);
					jsonSend.put("endRow", PAGE_END);


					Log.e("기기 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "selectAtm");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("기기 조회 결과 >>" , jsonRes.toString());

					if (jsonRes == null || jsonRes.getJSONObject("selectAtm").getJSONArray("list").length() <= 0) {
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

				if (page){
					if(lv_machine.getFooterViewsCount() == 1){
						Log.e("결과 정리 이거 삭제 됨?", "footer 삭제");
						lv_machine.removeFooterView(footer);
					}
				}else{
					if (adapter != null){
						adapter.clear();
						data_AL.clear();
						adapter.notifyDataSetChanged();
					}
				}

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
