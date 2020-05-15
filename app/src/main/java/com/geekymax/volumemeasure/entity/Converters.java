package com.geekymax.volumemeasure.entity;

import androidx.room.TypeConverter;

import java.util.Date;

public class Converters {
    @TypeConverter
    public Date fromTimestamp(long value) {
        return new Date(value);
    }

    @TypeConverter
    public long dateToTimestamp(Date date) {
        return date.getTime();
    }
}
