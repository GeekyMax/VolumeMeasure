package com.geekymax.volumemeasure.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.geekymax.volumemeasure.entity.Record;

import java.util.List;

@Dao
public interface RecordDao {
    @Query("SELECT * FROM record ORDER BY date DESC")
    List<Record> getAll();

    @Insert
    void insert(Record record);

    @Delete
    void delete(Record... records);
    @Delete
    int deleteAll(List<Record> records);
}
