package com.geekymax.volumemeasure.measurer;


import java.util.List;

public class MyKMeans extends KMeansClustering<Double> {
    public MyKMeans() {
        super();
        setK(2);
        setMaxClusterTimes(500);
    }

    @Override
    public double similarScore(Double o1, Double o2) {
        return Math.abs(o1 - o2);
    }

    @Override
    public boolean equals(Double o1, Double o2) {
        return o1.equals(o2);
    }

    @Override
    public Double getCenterT(List<Double> list) {
        double center = 0;
        for (Double d : list) {
            center += d;
        }
        return center / list.size();
    }
}
