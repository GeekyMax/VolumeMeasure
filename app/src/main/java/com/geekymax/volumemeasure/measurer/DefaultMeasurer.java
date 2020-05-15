package com.geekymax.volumemeasure.measurer;


import org.nd4j.linalg.api.ndarray.INDArray;

public class DefaultMeasurer implements Measurer {
    @Override
    public void measure(INDArray pointData, double underHeight, MeasureCallback callback) {
        try {
//            Thread.sleep(1000);
            callback.onSuccess("success", new double[]{0, 0.4, 0, 0.2, 0, 0.3}, 0);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("fail");
        }

    }
}
