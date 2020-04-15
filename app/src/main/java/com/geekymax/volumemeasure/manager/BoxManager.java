package com.geekymax.volumemeasure.manager;

import android.content.Context;
import android.util.Log;

import com.geekymax.volumemeasure.entity.BoxEdge;
import com.geekymax.volumemeasure.entity.BoxFace;
import com.geekymax.volumemeasure.entity.BoxVertex;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 体积盒AR显示管理器
 */
public class BoxManager {
    private static final String TAG = "Geeky-BoxManager";
    private static BoxManager instance;
    private static final Object INSTANCE_LOCK = new Object();
    private Context context;

    // 已渲染的图形对象
    private List<Vector3> locations = new ArrayList<>();
    private List<BoxVertex> boxVertices = new ArrayList<>();
    private List<BoxEdge> boxEdges = new ArrayList<>();
    private List<BoxFace> boxFaces = new ArrayList<>();

    // 当前选择的面
    private BoxFace selectedFace;

    private BoxManager(Context context) {
        this.context = context;
    }

    public static BoxManager getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new BoxManager(context);
            }
            return instance;
        }
    }

    public static BoxManager getInstance() {
        return instance;
    }

    public void drawBox(Node parent, float a, float b, float h) {
        clearBox();
        for (float i = 0; i <= 1; i++) {
            for (float j = 0; j <= 1; j++) {
                for (float k = 0; k <= 1; k++) {
                    locations.add(new Vector3((i - 1 / 2f) * a, h * (j - 1 / 2f), (k - 1 / 2f) * b));
                }
            }
        }
        locations.forEach(loc -> {
            BoxVertex boxVertex = new BoxVertex(loc, parent, MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_VERTEX_DEFAULT));
            boxVertices.add(boxVertex);
        });

        int[][] faceVerticesArray = getFaceVertexList();
        for (int[] faceVertices : faceVerticesArray) {
            BoxFace boxFace = new BoxFace(parent, MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_FACE_DEFAULT));
            for (int faceVertex : faceVertices) {
                boxFace.addVertex(boxVertices.get(faceVertex));
            }
            boxFaces.add(boxFace);
        }
        for (int i = 0; i < boxVertices.size(); i++) {
            for (int j = 0; j < boxVertices.size(); j++) {
                if (isEdge(i, j)) {
                    BoxEdge boxEdge = new BoxEdge(parent, MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_EDGE_DEFAULT));
                    int relativeFaceIndex = getEdgeRelativeFace(i, j);
                    if (relativeFaceIndex >= 0) {
                        boxEdge.setRelativeSideFace(boxFaces.get(relativeFaceIndex));
                    }
                    boxEdge.set(i, j);
                    boxEdge.addVertex(boxVertices.get(i));
                    boxEdge.addVertex(boxVertices.get(j));
                    boxEdges.add(boxEdge);
                }
            }
        }
        boxVertices.forEach(BoxVertex::update);

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

    private int getEdgeRelativeFace(int i, int j) {
        if (i == 7 && j == 6) {
            return 3;
        } else if (i == 7 && j == 3) {
            return 1;
        } else if (i == 3 && j == 2) {
            return 5;
        } else if (i == 6 && j == 2) {
            return 4;
        } else {
            return -1;
        }
    }

    public int[][] getFaceVertexList() {
        return new int[][]{{0, 1, 5, 4}, {1, 3, 7, 5}, {3, 2, 6, 7}/*顶面*/, {4, 5, 7, 6}, {2, 0, 4, 6}, {2, 3, 1, 0}};
    }

    public void setSelectedFace(BoxFace selectedFace) {
        this.selectedFace = selectedFace;
        boxFaces.forEach(boxFace -> boxFace.setMaterial(MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_FACE_DEFAULT)));
        selectedFace.setMaterial(MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_FACE_SELECTED));
        boxFaces.forEach(BoxFace::update);
    }

    // 移动Face,以"向外"为正方向
    public void moveFace(float distance) {
        if (selectedFace == null) {
            return;
        }
        Vector3 normal = selectedFace.getNormal();
        Vector3 diff = normal.scaled(distance);
        List<BoxVertex> vertices = selectedFace.getVertices();
        vertices.forEach(v -> {
            Vector3 pos = Vector3.add(v.getPosition(), diff);
            v.setPosition(pos);
        });
        // 等所有pos更新完数据之后,才能更新显示
        vertices.forEach(BoxVertex::update);
    }
}