package com.geekymax.volumemeasure.entity;

import android.content.SharedPreferences;
import android.util.Log;

import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.manager.BoxManager;
import com.geekymax.volumemeasure.manager.MaterialManager;
import com.geekymax.volumemeasure.manager.SettingManager;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.ArrayList;
import java.util.List;

public class BoxEdge {
    private static final String TAG = "Geeky-BoxEdge";
    private List<BoxVertex> vertices = new ArrayList<>();
    private Node parent;
    private Node node;
    private Node textNode;

    private Material material;

    private int i;
    private int j;
    // 顶面四条棱关联四个侧面
    private BoxFace relativeSideFace;

    public BoxEdge(Node parent, Material material) {
        this.parent = parent;
        this.material = material;
    }

    public void update() {
        if (node == null) {
            node = new Node();
            node.setOnTapListener((hitTestResult, motionEvent) -> {
                Log.d(TAG, "onTap: i=" + i + ";j=" + j);
                if (relativeSideFace != null) {
                    BoxManager.getInstance().setSelectedFace(relativeSideFace);
                }
            });

        }
        Vector3 difference = Vector3.subtract(vertices.get(0).getPosition(), vertices.get(1).getPosition());

        Vector3 directionFromTopToBottom = difference.normalized();
        float length = difference.length();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, length), Vector3.zero(), material);
        line.setShadowCaster(false);
        line.setShadowReceiver(false);
        node.setRenderable(line);
        node.setParent(parent);
        node.setLocalPosition(Vector3.add(vertices.get(0).getPosition(), vertices.get(1).getPosition()).scaled(0.5f));
        node.setLocalRotation(rotationFromAToB);
        if (showText()) {
            if (textNode == null) {
                textNode = new Node();
            }
            textNode.setEnabled(true);
            MaterialManager.getInstance().getArInfo(viewRenderable -> {
                viewRenderable.setShadowCaster(false);
                viewRenderable.setShadowReceiver(false);
                textNode.setParent(parent);
                textNode.setRenderable(viewRenderable);
                textNode.setLocalPosition(Vector3.add(getInfoLocalLocation(), node.getLocalPosition()));
                node.setLocalRotation(rotationFromAToB);
            });
        } else {
            if (textNode != null) {
                textNode.setEnabled(false);
            }
        }

    }

    public void addVertex(BoxVertex v) {
        v.addRelativeEdge(this);
        this.vertices.add(v);
    }

    public void setRelativeSideFace(BoxFace relativeSideFace) {
        Log.d(TAG, "setRelativeSideFace: ");
        this.relativeSideFace = relativeSideFace;
    }

    public void set(int i, int j) {
        this.i = i;
        this.j = j;
    }

    private boolean showText() {
        if (!BoxManager.getInstance().isShowArLabel()) {
            return false;
        }
        if (i == 7 || i == 2 || i == 3 || i == 6) {
            return true;
        }
        return false;
    }

    private Vector3 getInfoLocalLocation() {
        if ((i == 7 || i == 2 || i == 3 || i == 6) && (j == 7 || j == 2 || j == 3 || j == 6)) {
            return Vector3.zero();
        }
        if (i == 3 && j == 1) {
            return new Vector3(0.02f, 0f, -0.02f);
        } else if (i == 7 && j == 5) {
            return new Vector3(0.02f, 0f, 0.02f);
        } else if (i == 6 && j == 4) {
            return new Vector3(-0.02f, 0f, 0.02f);
        } else if (i == 2 && j == 0) {
            return new Vector3(-0.02f, 0f, -0.02f);
        }
        return Vector3.zero();
    }

    public void clear() {
        if (this.node != null) {
            this.node.setParent(null);
            this.node.setEnabled(false);
        }
        if (this.textNode != null) {
            this.textNode.setParent(null);
            this.textNode.setEnabled(false);
        }
    }
}
