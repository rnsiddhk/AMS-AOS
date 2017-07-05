package com.cashnet.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.FunctionClass;
import com.cashnet.common.JsonClient;
import com.cashnet.main.BaseActivtiy;
import com.cashnet.vo.BasicVO;
import com.google.android.map.GoogleMapViewer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  지역 조회 리스트 액티비티
 *  see location_search_list_view
 * @since 2016-10-11
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * Created by JJH on 2016-10-11.
 */
public class LocationSearchListActivity extends BaseActivtiy implements View.OnClickListener {

    // 조회된 내용 담을 VO Array 객체
    private ArrayList<BasicVO> locationAL = new ArrayList<BasicVO>();

    // 정보 담을 VO객체
    private BasicVO locationVO;

    private Context ctx;

    // 통신 결과 Json 객체
    private JSONObject jsonRes;

    // 리스트뷰에 담을 정보
    private ArrayList<String> name_AL;

    // String 어댑터
    private ArrayAdapter<String> adapter;

    // 리스트 뷰 객체
    private ListView lv_location;

    // 검색 버튼 객체
    private Button btn_loc_search;

    // 검색어
    private  String keyword = "";

    private Intent intent;

    private String ORG_CD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.location_search_list_view);

        super.onCreate(savedInstanceState);

        ctx = this;

        initView();
    }

    // 뷰 객체 생성
    private void initView(){

        // 타이틀바 이름 정하기
        ((TextView)(((View) findViewById(R.id.top)).findViewById(R.id.txt_title))).setText("지역 조회");

        name_AL = new ArrayList<String>();

        // 어댑터 생성
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, name_AL);


        // 기기번호(구글맵 다시 전달용)
        ORG_CD = getIntent().getExtras().getString("ORG_CD", "");

        lv_location = (ListView) findViewById(R.id.lv_location);
        lv_location.setAdapter(adapter);
        lv_location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                intent = new Intent(ctx, GoogleMapViewer.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("ACT_GB", "POINT");
                intent.putExtra("X_GRID", Double.parseDouble(locationAL.get(position).getDATA02()));
                intent.putExtra("Y_GRID", Double.parseDouble(locationAL.get(position).getDATA01()));
                intent.putExtra("ATM_NAME", name_AL.get(position));
                intent.putExtra("ORG_CD", ORG_CD);
                startActivity(intent);

                finish();
            }
        });

        btn_loc_search = (Button) findViewById(R.id.btn_loc_search);
        btn_loc_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v.getId() == R.id.btn_loc_search){
            keyword = ((EditText) findViewById(R.id.edt_keyword)).getText().toString().trim();

            if (!FunctionClass.isNullOrBlank(keyword)){

                SearchTask searchTask = new SearchTask();
                searchTask.execute();

            }else{
                Toast.makeText(ctx, getString(R.string.str_input_msg), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 조회된 결과 처리 메소드
    // 위경도 처리
    private void setGeoPointData(JSONObject json){

        // route(경로)
        JSONArray jsonResult = null;

        // 위경도
        double lat, lng;

        // 명칭
        String addr_nm = "";

        if (name_AL != null) {
            name_AL.clear();
            adapter.notifyDataSetChanged();
        }

        try {

            // 전체 JSON에서 route(경로) Array만 분리
            jsonResult = json.getJSONArray("results");

            for (int i = 0; i < jsonResult.length(); i++){

                locationVO = new BasicVO();

                Log.e("조회된 위도 >>", jsonResult.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat"));
                Log.e("조회된 경도 >>", jsonResult.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng"));

                locationVO.setDATA01(jsonResult.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat"));     // 위도
                locationVO.setDATA02(jsonResult.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng"));     // 경도

                locationAL.add(locationVO);

                name_AL.add(jsonResult.getJSONObject(i).getString("formatted_address"));    // 명칭
            }

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Exception >> ", e.toString());
        }
    }

    // 조회 Task
    private class SearchTask extends AsyncTask<String, Integer, String> {

        private String url = "";

        private ProgressDialog dialog;

        private String err_msg = "";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            dialog = new ProgressDialog(LocationSearchListActivity.this);
            dialog.setMessage(getString(R.string.str_loading_msg));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(true);
                        }
                    });
            dialog.show();

            FunctionClass.doHideKeyBoard(LocationSearchListActivity.this);
        }

        @Override
        protected String doInBackground(String... result) {
            // TODO Auto-generated method stub

            // 네트워크 연결이 안되었을 때
            if (!FunctionClass.getNetworkState(LocationSearchListActivity.this)) {

                return "net";

            }else{
                try {

                    // 위경도 검색 API (구글)
                    url = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&language=ko&address=" + keyword;


                    // GET 방식 호출
                    jsonRes = JsonClient.connect(url);

                    // 결과 값 유무 확인
                    if (jsonRes == null || 0 > jsonRes.length() ) {
                        return "null";
                    }else{

                        // 위치조회 API
                        // 결과중 에러값 처리 로직
                        // 키값 중 error 값 확인
                        if (!jsonRes.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                            return "success";
                        }else{
                            return "null";
                        }
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    err_msg = "조회중 오류가 발생하였습니다. \n 다시 시도 하시기 바랍니다.";
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
                Toast.makeText(ctx, err_msg,Toast.LENGTH_LONG).show();
            } else {

                // 성공시 데이터 결과 셋팅 및 처리
                setGeoPointData(jsonRes);
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
