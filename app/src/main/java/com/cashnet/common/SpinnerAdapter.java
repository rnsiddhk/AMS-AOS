package com.cashnet.common;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 *  [공통] cusomer SpinnerAdapter
 *
 *  @author JJH
 *  @since 2013.02.14
 *
 * */
public class SpinnerAdapter extends ArrayAdapter<String> {

	Context context;
	String[] items = new String[] {};

	public SpinnerAdapter(final Context context, final int textViewResourceId, final String[] objects) {

		super(context, textViewResourceId, objects);
		this.items = objects;
		this.context = context;
	}

	/**
	 *  드롭다운에 표현 되는 부분
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
		}

		TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		tv.setText(items[position]);
		tv.setTextColor(Color.BLACK);
		tv.setTextSize(20);
		return convertView;
	}

	/**
	 * 스피너 표현 되는 부분
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
		}

		TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		tv.setText(items[position]);
		tv.setTextColor(Color.BLACK);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(16);
		return convertView;
	}
}
