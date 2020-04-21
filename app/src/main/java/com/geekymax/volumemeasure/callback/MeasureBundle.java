package com.geekymax.volumemeasure.callback;

import com.google.ar.sceneform.math.Vector3;

public class MeasureBundle {
    private boolean success;
    private float rotationAngle;
    private Vector3 center;
    private float x;
    private float y;
    private float z;

    public MeasureBundle(boolean success, float rotationAngle, Vector3 center, float x, float y, float z) {
        this.success = success;
        this.rotationAngle = rotationAngle;
        this.center = center;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public Vector3 getCenter() {
        return center;
    }

    public void setCenter(Vector3 center) {
        this.center = center;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
