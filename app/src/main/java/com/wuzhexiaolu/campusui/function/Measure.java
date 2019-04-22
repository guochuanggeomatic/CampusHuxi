package com.wuzhexiaolu.campusui.function;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.supermap.data.GeoPoint3D;
import com.supermap.data.GeoStyle3D;
import com.supermap.data.Point3D;
import com.supermap.realspace.Action3D;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.Sightline;
import com.supermap.realspace.Tracking3DEvent;
import com.supermap.realspace.Tracking3DListener;

public class Measure {

    private SceneControl sceneControl;
    private TextView result;

    private Sightline sightline;
    private Handler totalLengthHandler;

    public int AnalysisTypeArea = 1;
    public boolean functionState = false;

    public Measure(TextView result,SceneControl sceneControl){
        this.result = result;
        this.sceneControl = sceneControl;
        totalLengthHandler = new MeasureHandler();
        sceneControl.addTrackingListener(mTracking3dListener);
    }

    public void startMeasureAnalysis() {
        sceneControl.setAction(Action3D.MEASUREDISTANCE3D);
    }

    public void startSurearea() {
        sceneControl.setAction(Action3D.MEASUREAREA3D);

    }

    public void closeAnalysis() {
        sceneControl.setAction(Action3D.PANSELECT3D);
        result.setText("");
    }

    private Tracking3DListener mTracking3dListener = new Tracking3DListener() {

        @Override
        public void tracking(Tracking3DEvent event) {

            initAnalySis(sceneControl, event);

        }
    };

    public void initAnalySis(SceneControl sceneControl, Tracking3DEvent event) {

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

        } else if (sceneControl.getAction() == Action3D.MEASUREDISTANCE3D) {
            measureDistance(event);
        } else if (sceneControl.getAction() == Action3D.MEASUREAREA3D) {
            measureSurearea(event);
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

    private void measureSurearea(Tracking3DEvent event) {
        double TotalArea = event.getTotalArea();
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putDouble("Area", TotalArea);
        msg.setData(bundle);
        totalLengthHandler.sendMessage(msg);
    }

    class MeasureHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            if (AnalysisTypeArea == 0) {
                double msgLength;

                msgLength = Math.round(msg.getData().getDouble("length"));

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
