package com.wuzhexiaolu.campusui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.eicky.ViewPagerGallery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionAndLicenseManager.getPermission(this, new String[]{
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGallery();
    }

    private void initGallery(){
        ViewPagerGallery gallery = (ViewPagerGallery) findViewById(R.id.gallery);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 4; i++){
            int id = getResources().getIdentifier("campus" + i, "drawable", getPackageName());
            list.add(id);
        }
        gallery.setOnClickListener((ViewPagerGallery.GalleryOnClickListener) position -> {
            switch (position) {
                case 0:
                    Intent intent = new Intent(MainActivity.this, HuxiActivity.class);
                    startActivity(intent);
                    break;
                default:
                    Toast.makeText(this, "功能即将开放...", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        gallery.setImgResources(list);
    }
}
