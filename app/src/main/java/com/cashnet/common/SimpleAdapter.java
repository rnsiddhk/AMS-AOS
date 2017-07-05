package com.cashnet.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cashnet.R;

/**
 * 파출수납 리스트뷰 커스텀 어댑터 클래스
 *
 * */
public class SimpleAdapter extends BaseAdapter {

	Context mCtx;
	LayoutInflater inflater;
	int layout;

	public List<CommonListItem> mItems = new ArrayList<CommonListItem>();

	public SimpleAdapter(Context context, int layout){

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
	public View getView(final int position, View convertView,
						ViewGroup parent) {
		// TODO Auto-generated method stub

		try {

			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(layout, parent, false);

				holder = new ViewHolder();
				holder.mTextView1 = (TextView) convertView.findViewById(R.id.mData01);
				holder.mTextView2 = (TextView) convertView.findViewById(R.id.mData02);

				convertView.setTag(holder);
			} else {// View 재사용
				holder = (ViewHolder) convertView.getTag();
			}

			// SetText
			holder.mTextView1.setText(mItems.get(position).getData(0));
			holder.mTextView2.setText(mItems.get(position).getData(1));

			if (mItems.get(position).getData(1).equalsIgnoreCase("수납")){
				holder.mTextView2.setBackgroundColor(mCtx.getResources().getColor(R.color.Red));
			}else{
				holder.mTextView2.setBackgroundColor(mCtx.getResources().getColor(R.color.Blue));
			}




		} catch (Exception e) {

		}
		return convertView;
	}

	class ViewHolder {
		TextView mTextView1;
		TextView mTextView2;
	}
}