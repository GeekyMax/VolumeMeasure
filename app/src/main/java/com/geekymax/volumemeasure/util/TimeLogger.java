package com.geekymax.volumemeasure.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeLogger {
    private static final String TAG = "Geeky-TimeLogger";
    private Date date;
    private static Map<String, TimeLogger> timeLoggerMap = new HashMap<>();
    private static final String DEFAULT = "default";
    private String name;

    private TimeLogger(String name) {
        date = new Date();
        this.name = name;
    }

    public static TimeLogger getLogger() {
        return getLogger(DEFAULT);
    }

    public static TimeLogger getLogger(String name) {
        TimeLogger timeLogger = timeLoggerMap.get(name);
        if (timeLogger == null) {
            timeLogger = new TimeLogger(name);
            timeLoggerMap.put(name, timeLogger);
        }
        return timeLogger;
    }

    public void log(String name) {
        Date now = new Date();
        Log.d(TAG, this.name + ":::" + name + ": spend " + (now.getTime() - date.getTime()) + " ms");
        date = now;

    }
}
