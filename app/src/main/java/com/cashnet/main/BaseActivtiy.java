package com.cashnet.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cashnet.R;
import com.cashnet.common.ActionItemVertical;
import com.cashnet.common.QuickActionVertical;
import com.cashnet.menu.NoticeListActivity;
import com.cashnet.menu.SettingActivity;
import com.cashnet.menu.StatusMgrActivity;
import com.cashnet.menu.TotalMgrActivity;
import com.cashnet.menu.UrgentIssueActivity;
import com.google.android.map.GoogleMapViewer;

/**
 * @author JJH hirosi@bgf.co.kr
 * @since 2016.08.18
 * */
public class BaseActivtiy extends Activity implements OnClickListener{

//	public final static String SERVER_URL = "http://210.105.193.144:10004/BGFMams/api/processor/work.do";	// 테스트 서버(tweb)
//	public final static String SERVER_IMAGE_URL = "http://210.105.193.144:10004/BGFMams/api/common/upload.do";

	public final static String SERVER_URL = "http://210.105.193.181:10004/BGFMams/api/processor/work.do";
	public final static String SERVER_IMAGE_URL = "http://210.105.193.181:10004/BGFMams/api/common/upload.do";

	private Button btn_back, btn_menu, btn_search;

	private ToggleButton btn_find;

	private EditText edt_search_key;

	private int resId;

	private Intent intent;

	// DropDown 객체
	private ActionItemVertical[] menuItem;

	// DropDown String 배열
	private String[] MENU_GUBUN;	// 메뉴선택

	private QuickActionVertical qa;

	private Dialog dialog;

	// xml  뷰 셋팅 메소드
	protected void setActivity(int resId) {
		this.resId = resId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(resId);

		initUI();
	}

	// 뷰 객체 생성
	protected void initUI(){

		// back
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		// 찾기
		btn_find = (ToggleButton) findViewById(R.id.btn_find);
		btn_find.setOnClickListener(this);
		btn_find.setBackgroundResource(R.drawable.icon_search);

		// 검색
		btn_search = (Button) findViewById(R.id.btn_search);
		btn_search.setOnClickListener(this);

		// 전체 메뉴
		btn_menu = (Button) findViewById(R.id.btn_menu);
		btn_menu.setOnClickListener(this);

		edt_search_key = (EditText) findViewById(R.id.edt_search_key);

		MENU_GUBUN = new String[7];
		MENU_GUBUN[0] = "공지사항";
		MENU_GUBUN[1] = "긴급출동";
		MENU_GUBUN[2] = "장애관리";
		MENU_GUBUN[3] = "현장관리";
		MENU_GUBUN[4] = "상태관리";
		MENU_GUBUN[5] = "집계관리";
		MENU_GUBUN[6] = "주변기기";
	}

	// 화면 초기화
	protected void setViewClear(){

		btn_find.setChecked(false);
		edt_search_key.setText("");
		((RelativeLayout) findViewById(R.id.rl_search_key)).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

			// 로그아웃
			case R.id.btn_logout:
				Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show();
				break;

			// 콜센터
			case R.id.btn_call:
				showDialog();
				break;

			// 셋팅
			case R.id.btn_setting:
				Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
//				intent = new Intent(this, SettingActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
				break;

			// TOP
			case R.id.btn_top:
				Toast.makeText(this, "TOP", Toast.LENGTH_SHORT).show();
				break;

			// back
			case R.id.btn_back:
				finish();
				break;

			// 찾기
			case R.id.btn_find:

				if (btn_find.isChecked()) {
					((RelativeLayout) findViewById(R.id.rl_search_key)).setVisibility(View.VISIBLE);
				}else{
					((RelativeLayout) findViewById(R.id.rl_search_key)).setVisibility(View.GONE);
				}
				break;

			// 찾기
			case R.id.btn_search:
				intent = new Intent(this, StatusMgrActivity.class);
				intent.putExtra("keyword", edt_search_key.getText().toString().trim());
				startActivity(intent);
				break;


			// 메뉴
			case R.id.btn_menu:

				menuItem = new ActionItemVertical[MENU_GUBUN.length];
				for (int i = 0; i < MENU_GUBUN.length; i++) {

					menuItem[i] = new ActionItemVertical();
					final int nSEQ = i;

					menuItem[i].setTitle(MENU_GUBUN[i]);
					menuItem[i].setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							qa.dismiss();

							switch (nSEQ) {
								// 공지사항
								case 0:
									intent = new Intent(BaseActivtiy.this, NoticeListActivity.class);
									startActivity(intent);
									break;

								// 긴급출동
								case 1:
									intent = new Intent(BaseActivtiy.this, UrgentIssueActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("ACT_NM", "URGENT");
									startActivity(intent);
									break;

								// 장애관리
								case 2:
									intent = new Intent(BaseActivtiy.this, UrgentIssueActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("ACT_NM", "ISSUE");
									startActivity(intent);
									break;

								// 현장관리
								case 3:
									Toast.makeText(BaseActivtiy.this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
									break;

								// 상태관리
								case 4:
									intent = new Intent(BaseActivtiy.this, StatusMgrActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("keyword", "");
									startActivity(intent);
									break;

								// 집계관리
								case 5:
									intent = new Intent(BaseActivtiy.this, TotalMgrActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									break;

								// 주변기기
								case 6:
									intent = new Intent(BaseActivtiy.this, GoogleMapViewer.class);
									intent.putExtra("ACT_GB", "PROXIMITY");
									startActivity(intent);
									break;
							}
						}
					});
				}
				qa = new QuickActionVertical(v);

				for (int j = 0; j < menuItem.length; j++) {
					qa.addActionItem(menuItem[j]);
				}
				qa.show();

				break;
		}
	}

	// 콜센터 팝업 띄우기
	private void showDialog(){

		// 팝업 레이아웃 커스텀
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dailog_two_btn, null);

		// 레이아웃 속 버튼 사용
		TextView tvTitle = (TextView) layout.findViewById(R.id.title);
		tvTitle.setText("콜센터");

		TextView tvMsg = (TextView) layout.findViewById(R.id.msg);
		tvMsg.setText("080-330-2114");
		tvMsg.setText(Html.fromHtml("<u>" + "080-330-2114" + "</u>"));	// 텍스트에 밑줄 긋기

		// 버튼 이벤트 등록
		Button confirm = (Button) layout.findViewById(R.id.btnConfirm);
		confirm.setText("연결하기");
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:0803302114")));

			}
		});

		Button cancel = (Button) layout.findViewById(R.id.btnCancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dialog.setContentView(layout);
		dialog.show();
	}
}
