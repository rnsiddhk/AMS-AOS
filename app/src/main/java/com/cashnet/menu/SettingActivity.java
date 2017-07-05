package com.cashnet.menu;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cashnet.R;
import com.cashnet.main.BaseActivtiy;

/**
 *  셋팅
 *  see setting_view
 * @since 2016-09-06
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 * Created by JJH on 2016-09-06.
 */

public class SettingActivity extends BaseActivtiy {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.setting_view);

        super.onCreate(savedInstanceState);

        initView();
    }

    // 뷰 객체 생성 메소드
    private void initView(){

        // 패스워드 변경
        ((TextView) findViewById(R.id.txt_pwd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 벨소리 변경
        ((TextView) findViewById(R.id.txt_ring)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(SettingActivity.this, RingtoneManager.TYPE_ALARM);
                intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM); // 리스트에 보여줄 알림음 리스트 타입
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");   // 타이틀
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);    // 무음
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);    // 기본 벨소리
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if(requestCode == 0){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            RingtoneManager.setActualDefaultRingtoneUri(SettingActivity.this,
                    RingtoneManager.TYPE_RINGTONE,
                    uri);
        }
    }
}
