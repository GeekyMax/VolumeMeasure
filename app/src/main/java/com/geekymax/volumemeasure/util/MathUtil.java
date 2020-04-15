package com.geekymax.volumemeasure.util;

import android.util.Log;

import com.geekymax.volumemeasure.math.Matrix3f;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class MathUtil {
    private static final String TAG = "Geeky-MathUtil";

    // 获取三点确定平面的法向量(normalized)
    public static Vector3 getNormal(Vector3 p1, Vector3 p2, Vector3 p3) {
        float a = ((p2.y - p1.y) * (p3.z - p1.z) - (p2.z - p1.z) * (p3.y - p1.y));

        float b = ((p2.z - p1.z) * (p3.x - p1.x) - (p2.x - p1.x) * (p3.z - p1.z));

        float c = ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x));

        Vector3 res = new Vector3(a, b, c).normalized();
        Log.d(TAG, "getNormal: " + p1 + "; " + p2 + "; " + p3 + "; res: " + res);
        return res;
    }

    // 根据三个坐标轴向量获取坐标系旋转四元数
    public static Matrix3f getRotationMatrix(Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 axis1 = new Vector3(1, 0, 0);
        Vector3 axis2 = new Vector3(0, 1, 0);
        Vector3 axis3 = new Vector3(0, 0, -1);
        return getRotationMatrix(axis1, axis2, axis3, v1, v2, v3);
    }


    public static Quaternion rotationMatrixToQuaternion(Matrix3f r) {
        com.geekymax.volumemeasure.math.Quaternion q = new com.geekymax.volumemeasure.math.Quaternion();
        q.fromRotationMatrix(r);
        return arQuaternion(q);
    }

    private static Matrix3f getRotationMatrix(Vector3 v11, Vector3 v12, Vector3 v13, Vector3 v21, Vector3 v22, Vector3 v23) {
        Matrix3f ma = new Matrix3f(v11.x, v11.y, v11.z, v12.x, v12.y, v12.z, v13.x, v13.y, v13.z);
        Matrix3f mb = new Matrix3f(v21.x, v21.y, v21.z, v22.x, v22.y, v22.z, v23.x, v23.y, v23.z);
        Matrix3f inverseMb = mb.invert();
        Matrix3f r = ma.mult(inverseMb);
        return r;
    }

    private static Quaternion arQuaternion(com.geekymax.volumemeasure.math.Quaternion q) {
        return new Quaternion(q.x, q.y, q.z, q.w);
    }


}