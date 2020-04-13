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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.geekymax.volumemeasure.entity.ArState;
import com.geekymax.volumemeasure.entity.OnSceneUpdateListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;

/**
 * This is an example activity that uses the Sceneform UX package_1 to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity implements OnSceneUpdateListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private MyArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ViewRenderable infoRenderable;
    private Session arSession;
    private ArState arState;

    // 控件
    private ImageView translucenceDown;
    private CircleButton measureButton;

    private List<Node> vertexList = new ArrayList<>();

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
        arState = ArState.Initial;
        arFragment = (MyArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        Log.d(TAG, "onCreate: " + arSession);
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        ViewRenderable.builder()
                .setView(this, R.layout.ar_info)
                .build()
                .thenAccept(renderable -> infoRenderable = renderable);
        arFragment.setOnUpdateListener(this);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();
                    TransformableNode info = new TransformableNode(arFragment.getTransformationSystem());
                    info.setParent(anchorNode);
                    info.setRenderable(infoRenderable);
                    info.select();
                });
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

    @Override
    public void onUpdate(ArSceneView arSceneView) {
        if (arSceneView == null) {
            return;
        }
        if (arState == ArState.Initial && hasTrackingPlane(arSceneView.getSession())) {
            this.arState = ArState.Ready;
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
            int height = arFragment.getArSceneView().getHeight();
            int width = arFragment.getArSceneView().getWidth();
            Log.d(TAG, "onClick: Height,width is " + height + ", " + width);
            float x = width / 2.0f;
            float y = height / 2.0f;
            try {
                Frame frame = arSession.update();
                for (HitResult hitResult : frame.hitTest(x, y)) {
                    if (andyRenderable == null) {
                        return;
                    }
                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
//                    AnchorNode anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                    // Create the transformable andy and add it to the anchor.
//                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//                    andy.setParent(anchorNode);
//                    andy.setRenderable(andyRenderable);
//                    andy.select();
                    drawBox(anchor, 0.2f, 0.4f, 0.3f);
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

    private void drawBox(Anchor startAnchor, float a, float b, float h) {
        AnchorNode anchorNode = new AnchorNode(startAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        List<Vector3> locations = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int k = 0; k <= 1; k++) {
                    locations.add(Vector3.add(new Vector3(a * i, h * j, k * b), new Vector3(-a / 2, 0, -b / 2)));
                }
            }
        }
        drawPoint(anchorNode, locations);
        locations.forEach(v1 -> locations.forEach(v2 -> {
            Vector3 sub = Vector3.subtract(v1, v2);
            if (sub.x == 0 && sub.y == 0 && sub.z > 0 || sub.x == 0 && sub.z == 0 && sub.y > 0 || sub.z == 0 && sub.y == 0 && sub.x > 0) {
                drawLine(anchorNode, v1, v2);
            }
        }));
    }

    private void drawPoint(AnchorNode anchorNode, List<Vector3> locations) {
        MaterialFactory.makeOpaqueWithColor(this, new Color(0.53f, 0.92f, 0f))
                .thenAccept(material -> {
                    ModelRenderable sphere = ShapeFactory.makeSphere(0.01f, new Vector3(0.0f, 0.0f, 0.0f), material);
                    for (Vector3 loc : locations) {
                        Node node = new Node();
                        node.setRenderable(sphere);
                        node.setParent(anchorNode);
                        node.setLocalPosition(loc);
                        vertexList.add(node);
                    }
                });
    }

    private void drawLine(AnchorNode anchorNode, Vector3 firstPosition, Vector3 secondPosition) {
        MaterialFactory.makeOpaqueWithColor(this, new Color(0.3f, 0.87f, 0f)).thenAccept(material -> {
            Vector3 difference = Vector3.subtract(firstPosition, secondPosition);

            Vector3 directionFromTopToBottom = difference.normalized();
            Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, difference.length()), Vector3.zero(), material);
            Node lineNode = new Node();
            lineNode.setRenderable(line);
            lineNode.setParent(anchorNode);
            lineNode.setLocalPosition(Vector3.add(firstPosition, secondPosition).scaled(0.5f));
            lineNode.setLocalRotation(rotationFromAToB);
        });
    }

    private void drawSideArea(AnchorNode anchorNode, Vector3 firstPosition, Vector3 secondPosition, float height) {
        MaterialFactory.makeOpaqueWithColor(this, new Color(1f, 0, 0)).thenAccept(material -> {
            Vector3 difference = Vector3.subtract(firstPosition, secondPosition);

            Vector3 directionFromTopToBottom = difference.normalized();
            Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, height, difference.length()), Vector3.zero(), material);
            Node areaNode = new Node();
            areaNode.setRenderable(line);
            areaNode.setParent(anchorNode);
            areaNode.setLocalPosition(Vector3.add(firstPosition, secondPosition).scaled(0.5f));
            areaNode.setLocalRotation(rotationFromAToB);
        });
    }
}
