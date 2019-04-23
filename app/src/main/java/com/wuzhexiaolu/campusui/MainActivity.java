package com.wuzhexiaolu.campusui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

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
        RelativeLayout activitymain = (RelativeLayout) findViewById(R.id.activity_main);
        ViewPagerGallery gallery = (ViewPagerGallery) findViewById(R.id.gallery);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 4; i++){
            int id = getResources().getIdentifier("campus" + i, "drawable", getPackageName());
            list.add(id);
        }
        gallery.setOnClickListener(new ViewPagerGallery.GalleryOnClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this,HuxiActivity.class);
                startActivity(intent);
            }
        });
        gallery.setImgResources(list);
    }
}
