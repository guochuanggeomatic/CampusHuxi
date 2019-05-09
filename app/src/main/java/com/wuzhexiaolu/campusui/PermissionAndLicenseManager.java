package com.wuzhexiaolu.campusui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.supermap.data.Environment;

public class PermissionAndLicenseManager {

    public static void getPermissionAndLicense(Activity context) {
        getPermission(context, new String[]{
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION
        });

        String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Environment.setLicensePath(rootPath +"/SuperMap/license/");
        Environment.setWebCacheDirectory(rootPath +"/SuperMap/WebCache/");
        Environment.setTemporaryPath(rootPath +"/SuperMap/temp/");
        Environment.initialization(context);
    }

    /**
     * 搞定权限
     * @param permissions 是申请权限的字符串，比如{Manifest.permission.READ_PHONE_STATE}
     */
    public static void getPermission(Activity context, String[] permissions) {
        for (String entry: permissions) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(context,
                    entry)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context,
                        entry)) {
                    ActivityCompat.requestPermissions(context,
                            new String[]{entry},
                            1);
                }
            }
        }
    }
}
