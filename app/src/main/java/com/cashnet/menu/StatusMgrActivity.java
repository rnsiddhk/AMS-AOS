package com.cashnet.menu;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CommonListItem;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.common.SpinnerAdapter;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;


/**
 * 상태관리
 *
 * see status_mgr_view.xml
 * @since 2016-08-23
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class StatusMgrActivity extends BaseActivtiy implements OnClickListener{

	// 스피너: 조회 조건 1, 2, 3
	private Spinner sp_con1, sp_con2, sp_con3;

	// 커스텀 스피너 어댑터
	private SpinnerAdapter adapter_con1, adapter_con2, adapter_con3;

	// 커스텀 어댑터 사용할 문자 배열
	private String[] con_arr1, con_arr2, con_arr3;

	private Button btnSearch;

	// 현재 액티비티의 컨택스트 객체
	private Context ctx;

	// 조회 결과 받을 JSON 객체
	private JSONObject jsonRes ;

	// android-support-v4 라이브러리에서 제공하느, Pull to Refresh 기능
	private SwipeRefreshLayout swipeRefreshLayout;

	// 리스트뷰 객체
	private ListView lv_status;

	// 리스트뷰 커스텀 어댑터
	private SearchAdapter adapter;

	// 상단 타이틀바 객체
	private View top_view;

	// 인텐트 객체
	private Intent intent;

	private SearchTask searchTask;

	// 지사, 채널 조회 테스크
	private JisaSearchTask jisaSearchTask;

	private ChannelSearchTask channelSearchTask;

	private boolean top = true;

	// 리스트뷰 footer
	private View footer;

	private int PAGE_NUMBER = 25;

	private int PAGE_START = 1, PAGE_END = 25;

	private ArrayList<BasicVO> data_AL = new ArrayList<BasicVO>();

	// 지사 리스트
	private ArrayList<BasicVO> jisa_AL = new ArrayList<BasicVO>();

	// 채널 리스트
	private ArrayList<BasicVO> ch_AL = new ArrayList<BasicVO>();

	private SGPreference user_info;

	// 지사구분, 채널구분, 상태구분
	private String str_jisa = "", str_ch = "", str_stat = "";

	// 자동조회 플래그
	private static boolean flag = true;

	private boolean page = false;

	private String keyword = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.status_mgr_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();

		// ■■■■■■■■■■■■■ 테스트 데이터 ■■■■■■■■■■■■■■■
//		for (int i = 0; i < 10; i++) {
//
//			if (i < 3) {
//
//				adapter.addItem( new CommonListItem("CU 마산산호제일점(출금불가)",
//						(i+1) + ""));
//			}else if (i < 6) {
//				adapter.addItem( new CommonListItem("CU 마산산호제일점(출금불가)",
//						(i+1) + ""));
//			}else{
//				adapter.addItem( new CommonListItem("CU 마산산호제일점(출금불가)",
//						(i+1) + ""));
//			}
//		}
//		adapter.notifyDataSetChanged();
		// ■■■■■■■■■■■■■ 테스트 데이터 ■■■■■■■■■■■■■■■

		jisaSearchTask = new JisaSearchTask();
		jisaSearchTask.execute();
	}

	// 뷰 객체 생성
	private void initView(){

		// 상단 타이틀바 셋팅
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("상태관리");

		// 조회조건 스피너 객체 생성
		sp_con1 = (Spinner) findViewById(R.id.sp_con1);
		sp_con2 = (Spinner) findViewById(R.id.sp_con2);
		sp_con3 = (Spinner) findViewById(R.id.sp_con3);

		// 조회 키워드
		keyword = getIntent().getExtras().getString("keyword","");

		// 조회 조건 : 지역
//		con_arr1 = new String[3];
//		con_arr1[0] = "전체";
//		con_arr1[1] = "긴급";
//		con_arr1[2] = "일반";
//
//		con_arr2 = new String[1];
//		con_arr2[0] = "전체";

		// 조회 조건 : 처리 상태
		con_arr3 = new String[2];
		con_arr3[0] = "전체";
		con_arr3[1] = "현금부족경고";


		// 스피너 어댑터 생성
//		adapter_con1 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr1);
//		adapter_con2 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr2);
		adapter_con3 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr3);

		// 스피너에 어댑터 셋팅
//		sp_con1.setAdapter(adapter_con1);
//		sp_con2.setAdapter(adapter_con2);
		sp_con3.setAdapter(adapter_con3);
		sp_con3.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				if ( con_arr3[position].equalsIgnoreCase("전체")) {
					str_stat = "0";
				}else{
					str_stat = "1";
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// 리스트뷰 객체 생성
		lv_status = (ListView) findViewById(R.id.lv_status);

		// 리스트뷰 어댑터 생성
		adapter = new SearchAdapter(ctx, R.layout.status_mgr_list_item);

		// 어댑터 셋팅
		lv_status.setAdapter(adapter);

		// 리스트 뷰 이벤트 구현
		lv_status.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
									long arg3) {
				// TODO Auto-generated method stub

				intent = new Intent(StatusMgrActivity.this, StatusMgrDetailActivity.class);
				intent.putExtra("ORG_CD", data_AL.get(position).getDATA01());
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

		// 조회 버튼
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(this);

		footer = getLayoutInflater().inflate(R.layout.footer_view, null, false);
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()){

			case R.id.btnSearch:
				eventClick();
				break;
		}
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

	// 자동 조회 하기 위한 메소드
	private void eventClick(){

		top = true;
		PAGE_START =  1;
		PAGE_END =  25;

		if(lv_status.getFooterViewsCount() == 1){
			Log.e("이거 삭제 됨?", "footer 삭제");
			lv_status.removeFooterView(footer);
		}

		searchTask =  new SearchTask();
		searchTask.execute();
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		JSONArray jsonArray;
		BasicVO dataVO;
		try {

			if (top) {
				if (adapter.mItems != null) {
					adapter.mItems.clear();
					data_AL.clear();
				}

				if(lv_status.getFooterViewsCount() == 1){
					Log.e("이거 삭제 됨?", "footer 삭제");
					lv_status.removeFooterView(footer);
				}
			}

			jsonArray = json.getJSONObject("state").getJSONArray("list");

			for (int i = 0; i < jsonArray.length(); i++ ) {

				adapter.addItem( new CommonListItem(
						jsonArray.getJSONObject(i).getString("ATM_NM") + "("  +
								jsonArray.getJSONObject(i).getString("DOWN_ATM_NM") + ")",
						jsonArray.getJSONObject(i).getString("ATM_GRADE")));	// 처리상태

				dataVO = new BasicVO();
				dataVO.setDATA01(jsonArray.getJSONObject(i).getString("ORG_CD"));		// 기기번호

				data_AL.add(dataVO);

			}

//			((TextView) findViewById(R.id.txt_count)).setText("총 건수 : " + badge);

			if (PAGE_NUMBER <= jsonArray.length()){



				if (lv_status.getFooterViewsCount()  == 0) {

					lv_status.addFooterView(footer);
					top = false;
				}

				footer.findViewById(R.id.btn_footer).setOnClickListener(new OnClickListener() {
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
					lv_status.removeFooterView(footer);
				}

			}
			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String>{

		private JSONObject jsonSend;

		private JSONArray jsonArray;

		private JSONObject jsonContain;

		private String url = "";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("user_id", user_info.getValue("USER_ID", ""));      		  // 사용자 아이디
					jsonSend.put("keyword", keyword);      									 // 키워드
					jsonSend.put("cash_shortage", str_stat ); 	 							 // 현금상태
					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
					jsonSend.put("branch_gb", str_jisa);    								 // 지사구분	<< 수정해야함
					jsonSend.put("team_gb", str_ch);           							     // 채널 구분	<< 수정해야함
					jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));         // 사용자 직책
					jsonSend.put("startRow", PAGE_START);                                    // 시작행
					jsonSend.put("endRow", PAGE_END);                                        // 끝행

					Log.e("상태 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "state");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("상태 조회 결과 >>" , jsonRes.toString());

					if (jsonRes == null || jsonRes.getJSONObject("state").getJSONArray("list").length() <= 0) {
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

				if (page){
					if(lv_status.getFooterViewsCount() == 1){
						Log.e("결과 정리 이거 삭제 됨?", "footer 삭제");
						lv_status.removeFooterView(footer);
					}
				}else{
					if (adapter.mItems != null){
						adapter.mItems.clear();
						data_AL.clear();
						adapter.notifyDataSetChanged();
					}
				}

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

	// 지사/채널 조회 결과값 셋팅
	private void setJisaCodeData(JSONObject jisa){

		JSONArray jisaArr;
		BasicVO basicVO;
		int cnt = 0;
		try {
			jisaArr = jisa.getJSONObject("jisa").getJSONArray("list");

			// 지사 리스트 값 셋팅
			for (int i = 0; i < jisaArr.length(); i++){

				basicVO =  new BasicVO();

				// 전체 값 변경
				if (user_info.getValue("BRANCH_GB","").equalsIgnoreCase("00")) {

					if (cnt < 1) {
						basicVO.setDATA01("ALL");    // 코드값
						basicVO.setDATA02("전체");    // 코드명
						jisa_AL.add(basicVO);
						cnt++;
					}

					basicVO =  new BasicVO();
					basicVO.setDATA01(jisaArr.getJSONObject(i).getString("BRANCH_GB"));    // 코드값
					basicVO.setDATA02(jisaArr.getJSONObject(i).getString("BRANCH_GB_NM")); // 코드명

				}else{
					basicVO.setDATA01(jisaArr.getJSONObject(i).getString("BRANCH_GB"));    // 코드값
					basicVO.setDATA02(jisaArr.getJSONObject(i).getString("BRANCH_GB_NM")); // 코드명
				}

				jisa_AL.add(basicVO);
			}

			con_arr1 = new String[jisa_AL.size()];

			for (int i = 0; i <  jisa_AL.size(); i++){
				con_arr1[i] = jisa_AL.get(i).getDATA02();
			}

			// 스피너 어댑터 셋팅(지사)
			adapter_con1 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr1);
			sp_con1.setAdapter(adapter_con1);
			sp_con1.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

					str_jisa = jisa_AL.get(position).getDATA01();

					Log.e("지사 어댑터 이벤트 여기 타는가?  >> ", con_arr1[position]);
					Log.e(" 선택된 값 >> ", str_jisa);
					channelSearchTask = new ChannelSearchTask();
					channelSearchTask.execute();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

			for (int i = 0; i <  jisa_AL.size(); i++){
				con_arr1[i] = jisa_AL.get(i).getDATA02();
			}

		}catch (Exception e){

		}
	}

	// 지사 조회 Task
	private class JisaSearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
					jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB", "")); 	     // 지사구분


					Log.e("지사 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "jisa");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("지사 조회 결과 >>" , jsonRes.toString());


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
				// 성공시 데이터 결과 셋팅 및 처리
				setJisaCodeData(jsonRes);
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if(dialog != null) dialog.dismiss();
		}
	}

	// 채널 조회 결과값 셋팅
	private void setChannelCodeData(JSONObject channel){

		JSONArray chArr;
		BasicVO basicVO;
		int cnt = 0;
		try {

			chArr = channel.getJSONObject("channel").getJSONArray("list");

			if (ch_AL  != null) ch_AL.clear();

			// 채널 리스트 값 셋팅
			for (int i = 0; i < chArr.length(); i++){

				basicVO =  new BasicVO();

				// 본사 직원인 경우 TEAM_BG 값이 0임
				// 본사직원, 지사, 지소장인 경우 채널의 전체 값을 봐야함
				if (user_info.getValue("BRANCH_GB","").equalsIgnoreCase("00") ||
						user_info.getValue("USER_GRADE","").equalsIgnoreCase("5")) {

					if (cnt < 1) {
						basicVO.setDATA01("ALL");    // 코드값
						basicVO.setDATA02("전체"); // 코드명
						ch_AL.add(basicVO);
						cnt++;
					}

					// 본사의 경우 채널이 없음
					// 본사를 제외하고 데이터 셋팅
					if (!chArr.getJSONObject(i).getString("TEAM_GB").equalsIgnoreCase("0")) {
						basicVO = new BasicVO();

						basicVO.setDATA01(chArr.getJSONObject(i).getString("TEAM_GB"));    // 코드값
						basicVO.setDATA02(chArr.getJSONObject(i).getString("TEAM_GB_NM")); // 코드명

						ch_AL.add(basicVO);
					}

				}else {

					basicVO.setDATA01(chArr.getJSONObject(i).getString("TEAM_GB"));    // 코드값
					basicVO.setDATA02(chArr.getJSONObject(i).getString("TEAM_GB_NM")); // 코드명

					ch_AL.add(basicVO);
				}
			}

			// 채널 명칭으로 값 치환
			con_arr2 = new String[ch_AL.size()];
			for (int i = 0; i < ch_AL.size(); i++){
//				if (ch_AL.get(i).getDATA02().equalsIgnoreCase("SM1")) {
//					con_arr2[i] = "1채널";
//				}else if (ch_AL.get(i).getDATA02().equalsIgnoreCase("SM2")){
//					con_arr2[i] = "2채널";
//				}else{
					con_arr2[i] = ch_AL.get(i).getDATA02();
//				}
			}

			// 스피너 어댑터 셋팅(채널)
			adapter_con2 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr2);
			sp_con2.setAdapter(adapter_con2);
			sp_con2.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

					str_ch = ch_AL.get(position).getDATA01();

					eventClick();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

		}catch (Exception e){

		}
	}


	// 채널 조회 Task
	private class ChannelSearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();
					jsonSend.put("user_id", user_info.getValue("USER_ID", ""));     		 // 사용자 아이디
					jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));        // 사용자 직책
					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹

					// 선택한 스피너의 값이 전체일 경우
					// 로그인한 사람의 지사 구분 값으로 셋팅
					if (str_jisa.equalsIgnoreCase("ALL")) {
						jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB", ""));         // 지사구분
					}else{
						jsonSend.put("branch_gb", str_jisa);
					}

					Log.e("채널 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "channel");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);
					Log.e("채널 조회 >>" , jsonRes.toString());

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
				// 성공시 데이터 결과 셋팅 및 처리
				setChannelCodeData(jsonRes);

			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if(dialog != null) dialog.dismiss();
		}
	}




	// 리스트뷰 커스텀 어댑터
	private class SearchAdapter extends BaseAdapter{

		Context mCtx;
		LayoutInflater inflater;
		int layout;

		public List<CommonListItem> mItems = new ArrayList<CommonListItem>();

		public SearchAdapter(Context ctx, int layout){
			this.mCtx = ctx;
			this.layout = layout;
			inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			if(mItems == null) return 0;
			return mItems.size();
		}

		@Override
		public CommonListItem getItem(int position) {
			return mItems.get( position);
		}

		@Override
		public long getItemId(int position) {
			return mItems.get( position).getId();
		}

		public void addItem(CommonListItem it) {
			mItems.add(it);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			try {

				ViewHolder holder;

				if(convertView == null) {
					convertView = inflater.inflate(layout, parent, false);

					holder = new ViewHolder();
					holder.mTextView1 = (TextView) convertView.findViewById(R.id.mData01);
					holder.mTextView2 = (TextView) convertView.findViewById(R.id.mData02);

					convertView.setTag(holder);
				}
				else {// View 재사용
					holder = (ViewHolder) convertView.getTag();
				}

				// SetText
				holder.mTextView1.setText(mItems.get(position).getData(0));

				// 등급에 따른 이미지 치환
				if (mItems.get(position).getData(1).equalsIgnoreCase("0")) {

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_0);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("1")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_1);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("2")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_2);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("3")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_3);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("4")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_4);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("5")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_5);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("6")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_6);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("7")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_7);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("8")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_8);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("9")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_9);

				}else if(mItems.get(position).getData(1).equalsIgnoreCase("10")){

					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_10);

				}else{
					holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_11);
				}

			} catch (Exception e) {

				// TODO: handle exception

			}
			return convertView;
		}

		class ViewHolder {

			TextView mTextView1;
			TextView mTextView2;
		}
	}
}
