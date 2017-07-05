package com.cashnet.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.LockableScrollView;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 상태관리 상세
 *
 * see status_mgr_detail_view.xml
 * @since 2016-08-23
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class StatusMgrDetailActivity extends BaseActivtiy implements OnClickListener {

	// 이미지 정보 VO 객체
	private BasicVO imageVO = null;

	// 임시 저장 VO 객체
	private BasicVO tempV0 = null;

	// 이미지 이름 map
	private HashMap<Integer, String> imgNmMap = new HashMap<>();

	// 이미지 정보 map
	private HashMap<Integer, BasicVO> imageMap = new HashMap<>();

	private Context ctx;

	// 타이틀 바 객체
	private View top_view;

	private Intent intent;

	private Dialog dialog;

	// 객체 리소스 아이디 저장 변수
	private int btnId = 0, imgId = 0;

	// 잠금 스크롤 뷰
	private LockableScrollView scroll;

	// 특이사항 스피너
	private Spinner spinner;

	private String[] strTag ={"기기명", "기기번호", "기기등급", "발생시간", "기기상태", "지사", "기기위치"};

	// 스피너 사용항 아이템
	private String[] strArr ={"선택", "간판점검", "부스청소", "기기청소", "누진점검", "기기내부청소", "기타"};

	// 특이사항 스피너 어댑터
	private SpinnerAdapter sp_adpter;

	private LinearLayout layout_fir;

	private JSONObject jsonRes;

	// 기기번호
	private String ORG_CD = "";

	// 조회 Task
	private SearchTask searchTask;

	private BasicVO basicVO;

	// 사용자 정보
	private SGPreference user_info;

	// 점검사항 유형
	private String check_case = "";

	// 점검사항 메모
	private String repair_memo = "";

	private int[] gradeId = new int[] { R.drawable.icon_grade_0, R.drawable.icon_grade_1, R.drawable.icon_grade_2, R.drawable.icon_grade_3, R.drawable.icon_grade_4,
			R.drawable.icon_grade_5, R.drawable.icon_grade_6, R.drawable.icon_grade_7, R.drawable.icon_grade_8, R.drawable.icon_grade_9,
			R.drawable.icon_grade_10, R.drawable.icon_grade_11 };

	private int[] btnImgId = new int[] { R.id.btn_picture1, R.id.btn_picture2, R.id.btn_picture3, R.id.btn_picture4, R.id.btn_picture5, R.id.btn_picture6,
			R.id.btn_picture7, R.id.btn_picture8 };

	// 경도, 위도 정보
	private Double xGrid, yGrid;

	// 첨부된 이미지 카운트
	private int imgCnt = 0;

	// 총 이미지 수
	private int IMG_CNT = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		setActivity(R.layout.status_mgr_detail_view);

		super.onCreate(savedInstanceState);

		ctx = this;

		user_info = new SGPreference(ctx);

		initView();

		ORG_CD = getIntent().getExtras().getString("ORG_CD", "");

	}

	@Override
	protected void onResume() {
		super.onResume();

		searchTask = new SearchTask();
		searchTask.execute();
	}

	private void initView(){

		// 상단 타이틀바
		top_view = (View) findViewById(R.id.top);
		((TextView)((top_view).findViewById(R.id.txt_title))).setText("상태관리 상세");

		// 이미지 타입 맵(이미지 이름과 같음)
		imgNmMap.put(R.id.btn_picture1, "01");
		imgNmMap.put(R.id.btn_picture2, "02");
		imgNmMap.put(R.id.btn_picture3, "03");
		imgNmMap.put(R.id.btn_picture4, "04");
		imgNmMap.put(R.id.btn_picture5, "05");
		imgNmMap.put(R.id.btn_picture6, "06");
		imgNmMap.put(R.id.btn_picture7, "07");
		imgNmMap.put(R.id.btn_picture8, "08");

		scroll = (LockableScrollView) findViewById(R.id.scroll);

		layout_fir = (LinearLayout) findViewById(R.id.ll_list);

		// 사용하는 버튼 이벤트 등록
		((Button) findViewById(R.id.btn_map)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_route)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_dev_info)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_issue_list)).setOnClickListener(this);


		// 시재정보 열람 불가 사용자 직책
		// 7:SM, 9:개발, 99:일반
		if (user_info.getValue("USER_GRADE","").equalsIgnoreCase("7") ||
				user_info.getValue("USER_GRADE","").equalsIgnoreCase("9") ||
				user_info.getValue("USER_GRADE","").equalsIgnoreCase("99")) {
			((Button) findViewById(R.id.btn_currency_info)).setVisibility(View.GONE);
		}else {
			((Button) findViewById(R.id.btn_currency_info)).setOnClickListener(this);
		}

		((Button) findViewById(R.id.btn_issue)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_urgent)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_regist)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);

		// 사진첨부 버튼 이벤트 등록
		((Button) findViewById(R.id.btn_picture1)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture2)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture3)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture4)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture5)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture6)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture7)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_picture8)).setOnClickListener(this);


		// 스크롤 잠금 기능
		((EditText) findViewById(R.id.edt_bigo)).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				if (v.getId() == R.id.edt_bigo) {
					scroll.setEnableScrolling(false);
				}else{
					scroll.setEnableScrolling(true);
				}
				return false;
			}
		});

		// 점검유형 스피너
		spinner = (Spinner) findViewById(R.id.sp_gb);

		// 스피너 어댑터 생성, 어댑터 셋팅, 이벤트 등록
		sp_adpter = new SpinnerAdapter(ctx, android.R.layout.simple_expandable_list_item_1, strArr);
		spinner.setAdapter(sp_adpter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View v, int position, long arg3) {
				// TODO Auto-generated method stub

				check_case = Integer.toString(position);

				if (0 < position) {
					((EditText) findViewById(R.id.edt_bigo)).setText(strArr[position]);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);

		switch (v.getId()) {

			// 지도보기
			case R.id.btn_map:
				intent = new Intent(ctx, GoogleMapViewer.class);
				intent.putExtra("ACT_GB", "POINT");
				intent.putExtra("X_GRID", Double.parseDouble(basicVO.getDATA03()));
				intent.putExtra("Y_GRID", Double.parseDouble(basicVO.getDATA04()));
				intent.putExtra("ATM_NAME", basicVO.getDATA02());
				intent.putExtra("ORG_CD", ORG_CD);
				startActivity(intent);
				break;

			// 경로보기
			case R.id.btn_route:
				if (xGrid != 0.0 && yGrid != 0.0) {
					intent = new Intent(ctx, GoogleMapViewer.class);
					intent.putExtra("ACT_GB", "ROUTE");
					intent.putExtra("X_GRID", Double.parseDouble(basicVO.getDATA03()));
					intent.putExtra("Y_GRID", Double.parseDouble(basicVO.getDATA04()));
					intent.putExtra("ATM_NAME", basicVO.getDATA02());
					startActivity(intent);
				}else{
					Toast.makeText(ctx, getString(R.string.str_no_code_msg), Toast.LENGTH_SHORT).show();
				}
				break;

			// 기기정보
			case R.id.btn_dev_info:
				intent = new Intent(ctx, MachineInfoActivity.class);
				intent.putExtra("ORG_CD", ORG_CD);
				startActivity(intent);
				break;

			// 장애이력
			case R.id.btn_issue_list:
				intent = new Intent(ctx, IssueHistoryActivity.class);
				intent.putExtra("ORG_CD", ORG_CD);
				startActivity(intent);
				break;

			// 시재정보
			case R.id.btn_currency_info:

				intent = new Intent(ctx, ConfirmPassActivity.class);
				intent.putExtra("ORG_CD", ORG_CD);
				intent.putExtra("USER_ID", user_info.getValue("USER_ID", ""));
				startActivity(intent);

				break;

			// 장애관리
			case R.id.btn_issue:
				intent = new Intent(this, UrgentIssueActivity.class);
				intent.putExtra("ACT_NM", "ISSUE");
				intent.putExtra("BADGE", Integer.parseInt(basicVO.getDATA05()));
				intent.putExtra("ORG_CD", ORG_CD);
				startActivity(intent);
				break;

			// 긴급출동
			case R.id.btn_urgent:
				intent = new Intent(this, UrgentIssueActivity.class);
				intent.putExtra("ACT_NM", "URGENT");
				intent.putExtra("BADGE", Integer.parseInt(basicVO.getDATA06()));
				intent.putExtra("ORG_CD", ORG_CD);
				startActivity(intent);
				break;

			// 사진첨부
			case R.id.btn_picture1:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img1;
				btnId = R.id.btn_picture1;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture1","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture1","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture1","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture1","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture2:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img2;
				btnId = R.id.btn_picture2;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture2","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture2","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture2","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture2","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture3:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img3;
				btnId = R.id.btn_picture3;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture3","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture3","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture3","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture3","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture4:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img4;
				btnId = R.id.btn_picture4;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture4","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture4","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture4","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture4","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture5:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img5;
				btnId = R.id.btn_picture5;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture5","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture5","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture5","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture5","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture6:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img6;
				btnId = R.id.btn_picture6;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture6","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture6","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture6","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture6","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture7:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img7;
				btnId = R.id.btn_picture7;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture7","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture7","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture7","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture7","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 사진첨부
			case R.id.btn_picture8:
//				Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
				imgId = R.id.img8;
				btnId = R.id.btn_picture8;

				// Map에서 데이터 추출
				tempV0 = imageMap.get(btnId);
				Log.e("btn_picture8","imageMap에 데이터 추출 " + imageMap.get(btnId));

				// 맵에 저장된 데이터 확인 로직
				if (tempV0 != null) {

					((ImageView) findViewById(imgId)).setVisibility(View.GONE);
					((ImageView) findViewById(imgId)).setImageBitmap(null);
					((Button) findViewById(btnId)).setText("사진첨부");

					Log.e("btn_picture8","imageMap에 처음 데이터 삭제 전 " + tempV0.getDATA01());

					imageMap.put(btnId, null);
					imgCnt--;

					Log.e("btn_picture8","imageMap에 처음 데이터 삭제 후 " + imageMap.get(btnId));
					Log.e("imageMap 사이즈 >> ",imageMap.size() + "");
					Log.e("imgCnt >> ", imgCnt + "");

				}else{
					Log.e("btn_picture8","imageMap에 처음 데이터 쓸 경우");
					showDialog();
				}

				break;

			// 특이사항 등록
			case R.id.btn_regist:

				if (!check_case.equalsIgnoreCase("0")) {
					// 특이사항 입력 공백 값 체크
					if (!FunctionClass.isNullOrBlank(((EditText) findViewById(R.id.edt_bigo)).getText().toString().trim())) {
						repair_memo = ((EditText) findViewById(R.id.edt_bigo)).getText().toString().trim();
						InputDataTask inputDataTask = new InputDataTask();
						inputDataTask.execute();
					} else {
						Toast.makeText(ctx, getString(R.string.str_input_issue_msg), Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(ctx, "점검유형을 선택하여 주십시오.", Toast.LENGTH_SHORT).show();
				}
				break;

			// 취소
			case R.id.btn_cancel:

				break;
		}
	}

	// 조회된 결과 처리 메소드
	private void setData(JSONObject json){

		int grade = 0;

		try {

			String [] temp = new String[json.getJSONObject("dscState").length()];

			basicVO = new BasicVO();
			basicVO.setDATA01(json.getJSONObject("dscState").getString("ORG_CD"));	// 기기번호
			basicVO.setDATA02(json.getJSONObject("dscState").getString("ATM_NM"));	// 기기명
			basicVO.setDATA03(json.getJSONObject("dscState").getString("X_GRID"));	// 경도
			basicVO.setDATA04(json.getJSONObject("dscState").getString("Y_GRID"));	// 위도
			basicVO.setDATA05(json.getJSONObject("dscState").getString("FAILURE_COUNT"));	// 장애 건수
			basicVO.setDATA06(json.getJSONObject("dscState").getString("URGENT_COUNT"));	// 긴급출동 건수

			temp[0] = json.getJSONObject("dscState").getString("ATM_NM");
			temp[1] = json.getJSONObject("dscState").getString("ORG_CD");
			temp[2] = json.getJSONObject("dscState").getString("ATM_GRADE");		// 기기등급
			temp[3] = json.getJSONObject("dscState").getString("DOWN_TIME");		// 발생시간
			temp[4] = json.getJSONObject("dscState").getString("DOWN_ATM_NM");		// 기기상태
			temp[5] = json.getJSONObject("dscState").getString("BRANCH_GB_NM");		// 지사
			temp[6] = json.getJSONObject("dscState").getString("ADDR_RMK");			// 기기 우치

			// 경도, 위도
			xGrid = Double.parseDouble(json.getJSONObject("dscState").getString("X_GRID"));
			yGrid = Double.parseDouble(json.getJSONObject("dscState").getString("Y_GRID"));

			// 정보를 표현하는 레이아웃 부분에
			// 자식 객체가 있을 경우 다시 생성하기 위해 삭제처리
			if (0 < layout_fir.getChildCount()){
				layout_fir.removeAllViews();
			}

			int px = (int)ScreenUtil.convertDpToPixel(30f, this);
			int px_txt = (int)ScreenUtil.convertDpToPixel(25f, this);

			// 기기등급 이미지 치환
			grade = Integer.parseInt(temp[2]);
			LinearLayout.LayoutParams txt_1_params;
			LinearLayout.LayoutParams txt_2_params;

			for (int i = 0; i < strTag.length; i++) {

				LinearLayout ll_contain = new LinearLayout(this);
				LinearLayout.LayoutParams params =
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, px*2, 0);

				params.setMargins(5, 15, 5, 5);

				TextView txt_1 = new TextView(this);
				txt_1.setText(strTag[i]);
				txt_1.setGravity(Gravity.CENTER);
				txt_1.setTextSize(18f);
				txt_1.setBackgroundResource(R.drawable.gradient_gray_linear);

				TextView txt_2 = new TextView(this);

				txt_1_params = new LinearLayout.LayoutParams(0, px, 1);
				txt_1_params.setMargins(5, 0, 0, 0);

				txt_2.setTextSize(18f);
				txt_2.setGravity(Gravity.LEFT);

				// 기기등급 이미지 표시
				if (i == 2){
					txt_2.setBackgroundResource(gradeId[grade]);

					txt_2_params = new LinearLayout.LayoutParams(px_txt, px_txt, 0);
					txt_2_params.setMargins(10, 0, 0, 0);

					TextView txt_3 = new TextView(this);
					LinearLayout.LayoutParams txt_3_params = new LinearLayout.LayoutParams(0, px, 3);

					ll_contain.addView(txt_1, txt_1_params);
					ll_contain.addView(txt_2, txt_2_params);
					ll_contain.addView(txt_3, txt_3_params);
					Button btn = new Button(this);


				}else{
					txt_2.setText(temp[i]);

					txt_2_params = new LinearLayout.LayoutParams(0, px*2, 3);
					txt_2_params.setMargins(10, 0, 0, 0);

					ll_contain.addView(txt_1, txt_1_params);
					ll_contain.addView(txt_2, txt_2_params);
				}

				layout_fir.addView(ll_contain, params);
			}

			((TextView)findViewById(R.id.txt_ur_is_cnt)).setText("장애 " + basicVO.getDATA05() + "건, "
			+ "긴급 " + basicVO.getDATA06() + "건");

			((TextView) findViewById(R.id.txt_data1)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_01")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data2)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_02")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data3)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_03")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data4)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_04")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data5)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_05")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data6)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_06")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data7)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_07")) ? "등록":"미등록");
			((TextView) findViewById(R.id.txt_data8)).setText(!FunctionClass.isNullOrBlank(json.getJSONObject("dscState").getString("IMG_REG_DATE_08")) ? "등록":"미등록");

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String> {

		private JSONObject jsonSend;

		private JSONArray jsonArray;

		private JSONObject jsonContain;

		private String url = "";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrDetailActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("org_cd", ORG_CD);      	     // 기기번호

					Log.e("상태관리 상세조회 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "dscState");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("상태관리 상세조회 결과 >>" , jsonRes.toString());

					if (jsonRes == null) {
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

			} // 결과 없음
			else if (result.equalsIgnoreCase("null")){

			} // 에러 처리
			else if (result.equalsIgnoreCase("fail")){

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

	// 특이사항 입력 결과 처리 메소드
	private void setResultView(){

		try {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setMessage("특이사항 내용이 등록 되었습니다.");
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

	// 특이사항 입력 Task
	private class InputDataTask extends AsyncTask<String, Integer, String> {

		private JSONObject jsonSend;

		private JSONArray jsonArray;

		private JSONObject jsonContain;

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrDetailActivity.this)) {

				return "net";

			}else{

				try {

					jsonSend = new JSONObject();

					jsonSend.put("org_cd", ORG_CD);      		  								// 기기번호
					jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB",""));      		// 지사구분
					jsonSend.put("check_case", check_case);      								// 특이사항 케이스
					jsonSend.put("repair_memo", repair_memo);      								// 특이사항 내용
					jsonSend.put("photo_nm", "");      		  									// 사진명
					jsonSend.put("repair_reg_nm", user_info.getValue("USER_ID",""));            // 작성자 아이디

					Log.e("특이사항 입력 params >>" , jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "saveIssue");		// API 메소드 명
					jsonContain.put("params", jsonSend);		// 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("특이사항 입력 결과 >>" , jsonRes.toString());

					if (jsonRes.getJSONObject("saveIssue").getString("code").equalsIgnoreCase("0")) {
						return "success";
					}else{

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

				// 첨부된 이미지가 있을 경우 이미지 Task 실행
				// 없으면 결과 정리 화면
				if (0 < imgCnt){
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

		private JSONObject jsonSend;     // 정보 json
		private JSONObject jsonImg;		 // 이미지 상세정보
		private JSONArray jsonArray;	 // 이미지 정보

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(StatusMgrDetailActivity.this);
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
			if (!FunctionClass.getNetworkState(StatusMgrDetailActivity.this)) {

				return "net";

			}else{

				try {
					jsonSend = new JSONObject();

					jsonArray = new JSONArray();

					jsonSend.put("org_cd", ORG_CD);   // 기기번호
					jsonSend.put("type", "STATE");    // 장애와 상태관리 구분자 값: STATE : 상태관리, FAIL :  장애괸리
					jsonSend.put("img_reg_user", user_info.getValue("USER_ID",""));    // 작성자 아이디

					for (int i = 0; i < IMG_CNT; i++) {

						// 여기수정
						jsonImg = new JSONObject();
						tempV0 = imageMap.get(btnImgId[i]);

						if (tempV0 != null){
							jsonImg.put("original", tempV0.getDATA01());		// 원본이미지
							jsonImg.put("thumb", tempV0.getDATA02());			// 썸네일 이미지
							jsonImg.put("img_type", tempV0.getDATA03());		// 이미지 타입 (이미지 이름)
							jsonArray.put(jsonImg);
						}
					}

					jsonSend.put("file", jsonArray);

					Log.e("이미지 전송 갯수 >> " , jsonArray.length() + "개");

					Log.e("이미지 전송 params >> " , jsonSend.toString());

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_IMAGE_URL, jsonSend);

					Log.e("이미지 전송 결과 >> " , jsonRes.toString());

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

	// 카메라, 사진첩 사용할 다이얼로그 팝업
	private void showDialog(){

		ListClickEvent event = new ListClickEvent();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.selected_picture, null);

		TextView tvTitle = (TextView) layout.findViewById(R.id.title);
		tvTitle.setText("사진등록");

		Button confirm = (Button) layout.findViewById(R.id.confirm);
		confirm.setVisibility(View.GONE);

		Button cancel = (Button) layout.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		LinearLayout listLayout = (LinearLayout) layout.findViewById(R.id.list);
		String[] array = getResources().getStringArray(R.array.camera);

		for (int i = 0; i < array.length; i++) {

			Button btn = new Button(this);
			btn.setText(array[i]);
			btn.setBackgroundResource(R.drawable.login_btn_select);
			btn.setGravity(Gravity.CENTER);
			btn.setTextColor(Color.WHITE);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);

			int px = (int)ScreenUtil.convertDpToPixel(8f, this);

			params.bottomMargin = px;

			event.add(btn);

			listLayout.addView(btn, params);
		}

		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dialog.setContentView(layout);
		dialog.show();
	}

	private class ListClickEvent implements OnClickListener {
		private ArrayList<View> array;

		protected ListClickEvent() {
			this.array = new ArrayList<View>();
		}

		public void add(View view) {
			view.setOnClickListener(this);
			array.add(view);
		}

		@Override
		public void onClick(View v) {
			for (int i = 0; i < array.size(); i++) {

				View seleted = array.get(i);
				// 2016-08-17 JJH 플러그인 사용하는 부분 막음
				if (seleted == v) {
					if (i == 0) {
						Intent intent = new Intent(ctx, CameraView.class);
						startActivityForResult(intent, 1);
						dialog.dismiss();

					} else if (i == 1) {
						Intent intent = new Intent(ctx, AlbumView.class);
						startActivityForResult(intent, 2);
						dialog.dismiss();
					}

				}
			}
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == Activity.RESULT_OK) {

			switch (requestCode) {

				// 카메라, 사진첩 연동
				case 1:
				case 2:
					String cameraPath = data.getStringExtra("filepath");
					uploadImageData(cameraPath);
					break;

			}
		}
	}

	public void uploadImageData(String cameraPath) {

		File file = new File(cameraPath);
		String filename = file.getName();
		Bitmap bmOrigin = null;

		imageVO = new BasicVO();

		long id = getDisplayNameToId(filename);
		if ( id == -1 ) {
			return ;
		}
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inSampleSize = 2;
		Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, option);

		// 원본이미지 생성
		try {
			bmOrigin = BitmapFactory.decodeFile(file.getAbsolutePath());
			Log.e("이미지 path >>", file.getAbsolutePath());

		}catch (Exception e) {
		}

		if ( bmOrigin != null || bm != null ) {

			// 전송할 이미지 Base64로 인코딩

			imageVO.setDATA01(getEncodeString(bmOrigin, 50));	// 원본이미지
			imageVO.setDATA02(getEncodeString(bm, 100));		// 썸네일 이미지
			imageVO.setDATA03(imgNmMap.get(btnId));				// 이미지 타입(이름)

			Log.e("업로드할 이미지 타입 >> ", imageVO.getDATA03());


			if (FunctionClass.isNullOrBlank(imageVO.getDATA01())) {
				Toast.makeText(ctx, "이미지 변환 중 오류가 발생했습니다.\n" +
						"해상도를 낮춰서 재시도 하시기 바랍니다." ,Toast.LENGTH_SHORT).show();
				return;
			}

			imageMap.put(btnId, imageVO);
			imgCnt++;

			Log.e("이미지 첨부 갯수 >> ", imgCnt + "개");

			// 이미지 셋팅
			// 기존 코딩 삭제 JJH
			((ImageView) findViewById(imgId)).setVisibility(View.VISIBLE);
			((ImageView) findViewById(imgId)).setImageBitmap(bm);

			((Button) findViewById(btnId)).setText("삭제");

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

	String getEncodeString(Bitmap bm, int qualityPer) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, qualityPer, baos);

		byte[] b = baos.toByteArray();
		String base64 = "";
		try {
			base64 = Base64.encodeToString(b, Base64.DEFAULT);
			Log.e("이미지 인코딩 ", "길이" + base64.length());
		} catch(OutOfMemoryError e) {
			Log.e("이미지 인코딩 ", e.toString());
			base64 = null;
		}
		return base64;
	}
}