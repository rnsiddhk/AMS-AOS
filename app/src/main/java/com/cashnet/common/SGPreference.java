package com.cashnet.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SGPreference {	
	
	private final String PREF_NAME = "info";
	static Context mContext;
	
	public SGPreference(Context c){
		mContext = c;
	}
	
	public void put(String key, String value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(key, value);
		editor.commit();
	}
	
	public void put(String key, boolean value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public void put(String key, int value) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		editor.putInt(key, value);
		editor.commit();
	}
	
	public String getValue(String key, String defValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		
		try {
			return pref.getString(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}
	
	public int getValue(String key, int defValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		
		try {
			return pref.getInt(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}
	
	public boolean getValue(String key, boolean defValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		
		try {
			return pref.getBoolean(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}
	
}
