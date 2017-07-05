package com.cashnet.common;

/**
 * Created by Won on 2016-08-29.
 */
public class MarkerItem {

    double lat;
    double lon;
    String contents;

    public MarkerItem(double lat, double lon, String contents){
        this.lat = lat;
        this.lon = lon;
        this.contents = contents;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
