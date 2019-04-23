package com.wuzhexiaolu.campusui.geocomponent;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.View;
import android.widget.Toast;

import com.supermap.realspace.Scene;
import com.wuzhexiaolu.campusui.HuxiActivity;
import com.wuzhexiaolu.campusui.R;
import com.supermap.data.GeoPlacemark;
import com.supermap.data.GeoPoint3D;
import com.supermap.data.Point3D;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.Feature3DSearchOption;
import com.supermap.realspace.Feature3Ds;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.Layer3DType;
import com.supermap.realspace.Layer3Ds;
import com.supermap.realspace.PixelToGlobeMode;
import com.supermap.realspace.SceneControl;
import com.wuzhexiaolu.campusui.control.IntroduceDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * 这个类应该使用单例模式，防止存储多个文件
 */
public class LandmarkComponent {
    public static final String TAG = "On LandmarkComponent";
    private static final String layerName = "Favorite_KML";
    private static final double radius = 15.;
    private static final int flyTime = 3000;
    private static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String layerKMlPath = rootPath + "/SuperMap/initKML/default.kml";
    private static String cameraPath = rootPath + "/SuperMap/initKML/camera.txt";

    private HuxiActivity context;
    private SceneControl sceneControl;
    private IntroduceDialog introduceDialog;
    private Layer3Ds layer3Ds;

    private ArrayList<LandFeature> landFeatures= new ArrayList<>();

    public LandmarkComponent(HuxiActivity context) {
        super();
        this.context = context;
        this.sceneControl = context.findViewById(R.id.sceneControl);
        loadFeaturesFromFile();
    }

    public void processSingleTap(Point point) {
        nearByLandmark(point);
    }

    /**
     * 通过传入的 android.graphics.Point 的类和半径来判断，附近是否有点.
     *
     * @param point
     * @return
     */
    private void nearByLandmark(Point point) {
        Point3D point3D = sceneControl.getScene().pixelToGlobe(point, PixelToGlobeMode.TERRAINANDMODEL);
        double minDistance = LandmarkComponent.radius;
        Feature3D nearPoint = null;
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
        if (nearPoint != null) {
            setSiteClickDialog();
            introduceDialog.show();
        } else {
            Toast.makeText(context, "附近没有地标" , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取全部的地标名字
     *
     * @return
     */
    public String[] getLandmarkNames() {
        String[] ret = new String[landFeatures.size()];
        for (int i = 0; i < landFeatures.size(); i++) {
            ret[i] = landFeatures.get(i).getFeature3D().getName();
        }
        return ret;
    }

    /**
     * 提供地标的名称，点击之后将会飞到那个地方。
     *
     * @param landmarkName
     */
    @SuppressLint("NewApi")
    public void flyToSpecifiedLand(String landmarkName) {
        for (LandFeature landFeature :
                landFeatures) {
            if (Objects.equals(landFeature.getFeature3D().getName(), landmarkName)) {
                changeCameraTo(landFeature);
                return ;
            }
        }
        Toast.makeText(context, layerKMlPath + ".kml 文件中并没有这个地标", Toast.LENGTH_LONG).show();
    }

    /**
     * 提供地标的序号，点击之后将会飞到那个地方。
     *
     * @param index
     */
    public void flyToSpecifiedLand(int index) {
        if (index < 0 || index >= landFeatures.size()) {
            return ;
        }
        changeCameraTo(landFeatures.get(index));
    }

    private void flyToFeature3D(Feature3D feature3D) {
        GeoPlacemark geoPlacemark = (GeoPlacemark) feature3D.getGeometry();
        GeoPoint3D geoPoint3D = (GeoPoint3D) geoPlacemark.getGeometry();
        Point3D pot = new Point3D(geoPoint3D.getX(), geoPoint3D.getY(), geoPoint3D.getZ() + 200);
        sceneControl.getScene().flyToPoint(pot, flyTime);
        Toast.makeText(context, "正在飞向" + feature3D.getName(), Toast.LENGTH_LONG).show();
    }

    private void changeCameraTo(LandFeature landFeature) {
        Scene scene = sceneControl.getScene();
        scene.setCamera(landFeature.getCamera());
    }

    /**
     * 如果文件没有存在，那么就会创建一个文件
     */
    private void loadFeaturesFromFile() {
        openOrCreateFile(layerKMlPath);
        // 从 kml 中添加
        layer3Ds = sceneControl.getScene().getLayers();
        layer3Ds.addLayerWith(layerKMlPath, Layer3DType.KML, true, layerName);
        Layer3D layer3d = sceneControl.getScene().getLayers().get(layerName);
        if (layer3d != null) {
            Feature3Ds feature3Ds = layer3d.getFeatures();
            Feature3D[] feature3DArray = feature3Ds.getFeatureArray(Feature3DSearchOption.ALLFEATURES);
            landFeatures.clear();
            try {
                FileInputStream fis = new FileInputStream(cameraPath);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                for (Feature3D feature3D :
                        feature3DArray) {
                    String record = bufferedReader.readLine();
                    if (record  == null) {
                        break;
                    }
                    CameraArgs cameraArgs = new CameraArgs(record);
                    landFeatures.add(new LandFeature(feature3D, cameraArgs.toCamera(), cameraArgs.toLookAt()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void hideLandmarks(String hideLayerName) {
        Layer3D layer3D = layer3Ds.get(hideLayerName);
        if (layer3D != null) {
            layer3Ds.removeLayerWithName(hideLayerName);
        }
    }

    /**
     * 根据文件名生成文件，存储 kml 文件的信息。
     *
     * @param filePath
     * @return
     */
    private static void openOrCreateFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置地标点击框
    private void setSiteClickDialog() {
        View v = context.getLayoutInflater().inflate(R.layout.site_introduce, null);
        introduceDialog = new IntroduceDialog(context, 0, 0, v, R.style.DialogTypeTheme);
        introduceDialog.setCancelable(true);
    }
}
