package com.geekymax.volumemeasure.measurer;

import com.geekymax.volumemeasure.entity.MyPoint;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Map;

/**
 * 定义一个抽象的测量器
 */
public interface Measurer {
    /**
     * 测量接口
     *
     * @param pointData   点云数据矩阵
     * @param underHeight 地面高度
     * @param callback    回调方法
     * @return
     */
    public void measure(INDArray pointData, double underHeight, MeasureCallback callback);
}
