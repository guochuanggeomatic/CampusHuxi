package com.wuzhexiaolu.campusui.function;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.supermap.realspace.Action3D;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.Tracking3DEvent;
import com.supermap.realspace.Tracking3DListener;
import com.wuzhexiaolu.campusui.HuxiActivity;
import com.wuzhexiaolu.campusui.R;

/**
 * 测量功能模块。
 */
public class Measure {

    private HuxiActivity huxiActivity;
    private SceneControl sceneControl;
    private TextView result;

    private Handler totalLengthHandler;

    private int AnalysisTypeArea = 1;
    /**
     *
     */
    public boolean functionState = false;

    private boolean readyToMeasure = false;

    public Measure(HuxiActivity huxiActivity){
        this.huxiActivity = huxiActivity;
        this.result = huxiActivity.findViewById(R.id.measureResult);
        this.sceneControl = huxiActivity.findViewById(R.id.sceneControl);
        totalLengthHandler = new MeasureHandler();
        Tracking3DListener mTracking3dListener = event -> initAnalysis(sceneControl, event);
        sceneControl.addTrackingListener(mTracking3dListener);
    }

    /**
     * 距离测量，单击地面上的各点，获得结果显示在在文本框。
     */
    public void doDistanceMeasurement() {
        showAllViews();
        closeAnalysis();
        AnalysisTypeArea = 0;
        sceneControl.setAction(Action3D.MEASUREDISTANCE3D);
    }

    /**
     * 面积测量，点击地面上的点，获得面积。
     */
    public void doAreaMeasurement() {
        showAllViews();
        closeAnalysis();
        AnalysisTypeArea = 1;
        sceneControl.setAction(Action3D.MEASUREAREA3D);
    }

    private void closeAnalysis() {
        sceneControl.setAction(Action3D.PANSELECT3D);
        result.setText("");
    }

    /**
     * 显示组件。
     */
    private void showAllViews(){
        functionState = true;
        result.setVisibility(View.VISIBLE);
    }

    /**
     * 退出，清理界面。
     */
    public void exitMeasurement() {
        result.setVisibility(View.INVISIBLE);
        sceneControl.setAction(Action3D.PANSELECT3D);
        functionState = false;
        Toast.makeText(huxiActivity, "您已退出测量模式", Toast.LENGTH_SHORT).show();
        huxiActivity.changeExitAndArcMenuButtonState();
    }

    private void initAnalysis(SceneControl sceneControl, Tracking3DEvent event) {

        /*
        因为 sightLine 总是为 null，没有初始化，所以不执行。
        if (sightline != null && sceneControl.getAction() == Action3D.CREATEPOINT3D) {

            Point3D p3D = new Point3D(event.getX(), event.getY(), event.getZ());

            if (sightline.getvViewerPosition().getX() == 0) {
                sightline.setViewerPosition(p3D);
                sightline.build();
                Point3D point3d = new Point3D(event.getX(), event.getY(), event.getZ());
                GeoPoint3D geoPoint3D = new GeoPoint3D(point3d);
                GeoStyle3D geoStyle3D = new GeoStyle3D();
                geoPoint3D.setStyle3D(geoStyle3D);
                sceneControl.getScene().getTrackingLayer().add(geoPoint3D, "point");
            } else {
                sightline.addTargetPoint(p3D);
                Point3D point3d = new Point3D(event.getX(), event.getY(), event.getZ());
                GeoPoint3D geoPoint3D = new GeoPoint3D(point3d);
                GeoStyle3D geoStyle3D = new GeoStyle3D();
                geoPoint3D.setStyle3D(geoStyle3D);
                sceneControl.getScene().getTrackingLayer().add(geoPoint3D, "point");
            }

        } else */
        if (sceneControl.getAction() == Action3D.MEASUREDISTANCE3D) {
            measureDistance(event);
        } else if (sceneControl.getAction() == Action3D.MEASUREAREA3D) {
            measureSelectedRegionArea(event);
        }
    }

    private void measureDistance(Tracking3DEvent event) {
        double totalLength = event.getTotalLength();
        double x = event.getX();
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putDouble("length", totalLength);
        msg.setData(bundle);
        totalLengthHandler.sendMessage(msg);
    }

    private void measureSelectedRegionArea(Tracking3DEvent event) {
        double TotalArea = event.getTotalArea();
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putDouble("Area", TotalArea);
        msg.setData(bundle);
        totalLengthHandler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private class MeasureHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (AnalysisTypeArea == 0) {
                double msgLength = Math.round(msg.getData().getDouble("length"));
                if (msgLength < 1000) {
                    result.setText(" 距离 " + msgLength + " 米");
                } else {
                    result.setText(" 距离 " + Math.round(msgLength / 1000) + "公里");
                }
            } else if (AnalysisTypeArea == 1) {
                double msgLength = Math.round(msg.getData().getDouble("Area"));
                if (msgLength < 1000000) {
                    result.setText(" 面积 " + msgLength + " 平方米");
                } else {
                    result.setText(" 面积 " + Math.round(msgLength / 1000000) + "平方公里");
                }
            }
        }
    }
}
