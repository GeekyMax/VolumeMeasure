package com.geekymax.volumemeasure.manager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.geekymax.volumemeasure.HistoryActivity;
import com.geekymax.volumemeasure.MyArFragment;
import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.SettingsActivity;
import com.geekymax.volumemeasure.entity.ArState;
import com.geekymax.volumemeasure.entity.OnSceneUpdateListener;
import com.geekymax.volumemeasure.measurer.MeasureBundle;
import com.geekymax.volumemeasure.measurer.MeasureCallback;
import com.geekymax.volumemeasure.util.IdGenerator;
import com.geekymax.volumemeasure.util.TimeLogger;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.meetic.marypopup.MaryPopup;

import at.markushi.ui.CircleButton;

public class StateController implements OnSceneUpdateListener {
    private float startY; //手指刚开始滑动时记录点 Y轴
    private float startX; //手指刚开始滑动时记录点 X轴
    private static final String TAG = "Geeky-StateController";
    private long startTime;
    // AR相关对象
    MyArFragment arFragment;
    private ArSceneView sceneView;
    private Session arSession;
    private ArState arState;

    // sceneform中模型的 anchor 和root node
//    Plane mainPlane;
    AnchorNode anchorNode;
    Node rootNode;

    // 控件
    private ImageView translucenceDown;
    private CircleButton collectButton;
    //    private CircleButton collectDoneButton;
//    private CircleButton restartButton;
    private ImageButton screenshotButton;
    private ImageButton refreshButton;
    private TextView infoText;
    private ImageButton settingButton;
    private ImageButton historyButton;
    private ConstraintLayout slideView;
    private MaryPopup sizePopup;
    // manager
    private BoxManager boxManager;
    private MaterialManager materialManager;
    private MeasureManager measureManager;
    private HistoryManager historyManager;

    private MeasureBundle measureBundle;
    // 主活动
    private AppCompatActivity activity;

    private int startScreenshot = -1;

    private void onClickCollectButton() {
        if (arState == ArState.READY) {
            setArState(ArState.COLLECTING);
            anchorNode = measureManager.initMeasuring(arSession, sceneView.getHeight(), sceneView.getWidth(), new MeasureCallback() {
                @Override
                public void onFail(String msg) {
                    TimeLogger.getLogger().log("fail 1");
                    Toast.makeText(activity, "测量失败:" + msg, Toast.LENGTH_SHORT).show();
                    restart();
                }

                @Override
                public void onSuccess(String msg, double[] result, float angle) {
                    TimeLogger.getLogger().log("1");
                    setArState(ArState.DONE);
                    Log.d(TAG, "OnSuccess");
                    measureBundle = new MeasureBundle(result, angle);
                    showText(measureBundle.toString());
                    measureBundle.setAction(bundle -> {
                        showText(bundle.toString());
                    });
                    if (anchorNode != null) {
                        TimeLogger.getLogger().log("2");

                        anchorNode.setParent(sceneView.getScene());
                        rootNode = new Node();
                        rootNode.setParent(anchorNode);
                        rootNode.setLocalPosition(measureBundle.getCenter());
                        rootNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), angle));
                        TimeLogger.getLogger().log("3");

                        boxManager.drawBox(rootNode, measureBundle);
                        TimeLogger.getLogger().log("4");
                        if (SettingManager.getInstance().autoSaveHistory(activity)) {
                            TimeLogger.getLogger().log("5");
                            startScreenshot = 10;
                        }
                    }
                }
            });
            collectButton.setImageResource(R.drawable.done);
        } else if (arState == ArState.COLLECTING) {

            setArState(ArState.COLLECTING_DONE);
            TimeLogger.getLogger().log("startMeasuring");

            measureManager.startMeasuring(arSession);
            collectButton.setImageResource(R.drawable.refresh_line);
        } else if (arState == ArState.DONE) {
            collectButton.setImageResource(R.drawable.add_line);
            restart();
        }
    }


    // 重新开始一次新的测量
    public void restart() {
        this.arState = ArState.READY;
        BoxManager.getInstance().clear();
        showText("ready");
    }

    // 每帧更新时调用
    @Override
    public void onUpdate(ArSceneView arSceneView) {
        if (arSceneView == null) {
            return;
        }
        if (arState == ArState.INITIAL && hasTrackingPlane(arSceneView.getSession())) {
            setArState(ArState.READY);
            changeWidgetState(true);
        }
        if (measureManager != null) {
            measureManager.onUpdate(arSceneView);
        }
        // 数帧之后,自动截图
        if (startScreenshot > 0) {
            startScreenshot--;
        } else if (startScreenshot == 0) {
            startScreenshot--;
            saveSnapshot();
        }
    }

    // 判断是否已有定位到的平面
    private boolean hasTrackingPlane(Session session) {
        if (session == null) {
            return false;
        }
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
//                mainPlane = plane;
                return true;
            }
        }
        return false;
    }

    // 改变控件的可用状态
    private void changeWidgetState(boolean available) {
        if (available) {
            collectButton.setVisibility(View.VISIBLE);
            translucenceDown.setVisibility(View.VISIBLE);
            screenshotButton.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
        } else {
            collectButton.setVisibility(View.INVISIBLE);
            translucenceDown.setVisibility(View.INVISIBLE);
            screenshotButton.setVisibility(View.INVISIBLE);
            refreshButton.setVisibility(View.INVISIBLE);
        }
    }

    // 初始化控件等元素
    @SuppressLint("ClickableViewAccessibility")
    public void initWidget(AppCompatActivity activity) {
        this.activity = activity;
        materialManager = MaterialManager.getInstance(activity);
        boxManager = BoxManager.getInstance(activity, this);
        measureManager = MeasureManager.getInstance(activity);
        historyManager = HistoryManager.getInstance(activity);
        setArState(ArState.INITIAL);
        // 获取Fragment
        arFragment = (MyArFragment) activity.getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        assert arFragment != null;
        arFragment.setOnUpdateListener(this);
        translucenceDown = activity.findViewById(R.id.down);
        collectButton = activity.findViewById(R.id.collect_btn);
//        collectDoneButton = activity.findViewById(R.id.collect_done_btn);
        screenshotButton = activity.findViewById(R.id.test_btn);
        refreshButton = activity.findViewById(R.id.refresh_btn);
        infoText = activity.findViewById(R.id.info_text);
        settingButton = activity.findViewById(R.id.setting_btn);
        historyButton = activity.findViewById(R.id.history_btn);
        slideView = activity.findViewById(R.id.slide_view);
        collectButton.setVisibility(View.INVISIBLE);
//        collectDoneButton.setVisibility(View.INVISIBLE);
        translucenceDown.setVisibility(View.INVISIBLE);
        screenshotButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);
        slideView.setVisibility(View.INVISIBLE);
//        collectDoneButton.setOnClickListener(v -> this.onClickCollectDoneButton());
        collectButton.setOnClickListener(v -> this.onClickCollectButton());
        screenshotButton.setOnClickListener(v -> {
            if (arState != ArState.DONE) {
                return;
            }
            saveSnapshot();
        });
        refreshButton.setOnClickListener(v -> {
            if (arState != ArState.DONE) {
                return;
            }
            boxManager.moveFace(-0.1f);
        });
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SettingsActivity.class);
            activity.startActivity(intent);
        });
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, HistoryActivity.class);
            activity.startActivity(intent);
        });
        arFragment.getArSceneView().setDrawingCacheEnabled(true);
        slideView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    //1.按下时记录相关值
                    startY = event.getY();
                    startX = event.getX();
                    startTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE://手指滑动
                    //2.滑动时记录相关值
                    float endY = event.getY();
                    float endX = event.getX();
                    float distanceY = startY - endY;//滑动距离
                    float distanceX = startX - endX;//滑动距离
                    Log.d(TAG, "initWidget: OnTouch" + distanceX + ";" + distanceY);
                    float moveFaceDist = distanceX / 10000;
                    boxManager.moveFace(moveFaceDist);
                    startX = endX;
                    startY = endY;
                    break;
                case MotionEvent.ACTION_UP://手指离开
                    break;
            }
            return true;
        });


        infoText.setOnClickListener(v -> {
            if (arState == ArState.DONE) {
                View popView = LayoutInflater.from(activity).inflate(R.layout.size_popup, null);
                Button ok = popView.findViewById(R.id.ok_btn);
                ok.setOnClickListener(v1 -> sizePopup.close(true));
                popView = setPopView(popView);
                sizePopup = MaryPopup.with(activity)
                        .cancellable(true)
                        .blackOverlayColor(Color.parseColor("#DD444444"))
                        .backgroundColor(Color.parseColor("#EFF4F5"))
                        .content(popView)
                        .draggable(true)
                        .from(infoText);
                sizePopup.show();
            }

        });

    }

    private View setPopView(View popView) {
        TextView v1 = popView.findViewById(R.id.v_1);
        TextView v2 = popView.findViewById(R.id.v_2);
        TextView v3 = popView.findViewById(R.id.v_3);
        TextView v4 = popView.findViewById(R.id.v_4);
        TextView u1 = popView.findViewById(R.id.unit_1);
        TextView u2 = popView.findViewById(R.id.unit_2);
        TextView u3 = popView.findViewById(R.id.unit_3);
        TextView u4 = popView.findViewById(R.id.unit_4);
        Vector3 size = BoxManager.getInstance().getMeasureBundle().getLength();
        v1.setText("" + size.x);
        v2.setText("" + size.y);
        v3.setText("" + size.z);
        v4.setText("" + BoxManager.getInstance().getMeasureBundle().getVolume());
        u1.setText("cm");
        u2.setText("cm");
        u3.setText("cm");
        u4.setText("cm3");
        return popView;

    }

    // 当activity resume时调用
    public void onPostResume() {
        Log.d(TAG, "onResume: Post Activity");
        arSession = arFragment.getArSceneView().getSession();
        sceneView = arFragment.getArSceneView();
    }

    private void setArState(ArState arState) {
        this.arState = arState;
        switch (arState) {
            case INITIAL:
                showText("initial");
                break;
            case COLLECTING:
                showText("collecting");
                break;
            case READY:
                showText("ready");
                break;
            case MEASURING:
                showText("measuring");
                break;
            case DONE:
                showText("done");
                break;
        }
    }

    private void showText(String s) {
        if (infoText == null) {
            return;
        }
        if (s == null || "".equals(s)) {
            infoText.setVisibility(View.INVISIBLE);
        } else {
            infoText.setVisibility(View.VISIBLE);
            infoText.setText(s);
        }
    }

    public void showSlideView(int visibility) {
        slideView.setVisibility(visibility);
    }

    private void saveSnapshot() {
        String id = IdGenerator.genUuid();
        Vector3 size = BoxManager.getInstance().getMeasureBundle().getLength();
        Log.d(TAG, "onPixelCopyFinished: start");
        historyManager.saveMeasureRecord(id, arFragment.getArSceneView(), size);
        Toast.makeText(activity, "记录保存成功!", Toast.LENGTH_SHORT).show();
    }

    public boolean onBackPressed() {
        return !sizePopup.close(true);
    }
}
