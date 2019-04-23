package com.wuzhexiaolu.campusui.geocomponent;

import com.supermap.data.AltitudeMode;
import com.supermap.realspace.Camera;
import com.supermap.realspace.LookAt;

class CameraArgs {
    private  double longtitude;
    private  double latitude;
    private  double altitude;
    private AltitudeMode altitudeMode;
    private  double heading;
    private double tilt;

    public CameraArgs(Camera camera) {
        longtitude = camera.getLongitude();
        latitude = camera.getLatitude();
        altitude = camera.getAltitude();
        altitudeMode = camera.getAltitudeMode();
        heading = camera.getHeading();
        tilt = camera.getTilt();
    }

    public CameraArgs(String record) {
        String[] args =  record.split(" ");

        longtitude = Double.valueOf(args[0]);
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
        return longtitude + " " + latitude + " " + altitude + " " + altitudeIndex + " " + heading + " " + tilt;
    }

    public Camera toCamera() {
        return new Camera(longtitude, latitude, altitude, altitudeMode, heading, tilt);
    }

    public LookAt toLookAt() {
        return new LookAt(longtitude, latitude, altitude, altitudeMode, heading, tilt, 100);
    }
}
