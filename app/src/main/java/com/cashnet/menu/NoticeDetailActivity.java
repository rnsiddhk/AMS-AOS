package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
 *  공지사항 상세
 *  see notice_detail_view
 * @since 2016-08-29
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * Created by JJH on 2016-08-29.
 */
public class NoticeDetailActivity extends BaseActivtiy implements View.OnClickListener{

    // 공지사항 순번, 조회 구분
    private String SEQ = "", str_gb = "";

    // 버튼 객체
    private Button btn_pre, btn_next;

    // JSON 결과 객체
    private JSONObject jsonRes;

    private Context ctx;

    private SGPreference user_info;

    private String type = "";

    // 0 : 이전, 1 : 다음, 2 : 현재
    private String search_gb = "2";

    // 호출 메소드 명
    private String METHOD = "dscNotice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.notice_detail_view);

        super.onCreate(savedInstanceState);

        ctx = this;

        user_info = new SGPreference(ctx);

        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        eventClick();
    }

    // 뷰 객체 생성
    private void initView() {

        // 타이틀바 이름 정하기
        ((TextView) (((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("공지사항 상세");

        // 순번
        SEQ = getIntent().getExtras().getString("SEQ", "");

        // 조회구분
        type = getIntent().getExtras().getString("TYPE", "");

        // 이전 공지사항
        btn_pre = (Button) findViewById(R.id.btn_pre);
        btn_pre.setOnClickListener(this);

        // 다음 공지사항
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
    }

    private void eventClick(){

        SearchTask searchTask = new SearchTask();
        searchTask.execute();
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()){

            // 이전 공지사항
            case R.id.btn_pre :
                search_gb = "0";
                METHOD = "preIdx";
                eventClick();
                break;

            // 다음 공지사항
            case  R.id.btn_next :
                search_gb = "1";
                eventClick();
                METHOD = "nextIdx";
                break;
        }
    }


    private void setData(JSONObject json){

        try {

            // 공지사항 제목
            ((TextView) findViewById(R.id.txt_notice_title)).setText("[" + json.getJSONObject(METHOD).getString("URGENT_YN") +"]"
                    + json.getJSONObject(METHOD).getString("TITLE"));

            // 조회 구분
            // 0 : 전체, 1 : 임직원, 2 : 업체
            if (json.getJSONObject(METHOD).getString("URGENT_YN").equalsIgnoreCase("0")){
                str_gb = "전체";
            }else if (json.getJSONObject(METHOD).getString("URGENT_YN").equalsIgnoreCase("1")){
                str_gb = "임직원";
            }else{
                str_gb = "업체";
            }
            // 조회 구분
            ((TextView) findViewById(R.id.txt_gb)).setText(" : " + str_gb);

            // 작성자
            ((TextView) findViewById(R.id.txt_writer)).setText(" : " + json.getJSONObject(METHOD).getString("USER_NM"));

            // 작성시간
            ((TextView) findViewById(R.id.txt_time)).setText(" : " + json.getJSONObject(METHOD).getString("REG_DATE"));

            // 내용
            ((TextView) findViewById(R.id.txt_contents)).setText(" : " + json.getJSONObject(METHOD).getString("CONTENTS"));


        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void setSeqData(JSONObject json){

        try {
            SEQ = json.getJSONObject(METHOD).getString("NOTICE_NO");

            METHOD = "dscNotice";
            search_gb = "2";
            eventClick();

        } catch (Exception e){

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

            dialog = new ProgressDialog(NoticeDetailActivity.this);
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
            if (!FunctionClass.getNetworkState(NoticeDetailActivity.this)) {

                return "net";

            }else{

                try {

                    jsonSend = new JSONObject();
                    jsonContain = new JSONObject();

                    jsonSend.put("notice_no", SEQ);        // 공지사항 순번

                    // 현재 페이지 공지사항 조회가 아닌경우
                    if (!search_gb.equalsIgnoreCase("2")) {

                        jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
                        jsonSend.put("search_type", type);           							 // 조회 구분
                        jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));        // 사용자 직책
                        jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB", ""));          // 지사구분

                    }

                    jsonContain.put("method", METHOD);
                    jsonContain.put("params", jsonSend);        // 파라메터

                    Log.e("공지사항 상세 조회 params >>", jsonSend.toString());
                    Log.e("공지사항 상세 조회 Contain >>", jsonContain.toString());

                    jsonArray = new JSONArray();
                    jsonArray.put(jsonContain);

                    // POST 방식 호출
                    jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

                    Log.e("공지사항 상세 조회 결과 >>" , jsonRes.toString());
                    Log.e("공지사항 상세 조회 결과2 >>" , FunctionClass.isNullOrBlank(jsonRes.getString(METHOD)) + "");

                    if (jsonRes == null || FunctionClass.isNullOrBlank(jsonRes.getString(METHOD))) {
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
                if (search_gb == "2"){
                    setData(jsonRes);
                } else {
                    setSeqData(jsonRes);
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
}
