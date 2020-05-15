package com.geekymax.volumemeasure.measurer;

import com.geekymax.volumemeasure.util.LogUtil;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;

import static com.geekymax.volumemeasure.util.LogUtil.print;

public class OfflineMeasurer implements Measurer {

    public OfflineMeasurer() {
    }

    @Override
    public void measure(INDArray pointData, double underHeight, MeasureCallback callback) {
        try {
            double minConfidence = 0.2;
            // 对低confidence进行一个过滤
            INDArray subData = pointData.get(NDArrayIndex.all(), NDArrayIndex.point(4));
            INDArray[] mask = Nd4j.where(subData.match(0, Conditions.greaterThan(minConfidence)), null, null);
            INDArray inData = pointData.get(mask[0]);
            print("point data 2: ", inData);
            TopSurfaceHandler topSurfaceHandler = new TopSurfaceHandler();
            topSurfaceHandler.handle(inData, underHeight);
            INDArray topData = topSurfaceHandler.getTopData();
            INDArray underData = topSurfaceHandler.getUnderData();
            double topHeight = topSurfaceHandler.getTopHeight();
            underHeight = topSurfaceHandler.getUnderHeight();
            print("topData", topData);
            print("underData", underData);
            LogUtil.log("topHeight", topHeight);
            UnderSurfaceHandler underSurfaceHandler = new UnderSurfaceHandler();
            underSurfaceHandler.handle(underData, topData);
            double maxX = underSurfaceHandler.getMaxX();
            double minX = underSurfaceHandler.getMinX();
            double minY = underSurfaceHandler.getMinY();
            double maxY = underSurfaceHandler.getMaxY();
            callback.onSuccess("success", new double[]{minX, maxX, minY, maxY, underHeight, topHeight}, 0);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail(e.getMessage());
        }
    }


}
