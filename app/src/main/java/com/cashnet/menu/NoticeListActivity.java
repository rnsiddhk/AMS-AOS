package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CommonListItem;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.common.NoticeAdapter;
import com.cashnet.common.SGPreference;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 공지사항
 * see notice_list_view.xml
 * @since 2016-08-29
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 */
public class NoticeListActivity extends BaseActivtiy implements View.OnClickListener {

    private Context ctx;

    private JSONObject jsonRes;

    private ListView lv_notice;

    private NoticeAdapter adapter;

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

    private SGPreference user_info;

    // 자동조회 플래그
    private static boolean flag = true;

    private SearchTask searchTask;

    // 조회 타입 1 : 긴급, 2 : 전사,  3 :지사
    private String type = "1";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.notice_list_view);

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
    private void initView() {


        // 타이틀바 이름 정하기
        ((TextView) (((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("공지사항");

        // 긴급
        ((Button) findViewById(R.id.btn_01)).setOnClickListener(this);

        // 전사
        ((Button) findViewById(R.id.btn_02)).setOnClickListener(this);

        // 자사
        ((Button) findViewById(R.id.btn_03)).setOnClickListener(this);

        // 공지사항 리스트뷰 생성
        lv_notice = (ListView) findViewById(R.id.lv_notice);
        adapter = new NoticeAdapter(ctx, R.layout.notice_list_item);
        lv_notice.setAdapter(adapter);
        lv_notice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent = new Intent(NoticeListActivity.this, NoticeDetailActivity.class);
                intent.putExtra("SEQ", adapter.getItem(position).getData(6));
                intent.putExtra("TYPE", type);
                startActivity(intent);
            }
        });

        footer = getLayoutInflater().inflate(R.layout.footer_view, null, false);

    }

    // 버튼 글자색, 바탕색 변경
    private void setButton() {

        ((Button) findViewById(R.id.btn_01)).setTextColor(Color.WHITE);
        ((Button) findViewById(R.id.btn_01)).setBackgroundResource(R.color.Gray);
        ((Button) findViewById(R.id.btn_02)).setTextColor(Color.WHITE);
        ((Button) findViewById(R.id.btn_02)).setBackgroundResource(R.color.Gray);
        ((Button) findViewById(R.id.btn_03)).setTextColor(Color.WHITE);
        ((Button) findViewById(R.id.btn_03)).setBackgroundResource(R.color.Gray);

    }

    // 자동 조회 하기 위한 메소드
    private void eventClick(){

        Log.e("eventClick?", "footer 삭제");
        top = true;
        PAGE_START =  1;
        PAGE_END =  25;

        if(lv_notice.getFooterViewsCount() == 1){
            Log.e("btn_seacrh 이거 삭제 됨?", "footer 삭제");
            lv_notice.removeFooterView(footer);
        }

        searchTask =  new SearchTask();
        searchTask.execute();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

            // 긴급
            case R.id.btn_01:
                setButton();
                ((Button) findViewById(R.id.btn_01)).setTextColor(Color.BLACK);
                ((Button) findViewById(R.id.btn_01)).setBackgroundResource(R.color.White);
                type = "1";
                eventClick();
                break;

            // 전사
            case R.id.btn_02:
                setButton();
                ((Button) findViewById(R.id.btn_02)).setTextColor(Color.BLACK);
                ((Button) findViewById(R.id.btn_02)).setBackgroundResource(R.color.White);
                type = "2";
                eventClick();
                break;

            // 지사
            case R.id.btn_03:
                setButton();
                ((Button) findViewById(R.id.btn_03)).setTextColor(Color.BLACK);
                ((Button) findViewById(R.id.btn_03)).setBackgroundResource(R.color.White);
                type = "3";
                eventClick();
                break;
        }
    }

    private void setData(JSONObject json){
        JSONArray jsonArray;
        BasicVO basicVO;
        try {

            if (top) {
                if (adapter.mItems != null) {
                    adapter.mItems.clear();
                }
                Log.e("이거 삭제 됨?", lv_notice.getFooterViewsCount() + "개");
                if(lv_notice.getFooterViewsCount() == 1){
                    Log.e("setData 이거 삭제 됨?", "footer 삭제");
                    lv_notice.removeFooterView(footer);
                }
            }

            jsonArray = json.getJSONObject("notice").getJSONArray("list");

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

            if (PAGE_NUMBER <= jsonArray.length()){

                if (lv_notice.getFooterViewsCount()  == 0) {

                    lv_notice.addFooterView(footer);
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
                    lv_notice.removeFooterView(footer);
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

            dialog = new ProgressDialog(NoticeListActivity.this);
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
            if (!FunctionClass.getNetworkState(NoticeListActivity.this)) {

                return "net";

            }else{

                try {

                    jsonSend = new JSONObject();
                    jsonSend.put("user_group", user_info.getValue("USER_GROUP", ""));        // 사용자 그룹
                    jsonSend.put("search_type", type);           							 // 조회 구분
                    jsonSend.put("user_grade", user_info.getValue("USER_GRADE", ""));        // 사용자 직책
                    jsonSend.put("startRow", PAGE_START);									// 시작행
                    jsonSend.put("endRow", PAGE_END);										// 마지막행

                    Log.e("공지사항 조회 params >>" , jsonSend.toString());

                    jsonContain = new JSONObject();
                    jsonContain.put("method", "notice");			// API 메소드 명
                    jsonContain.put("params", jsonSend);		// 파라메터

                    jsonArray = new JSONArray();
                    jsonArray.put(jsonContain);

                    // POST 방식 호출
                    jsonRes = JsonClient.sendHttpPost(BaseActivtiy.SERVER_URL, jsonArray);

                    Log.e("공지사항 조회 결과 >>" , jsonRes.toString());

                    if (jsonRes == null || jsonRes.getJSONObject("notice").getJSONArray("list").length() <= 0) {
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
                    if(lv_notice.getFooterViewsCount() == 1){
                        Log.e("결과 정리 이거 삭제 됨?", "footer 삭제");
                        lv_notice.removeFooterView(footer);
                    }
                }else{
                    if (adapter.mItems != null){
                        adapter.mItems.clear();
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
