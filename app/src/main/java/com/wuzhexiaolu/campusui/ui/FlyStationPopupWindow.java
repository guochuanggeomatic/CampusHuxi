package com.wuzhexiaolu.campusui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.supermap.realspace.FlyManager;
import com.supermap.realspace.FlyStatus;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.wuzhexiaolu.campusui.R;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 和飞行组件关系密切，并且它的内容和状态也会由飞行状态改变。
 * 这个 UI 不会保存数据，数据需要 caller 提供。
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

    /**
     * 用来方便在飞行的时候更新状态。
     */
    private ArrayAdapter flyStationItems;

    public FlyStationPopupWindow(Activity activity, View contentView, int width, int height) {
//        super(context);
//        setContentView(contentView);
//        setWidth(width);
//        setHeight(height);
        super(contentView, width, height, false);
        setFocusable(false);
        // 点击外部不能够关闭这个 PopupWindow，前提是focusable: false
        setOutsideTouchable(false);
        // 透明
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.activity = activity;
        List<FlyStationItem> flyStations =  new ArrayList<>();
        flyStations.add(new FlyStationItem("松园", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("二食堂", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("综合楼", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("第一教学楼", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("缙湖", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("荷花池", R.drawable.icon_dark_star));
        flyStations.add(new FlyStationItem("图书馆", R.drawable.icon_dark_star));
        //对布局内的控件进行设置
        flyStationItems = new FlyStationAdapter(activity, R.layout.fly_station_item, flyStations);
        flyStationListView = contentView.findViewById(R.id.fly_station_list_view);
        flyStationListView.setAdapter(flyStationItems);
        flyStationListView.setOnItemClickListener((parent, view, position, id) -> {
            // 点击某一个地方，如果飞到了那儿就停止
        });
    }

    /**
     * 在 lambda 内部直接写，好像不能够 work。需要封装一下？
     * @param flyStationItem
     * @param resId
     */
    private void changeIcon(FlyStationItem flyStationItem, int resId, String text) {
        flyStationItem.setReachableImageId(resId);
        flyStationItem.setStationName(text);
    }

    /**
     * 用来停止 Handler.
     */
    private Handler stopHandler = new Handler();

    /**
     * 因为在 lambda 内部使用，不得不创建对象。
     * 为了追寻上一站
     */
    private int curStation = 0;

    /**
     * 展现这个框，并且在飞行开始的时候，刷新下一站为黄星。
     */
    @SuppressLint("HandlerLeak")
    public void traceFlyStation() {
        showAsDropDown(activity.findViewById(R.id.anchor_text_view), 0, 0);
        SceneControl sceneControl = activity.findViewById(R.id.sceneControl);
        Scene scene = sceneControl.getScene();

        TimerTask flyStationTimerTask;
        FlyManager flyManager = scene.getFlyManager();
        // 执行，使用Timer线程来查看飞行的状态。
        Timer flyProgressTimer = new Timer();

        Runnable tryToSetNextStation = () -> {
            int size = flyStationItems.getCount();
            if (flyManager.getStatus() == FlyStatus.PLAY) {
                int nextStation = flyManager.getCurrentStopIndex();
                Log.d(LandmarkComponent.TAG, "traceFlyStation: " + "OK " + nextStation);
                // round-up
                nextStation = (nextStation + size) % size;
                // 如果下一站发生了改变，那么就图标改变。
                if (nextStation != curStation) {
                    FlyStationItem curFlyStationItem = (FlyStationItem) flyStationItems.getItem(curStation);
//                    curFlyStationItem.setReachableImageId(R.drawable.icon_dark_star);
                    changeIcon(curFlyStationItem, R.drawable.icon_dark_star, "Gone");

                    FlyStationItem nextFlyStationItem = (FlyStationItem) flyStationItems.getItem(nextStation);
//                    nextFlyStationItem.setReachableImageId(R.drawable.icon_star);
                    changeIcon(nextFlyStationItem, R.drawable.icon_star, "Coming");

                    Log.d(LandmarkComponent.TAG, "traceFlyStation: Swapped" + "OK " + nextStation);

                    curStation = nextStation;
                }
            } else if (flyManager.getStatus() == FlyStatus.STOP) {
                // 如果停止了，那就终止
                stopHandler.sendEmptyMessage(0);
                // 终止
//                flyStationTimerTask.cancel();
            }
        };

        @SuppressLint("HandlerLeak")
        Handler flyProgressHandler = new Handler();
        stopHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        flyProgressHandler.removeCallbacks(tryToSetNextStation);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        flyStationTimerTask = new TimerTask() {

            @Override
            public void run() {
//                flyProgressHandler.sendEmptyMessage(0);
                flyProgressHandler.post(tryToSetNextStation);
            }
        };
        // milliseconds
        flyProgressTimer.schedule(flyStationTimerTask, 500, 10);
    }


    /**
     * 用来当做 flyStationListView 的填充。
     */
    private static class FlyStationItem {
        /**
         * 飞行站点的名称。
         */
        private String stationName;

        /**
         * 这个 Item 的图片，代表了飞行的下一站是不是自己。
         */
        private int reachableImageId;

        FlyStationItem(String stationName, int reachableImageId) {
            this.stationName = stationName;
            this.reachableImageId = reachableImageId;
        }

        private String getStationName() {
            return stationName;
        }

        /**
         * 这个方法能够配合飞行组件完成飞行站点数据的更新，
         * 代表到了那个站点。
         * @param stationName
         *      仅仅作为测试，随后应该弄成图片。
         */
        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        public void setReachableImageId(int reachableImageId) {
            this.reachableImageId = reachableImageId;
        }

        public int getReachableImageId() {
            return reachableImageId;
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
        FlyStationAdapter(Context context, int resource, List<FlyStationItem> objects) {
            super(context, resource, objects);
            resourceId = resource;
        }

        // 更新每一站的信息，使用上 FlyStationItem 的数据。
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FlyStationItem flyStationItem = getItem(position);
            @SuppressLint("ViewHolder")
            View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView stationName = view.findViewById(R.id.fly_station_item);
            assert flyStationItem != null;
            stationName.setText(flyStationItem.getStationName());
            int stationReachable = flyStationItem.getReachableImageId();
            ImageView stationReachableImageView = view.findViewById(R.id.dest_station_image_view);
            stationReachableImageView.setBackgroundResource(stationReachable);
            return view;
        }
    }
}
