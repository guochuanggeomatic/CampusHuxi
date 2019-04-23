package com.wuzhexiaolu.campusui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eicky.ViewPagerGallery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 申请权限和证书设置
        PermissionAndLicenseManager.getPermimssionAndLicense(MainActivity.this);
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
