/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geekymax.volumemeasure;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geekymax.volumemeasure.entity.ArState;
import com.geekymax.volumemeasure.entity.BoxEdge;
import com.geekymax.volumemeasure.entity.BoxFace;
import com.geekymax.volumemeasure.entity.BoxVertex;
import com.geekymax.volumemeasure.entity.OnSceneUpdateListener;
import com.geekymax.volumemeasure.manager.MaterialManager;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;


/**
 * This is an example activity that uses the Sceneform UX package_1 to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity implements OnSceneUpdateListener {
    private static final String TAG = "Geeky-Activity";
    private static final double MIN_OPENGL_VERSION = 3.0;
    public static final float PI = 3.1415926f;
    private MyArFragment arFragment;
    private Session arSession;
    // 当前全局状态
    private ArState arState;

    // 控件
    private ImageView translucenceDown;
    private CircleButton measureButton;

    // 已渲染的图形对象
    List<Vector3> locations = new ArrayList<>();
    List<BoxVertex> boxVertices = new ArrayList<>();
    List<BoxEdge> boxEdges = new ArrayList<>();
    List<BoxFace> boxFaces = new ArrayList<>();

    // anchor 和parent
    AnchorNode anchorNode;
    Node rootNode;

    // manager
    MaterialManager materialManager;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        arState = ArState.INITIAL;
        arFragment = (MyArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        Log.d(TAG, "onCreate: " + arSession);
        arFragment.setOnUpdateListener(this);
        arFragment.getArSceneView().getPlaneRenderer().setShadowReceiver(false);
        materialManager = MaterialManager.getInstance(this);
        findWidget();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "onResume: Post Activity");
        arSession = arFragment.getArSceneView().getSession();
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    // 每帧更新时调用
    @Override
    public void onUpdate(ArSceneView arSceneView) {
        if (arSceneView == null) {
            return;
        }
        if (arState == ArState.INITIAL && hasTrackingPlane(arSceneView.getSession())) {
            this.arState = ArState.READY;
            changeWidgetState(true);
        }
    }

    private boolean hasTrackingPlane(Session session) {
        if (session == null) {
            return false;
        }
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    // 界面操作相关函数
    private void findWidget() {
        translucenceDown = findViewById(R.id.down);
        measureButton = findViewById(R.id.measure_btn);
        measureButton.setVisibility(View.INVISIBLE);
        translucenceDown.setVisibility(View.INVISIBLE);
        measureButton.setOnClickListener(v -> {
            if (arState != ArState.READY) {
                return;
            }
            int height = arFragment.getArSceneView().getHeight();
            int width = arFragment.getArSceneView().getWidth();
            Log.d(TAG, "onClick: Height,width is " + height + ", " + width);
            float x = width / 2.0f;
            float y = height / 2.0f;
            try {
                Frame frame = arSession.update();
                List<HitResult> hitResults = frame.hitTest(x, y);
                if (hitResults.size() > 0) {
                    arState = ArState.MEASURING;
                    // Create the Anchor.
                    Anchor anchor = hitResults.get(0).createAnchor();
                    anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    rootNode = new Node();
                    rootNode.setParent(anchorNode);
                    rootNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1, 0), 30));
                    drawBox(rootNode, 0.2f, 0.4f, 0.3f);
                    arState = ArState.DONE;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void changeWidgetState(boolean available) {
        if (available) {
            measureButton.setVisibility(View.VISIBLE);
            translucenceDown.setVisibility(View.VISIBLE);
        } else {
            measureButton.setVisibility(View.INVISIBLE);
            translucenceDown.setVisibility(View.INVISIBLE);
        }
    }

    private void drawBox(Node parent, float a, float b, float h) {
        clearBox();
        for (float i = 0; i <= 1; i++) {
            for (float j = 0; j <= 1; j++) {
                for (float k = 0; k <= 1; k++) {
                    locations.add(new Vector3((i - 1 / 2f) * a, h * (j - 1 / 2f), (k - 1 / 2f) * b));
                }
            }
        }
        ModelRenderable sphere = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f, 0.0f), MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_VERTEX_DEFAULT));
        sphere.setShadowCaster(false);
        sphere.setShadowReceiver(false);
        locations.forEach(loc -> {
            BoxVertex boxVertex = new BoxVertex(loc, parent, sphere);
            boxVertices.add(boxVertex);
            boxVertex.update();
        });
        for (int i = 0; i < boxVertices.size(); i++) {
            for (int j = 0; j < boxVertices.size(); j++) {
                Log.d(TAG, "drawBox: boxEdge: " + i + " " + j + ":" + isEdge(i, j));
                if (isEdge(i, j)) {
                    BoxEdge boxEdge = new BoxEdge(boxVertices.get(i), boxVertices.get(j), parent, MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_EDGE_DEFAULT));
                    boxEdges.add(boxEdge);
                    boxEdge.update();
                }
            }
        }

        for (int i = 0; i < boxVertices.size(); i++) {
            for (int j = 0; j < boxVertices.size(); j++) {
                for (int k = 0; k < boxVertices.size(); k++) {
                    for (int l = 0; l < boxVertices.size(); l++) {
                        if (isFace(i, j, k, l)) {
                            BoxFace boxFace = new BoxFace(parent, MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_FACE_DEFAULT));
                            boxFace.addVertex(boxVertices.get(i));
                            boxFace.addVertex(boxVertices.get(j));
                            boxFace.addVertex(boxVertices.get(l));
                            boxFace.addVertex(boxVertices.get(k));
                            boxFaces.add(boxFace);
                            boxFace.update();

                        }
                    }
                }
            }
        }


    }

    private void clearBox() {
        boxVertices.clear();
        boxEdges.clear();
        boxFaces.clear();
    }

    private boolean isEdge(int i, int j) {
        if (i <= j) {
            return false;
        }
        int i3 = i % 2;
        i = i / 2;
        int i2 = i % 2;
        i = i / 2;
        int i1 = i % 2;
        int j3 = j % 2;
        j = j / 2;
        int j2 = j % 2;
        j = j / 2;
        int j1 = j % 2;
        return i1 == j1 && i2 == j2 || i1 == j1 && i3 == j3 || i3 == j3 && i2 == j2;
    }

    private boolean isFace(int i, int j, int k, int l) {
        if (!(i > j && j > k && k > l)) {
            return false;
        }
        int i3 = i % 2;
        i = i / 2;
        int i2 = i % 2;
        i = i / 2;
        int i1 = i % 2;
        int j3 = j % 2;
        j = j / 2;
        int j2 = j % 2;
        j = j / 2;
        int j1 = j % 2;
        int k3 = k % 2;
        k = k / 2;
        int k2 = k % 2;
        k = k / 2;
        int k1 = k % 2;
        int l3 = l % 2;
        l = l / 2;
        int l2 = l % 2;
        l = l / 2;
        int l1 = l % 2;
        return i1 == j1 && j1 == k1 && k1 == l1 || i2 == j2 && j2 == k2 && k2 == l2 || i3 == j3 && j3 == k3 && k3 == l3;

    }

}
