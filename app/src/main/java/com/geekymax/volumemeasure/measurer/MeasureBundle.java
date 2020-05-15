package com.geekymax.volumemeasure.measurer;

import com.google.ar.sceneform.math.Vector3;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MeasureBundle {
    //    private Vector3 center;
//    private Vector3 length;
    private float rotationAngle;

    private float minX;
    private float maxX;
    private float minZ;
    private float maxZ;

    private float minY;
    private float maxY;

    private Consumer<MeasureBundle> action;
//    public MeasureBundle(Vector3 center, Vector3 length, float rotationAngle) {
//        this.center = center;
//        this.length = length;
//        this.rotationAngle = rotationAngle;
//    }


    public void setAction(Consumer<MeasureBundle> action) {
        this.action = action;
    }

    public MeasureBundle(double[] borderXYZ, float rotationAngle) {
        //        minX, maxX, minY, maxY, underHeight, topHeight
        minX = (float) borderXYZ[0];
        maxX = (float) borderXYZ[1];
        minZ = (float) borderXYZ[2];
        maxZ = (float) borderXYZ[3];
        minY = (float) borderXYZ[4];
        maxY = (float) borderXYZ[5];
        this.rotationAngle = rotationAngle;
    }


    public Vector3 getCenter() {
        return new Vector3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
    }

    public Vector3 getLength() {
        return new Vector3((maxX - minX), (maxY - minY), (maxZ - minZ));
    }

    public float getVolume() {
        Vector3 v = getLength();
        return v.x * v.z * v.y;
    }


    @NotNull
    public String toString() {
        Vector3 length = getLength();
        float volume = length.x * length.y * length.z;
        return String.format("%.2f * %.2f * %.2f = %.6f", length.x, length.z, length.y, volume);
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void clear() {
        minZ = 100000;
        minX = 100000;
        minY = 100000;
        maxX = -100000;
        maxY = -100000;
        maxZ = -100000;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
        update();
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
        update();
    }

    public float getMinZ() {
        return minZ;
    }

    public void setMinZ(float minZ) {
        this.minZ = minZ;
        update();
    }

    public float getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(float maxZ) {
        this.maxZ = maxZ;
        update();
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
        update();

    }

    public float getMaxY() {
        return maxY;

    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
        update();
    }

    public void update() {
        if (action != null) {
            action.accept(this);
        }
    }
}
