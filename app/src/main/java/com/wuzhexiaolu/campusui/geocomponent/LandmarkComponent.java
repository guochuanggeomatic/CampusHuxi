package com.wuzhexiaolu.campusui.geocomponent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;

import com.supermap.realspace.Camera;
import com.supermap.realspace.Scene;
import com.wuzhexiaolu.campusui.HuxiActivity;
import com.wuzhexiaolu.campusui.R;
import com.supermap.data.Point3D;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.Feature3DSearchOption;
import com.supermap.realspace.Feature3Ds;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.Layer3DType;
import com.supermap.realspace.Layer3Ds;
import com.supermap.realspace.PixelToGlobeMode;
import com.supermap.realspace.SceneControl;
import com.wuzhexiaolu.campusui.ui.IntroductionDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 这个类控制着地标相关的一切活动。这个类即 SceneControl 的监听器。
 */
public class LandmarkComponent implements GestureDetector.OnGestureListener {
    public static final String TAG = "On LandmarkComponent";
    private static final String layerName = "Landmark_KML";
    private static final double radius = 1.;

    /**
     * 地标文件的地址，由外部传过来，通常和模型文件放在一起。
     */
    private String layerKMlPath;
    private String cameraPath;

    private SceneControl sceneControl;

    /**
     * @see com.wuzhexiaolu.campusui.ui.IntroductionDialog
     */
    private IntroductionDialog landmarkIntroduceDialog;

    private ArrayList<LandFeature> landFeatures= new ArrayList<>();

    /**
     * 在测量模式中，禁用点击地表响应地标。
     */
    private boolean enableShowIntroduceDialog = true;

    /**
     * 这个构造器完成的对地标文件的打开，如果打开失败，那么将会创建一个空文件。
     * 构造的时候，文件缺失会抛出 IOException。并且自身为 null。随后设置给场景
     * 控制一个Detector.
     *
     * @param context
     *      需要接管地标管理的那个 Activity。
     * @param landmarkIntroduceDialog
     *      介绍对话框。
     */
    public LandmarkComponent(Activity context, IntroductionDialog landmarkIntroduceDialog, String layerKMlPath, String cameraPath) {
        this.sceneControl = context.findViewById(R.id.sceneControl);
        this.layerKMlPath = layerKMlPath;
        this.cameraPath = cameraPath;
        loadFeaturesFromFile();
        this.landmarkIntroduceDialog = landmarkIntroduceDialog;
        GestureDetector gestureDetector = new GestureDetector(context, this);
        sceneControl.setGestureDetector(gestureDetector);
    }

    /**
     * 通过传入的 android.graphics.Point 的类和半径来判断，附近是否有点。
     * 如果有，那么就会响应。反之，什么都不做。
     *
     * @param point
     *      用户点击的那个点。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void nearByLandmark(Point point) {
        if (!enableShowIntroduceDialog) {
            return ;
        }
        Scene scene = sceneControl.getScene();
        Point3D point3D = scene.pixelToGlobe(point, PixelToGlobeMode.TERRAINANDMODEL);
        double minDistance = radius;
        Feature3D nearPoint = null;
        // Find the nearest landmark of the point, and record it.
        for (int i = 0; i < landFeatures.size(); i++) {
            LandFeature landFeature = landFeatures.get(i);
            Point3D featurePoint3D = landFeature.getFeature3D().getGeometry().getPosition();
            double distance = Math.sqrt(
                    Math.pow(point3D.getX() - featurePoint3D.getX(), 2.) +
                    Math.pow(point3D.getY() - featurePoint3D.getY(), 2.) +
                    Math.pow(point3D.getZ() - featurePoint3D.getZ(), 2.)
            );
            if (distance < minDistance) {
                nearPoint = landFeature.getFeature3D();
                minDistance = distance;
            }
        }

        if (nearPoint != null && landmarkIntroduceDialog != null) {
            showIntroductionDialogWith(nearPoint.getName());
        }
    }

    /**
     * 搜索框点击的时候，打开对话信息框。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showIntroductionDialogWith(String name) {
        landmarkIntroduceDialog.setLayoutGravity(Gravity.LEFT);
        landmarkIntroduceDialog.show(name);
    }

    /**
     * 获取全部的地标名字。
     *
     * @return
     *      返回一个数组，即所有从文件中读取的地标的名字。
     */
    public String[] getLandmarkNames() {
        String[] allLandmarkNames = new String[landFeatures.size()];
        for (int i = 0; i < landFeatures.size(); i++) {
            allLandmarkNames[i] = landFeatures.get(i).getFeature3D().getName();
        }
        return allLandmarkNames;
    }

    /**
     * 提供地标的名称，点击之后将会飞到那个地方。
     *
     * @param landmarkName
     *      用户在搜索框选择的地标名称。
     */
    @SuppressLint("NewApi")
    public void flyToSpecifiedLand(String landmarkName) {
        for (LandFeature landFeature :
                landFeatures) {
            if (Objects.equals(landFeature.getFeature3D().getName(), landmarkName)) {
                Scene scene = sceneControl.getScene();
                Camera camera = landFeature.getCamera();
                scene.setCamera(camera);
                return ;
            }
        }
    }

    /**
     * 显示和隐藏地标，同时禁止响应地标的点击，在使用的过程中更好观看场景。
     */
    public void setLandmarkVisible(boolean visible) {
        setEnableShowIntroduceDialog(visible);
        for (LandFeature landFeature :
                landFeatures) {
            landFeature.getFeature3D().setVisible(visible);
        }
    }

    /**
     * 如果文件没有存在，那么就会创建一个文件.
     */
    private void loadFeaturesFromFile() {
        openOrCreateFile();
        Scene scene = sceneControl.getScene();
        // 从 kml 中添加
        Layer3Ds layer3Ds = scene.getLayers();
        // 这一行文件不在会抛出错误吗，如果文件不再，那么就读取空文件。
        // 镇压住
        layer3Ds.addLayerWith(layerKMlPath, Layer3DType.KML, true, layerName);
        Layer3D layer3d = scene.getLayers().get(layerName);
        if (layer3d != null) {
            Feature3Ds feature3Ds = layer3d.getFeatures();
            Feature3D[] feature3DArray = feature3Ds.getFeatureArray(Feature3DSearchOption.ALLFEATURES);
            landFeatures.clear();
            try {
                CameraFileReader cameraFileReader = new CameraFileReader(cameraPath);
                Camera[] cameras = cameraFileReader.getCameraArray();
                for (int i = 0; i < feature3DArray.length; i++) {
                    landFeatures.add(new LandFeature(feature3DArray[i], cameras[i]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件名生成文件，存储 kml 文件的信息。
     *
     * @return
     */
    private void openOrCreateFile() {
        try {
            File file = new File(layerKMlPath);
            if (!file.exists()) {
                boolean createdOk= file.createNewFile();
                String msg = createdOk ? "成功" : "失败";
                Log.d(TAG, "openOrCreateFile: " + "没有空间，创建文件" + msg );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        double x = e.getX() - 28.1;
        double y = e.getY() - 0.5;
        Point point = new Point((int)x, (int)y);
        nearByLandmark(point);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void setEnableShowIntroduceDialog(boolean enableShowIntroduceDialog) {
        this.enableShowIntroduceDialog = enableShowIntroduceDialog;
    }

    /**
     * 封装数据的类，方便保存信息。
     */
    private static class LandFeature {
        private Feature3D feature3D;
        private Camera camera;

        LandFeature(Feature3D feature3D, Camera camera) {
            this.feature3D = feature3D;
            this.camera = camera;
        }

        Feature3D getFeature3D() {
            return feature3D;
        }

        Camera getCamera() {
            return camera;
        }
    }
}
