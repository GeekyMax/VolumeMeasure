package com.geekymax.volumemeasure.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;

import com.geekymax.volumemeasure.constant.SettingConstant;
import com.geekymax.volumemeasure.entity.MyPoint;
import com.geekymax.volumemeasure.measurer.DefaultMeasurer;
import com.geekymax.volumemeasure.measurer.MeasureCallback;
import com.geekymax.volumemeasure.measurer.Measurer;
import com.geekymax.volumemeasure.measurer.OfflineMeasurer;
import com.geekymax.volumemeasure.measurer.OnlineMeasurer;
import com.geekymax.volumemeasure.util.TimeLogger;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasureManager {
    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final String TAG = "Geeky-MeasureManager";
    private static MeasureManager instance;
    private static final Object INSTANCE_LOCK = new Object();
    private Context context;

    private Trackable mainPlane;
    private double mainPlaneHeight;
    private MeasureCallback measureCallback;
    private AnchorNode anchorNode;


    private boolean collecting;
    private Map<Integer, MyPoint> pointMap = new HashMap<>();

    private Map<String, Measurer> measurerMap;
    private long millis;

    public long getMillis() {
        return millis;
    }

    private MeasureManager(Context context) {
        this.context = context;
        measurerMap = new HashMap<>();
        measurerMap.put(SettingConstant.MEASURER_DEFAULT, new DefaultMeasurer());
        measurerMap.put(SettingConstant.MEASURER_OFFLINE, new OfflineMeasurer());
        measurerMap.put(SettingConstant.MEASURER_ONLINE, new OnlineMeasurer());
    }

    public static MeasureManager getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new MeasureManager(context);
            }
            return instance;
        }
    }

    /**
     * 初始化测量,记录基本信息
     * 开始收集点云信息
     *
     * @param session
     * @param windowHeight
     * @param windowWidth
     * @param callback
     * @return
     */
    public AnchorNode initMeasuring(Session session, float windowHeight, float windowWidth, MeasureCallback callback) {
        Log.d(TAG, "initMeasuring: ");
        try {
            Frame frame = session.update();
            List<HitResult> hitResults = frame.hitTest(windowWidth / 2, windowHeight / 2);
//            setMainPlane(mainPlane);
            float maxHeight = -10000;
            HitResult mainHit = null;
            for (HitResult hitResult : hitResults) {
                float planeHeight = hitResult.getHitPose().getTranslation()[1];
                if (planeHeight > maxHeight) {
                    maxHeight = planeHeight;
                    mainHit = hitResult;
                }
            }
            if (mainHit != null) {
                mainPlane = mainHit.getTrackable();
                Anchor anchor = mainHit.createAnchor();
                Pose centerPose = mainHit.getHitPose();
                this.mainPlaneHeight = centerPose.getTranslation()[1];
                anchorNode = new AnchorNode(anchor);
            } else {
                return null;
            }

            this.measureCallback = callback;
            collecting = true;

            // todo 一些初始化工作
            pointMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return anchorNode;
    }

    /**
     * 信息收集完成后,开始正式测量
     *
     * @param session
     */
    public void startMeasuring(Session session) {
        collecting = false;
        // 起新线程专门用于测量
        AsyncTask.execute(this::run);
//        Thread thread = new Thread(this::run);
//        thread.run();
    }

    private void run() {
        try {
            TimeLogger.getLogger().log("in run 1");

            INDArray pointData = map2NDArray(pointMap);
            FileManager.getInstance().outputPoint(pointMap);
            FileManager.getInstance().outputPlaneHeight(mainPlaneHeight);
            TimeLogger.getLogger().log("in run 2");

            Log.d(TAG, "run: PointData: " + pointData);
            Measurer measurer = measurerMap.get(SettingManager.getInstance().getMeasurer(context));
            if (measurer != null) {
                Date start = new Date();
                measurer.measure(pointData, mainPlaneHeight, measureCallback);
                Date end = new Date();
                millis = end.getTime() - start.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private INDArray map2NDArray(Map<Integer, MyPoint> map) {
        if (map.size() == 0) {
            return Nd4j.empty();
        }
        TimeLogger.getLogger().log("map2NDArray:1");
        List<double[]> list = new ArrayList<>();
        map.forEach((i, p) -> {
            list.add(new double[]{Double.valueOf(i), (double) p.x, (double) p.z, (double) p.y, (double) p.confidence});
        });
        TimeLogger.getLogger().log("map2NDArray:2");

        double[][] d = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            d[i] = list.get(i);
        }
        TimeLogger.getLogger().log("map2NDArray:3");
        INDArray res = Nd4j.create(d);
        TimeLogger.getLogger().log("map2NDArray:5 +size:" + d.length);
        return res;

    }

    void onUpdate(ArSceneView arSceneView) {
        if (collecting) {
            Frame frame = arSceneView.getArFrame();
            if (frame == null) {
                return;
            }
            try (PointCloud pointCloud = frame.acquirePointCloud()) {
                FloatBuffer pointsBuffer = pointCloud.getPoints();
                IntBuffer idBuffer = pointCloud.getIds();
                while (idBuffer.remaining() > 0) {
                    int id = idBuffer.get();
                    float x = pointsBuffer.get();
                    float y = pointsBuffer.get();
                    float z = pointsBuffer.get();
                    float confidence = pointsBuffer.get();
                    pointMap.put(id, new MyPoint(x, y, z, confidence));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        collecting = false;
        pointMap.clear();
    }
}
