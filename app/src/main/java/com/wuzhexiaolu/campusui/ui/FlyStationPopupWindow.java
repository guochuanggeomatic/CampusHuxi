package com.wuzhexiaolu.campusui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 和飞行组件关系密切，并且它的内容和状态也会由飞行状态改变。
 * 这个 UI 不会保存数据，数据需要 caller 提供。
 */
public class FlyStationPopupWindow extends PopupWindow {
    /**
     * 用来提示信息。
     */
    private Activity activity;
    /**
     * 用来呈现目前飞行到了哪一站，会有五角星提示。
     */
    private ListView flyStationListView;
    /**
     * 用来追踪数据的状态。
     */
    List<FlyStationItem> flyStations;
    /**
     * 用来完成点击框的弹出介绍。
     */
    private IntroductionDialog landmarkIntroduceDialog;
    /**
     * 使用一个HashMap，可以根据不同的飞行路线展示不同的站点。在点击事件后，
     * 根据其中 Adapter 的具体站点的响应介绍对话框。
     *
     * 还需要根据获取的内部adapter来更新飞行状态。
     */
    private HashMap<String, FlyStationAdapter> stationAdapterHashMap = new HashMap<>();
    /**
     * 用来获得飞行状态，暂停和停止等行为的依赖。
     */
    private FlyManager flyManager;
    /**
     * 屏幕左侧上显示站点信息的 View。
     */
    private View flyStationView;
    /**
     * 因为在 lambda 内部使用，不得不创建对象。
     * 为了追寻上一站
     */
    private int curStationIndex = 0;
    /**
     * 管理飞行状态变化的三个组合拳。
     */
    private TimerTask flyStationTimerTask;
    // Handler是安卓的
    private Handler flyProgressHandler;
    private Timer flyStationTimer = new Timer();
    /**
     * 飞行，暂停，和停止按钮。
     */
    private Button pausePlayButton;
    private Button stopButton;

    /**
     * @param activity
     *      需要展示地方的 Activity
     * @param contentView
     *      样式控件
     * @param width
     *      宽
     * @param height
     *      高
     * @param landmarkIntroduceDialog
     *      弹出的介绍框
     */
    public FlyStationPopupWindow(Activity activity, View contentView, int width, int height, IntroductionDialog landmarkIntroduceDialog) {
        super(contentView, width, height, false);
        setFocusable(false);
        // 点击外部不能够关闭这个 PopupWindow，前提是focusable: false
        setOutsideTouchable(false);
        // 透明
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.activity = activity;
        this.flyStationView = contentView;
        this.landmarkIntroduceDialog = landmarkIntroduceDialog;
        this.flyStationListView = contentView.findViewById(R.id.fly_station_list_view);
        SceneControl sceneControl = activity.findViewById(R.id.sceneControl);
        Scene scene = sceneControl.getScene();
        this.flyManager = scene.getFlyManager();

        pausePlayButton = flyStationView.findViewById(R.id.pause_play_button);
        pausePlayButton.setOnClickListener(v -> {
            FlyStatus curStatus = flyManager.getStatus();
            // 这个类能够加载，代表文件已经加载完成了。只需要在复杂的 stop操作。
            if (curStatus == FlyStatus.PLAY) {
                flyManager.pause();
                pausePlayButton.setBackgroundResource(R.drawable.fly_play);
            } else {
                flyManager.play();
                pausePlayButton.setBackgroundResource(R.drawable.fly_pause);
            }
        });
        stopButton = flyStationView.findViewById(R.id.fly_stop_button);

        // 这一段的拓展性不好，需要改动。
        List<FlyStationItem> flyStations =  new ArrayList<>();
        // 第一个是起始点，需要亮起来。
        flyStations.add(new FlyStationItem("松园", R.drawable.current_station_star));
        flyStations.add(new FlyStationItem("二食堂", R.drawable.other_station_dark_star));
        flyStations.add(new FlyStationItem("综合楼", R.drawable.other_station_dark_star));
        flyStations.add(new FlyStationItem("第一教学楼", R.drawable.other_station_dark_star));
        flyStations.add(new FlyStationItem("图书馆", R.drawable.other_station_dark_star));
        //对布局内的控件进行设置
        FlyStationAdapter flyStationItems = new FlyStationAdapter(activity, R.layout.fly_station_item, flyStations);
        stationAdapterHashMap.put("学习路线", flyStationItems);
    }

    /**
     * 展现这个框，并且在飞行开始的时候，刷新下一站为黄星。
     * 如果这一飞行路线 routeName 的话，就不会进行追踪以及显示出来。
     *
     * @param routeName
     *      飞行路线的名字。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("HandlerLeak")
    public void traceFlyStation(String routeName) {
        FlyStationAdapter flyStationItems = stationAdapterHashMap.getOrDefault(routeName, null);
        if (flyStationItems == null) {
            Log.d(LandmarkComponent.TAG, "traceFlyStation: No route " + routeName);
            return;
        }
        showAsDropDown(activity.findViewById(R.id.anchor_text_view), 0, 0);
        pausePlayButton.setBackgroundResource(R.drawable.fly_pause);
        ListView stationListView = flyStationView.findViewById(R.id.fly_station_list_view);
        stationListView.setAdapter(flyStationItems);
        stationListView.setOnItemClickListener((parent, view, position, id) -> {
            // 最后是否恢复飞行状态，取决于是否打断了飞行。使用变量来记录方便些。
            boolean isFlying = flyManager.getStatus() == FlyStatus.PLAY;
            if (isFlying) {
                flyManager.pause();
                pausePlayButton.setBackgroundResource(R.drawable.fly_play);
                // 恢复飞行
                landmarkIntroduceDialog.setOnDismissListener((dialog)-> {
                    flyManager.play();
                    pausePlayButton.setBackgroundResource(R.drawable.fly_pause);
                });
            } else {
                // 避免重新开始飞行，让他什么都不做
                landmarkIntroduceDialog.setOnDismissListener((dialog) -> {
                });
            }
            FlyStationItem clickedItem = flyStationItems.getItem(position);
            assert clickedItem != null;
            landmarkIntroduceDialog.setLayoutGravity(Gravity.CENTER);
            landmarkIntroduceDialog.show(clickedItem.getStationName());
        });
        TextView titleTextView = flyStationView.findViewById(R.id.route_name_text_view);
        titleTextView.setText(routeName);
        // 执行，使用Timer线程来查看飞行的状态。
        flyStationItems.setNotifyOnChange(true);
        int size = flyStationItems.getCount();
        flyProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 更新站点信息
                int nextStationIndex = flyManager.getCurrentStopIndex();
                Log.d(LandmarkComponent.TAG, "handleMessage: " + nextStationIndex );
                // round-up
                nextStationIndex = (nextStationIndex + size) % size;
                FlyStatus curFlyStatus = flyManager.getStatus();
                // 如果是暂停的情况，不需要做什么，更新的情况仅仅是飞行中或者结尾,
                // 并且精当站点发生改变
                if (curFlyStatus  != FlyStatus.PAUSE && curStationIndex != nextStationIndex) {
                    // 到达终点的情况，nextStation 已经更新为 0，所以需要手动改变。
                    if (curFlyStatus == FlyStatus.STOP) {
                        nextStationIndex = (curStationIndex + size + 1) % size;
                        pausePlayButton.setBackgroundResource(R.drawable.fly_play);
                        // 终止发出监听器。
                        flyStationTimerTask.cancel();
                    }
                    FlyStationItem curFlyStationItem = (FlyStationItem) flyStationItems.getItem(curStationIndex);
                    curFlyStationItem.setReachableImageId(R.drawable.other_station_dark_star);
                    FlyStationItem nextFlyStationItem = (FlyStationItem) flyStationItems.getItem(nextStationIndex);
                    nextFlyStationItem.setReachableImageId(R.drawable.current_station_star);
                    // 如果设置在改动的时候，才重新载入，那么影响也没有那么大。
                    flyStationItems.notifyDataSetChanged();
                    curStationIndex = nextStationIndex;
                }
            }
        };
        flyStationTimerTask = new TimerTask() {

            @Override
            public void run() {
                flyProgressHandler.sendEmptyMessage(0);
            }
        };
        // milliseconds
        flyStationTimer .schedule(flyStationTimerTask, 500, 10);
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
         * @param reachableImageId
         *      仅仅作为测试，随后应该弄成图片。
         */
        void setReachableImageId(int reachableImageId) {
            this.reachableImageId = reachableImageId;
        }

        int getReachableImageId() {
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
            TextView stationName = view.findViewById(R.id.fly_station_item_text_view);
            assert flyStationItem != null;
            stationName.setText(flyStationItem.getStationName());
            int stationReachable = flyStationItem.getReachableImageId();
            ImageView stationReachableImageView = view.findViewById(R.id.dest_station_image_view);
            stationReachableImageView.setBackgroundResource(stationReachable);
            return view;
        }
    }
}
