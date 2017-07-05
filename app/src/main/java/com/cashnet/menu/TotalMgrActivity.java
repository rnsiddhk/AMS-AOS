package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.common.SpinnerAdapter;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 집계관리
 * see total_mgr_view.xml
 * @since 2016-08-24
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class TotalMgrActivity extends BaseActivtiy implements OnClickListener{

	private View top_view;

	// 스피너: 조회 조건 1, 2
	private Spinner sp_con1, sp_con2;

	// 커스텀 스피너 어댑터
	private SpinnerAdapter adapter_con1, adapter_con2;

	// 커스텀 어댑터 사용할 문자 배열
	private String[] con_arr1, con_arr2;

	private Intent intent;

	private Context ctx;

	private SGPreference user_info;

	// 지사 리스트
	private ArrayList<BasicVO> jisa_AL = new ArrayList<BasicVO>();

	// 채널 리스트
	private ArrayList<BasicVO> ch_AL = new ArrayList<BasicVO>();

	// 지사구분, 채널구분, 기기번호
	private String str_jisa = "", str_ch = "", org_cd = "";

	private JSONObject jsonRes;

	private JisaSearchTask jisaSearchTask;

	private ChannelSearchTask channelSearchTask;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.total_mgr_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();

		jisaSearchTask = new JisaSearchTask();
		jisaSearchTask.execute();

	}

	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);

		// 타이틀바 이름 정하기
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("집계관리");

		// CM인 경우 파출수납만 보여야함
		if(user_info.getValue("USER_GRADE","").equalsIgnoreCase("6")){
			// 일/월별 종합 집계
			((LinearLayout) findViewById(R.id.btn_day_mon)).setVisibility(View.GONE);

			// 기기별 종합 집계
			((LinearLayout) findViewById(R.id.btn_machine)).setVisibility(View.GONE);

			// 장애 유형별 건수
			((LinearLayout) findViewById(R.id.btn_issue_cnt)).setVisibility(View.GONE);
		}

		// 일/월별 종합 집계
		((LinearLayout) findViewById(R.id.btn_day_mon)).setOnClickListener(this);

		// 기기별 종합 집계
		((LinearLayout) findViewById(R.id.btn_machine)).setOnClickListener(this);

		// 장애 유형별 건수
		((LinearLayout) findViewById(R.id.btn_issue_cnt)).setOnClickListener(this);

		// 파출수납 관리
		((LinearLayout) findViewById(R.id.btn_call_mgr)).setOnClickListener(this);

		sp_con1 = (Spinner) findViewById(R.id.sp_con1);
		sp_con2 = (Spinner) findViewById(R.id.sp_con2);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()) {

			// 일/월별 총 집계
			case R.id.btn_day_mon:
				intent = new Intent(this, TotalDMActivity.class);
				intent.putExtra("BRANCH_GB", str_jisa);
				intent.putExtra("TEAM_GB", str_ch);
				startActivity(intent);
				break;

			// 기기별 총 집계
			case R.id.btn_machine:
				intent = new Intent(this, TotalMachineListActivity.class);
				intent.putExtra("BRANCH_GB", str_jisa);
				intent.putExtra("TEAM_GB", str_ch);
				startActivity(intent);
				break;

			// 장애 유형별 건수
			case R.id.btn_issue_cnt:
				intent = new Intent(this, TotalIssueStatusActivity.class);
				intent.putExtra("BRANCH_GB", str_jisa);
				intent.putExtra("TEAM_GB", str_ch);
				startActivity(intent);
				break;

			// 파출수납
			case R.id.btn_call_mgr:
				intent = new Intent(this, CollectMoneyListActivity.class);
				intent.putExtra("BRANCH_GB", str_jisa);
				startActivity(intent);
				break;
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

			dialog = new ProgressDialog(TotalMgrActivity.this);
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
			if (!FunctionClass.getNetworkState(TotalMgrActivity.this)) {

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
				if (ch_AL.get(i).getDATA02().equalsIgnoreCase("SM1")) {
					con_arr2[i] = "1채널";
				}else if (ch_AL.get(i).getDATA02().equalsIgnoreCase("SM2")){
					con_arr2[i] = "2채널";
				}else{
					con_arr2[i] = ch_AL.get(i).getDATA02();
				}
			}

			// 스피너 어댑터 셋팅(채널)
			adapter_con2 = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, con_arr2);
			sp_con2.setAdapter(adapter_con2);
			sp_con2.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					str_ch = ch_AL.get(position).getDATA01();
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

			dialog = new ProgressDialog(TotalMgrActivity.this);
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
			if (!FunctionClass.getNetworkState(TotalMgrActivity.this)) {

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
}