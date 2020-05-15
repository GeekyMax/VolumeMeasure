package com.geekymax.volumemeasure.manager;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.geekymax.volumemeasure.dao.RecordDao;
import com.geekymax.volumemeasure.entity.Record;
import com.geekymax.volumemeasure.entity.Converters;


@Database(entities = {Record.class}, version = 2)

@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordDao recordDao();
}
