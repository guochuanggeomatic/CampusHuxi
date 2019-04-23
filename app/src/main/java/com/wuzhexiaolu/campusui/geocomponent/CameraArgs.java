package com.wuzhexiaolu.campusui.geocomponent;

import com.supermap.data.AltitudeMode;
import com.supermap.realspace.Camera;
import com.supermap.realspace.LookAt;

class CameraArgs {
    private  double longitude;
    private  double latitude;
    private  double altitude;
    private AltitudeMode altitudeMode;
    private  double heading;
    private double tilt;

    public CameraArgs(Camera camera) {
        longitude = camera.getLongitude();
        latitude = camera.getLatitude();
        altitude = camera.getAltitude();
        altitudeMode = camera.getAltitudeMode();
        heading = camera.getHeading();
        tilt = camera.getTilt();
    }

    public CameraArgs(String record) {
        String[] args =  record.split(" ");

        longitude = Double.valueOf(args[0]);
        latitude = Double.valueOf(args[1]);
        altitude = Double.valueOf(args[2]);

        int altitudeIndex = Integer.valueOf(args[3]);
        if (altitudeIndex == 0) {
            altitudeMode = AltitudeMode.ABSOLUTE;
        } else if (altitudeIndex == 1) {
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        } else {
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
        }
        heading = Double.valueOf(args[4]);
        tilt = Double.valueOf(args[5]);
    }

    /**
     * 用作把相机参数保存为一个字符串数据，然后保存在文件中。与构造函数中的参数{@code public CameraArgs(String record)}中的一致。
     * @return
     */
    @Override
    public String toString() {
        int altitudeIndex;
        if (altitudeMode == AltitudeMode.ABSOLUTE) {
            altitudeIndex = 0;
        } else if (altitudeMode == AltitudeMode.CLAMP_TO_GROUND ) {
            altitudeIndex = 1;
        } else {
            altitudeIndex = 2;
        }
        return longitude + " " + latitude + " " + altitude + " " + altitudeIndex + " " + heading + " " + tilt;
    }

    public Camera toCamera() {
        return new Camera(longitude, latitude, altitude, altitudeMode, heading, tilt);
    }

    public LookAt toLookAt() {
        return new LookAt(longitude, latitude, altitude, altitudeMode, heading, tilt, 100);
    }
}
