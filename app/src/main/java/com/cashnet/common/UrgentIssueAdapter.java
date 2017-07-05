package com.cashnet.common;

import java.util.ArrayList;
import java.util.List;

import com.cashnet.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 	긴급출동, 장애관리 리스트뷰 커스텀 어댑터 클래스
 *
 * */
public class UrgentIssueAdapter extends BaseAdapter {

	Context mCtx;
	LayoutInflater inflater;
	int layout;

	public List<CommonListItem> mItems = new ArrayList<CommonListItem>();

	public UrgentIssueAdapter(Context context, int layout){

		this.mCtx = context;
		this.layout = layout;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if(mItems == null) return 0;
		return mItems.size();
	}

	@Override
	public CommonListItem getItem(int position) {
		return mItems.get( position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.get( position).getId();
	}

	public void addItem(CommonListItem it) {
		mItems.add(it);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		try {

			ViewHolder holder;

			if(convertView == null) {
				convertView = inflater.inflate(layout, parent, false);

				holder = new ViewHolder();
				holder.mTextView1 = (TextView) convertView.findViewById(R.id.mData01);
				holder.mTextView2 = (TextView) convertView.findViewById(R.id.mData02);
				holder.mTextView3 = (TextView) convertView.findViewById(R.id.mData03);

				convertView.setTag(holder);
			}
			else {// View 재사용
				holder = (ViewHolder) convertView.getTag();
			}

			// SetText
			holder.mTextView1.setText(mItems.get(position).getData(0));
			holder.mTextView3.setText(mItems.get(position).getData(2));

			// 등급에 따른 이미지 치환
			if (mItems.get(position).getData(1).equalsIgnoreCase("0")) {

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_0);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("1")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_1);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("2")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_2);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("3")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_3);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("4")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_4);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("5")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_5);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("6")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_6);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("7")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_7);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("8")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_8);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("9")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_9);

			}else if(mItems.get(position).getData(1).equalsIgnoreCase("10")){

				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_10);

			}else{
				holder.mTextView2.setBackgroundResource(R.drawable.icon_grade_11);
			}

			// 접수상태에 따른 이미지 치환
			if (mItems.get(position).getData(3).equalsIgnoreCase("신규")) {

				holder.mTextView3.setBackgroundResource(R.drawable.gradient_blue_linear);

			}else if(mItems.get(position).getData(3).equalsIgnoreCase("접수")){

				holder.mTextView3.setBackgroundResource(R.drawable.gradient_green_linear);

			}else if(mItems.get(position).getData(3).equalsIgnoreCase("도착")){

				holder.mTextView3.setBackgroundResource(R.drawable.gradient_pink_linear);

			}else if(mItems.get(position).getData(3).equalsIgnoreCase("이관")){

				holder.mTextView3.setBackgroundResource(R.drawable.gradient_orange_linear);

			}else if(mItems.get(position).getData(3).equalsIgnoreCase("완료")){

				holder.mTextView3.setBackgroundResource(R.drawable.gradient_gray_linear);
			}


		} catch (Exception e) {

			// TODO: handle exception
		}
		return convertView;
	}

	class ViewHolder {

		TextView mTextView1;
		TextView mTextView2;
		TextView mTextView3;
	}
}