package com.geekymax.volumemeasure.measurer;

import com.geekymax.volumemeasure.util.LogUtil;
import com.geekymax.volumemeasure.util.TimeLogger;

import org.deeplearning4j.clustering.algorithm.Distance;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangmengxuan
 * @date 2020-04-28
 */
public class TopSurfaceHandler {
    private static final String TAG = "Geeky-TopSurface";
    private List<Double> centroidList = new ArrayList<>();
    private List<INDArray> newDataList = new ArrayList<>();
    private INDArray labels;
    private INDArray[] topMask;
    private INDArray[] underMask;
    private INDArray topData;
    private INDArray underData;

    public void handle(INDArray inData, double underHeight) {
        INDArray zData = inData.get(NDArrayIndex.all(), NDArrayIndex.point(3));
        zData = zData.reshape(-1, 1);
        TimeLogger.getLogger(TAG).log("ready");
//        KMeansClustering kMeansClustering = KMeansClustering.setup(2, 5, Distance.EUCLIDEAN, false);
//        TimeLogger.getLogger(TAG).log("1");

//        List<Point> zPoints = Point.toPoints(zData);
//        TimeLogger.getLogger(TAG).log("2");
//
//        ClusterSet clusterSet = kMeansClustering.applyTo(zPoints);
//        TimeLogger.getLogger(TAG).log("3");
//
//        clusterSet.getClusters().forEach(cluster -> {
//            centroidList.add(cluster.getCenter().getArray().getDouble(0));
//        });
        MyKMeans kMeans = new MyKMeans();
        List<Double> list = new ArrayList<>();
        for (double v : zData.toDoubleVector()) {
            list.add(v);
        }
        kMeans.clustering(list);
        centroidList = kMeans.getClusteringCenterT();
        double heightAverage = (centroidList.get(0) + centroidList.get(1)) / 2;
        topMask = Nd4j.where(zData.match(0, Conditions.greaterThan(heightAverage)), null, null);
        underMask = Nd4j.where(zData.match(0, Conditions.lessThanOrEqual(heightAverage)), null, null);
        topData = inData.get(topMask[0]);
        underData = inData.get(underMask[0]);
        TimeLogger.getLogger(TAG).log("4");

    }

    public INDArray getTopData() {
        return topData;
    }

    public INDArray getUnderData() {
        return underData;
    }

    public double getTopHeight() {
        if (centroidList.size() == 2) {
            return Math.max(centroidList.get(0), centroidList.get(1));
        } else {
            return Double.NaN;
        }
    }

    public double getUnderHeight() {
        if (centroidList.size() == 2) {
            return Math.min(centroidList.get(0), centroidList.get(1));
        } else {
            return Double.NaN;
        }
    }

}
