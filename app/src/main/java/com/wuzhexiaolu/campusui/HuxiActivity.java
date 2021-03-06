package com.wuzhexiaolu.campusui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;
import com.supermap.data.Environment;
import com.supermap.data.LicenseStatus;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.realspace.Camera;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.SceneServicesList;
import com.wuzhexiaolu.campusui.AdvanceTechnology.Rocker;
import com.wuzhexiaolu.campusui.ui.*;
import com.wuzhexiaolu.campusui.function.Measure;
import com.wuzhexiaolu.campusui.geocomponent.FlyComponent;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;


public class HuxiActivity extends AppCompatActivity {
    public static final String TAG = "HuxiActivity Tag:";
    //
    private static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/demo/osgb/HuxiCampus/";
    private String flyRoutePathName = rootPath + "fly_routes.fpf";
    private String workspacePath = rootPath + "HuxiCampus.sxwu";
    private String layerKMLPath = rootPath + "HuxiCampus.kml";
    private String cameraPath = rootPath + "cameras.txt";

    private static final String rootPathNoSurface = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/demo/HuxiCampus/";
    private static final String workspacePathNoSurface = rootPathNoSurface + "HuxiCampus2d.sxwu";
//
//    public static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/demo/CBD_android/";
//    public static final String flyRoutePathName = rootPath + "wujing.fpf";
//    public static final String workspacePath = rootPath + "CBD_android.sxwu";
//    public static final String layerKMLPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/initKML/default.kml";
//    public static final String cameraPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperMap/initKML/camera.txt";
//    private static final String sceneName = "CBD_android";

    private static final WorkspaceType workspaceType = WorkspaceType.SXWU;

    private SceneControl sceneControl;

    private ArcMenu arcMenu;
    private LandmarkSearchDialog landmarkSearchDialog;
    private Button buttonExit;
    // 初始视角
    private Camera originalCamera;

    // 用来展示。在刷新的时候，能够用来重新给地标组件重置。
    private IntroductionDialog landIntroduceDialog;

    //测量功能
    private Measure measure;
    //摇杆
    private Rocker rocker;
    //飞行管理与地标
    private FlyComponent flyComponent;
    private LandmarkComponent landmarkComponent;

    private SceneOpenState curSceneOpenState = SceneOpenState.NO_OPENED_SCENE;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 申请权限和证书设置
        PermissionAndLicenseManager.getPermissionAndLicense(this);
        setContentView(R.layout.activity_huxi);
        // 申请权限和证书设置
        boolean isLicenseAvailable = isLicenseAvailable();
        if (!isLicenseAvailable) {
            Log.d(LandmarkComponent.TAG, "Invalid license!:w");
            // 不能够取消，只能够退出
            AlertDialog licenseInvalidAlertDialog = new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("证书无效！点击确定退出")
                    .setPositiveButton("确定", (dialog, which) -> System.exit(0))
                    .setCancelable(false)
                    .create();
            licenseInvalidAlertDialog.show();
            return;
        }
        AlertDialog graphicQualityAlertDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("湖面特效显示需求更高的手机配置，可能会造成卡顿。可根据情况，在右上角选择是否禁用湖面特效。")
                .create();
        sceneControl = findViewById(R.id.sceneControl);
        findViewById(R.id.full_screen_image_campus_d).setVisibility(View.VISIBLE);
        sceneControl.sceneControlInitedComplete(success -> {
            graphicQualityAlertDialog.show();
            Log.d(TAG, "onCreate: Opened");
            if (curSceneOpenState == SceneOpenState.NO_OPENED_SCENE) {
                graphicQualityAlertDialog.dismiss();
                initGeoComponent();
                initUIComponent();
                initFunctionComponent();
                findViewById(R.id.full_screen_image_campus_d).setVisibility(View.GONE);
                originalCamera = sceneControl.getScene().getCamera();
                Button resetCameraButton = findViewById(R.id.reset_camera_button);
                resetCameraButton.setOnClickListener(v -> sceneControl.getScene().setCamera(originalCamera));
                curSceneOpenState = SceneOpenState.WITH_LAKE_SURFACE;

                Switch landmarkVisibleSwitch = findViewById(R.id.landmark_visible_switch);
                // 只有变化的时候才会消失
                landmarkVisibleSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> landmarkComponent.setLandmarkVisible(isChecked)));

                Switch showLakeSurfaceSwitch = findViewById(R.id.show_lake_surface_switch);
                showLakeSurfaceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    boolean openedOk = openLocalScene(isChecked ? workspacePath : workspacePathNoSurface);
                    if (!openedOk) {
                        Log.d(TAG, "onCreate: failed to reload scene。");
                        return ;
                    }
                    Log.d(TAG, "onCreate: changed Mode");
                    SceneOpenState changeSceneState = SceneOpenState.WITH_LAKE_SURFACE;
                    if (curSceneOpenState == SceneOpenState.WITH_LAKE_SURFACE && !isChecked) {
                        changeSceneState = SceneOpenState.NO_LAKE_SURFACE;
                    }
                    curSceneOpenState = changeSceneState;
                    // 重新更新地标组件，因为重新载入 Scene，所以之前在更新地理组建的时候，
                    // 重置了地标，同时搜索框的地理组建过期。
                    landmarkComponent = new LandmarkComponent(this, landIntroduceDialog, layerKMLPath, cameraPath);
                    // 新的地标需要重新映射到搜索框，不然调用以前的 landmarkComponent 是过期的
                    landmarkSearchDialog.stuffWithLandmark(landmarkComponent);
                    // 这个必须要放在更新 landmarkComponent 之后才能够更新，不然这个 landmarkComponent 已经过期了。
                    // 如果原来并没有显示地标，那么重新设置。
                    if (!landmarkVisibleSwitch.isChecked()) {
                        landmarkComponent.setLandmarkVisible(false);
                    }
                });
            }
        });
    }

    private void initFunctionComponent() {
        rocker = new Rocker(sceneControl, this);
        //设置功能
        measure = new Measure(this);
    }

    private void initGeoComponent() {
        openLocalScene(workspacePath);
        landIntroduceDialog = new IntroductionDialog(this);
        flyComponent = new FlyComponent(this, flyRoutePathName, landIntroduceDialog);
        // 场景浏览
        landmarkComponent = new LandmarkComponent(this, landIntroduceDialog, layerKMLPath, cameraPath);
    }

    //初始化超图场景
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initUIComponent() {
        //设置返回和退出按钮监听器
        setButtonBackAndExitListen();
        //设置菜单
        setMenu();
    }

    //设置按钮
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setMenu() {
        arcMenu = findViewById(R.id.arcMenu);
        arcMenu.setRadius(getResources().getDimension(R.dimen.radius));
        arcMenu.setStateChangeListener(new StateChangeListener() {
            @Override
            public void onMenuOpened() {
            }

            @Override
            public void onMenuClosed() {
            }
        });

        //设置地标搜索框
        //获取SearchDialog并且对类进行初始化
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.search_dialog, null);
        landmarkSearchDialog = new LandmarkSearchDialog(this, view, R.style.DialogTypeTheme);
        landmarkSearchDialog.stuffWithLandmark(landmarkComponent);
        FloatingActionButton landmarkSearchFloatingActionButton = findViewById(R.id.searchSubMenu);
        landmarkSearchFloatingActionButton.setOnClickListener(v -> {
            landmarkSearchDialog.show();
            arcMenu.toggleMenu();
        });

        // 从飞行组件中获取路径列表，如果没有就会得到空的，diaLog就没有数据。
        AlertDialog flyRouteAlertDialog = new AlertDialog.Builder(HuxiActivity.this)
                .setTitle("选择要浏览的路线")
                .setItems(flyComponent.getRouteNames(), (dialogInterface, i) -> flyComponent.doPauseOrFly(i))
                .create();
        setDialogTransparent(flyRouteAlertDialog);
        FloatingActionButton flyRouteFloatingActionButton = findViewById(R.id.showRouteSubMenu);
        flyRouteFloatingActionButton.setOnClickListener(v -> {
            arcMenu.toggleMenu();
            flyRouteAlertDialog.show();
        });

        findViewById(R.id.functionSubMenu).setOnClickListener(v -> {
            arcMenu.toggleMenu();
            showMeasureListDialog();
        });
        findViewById(R.id.advanceTechnologySubMenu).setOnClickListener(v -> {
            arcMenu.toggleMenu();
            if (rocker.rockerState == false) {
                rocker.rockerViewRight.setVisibility(View.VISIBLE);
                rocker.rockerViewLeft.setVisibility(View.VISIBLE);
                buttonExit.setVisibility(View.VISIBLE);
                arcMenu.setVisibility(View.INVISIBLE);
                rocker.rockerState = true;
                rocker.rollVerticalSeekBar.setVisibility(View.VISIBLE);
                rocker.panVerticalSeekBar.setVisibility(View.VISIBLE);
                rocker.altitudeVerticalSeekBar.setVisibility(View.VISIBLE);
                rocker.buttonPitchUp.setVisibility(View.VISIBLE);
                rocker.buttonPitchDown.setVisibility(View.VISIBLE);
                Toast.makeText(HuxiActivity.this, "无人机摇杆模拟开始", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置按钮透明度为 0.8
     * @param dialog
     */
    private void setDialogTransparent(Dialog dialog) {
        // 设置透明度
        Window window = dialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.75f;
        window.setAttributes(lp);

    }

    private void openOnlineScene() {
        String url = "http://182.61.28.88:8090/iserver/services/3D-releaseDataWorkspace/rest/realspace";
        SceneServicesList sceneServicesList = new SceneServicesList();
        boolean loadOk = sceneServicesList.load(url);
        if (loadOk) {
            boolean openOk = sceneControl.getScene().open(url, "releaseScene");
            if (openOk) {
                Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 打开一个本地场景
    private boolean openLocalScene(String workspacePath) {
        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
        // 新建一个工作空间对象
        Workspace workspace = new Workspace();
        // 根据工作空间类型，设置服务路径和类型信息。
        info.setServer(workspacePath);
        info.setType(workspaceType);
        // 场景关联工作空间
        Scene scene = sceneControl.getScene();
        boolean openInfoOk = workspace.open(info);
        if (openInfoOk) {
            scene.close();
            scene.setWorkspace(workspace);
        } else {
            return false;
        }
        // 打开场景
        String sceneName = workspace.getScenes().get(0);
        boolean ok = scene.open(sceneName);
        scene.refresh();
        if (ok) {
            Toast.makeText(this, "打开场景成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "打开场景失败", Toast.LENGTH_LONG).show();
        }
        return ok;
    }

//    private void reopenWith()

    // 判断许可是否可用
    @SuppressLint("ShowToast")
    private boolean isLicenseAvailable() {
        LicenseStatus licenseStatus = Environment.getLicenseStatus();
        if (!licenseStatus.isLicenseExsit()) {
            Log.d(LandmarkComponent.TAG, "isLicenseAvailable: " + "许可不存在，场景打开失败，请加入许可");
            return false;
        } else if (!licenseStatus.isLicenseValid()) {
            Log.d(LandmarkComponent.TAG, "isLicenseAvailable: " + "许可过期，场景打开失败，请更换有效许可");
            return false;
        }
        return true;
    }

    //返回和退出监听器
    private void setButtonBackAndExitListen() {
        Button buttonBack = findViewById(R.id.button_back);
//        buttonBack.setOnClickListener(v -> android.os.Process.killProcess(android.os.Process.myPid()));
        buttonBack.setOnClickListener(v -> System.exit(0));

        buttonExit = findViewById(R.id.button_exit);
        // 回调，左上角的退出后重置 rocker 和 测量功能状态。
        buttonExit.setOnClickListener(v -> {
            // 两个 state 用来判断正在执行哪一个功能。
            // 让飞行控件消失
            if (rocker.rockerState) {
                rocker.rockerViewLeft.setVisibility(View.GONE);
                rocker.rockerViewRight.setVisibility(View.GONE);
                rocker.rockerState = false;
                rocker.rollVerticalSeekBar.setVisibility(View.GONE);
                rocker.panVerticalSeekBar.setVisibility(View.GONE);
                rocker.altitudeVerticalSeekBar.setVisibility(View.GONE);
                rocker.buttonPitchUp.setVisibility(View.GONE);
                rocker.buttonPitchDown.setVisibility(View.GONE);
                Toast.makeText(HuxiActivity.this, "您已退出摇杆模式", Toast.LENGTH_SHORT).show();
            }
            // 退出的时候，让测量控件消失，重置选择的行为。
            if (measure.functionState) {
                measure.exitMeasurement();
                Toast.makeText(HuxiActivity.this, "您已退出测量模式", Toast.LENGTH_SHORT).show();
            }
            buttonExit.setVisibility(View.INVISIBLE);
            arcMenu.setVisibility(View.VISIBLE);
            landmarkComponent.setEnableShowIntroduceDialog(true);
        });
    }

    //展示路线列表框
    public void showMeasureListDialog() {
        final String[] items3 = new String[]{"距离计算", "面积计算"};//创建item
        //添加列表
        AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                .setTitle("功能选择")
                .setItems(items3, (dialogInterface, i) -> {
                    landmarkComponent.setEnableShowIntroduceDialog(false);
                    if (!measure.functionState) {
                        buttonExit.setVisibility(View.VISIBLE);
                        arcMenu.setVisibility(View.INVISIBLE);
                        Toast.makeText(HuxiActivity.this, "您已进入测量模式", Toast.LENGTH_SHORT).show();
                        switch (i) {
                            case 0:
                                measure.doDistanceMeasurement();
                                break;
                            case 1:
                                measure.doAreaMeasurement();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .create();
        setDialogTransparent(alertDialog3);
        alertDialog3.show();
    }
}
