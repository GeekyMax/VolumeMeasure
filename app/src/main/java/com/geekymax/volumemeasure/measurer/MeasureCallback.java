package com.geekymax.volumemeasure.measurer;

public interface MeasureCallback {
    public void onFail(String msg);

    public void onSuccess(String msg, double[] result,float angle);
}
