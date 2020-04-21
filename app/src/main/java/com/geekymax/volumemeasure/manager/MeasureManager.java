package com.geekymax.volumemeasure.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.geekymax.volumemeasure.callback.MeasureBundle;
import com.geekymax.volumemeasure.callback.MeasuringDoneCallback;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MeasureManager {
    private static final String TAG = "Geeky-MeasureManager";
    private static MeasureManager instance;
    private static final Object INSTANCE_LOCK = new Object();
    private Context context;

    private float windowHeight;
    private float windowWidth;

    private Trackable mainTrackable;
    private MeasuringDoneCallback measuringDoneCallback;
    private AnchorNode anchorNode;

    private Date startTime;

    private Handler uiHandler = new Handler();

    private MeasureManager(Context context) {
        this.context = context;
    }

    static MeasureManager getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new MeasureManager(context);
            }
            return instance;
        }
    }

    public AnchorNode startMeasuring(Session session, Trackable trackable, float windowHeight, float windowWidth, MeasuringDoneCallback callback) {
        Log.d(TAG, "startMeasuring: ");
        try {
            Frame frame = session.update();
            List<HitResult> hitResults = frame.hitTest(windowWidth / 2, windowHeight / 2);
            this.mainTrackable = trackable;
            hitResults.forEach(v -> {
                if (v.getTrackable().equals(trackable)) {
                    Anchor anchor = v.createAnchor();
                    anchorNode = new AnchorNode(anchor);
                }
            });
            this.measuringDoneCallback = callback;
            this.startTime = new Date();
            // todo 一些初始化工作
        } catch (Exception e) {
            e.printStackTrace();
        }
        return anchorNode;
    }

    /**
     * 以一定的时间间隔收集环境中的点信息
     *
     * @return 是否收集完毕
     */
    public boolean collectPointData(Session session) {
        boolean res = collectDone();
        if (res) {
            // 起新线程专门用于测量
            Thread thread = new Thread(this::run);
            thread.run();
        }
        return res;
    }


    private void run() {
        try {
            Log.d(TAG, "run: ");
            // todo
            Thread.sleep(2000);
            uiHandler.post(() -> measuringDoneCallback.callback(new MeasureBundle(true, 0, Vector3.zero(), 0.4f, 0.3f, 0.2f)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean collectDone() {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.SECOND, -2);
        return rightNow.getTime().after(startTime);
    }


}
