package com.geekymax.volumemeasure.manager;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.geekymax.volumemeasure.MyArFragment;
import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.entity.ArState;
import com.geekymax.volumemeasure.entity.OnSceneUpdateListener;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import at.markushi.ui.CircleButton;

public class StateController implements OnSceneUpdateListener {
    private static final String TAG = "Geeky-StateController";

    // AR相关对象
    MyArFragment arFragment;
    private ArSceneView sceneView;
    private Session arSession;
    private ArState arState;

    // sceneform中模型的 anchor 和root node
    Plane mainPlane;
    AnchorNode anchorNode;
    Node rootNode;

    // 控件
    private ImageView translucenceDown;
    private CircleButton measureButton;
    private ImageButton testButton;
    private ImageButton refreshButton;
    private TextView infoText;
    // manager
    private BoxManager boxManager;
    private MaterialManager materialManager;
    private MeasureManager measureManager;

    // 主活动
    private AppCompatActivity activity;

    private void onClickMeasureButton() {
        if (arState != ArState.READY) {
            return;
        }
        setArState(ArState.COLLECTING);
        anchorNode = measureManager.startMeasuring(arSession, mainPlane, sceneView.getHeight(), sceneView.getWidth(), b -> {
            setArState(ArState.DONE);
            Log.d(TAG, "onClickMeasureButton: DrawBox");
            if (anchorNode != null) {
                anchorNode.setParent(sceneView.getScene());
                rootNode = new Node();
                rootNode.setParent(anchorNode);
                rootNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), b.getRotationAngle()));
                boxManager.drawBox(rootNode, b.getX(), b.getZ(), b.getY());
            }
        });

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
        } else if (arState == ArState.COLLECTING) {
            // 当前状态为收集中时,以一定的时间间隔向MeasureManager发送数据包
            boolean collectResult = measureManager.collectPointData(arSession);
            if (collectResult) {
                // collecting结束,进入measuring状态
                setArState(ArState.MEASURING);
            }
        }
    }

    // 判断是否已有定位到的平面
    private boolean hasTrackingPlane(Session session) {
        if (session == null) {
            return false;
        }
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                mainPlane = plane;
                return true;
            }
        }
        return false;
    }

    // 改变控件的可用状态
    private void changeWidgetState(boolean available) {
        if (available) {
            measureButton.setVisibility(View.VISIBLE);
            translucenceDown.setVisibility(View.VISIBLE);
            testButton.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
        } else {
            measureButton.setVisibility(View.INVISIBLE);
            translucenceDown.setVisibility(View.INVISIBLE);
            testButton.setVisibility(View.INVISIBLE);
            refreshButton.setVisibility(View.INVISIBLE);
        }
    }

    // 初始化控件等元素
    public void initWidget(AppCompatActivity activity) {
        this.activity = activity;
        materialManager = MaterialManager.getInstance(activity);
        boxManager = BoxManager.getInstance(activity);
        measureManager = MeasureManager.getInstance(activity);
        setArState(ArState.INITIAL);
        // 获取Fragment
        arFragment = (MyArFragment) activity.getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        assert arFragment != null;
        arFragment.setOnUpdateListener(this);
        translucenceDown = activity.findViewById(R.id.down);
        measureButton = activity.findViewById(R.id.measure_btn);
        testButton = activity.findViewById(R.id.test_btn);
        refreshButton = activity.findViewById(R.id.refresh_btn);
        infoText = activity.findViewById(R.id.info_text);
        measureButton.setVisibility(View.INVISIBLE);
        translucenceDown.setVisibility(View.INVISIBLE);
        testButton.setVisibility(View.INVISIBLE);
        refreshButton.setVisibility(View.INVISIBLE);
        measureButton.setOnClickListener(v -> this.onClickMeasureButton());
        testButton.setOnClickListener(v -> {
            if (arState != ArState.DONE) {
                return;
            }
            boxManager.moveFace(0.1f);
        });
        refreshButton.setOnClickListener(v -> {
            if (arState != ArState.DONE) {
                return;
            }
            boxManager.moveFace(-0.1f);
        });
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
}
