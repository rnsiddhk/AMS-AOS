package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CommonListItem;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.GpsInfo;
import com.cashnet.common.JsonClient;
import com.cashnet.common.NoticeAdapter;
import com.cashnet.common.SGPreference;
import com.cashnet.main.BaseActivtiy;
import com.google.android.map.GoogleMapViewer;
import com.jauker.widget.BadgeView;
import com.nhn.android.map.NMapViewer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * 메인 메뉴 화면
 * <p/>
 * see main_menu_view.xml
 *
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * @since 2016-08-18
 */
public class MenuActivity extends BaseActivtiy implements OnClickListener {

    // 긴급출동, 장애관리, 현장관리
    private Button btn_urgent, btn_issue, btn_site;

    // 상태관리, 집계관리, 주변기기, 메뉴
    private Button btn_status, btn_total, btn_local, btn_menu;

    // 긴급출동용 뱃지, 장애관리용 뱃지
    private BadgeView badge_ag, badge_is;

    private Context ctx;

    private ListView lv_notice;

    private NoticeAdapter adapter;

    private Intent intent;

    // 상단 타이틀바 객체
    private View top_view;

    private GpsInfo gps;

    private JSONObject jsonRes;

    private SGPreference user_info;

    // 월, 일 날짜 스트링
    private String sMonth = "",  sDay = "";

    // 달력 객체
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        setActivity(R.layout.main_menu_view);

        super.onCreate(savedInstanceState);

        ctx = this;

        initView();

        user_info = new SGPreference(ctx);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SearchTask searchTask = new SearchTask();
        searchTask.execute();

        setViewClear();
    }

    // 뷰 객체 생성
    // 긴급출동, 장애관리만 뱃지가 있음
    private void initView() {

        // 상단 타이틀바 가리기
        top_view = (View) findViewById(R.id.top);
        ((top_view).findViewById(R.id.rl_title_bar)).setVisibility(View.GONE);

        // 긴급출동 버튼 생성
        btn_urgent = (Button) findViewById(R.id.btn_urgent);
        btn_urgent.setOnClickListener(this);
        badge_ag = new BadgeView(this);
        badge_ag.setBackground(5, Color.RED);
        badge_ag.setTargetView(btn_urgent);

        // 장애관리 버튼 생성
        btn_issue = (Button) findViewById(R.id.btn_issue);
        btn_issue.setOnClickListener(this);
        badge_is = new BadgeView(this);
        badge_is.setBackground(5, Color.RED);
        badge_is.setTargetView(btn_issue);

        // 현장관리 버튼 생성
        btn_site = (Button) findViewById(R.id.btn_site);
        btn_site.setOnClickListener(this);

        // 상태관리 버튼 생성
        btn_status = (Button) findViewById(R.id.btn_status);
        btn_status.setOnClickListener(this);

        // 집계관리 버튼 생성
        btn_total = (Button) findViewById(R.id.btn_total);
        btn_total.setOnClickListener(this);

        // 주변기기 버튼 생성
        btn_local = (Button) findViewById(R.id.btn_local);
        btn_local.setOnClickListener(this);

        // 메뉴버튼
        btn_menu = (Button) findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(this);


        // 공지사항 리스트뷰 생성
        lv_notice = (ListView) findViewById(R.id.lv_notice);
        adapter = new NoticeAdapter(ctx, R.layout.notice_list_item);
        lv_notice.setAdapter(adapter);
        lv_notice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                intent = new Intent(ctx, NoticeDetailActivity.class);
                intent.putExtra("SEQ", adapter.getItem(position).getData(6) );

                // 긴급
                if(adapter.getItem(position).getData(0).equalsIgnoreCase("U")){
                    intent.putExtra("TYPE", "1");
                }// 전사
                else if (adapter.getItem(position).getData(1).equalsIgnoreCase("0")){
                    intent.putExtra("TYPE", "2");
                }// 지사
                else{
                    intent.putExtra("TYPE", "3");
                }
                startActivity(intent);
            }
        });

        // 공지사항 더보기 클릭이벤트
        ((TextView) findViewById(R.id.txt_more)).setOnClickListener(this);

        // 날짜 셋팅
        calendar = Calendar.getInstance();
        sMonth =  (calendar.get(Calendar.MONTH) + 1) < 10 ?  "0" +  (calendar.get(Calendar.MONTH) + 1):  (calendar.get(Calendar.MONTH) + 1)+ "";
        sDay = calendar.get(Calendar.DAY_OF_MONTH) < 10 ?  "0" + (calendar.get(Calendar.DAY_OF_MONTH)) : (calendar.get(Calendar.DAY_OF_MONTH)) + "";
        ((TextView)findViewById(R.id.txt_date)).setText(calendar.get(Calendar.YEAR) + "-" + sMonth + "-"  + sDay);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        // 상속받은 부모 클래스 버튼 사용
        super.onClick(v);

        switch (v.getId()) {

            // 긴급출동
            case R.id.btn_urgent:
				intent = new Intent(this, UrgentIssueActivity.class);
				intent.putExtra("ACT_NM", "URGENT");
                intent.putExtra("BADGE", Integer.parseInt(badge_ag.getText().toString().trim()));
				startActivity(intent);

//                Toast.makeText(this, "Service 시작", Toast.LENGTH_SHORT).show();
//                intent = new Intent(this, GpsInfo.class);
//                startService(intent);

                break;

            // 장애관리
            case R.id.btn_issue:
				intent = new Intent(this, UrgentIssueActivity.class);
				intent.putExtra("ACT_NM", "ISSUE");
                intent.putExtra("BADGE", Integer.parseInt(badge_is.getText().toString().trim()));
				startActivity(intent);

//                Toast.makeText(this, "Service 종료", Toast.LENGTH_SHORT).show();
//                intent = new Intent(this, GpsInfo.class);
//                stopService(intent);

                break;

            // 현장관리
            case R.id.btn_site:
                Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
//                SearchTask searchTask = new SearchTask();
//                searchTask.execute();
                intent = new Intent(this, NMapViewer.class);
                startActivity(intent);
                break;

            // 상태관리
            case R.id.btn_status:
//                Toast.makeText(this, getString(R.string.str_service_msg), Toast.LENGTH_SHORT).show();
                intent = new Intent(this, StatusMgrActivity.class);
                intent.putExtra("keyword", "");
                startActivity(intent);
                break;

            // 집계관리
            case R.id.btn_total:
                // SM, 개발, 일반
                if (user_info.getValue("USER_GRADE", "").equalsIgnoreCase("7") ||
                        user_info.getValue("USER_GRADE", "").equalsIgnoreCase("9") ||
                        user_info.getValue("USER_GRADE", "").equalsIgnoreCase("99")) {
                    Toast.makeText(this, getString(R.string.str_service_auth_msg), Toast.LENGTH_SHORT).show();
                }else{

                    intent = new Intent(this, TotalMgrActivity.class);
                    startActivity(intent);
                }

                break;

            // 주변기기
            case R.id.btn_local:
                intent = new Intent(this, GoogleMapViewer.class);
				intent.putExtra("ACT_GB", "PROXIMITY");
                startActivity(intent);
                break;

            // 공지사항 더보기
            case R.id.txt_more:
                intent = new Intent(this, NoticeListActivity.class);
                startActivity(intent);
                break;

        }
    }

    private void setData(JSONObject json){

        JSONArray jsonArray;

        try {

            // 긴급 뱃지 카운트 셋팅
            badge_ag.setText(json.getJSONObject("main").getString("URGENT_COUNT"));

            // 장애 뱃지 카운트 셋팅
            badge_is.setText(json.getJSONObject("main").getString("FAILURE_COUNT"));

            // 공지사항 내역 추출
            jsonArray = json.getJSONObject("main").getJSONArray("noticeList");

            // 아이템 내역 클리어
            if (adapter.mItems != null){
                adapter.mItems.clear();
            }

            for (int i = 0; i < jsonArray.length(); i++ ) {
                adapter.addItem(new CommonListItem( jsonArray.getJSONObject(i).getString("URGENT_YN"),   // U: 긴급, N: 일반
                        jsonArray.getJSONObject(i).getString("NOTICE_GB"),                               // 0: 전사, 1: 지사
                        jsonArray.getJSONObject(i).getString("TITLE"),                                   // 제목
                        jsonArray.getJSONObject(i).getString("USER_NM"),                                 // 작성자
                        jsonArray.getJSONObject(i).getString("REG_DATE"),                                // 작성일자
                        "조회:" + jsonArray.getJSONObject(i).getString("VIEW_CNT"),                      // 조회수
                        jsonArray.getJSONObject(i).getString("NOTICE_NO")) );                            // 순번
            }

            adapter.notifyDataSetChanged();


        }catch (Exception e){

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

            dialog = new ProgressDialog(MenuActivity.this);
            dialog.setMessage(getString(R.string.str_loading_msg));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... result) {
            // TODO Auto-generated method stub

            // 네트워크 연결이 안되었을 때
            if (!FunctionClass.getNetworkState(MenuActivity.this)) {

                return "net";

            }else{

                try {

                    jsonSend = new JSONObject();
                    jsonSend.put("user_id", user_info.getValue("USER_ID", ""));               // 사용자 ID
                    jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));         // 사용자 그룹
                    jsonSend.put("user_ent", user_info.getValue("USER_ENT", ""));             // 업체 코드
                    jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));         // 사용자 직책
                    jsonSend.put("branch_gb", user_info.getValue("BRANCH_GB", ""));           // 지사구분
                    jsonSend.put("startRow", "1");                                            // 시작행
                    jsonSend.put("endRow", "5");                                              // 끝행

                    Log.e("메인화면 조회 params >>" , jsonSend.toString());

                    jsonContain = new JSONObject();
                    jsonContain.put("method", "main");			// API 메소드 명
                    jsonContain.put("params", jsonSend);		// 파라메터

                    jsonArray = new JSONArray();
                    jsonArray.put(jsonContain);

                    // POST 방식 호출
                    jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

                    Log.e("메인화면 조회 결과 >>" , jsonRes.toString());

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
