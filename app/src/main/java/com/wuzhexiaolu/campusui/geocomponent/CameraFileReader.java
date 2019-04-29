package com.wuzhexiaolu.campusui.geocomponent;

import com.supermap.data.AltitudeMode;
import com.supermap.realspace.Camera;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class CameraFileReader {

    private ArrayList<Camera> cameras = new ArrayList<>();

    CameraFileReader(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String record = bufferedReader.readLine();
        while (record != null) {
            cameras.add(parseStringToCamera(record));
            record = bufferedReader.readLine();
        }
    }

    private Camera parseStringToCamera(String record) {
        AltitudeMode altitudeMode;
        String[] args =  record.split(" ");

        double longitude = Double.valueOf(args[0]);
        double latitude = Double.valueOf(args[1]);
        double altitude = Double.valueOf(args[2]);

        int altitudeIndex = Integer.valueOf(args[3]);
        if (altitudeIndex == 0) {
            altitudeMode = AltitudeMode.ABSOLUTE;
        } else if (altitudeIndex == 1) {
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        } else {
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
        }
        double heading = Double.valueOf(args[4]);
        double tilt = Double.valueOf(args[5]);
        return new Camera(longitude, latitude, altitude, altitudeMode, heading, tilt);
    }

    Camera[] getCameraArray() {
        Camera[] ret = new Camera[cameras.size()];
        cameras.toArray(ret);
        return ret;
    }
}
