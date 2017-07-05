package com.cashnet.common;

import android.graphics.drawable.Drawable;

public class CommonListItem {

    private Drawable mIcon;
    private Drawable mIconNext;
    private String[] mData; 
    private boolean mSelectable = true;
    private boolean cb01 = false;
    private boolean mRed = false;
    private boolean isChecked = false;

    private static long index = 0;
    private long Id = 0;
    private String title;
    private String summary;

    public CommonListItem(Drawable icon, String... strings) {
        mIcon = icon;

        mData = new String[strings.length];

        for (int i = 0; i < strings.length; i++) {
            mData[i] = strings[i];
        }
    }

    public CommonListItem(String... strings) {
        mData = new String[strings.length];

        for (int i = 0; i < strings.length; i++) {
            mData[i] = strings[i];
        }
    }   
    
    public CommonListItem(boolean isSelect, String... strings) {
        mData = new String[strings.length];
        mSelectable = isSelect;
        for (int i = 0; i < strings.length; i++) {
            mData[i] = strings[i];
        }
    }
    
    public CommonListItem(boolean isSelect,boolean isTextRed, String... strings) {
        mData = new String[strings.length];
        mSelectable = isSelect;
        mRed = isTextRed;
        for (int i = 0; i < strings.length; i++) {
            mData[i] = strings[i];
        }
    }
    
    

    public boolean isChecked()
    {
        return isChecked;
    }
    
    
    public void setCheck(boolean check) {
        isChecked = check;
    }
    

    public boolean isTextRed()
    {
        return mRed;
    }
    
    
    public void setTextRed(boolean isRed) {
        mRed = isRed;
    }
    
    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public String[] getData() {
        return mData;
    }

    public int getCount()
    {
        return mData.length;
    }
    
    public String getData(int index) {
        if (mData == null || index >= mData.length) {
            return null;
        }
        return mData[index];
    }

    public void setData(int index, String obj) {
        if (mData != null) {

            if (index < mData.length) {

                mData[index] = obj;
            }
        }
    }

    public void setData(String[] obj) {
        mData = obj;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIconNext(Drawable iconNext) {
        mIconNext = iconNext;
    }

    public Drawable getIconNext() {
        return mIconNext;
    }

    public void setBoolean(boolean obj01) {
        cb01 = obj01;
    }

    public boolean getBoolean() {
        return cb01;
    }

    public long getId() {
        return Id;
    }

    public void setId(long Id) {
        this.Id = Id;
    }

    public int compareTo(CommonListItem other) {
        if (mData != null) {
            String[] otherData = other.getData();
            if (mData.length == otherData.length) {
                for (int i = 0; i < mData.length; i++) {
                    if (!mData[i].equals(otherData[i])) {
                        return -1;
                    }
                }
            } else {
                return -1;
            }
        } else {
            throw new IllegalArgumentException();
        }

        return 0;
    }

}
