package com.wuzhexiaolu.campusui.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wuzhexiaolu.campusui.R;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

/**
 * 这个类与地标类{@code LandmarkComponent}组合使用。地标类中，监听点击事件的时候，
 * 这个类会在弹出介绍窗口。
 */
public class IntroduceDialog extends Dialog {
    /**
     * 这个文件包含了所有地标的对应的名字，空格后紧跟介绍。
     */
    private static final String landmarkDescriptionFileName = "text.txt";

    /**
     * 这个 context 是 HuxiActivity，可以在上面进行一系列行为。
     */
    private Context context;

    /**
     * 这个 HashMap 包含了 地标名 向内容映射的键值对，
     * 在构造器中打开文件并且把内容解析到其中。
     *
     * 使用的时候，需要更具地标名来获取对应的信息。
     */
    private HashMap<String, String> landmarkDescriptionMap = new HashMap<>();

    @SuppressLint("RtlHardcoded")
    public IntroduceDialog(Context context) {
        super(context, R.style.DialogTypeTheme);
        setContentView(R.layout.site_introduce);
        this.context = context;

        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.LEFT;
        window.setAttributes(layoutParams);

        readFile();
    }

    /**
     * 接受一个地标名字，然后显示出这个地标的相关信息。
     *
     * @param landmarkName 地标的名字，使用它来进行哈希查找。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void show(String landmarkName) {
        TextView nameTextView = findViewById(R.id.site_name);
        nameTextView.setText(landmarkName);
        ImageView imageView = findViewById(R.id.gif_image_view);
        if (imageView != null) {
            Glide.with(context)
                    .load(R.drawable.test)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
            TextView siteIntroduceTextView = findViewById(R.id.introduce_content);
            String landmarkInfo = landmarkDescriptionMap.getOrDefault(landmarkName, "");
            Log.d(LandmarkComponent.TAG, "show: " + landmarkInfo);
            siteIntroduceTextView.setText(landmarkInfo);
            show();
        } else {
            Toast.makeText(context, "ImageView is null", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读取后的文件都会用键值<地标名字，描述>对来存入 HashMap.
     * 如果没有文件，那么 HashMap 就是空的
     */
    private void readFile() {
        try {
            InputStream inputStream = this.getContext().getAssets().open(landmarkDescriptionFileName);
            Scanner in = new Scanner(inputStream);
            while (in.hasNext()) {
                String key = in.next();
                if (!in.hasNext()) {
                    break;
                }
                String contents = in.nextLine();
                landmarkDescriptionMap.put(key, contents);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}