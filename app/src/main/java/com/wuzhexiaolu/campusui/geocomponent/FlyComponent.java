package com.wuzhexiaolu.campusui.geocomponent;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sa90.materialarcmenu.ArcMenu;
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
 * 实现接口，方便调用，解耦。
 */
public class FlyComponent implements Flyable {
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
     * 保存当前的正在飞行的路线索引。在 PopupWindow 调用 stop 之后，可以马上重新加载对应的文件，
     * 并且设置为当前值。可以重新飞行。
     */
    private int curRouteIndex = -1;

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
        flyStationPopupWindow = new FlyStationPopupWindow(this, activity, view, 300, 500, landmarkIntroduceDialog);
    }

    /**
     * 如果当前正在飞行状态，那么就暂停。然后重新开始飞行。
     * 如果加载的是空文件，或者没有飞行路线，那么选择空中也就没有 item。
     *
     * 选择了路线后，隐藏菜单，这样就可以避免了飞行途中，重新打开新的飞行路径。
     * 这种行为将会导致飞行路线文件的重读，需要交给方法 {@link #resetFlying()}处理。
     * 保证安全性。
     * 等待飞行停止在重新打开。
     *
     * 主要管理飞行路线，飞行站点有 PopupWindow 搞定。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void doPauseOrFly(int position) {
        // 如果返回 -1，代表没有文件，那么尝试重新读取。如果还是 -1 就什么也不做。
        curRouteIndex = routes.getCurrentRouteIndex();
        // 如果没有文件，或者改变了状态
        if (curRouteIndex == -1) {
            routes.clear();
            routes.fromFile(flyRouteFilePath);
        }
        curRouteIndex = routes.getCurrentRouteIndex();
        // 不可能找到路径，当然，大于等于的情况是不可能出现的，
        // 因为读取的文件获得的飞行路线总是根据相同的数量呈现在对话框。
        // 但是防止用户期间弄坏了文件。
        if (curRouteIndex == -1 || position >= routes.getCount()) {
            // 不显示飞行站点、控制框，不执行飞行动作，直接退出
            return ;
        }
        // 这两句总能够正确执行
        ArcMenu arcMenu = activity.findViewById(R.id.arcMenu);
        arcMenu.setVisibility(View.INVISIBLE);
        // 选择了不同的路线，先停止设置好
        if (curRouteIndex != position) {
            Log.d(TAG, "startOrPauseFly: change index!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            routes.setCurrentRoute(position);
            curRouteIndex = position;
        }
        Log.d(TAG, "startOrPauseFly: pos " + position + " name " + flyRouteNames.get(position) + " cur " + curRouteIndex);

        // 上面的代码为检查，执行过一次之后，这儿总可以正常运行。
        flyOrPause();
    }

    /**
     * 这个方法用来真正执行飞行或者暂停，需要一些检查作为前提。
     * 这个方法也是作为接口的实现，然后将会由 GUI 在暂停和飞行上再次调用。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void flyOrPause() {
        if (flyManager.getStatus() == FlyStatus.STOP) {
            sceneControl.setAction(Action3D.PAN3D);
            flyManager.play();
            // 只在启动的时候，开始监听。
            flyStationPopupWindow.traceFlyStation(flyRouteNames.get(curRouteIndex));
            Log.d(TAG, "flyOrPause: " + flyRouteNames.get(curRouteIndex));
        } else if (flyManager.getStatus() == FlyStatus.PAUSE){
            flyManager.play();
        } else {
            // 飞行的时候，就暂停
            sceneControl.setAction(Action3D.PANSELECT3D);
            flyManager.pause();
        }
    }

    /**
     * 停止飞行。然后菜单重现，PopupWindow 消失。
     * {@code routes.getCurrentIndex()} 将会返回 -1，然后下次再次飞行的时候，
     * 重新读取文件，尤其是 flyOrPause 方法。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void resetFlying() {
        stopAndResetCurrentRoute();
        // 飞行后马上停止，即切换视角。没有更好的方式来实现了。
        flyOrPause();
        flyOrPause();
    }

    /**
     * 用来退出飞行，让界面消失，恢复主菜单。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void exitFlying() {
        stopAndResetCurrentRoute();
        ArcMenu arcMenu = activity.findViewById(R.id.arcMenu);
        arcMenu.setVisibility(View.VISIBLE);
        flyStationPopupWindow.dismiss();
    }

    /**
     * 这段代码在 {@link #resetFlying()} 和 {@link #exitFlying()} 中冗余，先提取出来。
     */
    private void stopAndResetCurrentRoute() {
        flyManager.stop();
        routes.clear();
        routes.fromFile(flyRouteFilePath);
        routes.setCurrentRoute(curRouteIndex);
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
