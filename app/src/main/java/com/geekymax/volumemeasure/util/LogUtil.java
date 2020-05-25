package com.geekymax.volumemeasure.util;

import android.util.Log;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;

/**
 * @author huangmengxuan
 * @date 2020-04-28
 */
public class LogUtil {
    public static final String TAG = "Geeky-LogUtl";

    public static void log(Object... objs) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : objs) {
            stringBuilder.append(obj.toString());
            stringBuilder.append(" ");
        }
        Log.d(TAG, stringBuilder.toString());
    }

    public static void print(String tag, INDArray arr) {
        Log.d(TAG, "----------------");
        if (arr == null) {
            Log.d(TAG, tag + ":\n" + "empty");
        } else {
            Log.d(TAG, "shape " + Arrays.toString(arr.shape()));
            Log.d(TAG, tag + ":\n" + arr.toString());
        }

        Log.d(TAG, "----------------");
    }

    public static void print(String tag, INDArray[] arrays) {
        Log.d(TAG, "----------------");
        Log.d(TAG, tag);
        for (INDArray array : arrays) {
            Log.d(TAG, "\n" + array);
        }
        Log.d(TAG, "----------------");
    }


}
