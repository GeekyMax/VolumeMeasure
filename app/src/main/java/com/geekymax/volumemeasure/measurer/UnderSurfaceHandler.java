package com.geekymax.volumemeasure.measurer;

import android.util.Log;

import com.geekymax.volumemeasure.util.LogUtil;

import org.apache.commons.lang3.RandomUtils;
import org.math.plot.utils.Array;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.conditions.Conditions;


/**
 * @author huangmengxuan
 * @date 2020-04-28
 */
public class UnderSurfaceHandler {
    public static final String TAG = "Geeky-UnderSurface";

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    //
    public void handle(INDArray underData, INDArray topData) {
//        long underLength = underData.shape()[0];
//        long topLength = topData.shape()[0];
//        INDArray zero = Nd4j.zeros(underLength, 1);
//        INDArray one = Nd4j.zeros(topLength, 1);
//        underData = Nd4j.hstack(underData, zero);
//        topData = Nd4j.hstack(topData, one);
        underData = Nd4j.append(underData, 1, 0, 1);
        topData = Nd4j.append(topData, 1, 1, 1);
//        underData = Nd4j.concat(1, underData, Nd4j.zeros(length, 1));
//        topData = Nd4j.concat(1, topData, Nd4j.ones(topData.shape()[0], 1));
//        INDArray allData = Nd4j.concat(0, underData, topData);
        INDArray allData = Nd4j.vstack(underData, topData);
        for (int axis = 0; axis <= 1; axis++) {
            INDArray batch = selectFirstBatch(allData, topData, axis);
            Log.d(TAG, "handle: batch shape" + batch.shape()[0] + " " + batch.shape()[1]);
            // batch中的top data
            INDArray topBatch = getTop(batch, true);

            //计算top batch的平均值
            double topBatchAverage = topBatch.getColumn(axis + 1).mean(0).getDouble(0);
            LogUtil.log("topBatchAverage", topBatchAverage);

            //将data以该平均值为界,分为两部分
            INDArray greaterBatch = batch.get(Nd4j.where(batch.getColumn(axis + 1).match(0, Conditions.greaterThan(topBatchAverage)), null, null)[0]);
            INDArray lessBatch = batch.get(Nd4j.where(batch.getColumn(axis + 1).match(0, Conditions.lessThanOrEqual(topBatchAverage)), null, null)[0]);

            // 计算greaterBatch的边界
            double maxV = perceptron(greaterBatch.getColumn(axis + 1), greaterBatch.getColumn(5), 50000, 0.0001);
            LogUtil.log("max" + axis, maxV);
            double minV = perceptron(lessBatch.getColumn(axis + 1), lessBatch.getColumn(5), 50000, 0.0001);
            LogUtil.log("min" + axis, minV);
            if (axis == 0) {
                this.minX = minV;
                this.maxX = maxV;
            } else {
                this.minY = minV;
                this.maxY = maxV;
            }
        }


    }


    /**
     * @param data         [[x],..]
     * @param labels       [[0],[1],..]
     * @param maxIteration 随机梯度下降 迭代次数
     * @param learningStep 学习步长
     */
    private double perceptron(INDArray data, INDArray labels, int maxIteration, double learningStep) {
//        LogUtil.print("perceptron data", data);
//        LogUtil.print("perceptron labels", labels);
        double[] w = new double[]{0, 0};
        int dataSize = (int) labels.shape()[0];
        int correctCount = 0;
        int time = 0;
        while (time < maxIteration) {
            int index = RandomUtils.nextInt(0, dataSize);
            double x = data.getDouble(index);
            double y = labels.getDouble(index) * 2 - 1;
            double wx = x * w[0] + w[1];
            if (wx * y > 0) {
                correctCount++;
                if (correctCount > maxIteration) {
                    break;
                }
                continue;
            }
            w[0] += learningStep * y * x;
            w[1] += learningStep * y;
        }
        int testCorrect = 0;
        int testError = 0;
//        for (int index = 0; index < dataSize; index++) {
//            double x = data.getDouble(index);
//            double y = labels.getDouble(index) * 2 - 1;
//            double wx = x * w[0] + w[1];
//            if (wx * y > 0) {
//                testCorrect++;
//            } else {
//                testError++;
//            }
//        }
        LogUtil.log("testCorrect", testCorrect, "testError", testError);
        return -w[1] / w[0];
    }

    private INDArray getTop(INDArray data, boolean isTop) {
        if (isTop) {
            return data.get(Nd4j.where(data.getColumn(5).match(0, Conditions.greaterThan(0.5)), null, null)[0]);
        } else {
            return data.get(Nd4j.where(data.getColumn(5).match(0, Conditions.lessThanOrEqual(0.5)), null, null)[0]);
        }
    }

    // 以x轴为限制,挑选batch
    private INDArray selectFirstBatch(INDArray allData, INDArray topData, int axis) {
        int otherAxis = 1;
        if (axis == 1) {
            otherAxis = 0;
        }
        INDArray x = topData.getColumn(1 + otherAxis);
        double averageX = Nd4j.mean(x).getDouble(0);
        double std = Nd4j.std(x).getDouble(0);
        x = allData.getColumn(1 + otherAxis);
        INDArray x1 = x.add(-averageX);
        INDArray[] mask = Nd4j.where(x1.match(0, Conditions.absLessThan(std)), null, null);
        INDArray selected = allData.get(mask[0]);
        LogUtil.print("selectFirstBatch mask", mask[0]);
//        PlotUtil.builder()
//                .addScatter(selected.getColumn(1), selected.getColumn(2), Color.BLUE)
//                .show();
        return selected;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }
}
