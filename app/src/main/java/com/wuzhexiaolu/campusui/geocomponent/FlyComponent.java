package com.wuzhexiaolu.campusui.geocomponent;

import android.app.Activity;
import android.widget.Toast;

import com.supermap.realspace.Action3D;
import com.supermap.realspace.FlyManager;
import com.supermap.realspace.Routes;
import com.supermap.realspace.SceneControl;

import java.io.File;
import java.util.ArrayList;

/**
 * @author WuJing
 */

public class FlyComponent {

    private SceneControl sceneControl;

    private FlyManager flyManager;
    private ArrayList<String> flyRouteNames = new ArrayList<>();
    private static boolean isFlying = false;
    public static boolean isStop = false;

    /**
     * @param sceneControl 控件，获取信息，然后在这个 sceneControl 上面执行飞行操作
     */
    public FlyComponent(SceneControl sceneControl) {
        this.sceneControl = sceneControl;
        flyManager = sceneControl.getScene().getFlyManager();
    }

    /**
     * 传入路径参数，初始化飞行的数据。
     *
     * @param context 当错误的时候，文件找不到的时候，通过这个上下文 Toast 提醒
     * @param routePathName 飞行路径文件名，飞行路径文件 ".fpf" 的名称
     * @param localSceneDirPath scene 的文件目录。
     */
    public void prepareFly(Activity context, String routePathName, String localSceneDirPath) {
        flyRouteNames.clear();
        String flyRoute = getFlyRoutePath(localSceneDirPath, routePathName);
        if (flyRoute == null) {
            Toast.makeText(context, "该场景下无飞行路线", Toast.LENGTH_LONG).show();
        } else {
            Routes routes = flyManager.getRoutes();
            boolean hasRoutes = routes.fromFile(flyRoute);
            if (hasRoutes) {
                int numOfRoutes = routes.getCount();
                for (int i = 0; i < numOfRoutes; i++) {
                    flyRouteNames.add(routes.getRouteName(i));
                }
            } else {
                Toast.makeText(context, "该场景下无飞行路线", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 根据场景名称打开的飞行路线文件，需要确认飞行文件的存放位置
     *
     * @param sceneDirPath scene 目录名称
     * @param routeName 飞行路径文件 ".fpf" 的名字前缀
     * @return
     */
    private String getFlyRoutePath(String sceneDirPath, String routeName) {
        if (new File(sceneDirPath).exists()) {
            String flyRoutePath = sceneDirPath + routeName + ".fpf";
            if (new File(flyRoutePath).exists()) {
                return flyRoutePath;
            }
        }
        return null;
    }

    public void startOrPauseFly(Activity context) {
        if (sceneControl != null) {
            if (flyManager != null) {
                if (!isFlying) {
                    sceneControl.setAction(Action3D.PAN3D);
                    flyManager.play();
                    isFlying = true;
                } else {
                    flyManager.pause();
                    isFlying = false;
                }
            } else {
                Toast.makeText(context, "飞行管理出问题了", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "你应该先打开场景", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 停止飞行。
     *
     * @param context 传入当前的 Activity, 用于发生错误时 Toast 来提醒
     */
    public void stop(Activity context) {
        if (isFlying && flyManager != null) {
            flyManager.stop();
            sceneControl.setAction(Action3D.PANSELECT3D);
            isFlying = false;
        } else {
            Toast.makeText(context, "没有正在飞行或者飞行管理未初始化", Toast.LENGTH_SHORT).show();
        }
    }
}
