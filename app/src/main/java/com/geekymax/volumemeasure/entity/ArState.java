package com.geekymax.volumemeasure.entity;

public enum ArState {
    Initial, // 还未识别到平面的状态
    Collecting, // 获取目标物体信息中
    Ready, // 准备完成
    measuring, // 测量中,计算中
    Done, // 测量,显示完成

}
