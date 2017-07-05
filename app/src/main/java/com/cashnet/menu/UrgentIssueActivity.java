package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
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
import com.cashnet.common.UrgentIssueAdapter;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 긴급출동, 장애관리 공통으로 사용
 * Tip: 이전 액티비티에서 전달된 플래그 값에 따라 조회 조건이 다르게 셋팅되고 상단 타이틀바도 다르게 셋팅됨
 * see urgent_issue_view.xml
 * @since 2016-08-18
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class UrgentIssueActivity extends BaseActivtiy implements OnClickListener{

	// 스피너: 조회 조건 1, 2, 3
	private Spinner sp_con1, sp_con2, sp_con3;

	// 커스텀 스피너 어댑터
	private SpinnerAdapter adapter_con1, adapter_con2, adapter_con3;

	// 커스텀 어댑터 사용할 문자 배열
	private String[] con_arr1, con_arr2, con_arr3;

	// 조회 버튼
	private Button btn_click;

	// 현재 액티비티의 컨택스트 객체
	private Context ctx;

	// 조회 결과 받을 JSON 객체
	private JSONObject jsonRes;

	// android-support-v4 라이브러리에서 제공하느, Pull to Refresh 기능
	private SwipeRefreshLayout swipeRefreshLayout;

	// 리스트뷰 객체
	private ListView lv_urgent;

	// 커스텀 리스트뷰 어댑터
	private UrgentIssueAdapter adapter;

	// 상단 타이틀바 객체
	private View top_view;

	// 이전 액티비티 이름
	private String strActNM = "";

	// 인텐트 객체
	private Intent intent;

	private SGPreference user_info;

	// 지사구분, 채널구분, 처리상태구분, 기기번호
	private String str_jisa = "", str_ch = "", str_stat = "", org_cd = "";

	private ArrayList<BasicVO> data_AL = new ArrayList<BasicVO>();

	// 긴급/장애 조회 테스크
	private UrgentSearchTask searchTask;


	// 지사, 채널 조회 테스크
	private JisaSearchTask jisaSearchTask;

	private ChannelSearchTask channelSearchTask;

	// 처리상태 조회 테스크
	private CodeSearchTask codeSearchTask;

	private boolean top = true;

	// 리스트뷰 footer
	private View footer;

	private int PAGE_NUMBER = 25;

	private int PAGE_START = 1, PAGE_END = 25;

	private int badge = 0;

	// 지사 리스트
	private ArrayList<BasicVO> jisa_AL = new ArrayList<BasicVO>();

	// 채널 리스트
	private ArrayList<BasicVO> ch_AL = new ArrayList<BasicVO>();

	// 처리상태 리스트
	private ArrayList<BasicVO> tr_AL = new ArrayList<BasicVO>();

	// 자동조회 플래그
	private static boolean flag = true;

	private boolean page = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.urgent_issue_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		initView();

		user_info = new SGPreference(ctx);

		codeSearchTask = new CodeSearchTask();
		codeSearchTask.execute();

	}


	// 뷰 객체 생성 메소드
	private void initView(){

		// 이전 액티비티에서 전달된 액티비티 플래그 값 , 빈값일 시 기본 셋팅은 긴급출동 액티비티로 셋팅 되도록 함
		strActNM = getIntent().getExtras().getString("ACT_NM", "URGENT");

		// 긴급/장애의 총 건수
		badge = getIntent().getExtras().getInt("BADGE", 0);

		// 기기번호
		org_cd = getIntent().getExtras().getString("ORG_CD", "");

		// 상단 타이틀바 가리기
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		if (strActNM.equalsIgnoreCase("URGENT")) {
			((TextView)((top_view).findViewById(R.id.txt_title))).setText("긴급출동");
		}else{
			((TextView)((top_view).findViewById(R.id.txt_title))).setText("장애관리");
		}

		// 조회조건 스피너 객체 생성
		sp_con1 = (Spinner) findViewById(R.id.sp_con1);		// 지사
		sp_con2 = (Spinner) findViewById(R.id.sp_con2);		// 채널
		sp_con3 = (Spinner) findViewById(R.id.sp_con3);		// 처리상태

		// 조회버튼 이벤트
		btn_click = (Button) findViewById(R.id.btn_click);
		btn_click.setOnClickListener(this);

		// 조회 조건 : 지역
//		con_arr1 = new String[3];
//		con_arr1[0] = "전체";
//		con_arr1[1] = "긴급";
//		con_arr1[2] = "일반";

//		con_arr2 = new String[1];
//		con_arr2[0] = "전체";
//
//		// 조회 조건 : 처리 상태
//		con_arr3 = new String[6];
//		con_arr3[0] = "미처리";
//		con_arr3[1] = "전체";
//		con_arr3[2] = "신규";
//		con_arr3[3] = "접수";
//		con_arr3[4] = "이관";
//		con_arr3[5] = "완료";

		// 스피너 어댑터 생성
//		adapter_con1 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr1);
//		adapter_con2 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr2);


		// 스피너에 어댑터 셋팅
//		sp_con1.setAdapter(adapter_con1);
//		sp_con2.setAdapter(adapter_con2);


		// 리스트뷰 객체 생성
		lv_urgent = (ListView) findViewById(R.id.lv_urgent);

		// 리스트뷰 어댑터 생성
		adapter = new UrgentIssueAdapter(ctx, R.layout.urgent_issue_list_item);

		// 어댑터 셋팅
		lv_urgent.setAdapter(adapter);

		// 리스트 뷰 이벤트 구현
		lv_urgent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
									long arg3) {
				// TODO Auto-generated method stub

				intent = new Intent(UrgentIssueActivity.this, IssueDetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("DATA", data_AL.get(position));

				startActivity(intent);
			}
		});

		// pull to refresh 객체 생성 및 이벤트 구현
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {

				eventClick();

				swipeRefreshLayout.setRefreshing(false);

			}
		});

		footer = getLayoutInflater().inflate(R.layout.footer_view, null, false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()){

			case R.id.btn_click:
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

		Log.e("eventClick?", "footer 삭제");
		top = true;
		PAGE_START =  1;
		PAGE_END =  25;

		if(lv_urgent.getFooterViewsCount() == 1){
			Log.e("btn_seacrh 이거 삭제 됨?", "footer 삭제");
			lv_urgent.removeFooterView(footer);
		}

		searchTask =  new UrgentSearchTask();
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
				Log.e("이거 삭제 됨?", lv_urgent.getFooterViewsCount() + "개");
				if(lv_urgent.getFooterViewsCount() == 1){
					Log.e("setData 이거 삭제 됨?", "footer 삭제");
					lv_urgent.removeFooterView(footer);
				}
			}

			jsonArray = json.getJSONObject("obstacle").getJSONArray("list");

			for (int i = 0; i < jsonArray.length(); i++ ) {

				adapter.addItem( new CommonListItem(
						jsonArray.getJSONObject(i).getString("ATM_NM") + " (" +jsonArray.getJSONObject(i).getString("DOWN_ATM_NM") + ")",   // ATM 기기명 + 장애명
						jsonArray.getJSONObject(i).getString("ATM_GRADE"), // ATM 등급
						jsonArray.getJSONObject(i).getString("TREAT_STEP")
								+ "차 " + jsonArray.getJSONObject(i).getString("TREAT_STATUS_NM"),
						jsonArray.getJSONObject(i).getString("TREAT_STATUS_NM")));	// 처리상태

				dataVO = new BasicVO();
				dataVO.setDATA01(jsonArray.getJSONObject(i).getString("GIJUN_IL"));		// 장애발생일
				dataVO.setDATA02(jsonArray.getJSONObject(i).getString("ORG_CD"));		// 기기번호
				dataVO.setDATA03(jsonArray.getJSONObject(i).getString("DOWN_SI"));		// 장애발생시간
				dataVO.setDATA04(jsonArray.getJSONObject(i).getString("DOWN_GB"));		// 장애구분
				dataVO.setDATA05(jsonArray.getJSONObject(i).getString("TREAT_STATUS"));	// 조치상태

				data_AL.add(dataVO);

			}

			if (0 < badge){
				((TextView) findViewById(R.id.txt_count)).setText("총 건수 : " + badge);
			}else{
				((TextView) findViewById(R.id.txt_count)).setVisibility(View.GONE);
			}

			if (PAGE_NUMBER <= jsonArray.length()){

				if (lv_urgent.getFooterViewsCount()  == 0) {

					lv_urgent.addFooterView(footer);
					top = false;
				}

				footer.findViewById(R.id.btn_footer).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						page = true;

						PAGE_START = PAGE_END + 1;
						PAGE_END = PAGE_END + 25;

						searchTask =  new UrgentSearchTask();
						searchTask.execute();
					}
				});

			}else{

				if (!top){
					lv_urgent.removeFooterView(footer);
				}

			}
			adapter.notifyDataSetChanged();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class UrgentSearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(UrgentIssueActivity.this);
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
			if (!FunctionClass.getNetworkState(UrgentIssueActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					if (strActNM.equalsIgnoreCase("URGENT")){
						jsonSend.put("down_gb", "1");                                        // 장애구분( 장애 : 0, 긴급 : 1, 비장애 : 2)
					}else {
						jsonSend.put("down_gb", "0");
					}
					jsonSend.put("org_cd", org_cd);                     		    		 // 기기번호
					jsonSend.put("treat_status", str_stat);                         		 // 처리상태코드
					jsonSend.put("user_id", user_info.getValue("USER_ID", ""));              // 사용자 ID
					jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
					jsonSend.put("branch_gb", str_jisa);    								 // 지사구분
					jsonSend.put("team_gb", str_ch);           							     // 채널 구분
					jsonSend.put("user_ent", user_info.getValue("USER_ENT", ""));            // 업체 코드
					jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));        // 사용자 직책

					jsonSend.put("startRow", PAGE_START);									// 시작행
					jsonSend.put("endRow", PAGE_END);										// 마지막행


					Log.e("긴급/장애 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "obstacle");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("긴급/장애 조회 결과 >>" , jsonRes.toString());
					Log.e("긴급/장애 조회 결과 수 >>" , jsonRes.getJSONObject("obstacle").getJSONArray("list").length() + "개");

					if (jsonRes == null || jsonRes.getJSONObject("obstacle").getJSONArray("list").length() <= 0) {
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
					if(lv_urgent.getFooterViewsCount() == 1){
						Log.e("결과 정리 이거 삭제 됨?", "footer 삭제");
						lv_urgent.removeFooterView(footer);
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

			dialog = new ProgressDialog(UrgentIssueActivity.this);
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
			if (!FunctionClass.getNetworkState(UrgentIssueActivity.this)) {

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


					if (jsonRes == null || jsonRes.getJSONObject("jisa").getJSONArray("list").length() <= 0) {
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

			dialog = new ProgressDialog(UrgentIssueActivity.this);
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
			if (!FunctionClass.getNetworkState(UrgentIssueActivity.this)) {

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

					if (jsonRes == null || jsonRes.getJSONObject("channel").getJSONArray("list").length() <= 0) {
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

	private void setCodeData(JSONObject json){

		JSONArray jsonArray;
		BasicVO basicVO;
		try {
			jsonArray = json.getJSONObject("codeList").getJSONArray("list");

			for (int i = 0; i < jsonArray.length(); i++){

				basicVO =  new BasicVO();

				// 미처리 값 변경
				if (jsonArray.getJSONObject(i).getString("ITEM1").equalsIgnoreCase("-1")) {
					basicVO.setDATA01("NOT_TREATED_ALL");    // 코드값
					basicVO.setDATA02(jsonArray.getJSONObject(i).getString("ITEM2"));    // 코드명
				}else{

					// 미처리 다음 전체값 추가
					if (i == 1){
						basicVO.setDATA01("ALL");    // 코드값
						basicVO.setDATA02("전체");    // 코드명
						tr_AL.add(basicVO);

						basicVO =  new BasicVO();
						basicVO.setDATA01(jsonArray.getJSONObject(i).getString("ITEM1"));    // 코드값
						basicVO.setDATA02(jsonArray.getJSONObject(i).getString("ITEM2"));    // 코드명

					}else {
						basicVO.setDATA01(jsonArray.getJSONObject(i).getString("ITEM1"));    // 코드값
						basicVO.setDATA02(jsonArray.getJSONObject(i).getString("ITEM2"));    // 코드명
					}
				}
				tr_AL.add(basicVO);
			}

			con_arr3 = new String[tr_AL.size()];

			for(int i = 0; i < tr_AL.size(); i++){
				con_arr3[i] = tr_AL.get(i).getDATA02();
			}

			adapter_con3 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr3);
			sp_con3.setAdapter(adapter_con3);
			sp_con3.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					str_stat = tr_AL.get(position).getDATA01();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});


			jisaSearchTask = new JisaSearchTask();
			jisaSearchTask.execute();

		}catch (Exception e){

		}
	}

	// 조회 Task
	private class CodeSearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(UrgentIssueActivity.this);
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
			if (!FunctionClass.getNetworkState(UrgentIssueActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("gubun", "TR_STATUS");                           // 구분

					Log.e("장애상태 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "codeList");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("장애상태 조회 결과 >>" , jsonRes.toString());

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

				setCodeData(jsonRes);
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