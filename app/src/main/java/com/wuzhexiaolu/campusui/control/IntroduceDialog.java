package com.wuzhexiaolu.campusui.control;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wuzhexiaolu.campusui.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IntroduceDialog extends Dialog {
    TextView siteNameTextView;
    ImageView gifImageView;
    TextView siteIntroduceTextView;


    public IntroduceDialog(Context context, int width, int height, View layout, int style) {
        super(context, style);
        setContentView(layout);

        siteIntroduceTextView = (TextView) findViewById(R.id.site_name);
        siteIntroduceTextView = (TextView) findViewById(R.id.introduce_content);
        readFile();

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    private void readFile(){
        try (InputStream inputStream = this.getContext().getAssets().open("text.txt");
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            String content = "";
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                content += line;
            }
            siteIntroduceTextView.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}