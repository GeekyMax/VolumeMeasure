package com.geekymax.volumemeasure.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;

import androidx.room.Room;

import com.geekymax.volumemeasure.entity.Record;
import com.google.ar.sceneform.math.Vector3;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class HistoryManager {
    private static final String TAG = "Geeky-HistoryManager";
    private static HistoryManager historyManager;
    private AppDatabase db;
    private Context context;

    private HistoryManager(Context context) {
        this.context = context;
        db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "measure2.db").build();
    }

    public static HistoryManager getInstance(Context context) {
        if (historyManager == null) {
            historyManager = new HistoryManager(context);
        }
        return historyManager;
    }

    public static HistoryManager getInstance() {
        return getInstance(null);
    }

    public void saveMeasureRecord(String id, SurfaceView surfaceView, Vector3 size) {
        Date now = new Date();
        Record record = new Record(id, now, "未命名", size);
        Log.d(TAG, "saveMeasureRecord: id: " + record.uid);
        AsyncTask.execute(() -> {
            db.recordDao().insert(record);
        });
        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getDrawingCache());
        PixelCopy.request(surfaceView, bitmap, copyResult -> {
            FileManager.outputBitmap(id, bitmap);
        }, new Handler());
    }

    public void getAllRecord(Consumer<List<Record>> action) {
        AsyncTask.execute(() -> {
            List<Record> records = db.recordDao().getAll();
            action.accept(records);
        });

    }

    public void deleteRecordById(String id) {
        Record record = new Record(id);
        AsyncTask.execute(() -> db.recordDao().delete(record));
    }

    public void shareHistory(Record record) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        // 比如发送二进制文件数据流内容（比如图片、视频、音频文件等等）
        // 指定发送的内容 (EXTRA_STREAM 对于文件 Uri )
        byte[] bytes = FileManager.bitmap2Bytes(record.bitmap, 300);
        sendIntent.putExtra(Intent.EXTRA_STREAM, bytes);
        // 指定发送内容的类型 (MIME type)
        sendIntent.setType("image/jpeg");
        context.startActivity(Intent.createChooser(sendIntent, "Share to..."));
    }

    public void clearHistory() {
        AsyncTask.execute(() -> {
            List<Record> records = db.recordDao().getAll();
            db.recordDao().deleteAll(records);
        });

    }
}
