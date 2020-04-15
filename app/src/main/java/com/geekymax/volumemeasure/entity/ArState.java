package com.geekymax.volumemeasure.entity;

public enum ArState {
    INITIAL, // 还未识别到平面的状态
    COLLECTING, // 获取目标物体信息中
    READY, // 准备完成
    MEASURING, // 测量中,计算中
    DONE, // 测量,显示完成

}
