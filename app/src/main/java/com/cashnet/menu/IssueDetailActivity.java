package com.cashnet.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.SGPreference;
import com.cashnet.common.SpinnerAdapter;
import com.cashnet.dialog.ConfirmPassActivity;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.util.ScreenUtil;
import com.cashnet.view.AlbumView;
import com.cashnet.view.CameraView;
import com.cashnet.vo.BasicVO;
import com.google.android.map.GoogleMapViewer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * 장애관리 상세
 * Tip: 이전 액티비티에서 전달된 플래그 값에 따라 조회 조건이 다르게 셋팅되고 상단 타이틀바도 다르게 셋팅됨
 * see issue_detail_view.xml
 * @since 2016-08-19
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class IssueDetailActivity extends BaseActivtiy implements OnClickListener{

	// 지도보기, 경로보기, 기기정보, 장애이력, 시재정보, 접수, 취소
	private Button btn_map, btn_route, btn_dev_info, btn_issue_list, btn_currency_info, btn_regist, btn_cancle, btn_time;

	// 사진첨부, 삭제
	private Button btn_picutre, btn_camera, btn_album, btn_delete;

	private ImageView img_thum;

	// 도착예정 시간
	private Spinner sp_time;

	// 처리결과
	private Spinner sp_result;

	// 이관작업
	private Spinner sp_company;

	// 도착시간, 처리결과, 이관작업 어댑터
	private SpinnerAdapter adapter_time, adapter_result, adpter_company;

	// 커스텀 어댑터 사용할 문자 배열(시간)
	private String[] time_arr;

	// 커스텀 어댑터 사용할 문자 배열(처리결과)
	private String[] result_arr;

	// 커스텀 어댑터 사용할 문자 배열(이관업체)
	private String[] company_arr;

	public Context ctx;

	private Intent intent;

	// 이미지 파일 인코딩 변수
	private String origin_basr64 = "", thum_base64 = "";

	private View top_view;

	// 조치내역 수정버튼
	private Button btnModify;

	// 조치내역 플래그
	private boolean flags = false;

	// 조치내역 마지막 에티트 텍스트 아이디 값
	private View last_view;

	private BasicVO dataVO =  new BasicVO();

	private JSONObject jsonRes;

	private int[] gradeId = new int[] { R.drawable.icon_grade_0, R.drawable.icon_grade_1, R.drawable.icon_grade_2, R.drawable.icon_grade_3, R.drawable.icon_grade_4,
						R.drawable.icon_grade_5, R.drawable.icon_grade_6, R.drawable.icon_grade_7, R.drawable.icon_grade_8, R.drawable.icon_grade_9,
						R.drawable.icon_grade_10, R.drawable.icon_grade_11 };

	// 경도, 위도 정보
	private Double xGrid, yGrid;

	// 기기명
	private String atmNM = "";

	private SGPreference user_info;

	// 시간변경 플래그
	private boolean time_change = false;

	// 도착예정시간
	// 0 : 15분, 1 : 30분, 2 : 1시간, 3 : 2시간, 4 : 2시간 이상
	private int time_point = 0;

	// 처리상태 값
	// 0 : 신규, 1 : 접수, 2 : 이관, 3 : 도착, 8 : 자동복구, 9 : 처리완료
	private int treat_status = 0;

	private String tr_status = "";

	// 처리 찻수
	private String treat_step = "";

	private HashMap<Integer, String> TR_NM = new HashMap<Integer, String>();

	// 조치내역 메모
	private String REPAIR_MEMO = "";

	// 조치내역 입력 Task
	private InputDataTask inputDataTask;

	private ScrollView scrollView;

	private Handler handler;

	// 전송할 파일이름
	private String strFileName = "";

	// 조치일, 조치시간
	private String strRepair_il = "", strRepair_si = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.issue_detail_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		// 전달된 값
		dataVO = getIntent().getExtras().getParcelable("DATA");

		user_info = new SGPreference(ctx);

		initView();

	}

	@Override
	protected void onResume() {
		super.onResume();

		SearchTask searchTask =  new SearchTask();
		searchTask.execute();

	}

	// 뷰 객체 생성 및 셋팅
	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("장애 상세");

		// 조치내역 해쉬맵
		// 0 : 신규, 1 : 접수, 2 : 이관, 3 : 도착, 8 : 자동복구, 9 : 처리완료
		TR_NM.put(1, "접수완료");
		TR_NM.put(2, "이관");
		TR_NM.put(3, "현장도착");
		TR_NM.put(8, "자동복구");
		TR_NM.put(9, "처리완료");

		// 기기명, 기기번호, 발생시간, 장애유형, 요청내용, 연락처, 기기위치
		((TextView) findViewById(R.id.txt_name)).setText("");
		((TextView) findViewById(R.id.txt_sn)).setText("");
		((TextView) findViewById(R.id.txt_time)).setText("");
		((TextView) findViewById(R.id.txt_status)).setText("");
		((TextView) findViewById(R.id.txt_request)).setText("");
		((TextView) findViewById(R.id.txt_phone)).setText("");
		((TextView) findViewById(R.id.txt_location)).setText("");

		// 기기등급 이미지 치환
		((TextView) findViewById(R.id.txt_grade)).setBackgroundResource(0);

		// 지도보기
		btn_map = (Button) findViewById(R.id.btn_map);
		btn_map.setOnClickListener(this);

		// 경로보기
		btn_route = (Button) findViewById(R.id.btn_route);
		btn_route.setOnClickListener(this);

		// 기기정보
		btn_dev_info = (Button) findViewById(R.id.btn_dev_info);
		btn_dev_info.setOnClickListener(this);

		// 장애이력
		btn_issue_list = (Button) findViewById(R.id.btn_issue_list);
		btn_issue_list.setOnClickListener(this);

		// 시재정보
		btn_currency_info = (Button) findViewById(R.id.btn_currency_info);
		btn_currency_info.setOnClickListener(this);

		// 시재정보 열람 불가 사용자 직책
		// 7:SM, 9:개발, 99:일반
		if (user_info.getValue("USER_GRADE","").equalsIgnoreCase("7") ||
				user_info.getValue("USER_GRADE","").equalsIgnoreCase("9") ||
				user_info.getValue("USER_GRADE","").equalsIgnoreCase("99")) {
			btn_currency_info.setVisibility(View.GONE);
		}

		// 접수
		btn_regist = (Button) findViewById(R.id.btn_regist);
		btn_regist.setOnClickListener(this);

		// 취소
		btn_cancle = (Button) findViewById(R.id.btn_cancel);
		btn_cancle.setOnClickListener(this);

		// 시간변경
		btn_time = (Button) findViewById(R.id.btn_time);
		btn_time.setOnClickListener(this);

		// 사진첨부
		btn_picutre = (Button) findViewById(R.id.btn_picutre);
		btn_picutre.setOnClickListener(this);

		// 카메라
		btn_camera = (Button) findViewById(R.id.btn_camera);
		btn_camera.setOnClickListener(this);

		// 앨범
		btn_album = (Button) findViewById(R.id.btn_album);
		btn_album.setOnClickListener(this);

		// 삭제
		btn_delete = (Button) findViewById(R.id.btn_delete);
		btn_delete.setOnClickListener(this);

		// 첨부사진
		img_thum = (ImageView) findViewById(R.id.img_thum);

		// 도착시간 스피너
		sp_time = (Spinner) findViewById(R.id.sp_time);


		// time_point 변수에 인덱스 값만 사용
		time_arr = new String[5];
		time_arr[0] = "15분";			// 0
		time_arr[1] = "30분";			// 1
		time_arr[2] = "1시간";			// 2
		time_arr[3] = "2시간";			// 3
		time_arr[4] = "2시간 이상";		// 4

		adapter_time = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, time_arr);
		sp_time.setAdapter(adapter_time);
		sp_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				time_point = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// 처리결과 스피너
		sp_result = (Spinner) findViewById(R.id.sp_result);

		result_arr =  new String[2];
		result_arr[0] = "완료";			// 9
		result_arr[1] = "미완료(이관)";	// 2

		adapter_result = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, result_arr);
		sp_result.setAdapter(adapter_result);
		sp_result.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				// 완료
				if (position == 0) {
					treat_status = 9;
					((LinearLayout) findViewById(R.id.ll_company)).setVisibility(View.GONE);

					((EditText) findViewById(R.id.edt_result)).setText("");

				}else{ // 이관
					treat_status = 2;
					((LinearLayout) findViewById(R.id.ll_company)).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		// 조치 상태 값에 따른 화면 구성
		// 0 : 신규, 1 : 접수, 2 : 이관, 3 : 도착, 8 : 자동복구, 9 : 처리완료
		// 조치상태
		tr_status = dataVO.getDATA05();

		if (tr_status.equalsIgnoreCase("1")){

			btn_time.setVisibility(View.VISIBLE);
			btn_regist.setText("도착");

		}else if(tr_status.equalsIgnoreCase("3")){
			sp_time.setEnabled(false);
			((LinearLayout) findViewById(R.id.ll_result)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.ll_memo)).setVisibility(View.VISIBLE);
			btn_regist.setText("저장");
		} else if (tr_status.equalsIgnoreCase("9")){ // 조치상태가 완료시 하단 조치내역 입력 화면과 도차예정시간 부분 가리기

			((LinearLayout) findViewById(R.id.ll_result)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.ll_repair)).setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.ll_time_line)).setVisibility(View.GONE);

		}

		scrollView = (ScrollView) findViewById(R.id.scroll);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()) {

			// 지도보기
			case R.id.btn_map:
				// 좌표정보가 없는 기기들이 있음
//				if (xGrid != 0.0 && yGrid != 0.0){
					intent = new Intent(ctx, GoogleMapViewer.class);
					intent.putExtra("ACT_GB", "POINT");
					intent.putExtra("X_GRID", xGrid);
					intent.putExtra("Y_GRID", yGrid);
					intent.putExtra("ATM_NAME", atmNM);
					intent.putExtra("ORG_CD", dataVO.getDATA02());
					startActivity(intent);
//				}else{
//					Toast.makeText(ctx, getString(R.string.str_no_code_msg), Toast.LENGTH_SHORT).show();
//				}
				break;

			// 경로보기
			case R.id.btn_route:
				if (xGrid != 0.0 && yGrid != 0.0){
					intent = new Intent(ctx, GoogleMapViewer.class);
					intent.putExtra("ACT_GB", "ROUTE");
					intent.putExtra("X_GRID", xGrid);
					intent.putExtra("Y_GRID", yGrid);
					intent.putExtra("ATM_NAME", atmNM);
					startActivity(intent);
				}else{
					Toast.makeText(ctx, getString(R.string.str_no_code_msg), Toast.LENGTH_SHORT).show();
				}

				break;

			// 기기정보
			case R.id.btn_dev_info:
				intent = new Intent(ctx, MachineInfoActivity.class);
				intent.putExtra("ORG_CD", dataVO.getDATA02());
				startActivity(intent);
				break;

			// 장애이력
			case R.id.btn_issue_list:
				intent = new Intent(ctx, IssueHistoryActivity.class);
				intent.putExtra("ORG_CD", dataVO.getDATA02());
				startActivity(intent);
				break;

			// 시재정보
			case R.id.btn_currency_info:

				intent = new Intent(ctx, ConfirmPassActivity.class);
				intent.putExtra("ORG_CD", dataVO.getDATA02());
				intent.putExtra("USER_ID", user_info.getValue("USER_ID", ""));
				startActivity(intent);

				break;

			// 사진첨부
			case R.id.btn_picutre:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				// 완료와 이관시에만 이미지 보여주기
				if (treat_status == 2 || treat_status == 9){
					((LinearLayout) findViewById(R.id.ll_picture)).setVisibility(View.VISIBLE);
				}else{
					Toast.makeText(ctx, getString(R.string.str_failure_image_alert_msg),Toast.LENGTH_SHORT).show();
				}
				break;

			// 카메라
			case R.id.btn_camera:
				intent = new Intent(ctx, CameraView.class);
				startActivityForResult(intent, 1);
				break;

			// 앨범
			case R.id.btn_album:
				intent = new Intent(ctx, AlbumView.class);
				startActivityForResult(intent, 2);
				break;

			// 삭제
			case R.id.btn_delete:
				((LinearLayout) findViewById(R.id.ll_picture)).setVisibility(View.GONE);
				img_thum.setImageBitmap(null);
				break;

			// 시간변경
			case R.id.btn_time:
				time_change = true;
				treat_status = 1;
				UpdateTimeTask updateTimeTask = new UpdateTimeTask();
				updateTimeTask.execute();
				break;

			// 접수
			case R.id.btn_regist:

				// 버튼 라벨에 따라 조치상태값 변경
				if (btn_regist.getText().toString().equalsIgnoreCase("접수")){
					treat_status = 1;
				}else if (btn_regist.getText().toString().equalsIgnoreCase("도착")){
					treat_status = 3;
				}

				// 이관, 완료 내용 처리 로직
				if (treat_status == 2 || treat_status == 9) {

					REPAIR_MEMO = ((EditText) findViewById(R.id.edt_result)).getText().toString().trim();

					// 조치내역 메모 공백 체크
					if (!FunctionClass.isNullOrBlank(REPAIR_MEMO)) {

						inputDataTask = new InputDataTask();
						inputDataTask.execute();

					} else {
						Toast.makeText(ctx, getString(R.string.str_input_repair_msg), Toast.LENGTH_SHORT).show();
					}
				}else {
					inputDataTask = new InputDataTask();
					inputDataTask.execute();
				}
				break;

			// 취소
			case R.id.btn_cancel:
				finish();
				break;
		}
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		JSONArray jsonArray;
		int grade = 0;
		int time = 0;
		try {

			// 기기명, 기기번호, 발생시간, 장애유형, 요청내용, 연락처, 기기위치
			atmNM = json.getJSONObject("dscOb").getString("ATM_NM");
			((TextView) findViewById(R.id.txt_name)).setText(atmNM);
			((TextView) findViewById(R.id.txt_sn)).setText(json.getJSONObject("dscOb").getString("ORG_CD"));
			((TextView) findViewById(R.id.txt_time)).setText(json.getJSONObject("dscOb").getString("DOWN_TIME"));
			((TextView) findViewById(R.id.txt_status)).setText(json.getJSONObject("dscOb").getString("DOWN_ATM_NM"));
			((TextView) findViewById(R.id.txt_request)).setText(FunctionClass.isNullOrBlankReturn(json.getJSONObject("dscOb").getString("DOWN_MEMO")));
			((TextView) findViewById(R.id.txt_phone)).setText(FunctionClass.isNullOrBlankReturn(json.getJSONObject("dscOb").getString("ERR_TEL")));
			((TextView) findViewById(R.id.txt_location)).setText(json.getJSONObject("dscOb").getString("ADDR_RMK"));

			// 기기등급 이미지 치환
			grade = Integer.parseInt(json.getJSONObject("dscOb").getString("ATM_GRADE"));
			((TextView) findViewById(R.id.txt_grade)).setBackgroundResource(gradeId[grade]);

			//  조회된 도착예정시간 셋팅
			if (!FunctionClass.isNullOrBlank(json.getJSONObject("dscOb").getString("P_ARR_TIME"))){
				time = Integer.parseInt(json.getJSONObject("dscOb").getString("P_ARR_TIME"));
			}

			sp_time.setSelection(time);

			// 처리단계
			treat_step = json.getJSONObject("dscOb").getString("TREAT_STEP");

			/**
			 * 조치내역 화면 구성 시작
			 * 조치내역 수정은 마지막 내용만 수정해야 함
			 * */
			int px = (int)ScreenUtil.convertDpToPixel(30f, this);

			jsonArray = json.getJSONObject("dscOb").getJSONArray("list");

			((LinearLayout) findViewById(R.id.ll_issue_list)).removeAllViews();

			if (0 < jsonArray.length()){

				// 장애내역 관련 객체 동적으로 생성하기
				for (int i = 0; i < jsonArray.length(); i++) {

					EditText edtView = new EditText(this);
					edtView.setText(jsonArray.getJSONObject(i).getString("REPAIR_TIME") + " " +
									" [" + jsonArray.getJSONObject(i).getString("REPAIR_REG_NM") + "] " +
							jsonArray.getJSONObject(i).getString("REPAIR_MEMO"));
					edtView.setGravity(Gravity.LEFT);
					edtView.setTextSize(14f);
					edtView.setEnabled(false);
					edtView.setSingleLine(true);

					LinearLayout.LayoutParams txt_1_params =
							new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px, 0);
					txt_1_params.setMargins(15, 0, 0, 0);

					// 부모 레이아웃에 붙이기
					((LinearLayout) findViewById(R.id.ll_issue_list)).addView(edtView, txt_1_params);

				}

				// 수정 버튼 생성
				// 완료 처리가 장애의 경우 조치내역을 수정할 수 있다
				btnModify = new Button(this);
				LinearLayout.LayoutParams btn_params =
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
				btn_params.gravity = Gravity.RIGHT;
				btn_params.setMargins(0, 0, 15, 15);
				btnModify.setGravity(Gravity.CENTER);
				btnModify.setText("조치내역 수정");
				btnModify.setBackgroundResource(R.drawable.selector_btn_comm);

				((LinearLayout) findViewById(R.id.ll_issue_list)).addView(btnModify, btn_params);

				if (tr_status.equalsIgnoreCase("9")){
					btnModify.setVisibility(View.VISIBLE);
				}else{
					btnModify.setVisibility(View.GONE);
				}

				// 수정/저장 버튼 이벤트 등록
				btnModify.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						((LinearLayout) findViewById(R.id.ll_repair)).setVisibility(View.VISIBLE);
						((LinearLayout) findViewById(R.id.ll_result)).setVisibility(View.VISIBLE);
						((LinearLayout) findViewById(R.id.ll_memo)).setVisibility(View.VISIBLE);

						// 스크롤 이동
						handler = new Handler();
						handler.postDelayed(new Runnable(){
							@Override
							public void run() {
								scrollView.fullScroll(View.FOCUS_DOWN);
								scrollView.invalidate();
							}
						}, 100);

						btn_regist.setText("수정");

					}
				});
			}
			/***
			 * 조치내역 화면 구성 끝
			 */

			// 경도, 위도
			xGrid = Double.parseDouble(json.getJSONObject("dscOb").getJSONObject("atminfo").getString("X_GRID"));
			yGrid = Double.parseDouble(json.getJSONObject("dscOb").getJSONObject("atminfo").getString("Y_GRID"));

			// 0 : 신규, 1 : 접수, 2 : 이관, 3 : 도착, 8 : 자동복구, 9 : 처리완료

			// 이관업체 스피너
			sp_company = (Spinner) findViewById(R.id.sp_company);

			company_arr =  new String[8];
			company_arr[0] = "장애(주간) : " + json.getJSONObject("dscOb").getJSONObject("info").getString("DAY_ADMIN_NM");
			company_arr[1] = "장애(야간) : " + json.getJSONObject("dscOb").getJSONObject("info").getString("NIGHT_ADMIN_NM");
			company_arr[2] = "회선 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("LINE_ENT_NM");
			company_arr[3] = "부스 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("BOOTH_MAKER_NM");
			company_arr[4] = "간판 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("SIGN_ENT_NM");
			company_arr[5] = "생산 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("ATM_MAKER_NM");
			company_arr[6] = "채널개발 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("ATM_SPIC_NM");
			company_arr[7] = "보안 : " + json.getJSONObject("dscOb").getJSONObject("info").getString("SECURE_ENT_NM");

			adpter_company = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, company_arr);

			sp_company.setAdapter(adpter_company);
			sp_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

					((EditText) findViewById(R.id.edt_result)).setText(company_arr[position]);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});

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

			dialog = new ProgressDialog(IssueDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(IssueDetailActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("gijun_il", dataVO.getDATA01());       // 장애발생일
					jsonSend.put("org_cd", dataVO.getDATA02());         // 기기번호
					jsonSend.put("down_si", dataVO.getDATA03());        // 장애발생시간
					jsonSend.put("down_gb", dataVO.getDATA04());        // 장애구분

					Log.e("상세 조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "dscOb");			// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("긴급 조회 결과 >>" , jsonRes.toString());

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



	// 조회된 결과 처리 메소드
	private void setResultView(){

		try {
			// 0 : 신규, 1 : 접수, 2 : 이관, 3 : 도착, 8 : 자동복구, 9 : 처리완료
			AlertDialog.Builder ab = new AlertDialog.Builder(this);

			// 시간변경이 아닌 경우 메세지 출력
			if (!time_change) {
				ab.setMessage(TR_NM.get(treat_status) + " 처리가 정상적으로 되었습니다.");
			}else{
				ab.setMessage("도착예정시간이 변경 되었습니다.");
			}
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

	// 도착예정시간 변경 Task
	private class UpdateTimeTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(IssueDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(IssueDetailActivity.this)) {

				return "net";

			}else{

				try {
					jsonSend = new JSONObject();

					String temp =  Integer.toString(treat_status);

					jsonSend.put("gijun_il", dataVO.getDATA01());       // 장애발생일
					jsonSend.put("org_cd", dataVO.getDATA02());         // 기기번호
					jsonSend.put("down_si", dataVO.getDATA03());        // 장애발생시간
					jsonSend.put("down_gb", dataVO.getDATA04());        // 장애구분
					jsonSend.put("treat_status", temp);    				// 처리상태구분
					jsonSend.put("p_arr_time", time_point);        		// 도착예정 시간
					jsonSend.put("change_p_arr_time", time_change);     // 도착시간 변경 플래그

					Log.e("도착 시간변경 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "arrival");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("도착 시간변경 결과 >>" , jsonRes.toString());

					if (jsonRes.getJSONObject("arrival").getString("code").equalsIgnoreCase("0")) {
						return "success";
					}else {
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
				Toast.makeText(ctx, getString(R.string.str_network_err), Toast.LENGTH_LONG).show();
			}// 에러 처리
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

	// 조치내역 입력 Task
	private class InputDataTask  extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;	// Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(IssueDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(IssueDetailActivity.this)) {

				return "net";

			}else{

				try {
					jsonSend = new JSONObject();

					String temp =  Integer.toString(treat_status);

					jsonSend.put("gijun_il", dataVO.getDATA01());       // 장애발생일
					jsonSend.put("org_cd", dataVO.getDATA02());         // 기기번호
					jsonSend.put("down_si", dataVO.getDATA03());        // 장애발생시간
					jsonSend.put("down_gb", dataVO.getDATA04());        // 장애구분
					jsonSend.put("treat_status", temp);  			    // 처리상태구분
					jsonSend.put("treat_step", treat_step);  	   		// 처리 찻수
					jsonSend.put("p_arr_time", time_point);        		// 도착예정 시간
					jsonSend.put("repair_reg_nm", user_info.getValue("USER_NM",""));// 접수자명

					// 접수, 도착의 상태구분 하여 접수메모 값 셋팅
					if (treat_status == 1 || treat_status == 3) {
						jsonSend.put("repair_memo", TR_NM.get(treat_status));// 접수메모
					}else{
						jsonSend.put("repair_memo", REPAIR_MEMO);
					}

					// 이미지가 첨부 되었을때 값 전송
					if(!FunctionClass.isNullOrBlank(strFileName)){
						jsonSend.put("photo_nm", strFileName);     			 // 이미지 파일이름
					}

					jsonSend.put("change_p_arr_time", time_change);      // 도착시간 변경 플래그

					Log.e("장애 접수 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "updateOb");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("장애 접수 결과 >>" , jsonRes.toString());

					if (jsonRes.getJSONObject("updateOb").getString("code").equalsIgnoreCase("0")) {

						// 완료나 이관일시
						if (treat_status == 9 || treat_status == 2) {
							strRepair_il = jsonRes.getJSONObject("updateOb").getString("repair_il");
							strRepair_si = jsonRes.getJSONObject("updateOb").getString("repair_si");
						}

						return "success";
					}else {
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
			} // 에러 처리
			else if (result.equalsIgnoreCase("fail")){

				Toast.makeText(ctx, getString(R.string.str_input_fail),Toast.LENGTH_LONG).show();

			} else {

				// 이미지가 첨부 되었을 때 이미지 전송 Task 실행
				if(!FunctionClass.isNullOrBlank(strFileName)){

					UploadImageTask uploadImageTask = new UploadImageTask();
					uploadImageTask.execute();
				}else{
					setResultView();
				}

			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if(dialog != null) dialog.dismiss();
		}
	}

	// 이미지 전송 Task
	private class UploadImageTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(IssueDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(IssueDetailActivity.this)) {

				return "net";

			}else{

				try {
					jsonSend = new JSONObject();

					jsonSend.put("gijun_il", dataVO.getDATA01()); // 장애발생일
					jsonSend.put("org_cd", dataVO.getDATA02());   // 기기번호
					jsonSend.put("down_si", dataVO.getDATA03());  // 장애발생시간
					jsonSend.put("down_gb", dataVO.getDATA04());  // 장애구분
					jsonSend.put("repair_il", strRepair_il);      // 조치일
					jsonSend.put("repair_si", strRepair_si);      // 조치시간
					jsonSend.put("photo_nm", strFileName);        // 사진이름
					jsonSend.put("original", origin_basr64);      // 원본이미지
					jsonSend.put("thumb", thum_base64);           // 썸네일 이미지
					jsonSend.put("type", "FAIL");     		      // 장애와 상태관리 구분자 값: STATE : 상태관리, FAIL :  장애괸리

					Log.e("이미지 전송 params >>" , jsonSend.toString());

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_IMAGE_URL, jsonSend);

					Log.e("이미지 전송 결과 >>" , jsonRes.toString());

					if (jsonRes.getString("code").equalsIgnoreCase("0")) {
						return "success";
					}else {
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
			} // 에러 처리
			else if (result.equalsIgnoreCase("fail")){

				Toast.makeText(ctx, getString(R.string.str_input_fail),Toast.LENGTH_LONG).show();

			} else {
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


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == Activity.RESULT_OK) {

			switch (requestCode) {

				// 카메라, 앨범 연동
				case 1:
				case 2:
					String cameraPath = data.getStringExtra("filepath");
					uploadImageData(cameraPath);

					break;

			}
		}
	}

	// 이미지 업로드전 bitmap 생성 메소드
	// 여기수정
	public void uploadImageData(String cameraPath) {

		File file = new File(cameraPath);
		String filename = file.getName();
		Bitmap bmOrigin = null;

		long id = getDisplayNameToId(filename);

		if ( id == -1 ) {
			return ;
		}

		// 썸네일 만드는 로직
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inSampleSize = 2;
		Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, option);

		// 원본이미지 생성
		try {
			bmOrigin = BitmapFactory.decodeFile(file.getAbsolutePath());

		}catch (Exception e) {
		}

		// 원본과 썸네일 Base64 인코딩
		if (bmOrigin != null && bm != null ) {

			// 전송할 이미지 Base64로 인코딩
			origin_basr64 = getEncodeString(bmOrigin, 50);		// 원본이미지
			thum_base64 = getEncodeString(bm, 100);				// 썸네일 이미지

			// 전송할 파일이름 생성
			// 현재 날짜 + 실제 파일이름의 해쉬코드
			// ex) 20161014090000-1234567890.jpg
			strFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-"
			 + Integer.toString(filename.hashCode())
			 + ".jpg";

			// 원본이미지가 Base64 인코딩이 안되었을 때
			if ( origin_basr64 == null ) {
				Toast.makeText(ctx, "이미지 변환 중 오류가 발생했습니다.\n" +
						"해상도를 낮춰서 재시도 하시기 바랍니다." ,Toast.LENGTH_SHORT).show();
				return;
			}

			// 이미지 셋팅
			// 기존 코딩 삭제 JJH
			img_thum.setImageBitmap(bm);

		}
		else {
			Toast.makeText(ctx, "이미지 변환 중 오류가 발생했습니다.\n" +
					"해상도를 낮춰서 재시도 하시기 바랍니다." ,Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * DisplayName(파일이름)으로 id취득
	 * @return long 미디어스토어에있는 이미지의 아이디
	 */
	private long getDisplayNameToId(String filename) {
		long id = -1;
		String where = MediaStore.Images.Media.DISPLAY_NAME + " = '"+ filename + "'";
		Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, where, null, null); //사진 데이터 쿼리
		cursor.moveToFirst();
		if ( cursor.getCount() > 0 ) {
			do {
				id = cursor.getLong(cursor.getColumnIndexOrThrow(Images.Media._ID));
			} while(cursor.moveToNext());
		}
		return id;
	}

	// Base64 인코딩 메소드
	// Bitmap을 JPG 압축하고 ByteArrayOutputStream 객체에 담아
	// byte로 변경 후 Base64 인코딩
	String getEncodeString(Bitmap bm, int qualityPer) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, qualityPer, baos);

		byte[] b = baos.toByteArray();
		String base64 = null;
		try {
			base64 = Base64.encodeToString(b, Base64.DEFAULT);
		} catch(OutOfMemoryError e) {
			Log.e("getEncodeString() >> ", e.toString());
			base64 = null;
		}
		return base64;
	}
}