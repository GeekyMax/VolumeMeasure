package com.geekymax.volumemeasure.entity;

public class MyPoint {
    public float x;
    public float y;
    public float z;
    public float confidence;

    public MyPoint(float x, float y, float z, float confidence) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "" + x + ", " + y + ", " + z + ", " + confidence;
    }
}
