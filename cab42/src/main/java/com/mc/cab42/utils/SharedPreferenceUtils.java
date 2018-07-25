package com.mc.cab42.utils;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class SharedPreferenceUtils {

    private final float defaultLatitude = 52.038137f;
    private final float defaultLongitude = -0.7751537f;

    Context ctx;

    public SharedPreferenceUtils(Context ctx) {
        this.ctx = ctx;
    }

    public LatLng getLatLng() {
        SharedPreferences pref = ctx.getSharedPreferences("Cab42Pref", 0);
        return new LatLng(pref.getFloat("lastLat", defaultLatitude), pref.getFloat("lastLng", defaultLongitude));
    }

    public void setLatLng(LatLng newData) {
        SharedPreferences pref = ctx.getSharedPreferences("Cab42Pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("lastLat", (float)newData.latitude);
        editor.putFloat("lastLng", (float)newData.longitude);
        editor.apply();
    }

}
