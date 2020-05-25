package com.geekymax.volumemeasure.measurer;

import android.util.Log;

import com.geekymax.volumemeasure.util.LogUtil;
import com.geekymax.volumemeasure.util.TimeLogger;

import org.math.plot.utils.Array;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Conditions;

import static com.geekymax.volumemeasure.util.LogUtil.log;
import static com.geekymax.volumemeasure.util.LogUtil.print;

public class OfflineMeasurer implements Measurer {

    public OfflineMeasurer() {
    }

    // id, x, y, z, c
    @Override
    public void measure(INDArray pointData, double underHeight, MeasureCallback callback) {
        try {
            TimeLogger.getLogger().log("start off line");
            double minConfidence = 0.2;
            // 对低confidence进行一个过滤
            INDArray subData = pointData.get(NDArrayIndex.all(), NDArrayIndex.point(4));
            INDArray[] mask = Nd4j.where(subData.match(0, Conditions.greaterThan(minConfidence)), null, null);
            pointData = pointData.get(mask[0]);

            // 对低于目标平面的点做一个过滤

            subData = pointData.get(NDArrayIndex.all(), NDArrayIndex.point(3));
            mask = Nd4j.where(subData.match(0, Conditions.greaterThan(underHeight - 0.1)), null, null);
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
            LogUtil.log("underHeight", underHeight);
            TimeLogger.getLogger().log("finish top surface");
            UnderSurfaceHandler underSurfaceHandler = new UnderSurfaceHandler();
            underSurfaceHandler.handle(underData, topData);
            double maxX = underSurfaceHandler.getMaxX();
            double minX = underSurfaceHandler.getMinX();
            double minY = underSurfaceHandler.getMinY();
            double maxY = underSurfaceHandler.getMaxY();
            TimeLogger.getLogger().log("start under surface");
            double[] res = new double[]{minX, maxX, minY, maxY, underHeight, topHeight};
            LogUtil.log("measure Result", Array.toString(res));

            callback.onSuccess("success", res, 0);
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getStackTrace());
            log(e.getMessage());
            callback.onFail(e.getMessage());
        }
    }


}
