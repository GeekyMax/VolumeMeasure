package com.geekymax.volumemeasure.measurer;

import android.util.Log;

import com.geekymax.volumemeasure.entity.MyPoint;
import com.geekymax.volumemeasure.manager.NetManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.math.plot.utils.Array;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OnlineMeasurer implements Measurer {
    private OkHttpClient client;
    private String url = "";


    public OnlineMeasurer() {
        client = new OkHttpClient();
    }

    @Override
    public void measure(INDArray pointData, double underHeight, MeasureCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("pointData", Array.toString(pointData.toDoubleMatrix()));
            RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFail(e.toString());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String string = response.body().string();
                    Log.d("info", string + "");
                    try {
                        JSONObject json = new JSONObject(string);
                        String result = json.getString("result");
                        String angle = json.getString("angle");
                        float angleDouble = Float.parseFloat(angle);
                        String[] res = result.split(",");
                        double[] resultDouble = new double[res.length];
                        for (int i = 0; i < res.length; i++) {
                            resultDouble[i] = Double.parseDouble(res[i]);
                        }
                        callback.onSuccess(json.getString("msg"), resultDouble, angleDouble);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
