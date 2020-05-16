package com.geekymax.volumemeasure.entity;


import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.geekymax.volumemeasure.manager.FileManager;
import com.google.ar.sceneform.math.Vector3;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;


@Entity
public class Record {
    @Ignore
    public Record(@NonNull String uid) {
        this.uid = uid;
    }

    public Record(@NonNull String uid, Date date, String name, float x, float y, float z) {
        this.uid = uid;
        this.date = date;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bitmap = FileManager.loadBitmap(uid);
    }

    public Record(String uid, Date date, String name, Vector3 vector3) {
        this.uid = uid;
        this.date = date;
        this.name = name;
        this.x = vector3.x;
        this.y = vector3.y;
        this.z = vector3.z;
    }

    @PrimaryKey
    @NonNull
    public String uid;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "x")
    public float x;
    @ColumnInfo(name = "y")
    public float y;
    @ColumnInfo(name = "z")
    public float z;

    @Ignore
    public Bitmap bitmap;

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-DD hh:mm:ss");
        return String.format("%s:%s:%f,%f,%f", uid, sdf.format(date), x, y, z);
    }

    public float getVolume() {
        return this.x * this.y * this.z;
    }
}
