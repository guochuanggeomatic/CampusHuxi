package com.wuzhexiaolu.campusui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wuzhexiaolu.campusui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 和飞行组件关系密切，并且它的内容和状态也会由飞行状态改变。
 */
public class FlyStationPopupWindow extends PopupWindow {
    /**
     * 用来呈现飞行到了那一站，会有提示。
     */
    private ListView flyStationListView;
    /**
     * 用来提示信息。
     */
    private Activity activity;


    public FlyStationPopupWindow(Activity activity, View contentView, int width, int height) {
//        super(context);
//        setContentView(contentView);
//        setWidth(width);
//        setHeight(height);
        super(contentView, width, height, true);
        // 外部关闭
        setOutsideTouchable(true);
        setTouchable(true);
        // 透明
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.activity = activity;
        List<FlyStationItem> flyStations =  new ArrayList<>();
        flyStations.add(new FlyStationItem("松园"));
        flyStations.add(new FlyStationItem("二食堂"));
        flyStations.add(new FlyStationItem("综合楼"));
        flyStations.add(new FlyStationItem("第一教学楼"));
        flyStations.add(new FlyStationItem("缙湖"));
        flyStations.add(new FlyStationItem("荷花池"));
        flyStations.add(new FlyStationItem("图书馆"));
        //对布局内的控件进行设置
        final ArrayAdapter arrayAdapter = new FlyStationAdapter(activity, R.layout.fly_station_item, flyStations);
        flyStationListView = contentView.findViewById(R.id.fly_station_list_view);
        flyStationListView.setAdapter(arrayAdapter);
    }

    /**
     * 用来当做 flyStationListView 的填充。
     */
    private static class FlyStationItem {
        /**
         * 飞行站点的名称。
         */
        private String stationName;

        FlyStationItem(String stationName) {
            this.stationName = stationName;
        }

        private String getStationName() {
            return stationName;
        }
    }

    /**
     * 用来管理加载的 ListView
     */
    private static class FlyStationAdapter extends ArrayAdapter<FlyStationItem> {

        /**
         * 用来在找到每一项 item。
         */
        private int resourceId;

        /**
         * @param context
         * @param resource
         *      FlyStationItem 所在的控件。
         * @param objects
         */
        public FlyStationAdapter(Context context, int resource, List<FlyStationItem> objects) {
            super(context, resource, objects);
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FlyStationItem flyStationItem = getItem(position);
            @SuppressLint("ViewHolder")
            View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView stationName = view.findViewById(R.id.fly_station_item);
            stationName.setText(flyStationItem.getStationName());
            return view;
        }
    }
}
