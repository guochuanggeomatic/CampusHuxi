package com.wuzhexiaolu.campusui.geocomponent;

import com.supermap.data.AltitudeMode;
import com.supermap.realspace.Camera;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class CameraFileReader {

    private ArrayList<Camera> cameras = new ArrayList<>();

    /**
     *
     * @param filename
     *      保存相机的文件，从中按行读取，一行构造出一个相机对象。
     * @throws IOException
     *      如果文件不存在，抛出异常。
     */
    CameraFileReader(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        Scanner in = new Scanner(fileInputStream);
        while (in.hasNextLine()) {
            String record = in.nextLine();
            Camera camera = parseStringToCamera(record);
            cameras.add(camera);
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
