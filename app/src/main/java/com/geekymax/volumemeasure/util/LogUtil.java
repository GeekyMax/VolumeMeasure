package com.geekymax.volumemeasure.util;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;

/**
 * @author huangmengxuan
 * @date 2020-04-28
 */
public class LogUtil {
    public static void log(Object... objs) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : objs) {
            stringBuilder.append(obj.toString());
            stringBuilder.append(" ");
        }
        System.out.println(stringBuilder.toString());
    }

    public static void print(String tag, INDArray arr) {
        System.out.println("----------------");
        if (arr == null) {
            System.out.println(tag + ":\n" + "empty");
        } else {
            System.out.println("shape " + Arrays.toString(arr.shape()));
            System.out.println(tag + ":\n" + arr.toString());
        }

        System.out.println("----------------");
    }

    public static void print(String tag, INDArray[] arrays) {
        System.out.println("----------------");
        System.out.println(tag);
        for (INDArray array : arrays) {
            System.out.println("\n" + array);
        }
        System.out.println("----------------");
    }



}
