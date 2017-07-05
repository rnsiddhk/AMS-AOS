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
 * 	공지사항 리스트뷰 커스텀 어댑터 클래스
 *
 * */
public class NoticeAdapter extends BaseAdapter {

	Context mCtx;
	LayoutInflater inflater;
	int layout;

	public List<CommonListItem> mItems = new ArrayList<CommonListItem>();

	public NoticeAdapter(Context context, int layout){

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

				holder.mImgView1 = (TextView) convertView.findViewById(R.id.mImageView1);
				holder.mImgView2 = (TextView) convertView.findViewById(R.id.mImageView2);

				holder.mTextView1 = (TextView) convertView.findViewById(R.id.mData01);
				holder.mTextView2 = (TextView) convertView.findViewById(R.id.mData02);
				holder.mTextView3 = (TextView) convertView.findViewById(R.id.mData03);
				holder.mTextView4 = (TextView) convertView.findViewById(R.id.mData04);
				holder.mTextView5 = (TextView) convertView.findViewById(R.id.mData05);

				convertView.setTag(holder);
			}
			else {// View 재사용
				holder = (ViewHolder) convertView.getTag();
			}


			// 긴급, 일반 이미지 치환
			if (mItems.get(position).getData(0).equalsIgnoreCase("U")) {
				holder.mImgView1.setBackgroundResource(R.drawable.icon_notice_u);
			}else{
				holder.mImgView1.setBackgroundResource(R.drawable.icon_notice_n);
			}

			// 전사, 지사 이미지 치환
			if (mItems.get(position).getData(1).equalsIgnoreCase("0")) {
				holder.mImgView2.setBackgroundResource(R.drawable.icon_notice_0);
			}else{
				holder.mImgView2.setBackgroundResource(R.drawable.icon_notice_1);
			}

			// SetText
			holder.mTextView1.setText(mItems.get(position).getData(2));
			holder.mTextView2.setText(mItems.get(position).getData(3));
			holder.mTextView3.setText(mItems.get(position).getData(4));
			holder.mTextView4.setText(mItems.get(position).getData(5));
			holder.mTextView5.setText(mItems.get(position).getData(6));

		} catch (Exception e) {

			// TODO: handle exception
		}
		return convertView;
	}

	class ViewHolder {

		TextView mImgView1;
		TextView mImgView2;
		TextView mTextView1;
		TextView mTextView2;
		TextView mTextView3;
		TextView mTextView4;
		TextView mTextView5;

	}
}