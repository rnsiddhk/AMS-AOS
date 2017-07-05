package com.cashnet.menu;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cashnet.R;
import com.cashnet.common.CustomGallery;
import com.cashnet.main.BaseActivtiy;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 커스텀 이미지 갤러리
 * see collect_money_detail_view.xml
 * @since 2016-11-10
 * @author JJH, hirosi@bgf.co.kr
 * @version 1.0
 */

public class CustomGalleryActivity extends BaseActivtiy {

    // 상단 타이틀바 뷰 객체
    private View top_view;

    private GridView gridGallery;
    private Handler handler;
    private GalleryAdapter adapter;

    private ImageView imgNoMedia;
    private Button btn_ok;

    private ImageLoader imageLoader;

    private String strAction = "";

    public static int selectedSEQ = 1;
    private int SELECT_MAX = 10;

    public ArrayList<View> view_AL = new ArrayList<>();
    public  ArrayList<String> img_src_AL = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setActivity(R.layout.custom_gallery_view);

        super.onCreate(savedInstanceState);

        initImageLoader();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        selectedSEQ = 1;
    }

    // 이미지 로딩 메소드
    private void initImageLoader() {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(), CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    getBaseContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .discCache(new UnlimitedDiscCache(cacheDir))
                    .memoryCache(new WeakMemoryCache());

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

        } catch (Exception e) {

        }
    }

    // 객체 생성 메소드
    private void initView() {

        // 상단 타이틀바 작성
        top_view = (View) findViewById(R.id.top);
        ((TextView)((top_view).findViewById(R.id.txt_title))).setText("사진첩");

        strAction = getIntent().getExtras().getString("action", "");

        // 핸들러 등록
        handler = new Handler();

        // 그리드 뷰 객체 생성
        gridGallery = (GridView) findViewById(R.id.gridGallery);

        // 스크롤 속도 빠르기 활성화
        gridGallery.setFastScrollEnabled(true);

        // 그리드뷰 어댑터 생성
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);

        // 이미지로더 라이브러리 리스너 생성
        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, true, true);

        // 그리드뷰 스크롤 이벤트 리스너 등록
        gridGallery.setOnScrollListener(listener);

        if(strAction.equalsIgnoreCase("M")){

            // 그리드뷰 아이템 클릭 이벤트 리스너 등록(멀티)
            gridGallery.setOnItemClickListener(mItemMulClickListener);

            // 어댑터 다중선택 설정 활성화
            adapter.setMultiplePick(true);

        }else{

            // 그리드뷰 아이템 클릭 이벤트 리스너 등록(싱글)
            gridGallery.setOnItemClickListener(mItemSingleClickListener);

            // 어댑터 다중선택 설정 비활성화
            adapter.setMultiplePick(false);
        }

        // 그리드뷰 어댑터 셋팅
        gridGallery.setAdapter(adapter);

        // 미리보기 이미지 뷰 객체 생성
        imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);

        // 선택 완료 버튼 생성 및 이벤트 리스너 등록
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(mOkClickListener);

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        adapter.addAll(getGalleryPhotos());
                        checkImageStatus();
                    }
                });
                Looper.loop();
            }

        }.start();

    }

    // 이미지 어댑터 값에 따라 빈 이미지 보여주는 메소드
    private void checkImageStatus() {
        if (adapter.isEmpty()) {
            imgNoMedia.setVisibility(View.VISIBLE);
        } else {
            imgNoMedia.setVisibility(View.GONE);
        }
    }

    // 버튼 클릭 리스너
    View.OnClickListener mOkClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            // 선택된 이미지 값
            ArrayList<CustomGallery> selected = adapter.getSelected();

            // 실제 이미지 경로 가져오기
            String[] allPath = new String[selected.size()];
            for (int i = 0; i < allPath.length; i++) {
                allPath[i] = selected.get(i).sdcardPath;
            }
            // 결과 보내기
            Intent data = new Intent().putExtra("all_path", allPath);
            setResult(RESULT_OK, data);
            finish();

        }
    };

    // 어댑터 아이템 클릭 리스너(싱글 이미지 클릭)
    AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {

            Log.e("선택한 이미지 순번 >> ", selectedSEQ + "");
            adapter.changeSelectionSEQ(v, position, selectedSEQ);
            selectedSEQ++;


        }
    };

    AdapterView.OnItemClickListener mItemSingleClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            CustomGallery item = adapter.getItem(position);
            Intent data = new Intent().putExtra("single_path", item.sdcardPath);
            setResult(RESULT_OK, data);
            finish();
        }
    };

    // 이미지 저장소에 있는 이미지 데이터 가져오는 메소드
    private ArrayList<CustomGallery> getGalleryPhotos() {
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

        try {
            final String[] columns = { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media._ID;

            Cursor imagecursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    "bucket_display_name='bgf'", null, orderBy);

            if (imagecursor != null && imagecursor.getCount() > 0) {

                while (imagecursor.moveToNext()) {
                    CustomGallery item = new CustomGallery();

                    int dataColumnIndex = imagecursor
                            .getColumnIndex(MediaStore.Images.Media.DATA);

                    item.sdcardPath = imagecursor.getString(dataColumnIndex);

                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    private class GalleryAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater infalter;
        private ArrayList<CustomGallery> data = new ArrayList<CustomGallery>();
        ImageLoader imageLoader;
        private boolean isActionMultiplePick;

        public GalleryAdapter(Context c, ImageLoader imageLoader) {
            infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = c;
            this.imageLoader = imageLoader;
            // clearCache();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public CustomGallery getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setMultiplePick(boolean isMultiplePick) {
            this.isActionMultiplePick = isMultiplePick;
        }

        public ArrayList<CustomGallery> getSelected() {
            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).isSeleted) {
                    dataT.add(data.get(i));
                }
            }

            return dataT;
        }

        public void addAll(ArrayList<CustomGallery> files) {

            try {
                this.data.clear();
                this.data.addAll(files);

            } catch (Exception e) {
                e.printStackTrace();
            }

            notifyDataSetChanged();
        }

        public void changeSelectionSEQ(View v, int position, int seq) {

            int max = 0;
            int temp = 0;

            if (data.get(position).isSeleted) {
                Log.e("이미지 선택 취소 >> ",  " 도입부");

                data.get(position).isSeleted = false;

                Log.e("이미지 선택 취소 >> ",  " 선택값 : " + data.get(position).isSeleted);

                // 선택되었던 이미지의 순번을 0으로 초기화 작업
                // 순번이 마지막인 경우는 선택된 이미지의 순번만 초기화 함
                if (data.get(position).seq == seq){
                    Log.e("이미지 선택 취소 >> ",  " 마지막 선택된 이미지인 경우 취소");

                    data.get(position).seq = 0;
                    view_AL.remove(v);
                    img_src_AL.remove(data.get(position).sdcardPath);

                } // 선택된 이미지의 순번이 마지막의 순번이 아닌경우 순번정리 로직을 수행
                else{

                    Log.e("이미지 선택 취소 >> ",  " 마지막 선택된 이미지 아닌 경우 취소");
                    temp = data.get(position).seq;
                    data.get(position).seq = 0;

                    view_AL.remove(v);
                    img_src_AL.remove(data.get(position).sdcardPath);

                    // 전체 data에서 선택되어 있는 이미지 중 순번의 변화를 위해서 '순번 값 -1' 을 해주어 순서를 맞춰줌
                    for (int i = 0; i < data.size(); i++){

                        // 선택된 이미지 확인
                        if (data.get(i).isSeleted) {
                            Log.e("이미지 선택 취소 >> ",  " 선택된 이미지들 있는지 확인중");

                            // 입력 받은 순번보다 큰 순번값 - 1을 함
                            if (0 != data.get(i).seq && temp < data.get(i).seq) {

                                Log.e("이미지 선택 취소 >> ",  " 선택된 이미지들 순번 변경하기");
                                Log.e("기존 순번 >> ",  " " + data.get(i).seq );
                                Log.e("변경될 순번 >> ", " " +(data.get(i).seq - 1));
                                data.get(i).seq = data.get(i).seq - 1;

                            }

                            // 순번중 제일 큰 수 찾기
                            if (max < data.get(i).seq) {
                                max = data.get(i).seq;
                            }
                        }
                    }
                }

                Log.e("이미지 뷰 사이즈 >> ",  view_AL.size()+"개");

                for (int j = 0; j <  view_AL.size(); j++){

                    ((ViewHolder) view_AL.get(j).getTag()).txt_seq.setText( (j+1) +"");
                }

                // 순번 표시하는 텍스트 뷰 숨기고 값 초기화
                ((ViewHolder) v.getTag()).txt_seq.setVisibility(View.GONE);
                ((ViewHolder) v.getTag()).txt_seq.setText("");

                Log.e("이미지 선택 취소 >> ",  "CustomGalleryActivity 순번 값 변화 전" + CustomGalleryActivity.selectedSEQ);
                // 선택취소 한 이미지의 순번 값은 (마지막 순번의 값 - 1)으로 초기화
                CustomGalleryActivity.selectedSEQ = max;
                Log.e("이미지 선택 취소 >> ",  " CustomGalleryActivity 순번 값 변화 후" + CustomGalleryActivity.selectedSEQ);

            } else {

                if (view_AL.size() < SELECT_MAX) {
                    Log.e("이미지 선택 >> ", " CustomGalleryActivity 순번 값 : " + CustomGalleryActivity.selectedSEQ);
                    data.get(position).isSeleted = true;
                    data.get(position).seq = seq;
                    Log.e("이미지 선택 순번 >> ", " data 어댑터의 순번 값 : " + data.get(position).seq);

                    // 텍스트 뷰에 순번표시와 보이기
                    ((ViewHolder) v.getTag()).txt_seq.setVisibility(View.VISIBLE);
                    ((ViewHolder) v.getTag()).txt_seq.setText(data.get(position).seq + "");

                    // 텍스트 뷰 객체 임시저장
                    view_AL.add(v);

                    // 선택된 이미지 경로 임시저장
                    img_src_AL.add(data.get(position).sdcardPath);
                }else{
                    Toast.makeText(CustomGalleryActivity.this, "10개만 선택 가능합니다", Toast.LENGTH_SHORT).show();
                    selectedSEQ--;
                }
                Log.e("이미지 뷰 사이즈 >> ",  view_AL.size()+"개");
            }

            ((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(data.get(position).isSeleted);

        }
        public void changeSelectionSeq(View v, int position, int seq) {

            if (data.get(position).isSeleted) {

                data.get(position).isSeleted = false;

                view_AL.remove(v);

                img_src_AL.remove(data.get(position).sdcardPath);

                for (int j = 0; j <  view_AL.size(); j++){

                    ((ViewHolder) view_AL.get(j).getTag()).txt_seq.setText( (j+1) +"");
                }


                // 순번 표시하는 텍스트 뷰 숨기고 값 초기화
                ((ViewHolder) v.getTag()).txt_seq.setVisibility(View.GONE);
                ((ViewHolder) v.getTag()).txt_seq.setText("");

                CustomGalleryActivity.selectedSEQ = view_AL.size();

            } else {

                data.get(position).isSeleted = true;

                // 텍스트 뷰에 순번표시와 보이기
                ((ViewHolder) v.getTag()).txt_seq.setVisibility(View.VISIBLE);
                ((ViewHolder) v.getTag()).txt_seq.setText(seq+"");

                view_AL.add(v);
                img_src_AL.add(data.get(position).sdcardPath);
            }
            ((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(data.get(position).isSeleted);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {

                convertView = infalter.inflate(R.layout.gallery_item, null);

                holder = new ViewHolder();

                holder.imgQueue = (ImageView) convertView.findViewById(R.id.imgQueue);

                holder.imgQueueMultiSelected = (ImageView) convertView.findViewById(R.id.imgQueueMultiSelected);

                holder.txt_seq = (TextView) convertView.findViewById(R.id.txt_seq);

                if (isActionMultiplePick) {
                    holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);
                } else {
                    holder.imgQueueMultiSelected.setVisibility(View.GONE);
                }

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imgQueue.setTag(position);

            try {

                imageLoader.displayImage("file://" + data.get(position).sdcardPath,
                        holder.imgQueue, new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                holder.imgQueue.setImageResource(R.drawable.no_media);
                                super.onLoadingStarted(imageUri, view);
                            }
                        });

                if (isActionMultiplePick) {

                    holder.imgQueueMultiSelected.setSelected(data.get(position).isSeleted);
                    if (data.get(position).isSeleted) {
                        holder.txt_seq.setText(data.get(position).seq+"");
                        holder.txt_seq.setVisibility(View.VISIBLE);
                    }else{
                        holder.txt_seq.setText("");
                        holder.txt_seq.setVisibility(View.GONE);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        class ViewHolder {
            ImageView imgQueue;
            ImageView imgQueueMultiSelected;
            TextView txt_seq;
        }
    }
}
