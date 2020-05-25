package com.geekymax.volumemeasure.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.room.Room;

import com.geekymax.volumemeasure.entity.Record;
import com.geekymax.volumemeasure.util.IdGenerator;
import com.google.ar.sceneform.math.Vector3;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecordManager {
    private static final String TAG = "Geeky-RecordManager";
    private static RecordManager instance;
    private AppDatabase db;
    private Context context;
    private Activity activity;
    private OkHttpClient client;

    private RecordManager(Activity context) {
        this.context = context;
        this.activity = context;
        db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "measure2.db").build();
        client = new OkHttpClient();
    }

    public static RecordManager getInstance(Activity context) {
        if (instance == null) {
            instance = new RecordManager(context);
        }
        return instance;
    }

    public static RecordManager getInstance() {
        return getInstance(null);
    }

    public void saveMeasureRecord(String name, SurfaceView surfaceView, Vector3 size) {
        String id = IdGenerator.genUuid();
        Date now = new Date();
        Record record = new Record(id, now, name != null ? name : "", size);
        Log.d(TAG, "saveMeasureRecord: id: " + record.uid);
        AsyncTask.execute(() -> {
            db.recordDao().insert(record);
        });

        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getDrawingCache());
        PixelCopy.request(surfaceView, bitmap, copyResult -> {
            FileManager.outputBitmap(id, bitmap);
        }, new Handler());
        upload(record);
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


    public void upload(Record record) {
        if (SettingManager.getInstance().isUpload(context)) {
            try {
                String webhookUrl = SettingManager.getInstance().getWebhookUrl(context);
                if ("".equals(webhookUrl)) {
                    return;
                }
                // 开始开启子线程异步上传记录
                AsyncTask.execute(() -> {
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    JSONObject json = new JSONObject();
                    try {
                        json.put("name", record.name);
                        json.put("x", record.x);
                        json.put("y", record.y);
                        json.put("z", record.y);
                        json.put("volume", record.getVolume());
                        json.put("date", record.date);
                        json.put("uid", record.uid);
                        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
                        Request request = new Request.Builder()
                                .post(requestBody)
                                .url(webhookUrl)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                                activity.runOnUiThread(() -> Toast.makeText(context, "上传发生错误!请检查设置", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}
