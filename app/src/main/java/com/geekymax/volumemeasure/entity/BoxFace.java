package com.geekymax.volumemeasure.entity;

import android.util.Log;

import com.geekymax.volumemeasure.manager.MaterialManager;
import com.geekymax.volumemeasure.math.Matrix3f;
import com.geekymax.volumemeasure.util.MathUtil;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

public class BoxFace {
    private static final String TAG = "Geeky-BoxFace";
    private Node parent;
    private Node node;
    private List<BoxVertex> vertices; // 1-2-4-3为一个面
    private Material material;

    private Matrix3f rotationMatrix;
    private static final float faceThickness = 0.005f;

    public BoxFace(Node parent, Material material) {
        this.parent = parent;
        this.material = material;
        this.vertices = new ArrayList<>();
    }

    public BoxFace(Node parent, List<BoxVertex> vertices, Material material) {
        this.parent = parent;
        this.vertices = vertices;
        this.material = material;

    }

    public void update() {
        if (node == null) {
            node = new Node();
            node.setOnTapListener((hitTestResult, motionEvent) -> {
                Log.d(TAG, "update: hitTestResult:" + hitTestResult + "; motionEvent:" + motionEvent);
                this.material = MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_FACE_SELECTED);
                update();
            });
        }
        Vector3 firstPos = vertices.get(0).getPosition();
        Vector3 secondPos = vertices.get(1).getPosition();
        Vector3 thirdPos = vertices.get(2).getPosition();
        Vector3 v1 = Vector3.subtract(secondPos, firstPos);
        Vector3 v2 = Vector3.subtract(thirdPos, secondPos);
        Log.d(TAG, "update: V1:" + v1 + ";V2:" + v2);
        float length1 = v1.length();
        float length2 = v2.length();
        Vector3 normal = MathUtil.getNormal(firstPos, secondPos, thirdPos);
        rotationMatrix = MathUtil.getRotationMatrix(v1.normalized(), normal, v2.normalized());
        Log.d(TAG, "update: rotationMatrix:" + rotationMatrix);
        Quaternion rotation = MathUtil.rotationMatrixToQuaternion(rotationMatrix);
        Log.d(TAG, "update: rotation: " + rotation);
        ModelRenderable cube = ShapeFactory.makeCube(new Vector3(length1, faceThickness, length2), Vector3.zero(), material);
        cube.setShadowCaster(false);
        cube.setShadowReceiver(false);
        node.setRenderable(cube);
        node.setParent(parent);
        node.setLocalPosition(Vector3.add(firstPos, thirdPos).scaled(0.5f));
        Log.d(TAG, "update: local rotation:" + node.getLocalRotation());
        node.setLocalRotation(rotation);
    }

    public void addVertex(BoxVertex v) {
        this.vertices.add(v);
    }

    public Vector3 transformFrom(Vector3 v) {
        return rotationMatrix.mult(v);
    }

    public Vector3 transformTo(Vector3 v) {
        return rotationMatrix.invert().mult(v);
    }

}
