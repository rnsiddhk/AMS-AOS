package com.cashnet.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.menu.CurrencyInfoActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 2차 패스워드 확인
 * see confirm_password_view.xml
 * @since 2016-08-22
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * */
public class ConfirmPassActivity extends Activity implements OnClickListener{

	// 세컨비밀번호, 기기번호, 사용자 아이디
	private String secPass = "", org_cd = "", user_id = "";

	private EditText edt_pwd;

	private Intent intent;

	private JSONObject jsonRes;

	private Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.confirm_password_view);

		ctx = this;

		// 기기번호
		org_cd = getIntent().getExtras().getString("ORG_CD", "");

		// 사용자 아이디
		user_id = getIntent().getExtras().getString("USER_ID", "");

		edt_pwd = (EditText) findViewById(R.id.edt_pwd);

		// 취소
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);

		// 확인
		((Button) findViewById(R.id.btn_confirm)).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {

			// 취소
			case R.id.btn_cancel:
				finish();
				break;

			// 확인
			case R.id.btn_confirm:

				if(!FunctionClass.isNullOrBlank(edt_pwd.getText().toString().trim())){

					secPass = edt_pwd.getText().toString().trim();

					SearchTask searchTask = new SearchTask();
					searchTask.execute();

				}else{
					Toast.makeText(this, "비밀번호를 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}

	// 결과 정리 메소드
	private void setResultView(){

		intent = new Intent(this, CurrencyInfoActivity.class);
		intent.putExtra("ORG_CD", org_cd);
		startActivity(intent);
		finish();
	}

	// 조회 Task
	private class SearchTask extends AsyncTask<String, Integer, String> {

		private ProgressDialog dialog;

		private JSONObject jsonSend;     // 사용자 정보 json
		private JSONObject jsonContain;    // Body Json
		private JSONArray jsonArray;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(ConfirmPassActivity.this);
			dialog.setMessage(getString(R.string.str_matching_pwd_msg));
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
			if (!FunctionClass.getNetworkState(ConfirmPassActivity.this)) {

				return "net";

			} else {

				try {

					jsonSend = new JSONObject();


					jsonSend.put("org_cd", org_cd);         // 기기번호
					jsonSend.put("user_id", user_id);       // 사용자 아이디
					jsonSend.put("second_pw", secPass);     // 세컨 패스워드
					jsonSend.put("job_type", "00");         // job 구분 = 00:시재정보 조회. 10:OPT조회

					Log.e("2차 패스워드 확인 params >>", jsonSend.toString());

					jsonContain = new JSONObject();
					jsonContain.put("method", "auth");            // API 메소드 명
					jsonContain.put("params", jsonSend);        // 파라메터

					jsonArray = new JSONArray();
					jsonArray.put(jsonContain);

					// POST 방식 호출
					jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

					Log.e("2차 패스워드 확인 결과 >>", jsonRes.toString());

					if (jsonRes.getJSONObject("auth").getString("code").equalsIgnoreCase("-2")) {
						return "fail";
					} else {
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

			if (dialog != null) dialog.dismiss();

			// 네트워크 연결상태 문제
			if (result.equalsIgnoreCase("net")) {
				Toast.makeText(ctx, getString(R.string.str_network_err), Toast.LENGTH_LONG).show();
			} // 에러 처리
			else if (result.equalsIgnoreCase("fail")) {
				Toast.makeText(ctx, getString(R.string.str_fail_match_pwd_msg), Toast.LENGTH_LONG).show();
			} else {

				// 성공시 데이터 결과 셋팅 및 처리
				setResultView();
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if (dialog != null) dialog.dismiss();
		}
	}
}