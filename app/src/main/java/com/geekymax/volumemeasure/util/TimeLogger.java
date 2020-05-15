package com.geekymax.volumemeasure.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class TimeLogger {
    private static final String TAG = "Geeky-TimeLogger";
    private Date date;
    private static TimeLogger timeLogger;

    private TimeLogger() {
        date = new Date();
    }

    public static TimeLogger getLogger() {
        if (timeLogger == null) {
            timeLogger = new TimeLogger();
        }
        return timeLogger;
    }

    public void log(String name) {
        Date now = new Date();
        Log.d(TAG, "" + name + ": spend " + (now.getTime() - date.getTime()) + " ms");
        date = now;

    }
}
