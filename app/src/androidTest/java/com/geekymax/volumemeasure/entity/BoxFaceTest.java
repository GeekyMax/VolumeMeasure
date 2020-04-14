package com.geekymax.volumemeasure.entity;

import android.util.Log;

import com.geekymax.volumemeasure.util.MathUtil;
import com.google.ar.sceneform.math.Vector3;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoxFaceTest {
    public static final String TAG = "Geeky-Test";

    @Test
    void testGetNormal() {
        Vector3 v1 = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(1, 1, 0);
        Vector3 v3 = new Vector3(0, 1, 0);
        Vector3 normal = MathUtil.getNormal(v1, v2, v3);
        Log.d(TAG, "testGetNormal: " + normal);
    }
}