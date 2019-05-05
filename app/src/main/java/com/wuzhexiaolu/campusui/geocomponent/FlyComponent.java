package com.wuzhexiaolu.campusui.geocomponent;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.supermap.realspace.Action3D;
import com.supermap.realspace.FlyManager;
import com.supermap.realspace.FlyStatus;
import com.supermap.realspace.Routes;
import com.supermap.realspace.SceneControl;
import com.wuzhexiaolu.campusui.R;
import com.wuzhexiaolu.campusui.ui.FlyStationPopupWindow;
import com.wuzhexiaolu.campusui.ui.IntroductionDialog;

import java.util.ArrayList;

import static com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent.TAG;

/**
 * @author WuJing
 */

/**
 * 飞行相关操作管理类，操控着飞行文件的打开、飞行相关行为的管理。
 */
public class FlyComponent {
    /**
     * 用来重新打开飞行文件的时候当做参考。
     */
    private String flyRouteFilePath;
    /**
     * 传入的 activity，从中获取 sceneControl,
     * 汇报飞行进度以及使用Toast。
     */
    private Activity activity;
    private SceneControl sceneControl;
    /**
     * 进行操作的核心，各个方法都会依赖这个对象。
     * 但是这个flyManger只会选择flyRouteNames中的最后一个来飞行。
     */
    private FlyManager flyManager;
    /**
     * 这个对象来自{@code flyManager.getRoutes()}，
     * 单独取出来是因为考虑到要处理飞行过程中所到站点名称、下标等，可能需要多次访问它。
     */
    private Routes routes;
    /**
     * 这个ArrayList中包含的是飞行路线文件(.fpf)中，每一条飞行路线的名称。
     * 比如，wujing.fpf中就有：学习路线，参观路线。
     */
    private ArrayList<String> flyRouteNames = new ArrayList<>();
    private FlyStationPopupWindow flyStationPopupWindow;

    /**
     * @param activity
     *      控件，获取信息，然后在这个 sceneControl 上面执行飞行操作。
     * @param flyRouteFilePath
     *      这个飞行路径文件名需要完整的路径名，比如:
     *          "/storage/0/SuperMap/demo/CBD_android/CBD_android.fpf"
     *      如果文件不存在或者为空，那么将会没有可以飞行的路径，搜索框上也会没有选项。
     * @param landmarkIntroduceDialog
     *      介绍 Dialog
     */
    public FlyComponent(Activity activity, String flyRouteFilePath, IntroductionDialog landmarkIntroduceDialog) {
        this.activity = activity;
        this.sceneControl = activity.findViewById(R.id.sceneControl);
        this.flyRouteFilePath = flyRouteFilePath;
        flyManager = sceneControl.getScene().getFlyManager();
        routes = flyManager.getRoutes();
        boolean hasRoutes = routes.fromFile(flyRouteFilePath);
        if (hasRoutes) {
            int numRoutes = routes.getCount();
            for (int i = 0; i < numRoutes; i++) {
                flyRouteNames.add(routes.getRouteName(i));
            }
        } else {
            Toast.makeText(activity, "飞行路线文件信息空白", Toast.LENGTH_SHORT).show();
        }
        View view = activity.getLayoutInflater().inflate(R.layout.fly_station,null);
        flyStationPopupWindow = new FlyStationPopupWindow(activity, view, 300, 500, landmarkIntroduceDialog);
    }

    /**
     * 如果当前正在飞行状态，那么就暂停。然后重新开始飞行。
     * 如果加载的是空文件，或者没有飞行路线，那么选择空中也就没有 item。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startOrPauseFly(int position) {
        // 如果返回 -1，代表没有文件，那么尝试重新读取。如果还是 -1 就什么也不做。
        int curRouteIndex = routes.getCurrentRouteIndex();
        // 如果没有文件，或者改变了状态
        if (curRouteIndex == -1) {
            routes.clear();
            routes.fromFile(flyRouteFilePath);
        }
        curRouteIndex = routes.getCurrentRouteIndex();
        if (curRouteIndex == -1) {
            return ;
        }
        // 选择了不同的路线，先停止（stop）然后在重新启动
        if (curRouteIndex != position) {
            // stop 需要在设置路径之前？
            if (flyManager.getStatus() != FlyStatus.STOP) {
                // 需要注意：
                // 执行 stop 之后，routes 会被清空，需要再次读取。
                flyManager.stop();
                routes.clear();
                routes.fromFile(flyRouteFilePath);
            }
            routes.setCurrentRoute(position);
        }
        Log.d(TAG, "startOrPauseFly: " + position + " " + flyRouteNames.get(position));
        if (flyManager.getStatus() == FlyStatus.STOP) {
            sceneControl.setAction(Action3D.PAN3D);
            flyManager.play();
            // 只在启动的时候，开始监听。
            flyStationPopupWindow.traceFlyStation(flyRouteNames.get(position));
        } else if (flyManager.getStatus() == FlyStatus.PAUSE){
            flyManager.play();
        } else {
            // STOP
            sceneControl.setAction(Action3D.PANSELECT3D);
            flyManager.pause();
        }
    }

    /**
     * 停止飞行。
     */
    public void stop() {
        if (flyManager.getStatus() != FlyStatus.STOP) {
            flyManager.stop();
            sceneControl.setAction(Action3D.PANSELECT3D);
        } else {
            Toast.makeText(activity, "没有正在飞行", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 追踪飞行符的进度，然后对飞行提醒控件作出调整。
     */
    private void traceProgress() {

    }

    /**
     * 从{@code flyRouteNames}中选择第 position 条飞行路线，准备下次飞行。
     * @param position
     *      相应的第几条路线。如果超出了，那么就会默认第一条。
     */
    private void setRouteToFly(int position) {
        routes.setCurrentRoute(position);
        // ?
        sceneControl.setAction(Action3D.PAN3D);
        flyManager.play();
//        refreashFlyProgress();
//        isFlying = true;
//        dismiss();
//        isPopFlyShowing = true;

    }

    /**
     * 获取所有飞行路线名称。
     * @return
     *      返回一个新建的数组。
     */
    public String[] getRouteNames() {
        String[] allFlyRouteNames = new String[flyRouteNames.size()];
        flyRouteNames.toArray(allFlyRouteNames);
        return allFlyRouteNames;
    }
}
