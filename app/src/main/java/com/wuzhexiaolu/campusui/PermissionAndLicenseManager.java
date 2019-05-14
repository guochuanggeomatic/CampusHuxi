package com.wuzhexiaolu.campusui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.supermap.data.Environment;

public class PermissionAndLicenseManager {

    public static void getPermissionAndLicense(Activity context) {
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
