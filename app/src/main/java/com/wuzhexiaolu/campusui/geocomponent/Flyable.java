package com.wuzhexiaolu.campusui.geocomponent;

/**
 * 这个类的作用是简化 {@link com.wuzhexiaolu.campusui.geocomponent.FlyComponent}
 * 和 {@link com.wuzhexiaolu.campusui.ui.FlyStationPopupWindow}。让后者只需要知道，
 * 调用的时候，它实现了那个接口。
 */
public interface Flyable {
    /**
     * 用来启动飞行。
     */
    void flyOrPause();
    /**
     * 具体体现在用于停止飞行等用法。
     */
    void resetFlying();
    /**
     * 退出飞行功能。
     */
    void exitFlying();
}
