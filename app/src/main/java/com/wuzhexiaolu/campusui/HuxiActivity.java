package com.wuzhexiaolu.campusui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;
import com.supermap.data.Environment;
import com.supermap.data.LicenseStatus;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.realspace.Action3D;
import com.supermap.realspace.Camera;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.SceneServicesList;
import com.wuzhexiaolu.campusui.AdvanceTechnology.Rocker;
import com.wuzhexiaolu.campusui.ui.*;
import com.wuzhexiaolu.campusui.function.Measure;
import com.wuzhexiaolu.campusui.geocomponent.FlyComponent;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;

import java.util.concurrent.atomic.AtomicInteger;


public class HuxiActivity extends AppCompatActivity {
    public static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String flyRoutePathName = rootPath + "/SuperMap/demo/CBD_android/wujing.fpf";
    public static final String workspacePath = rootPath + "/SuperMap/demo/CBD_android/CBD_android.sxwu";

    private Workspace workspace;
    private SceneControl sceneControl;

    private ArcMenu arcMenu;
    private Button buttonExit;
    private TextView result;
    // 初始视角
    private Camera originalCamera;

    //测量功能
    private Measure measure;
    //摇杆
    private Rocker rocker;
    //飞行管理与地标
    private FlyComponent flyComponent;
    private LandmarkComponent landmarkComponent;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huxi);
        sceneControl = findViewById(R.id.sceneControl);
        findViewById(R.id.full_screen_image_campus_d).setVisibility(View.VISIBLE);
        sceneControl.sceneControlInitedComplete(success -> {
            initGeoComponent();
            initUIComponent();
            initFunctionComponent();
            findViewById(R.id.full_screen_image_campus_d).setVisibility(View.GONE);
            originalCamera = sceneControl.getScene().getCamera();
            Button resetCameraButton = findViewById(R.id.reset_camera_button);
            resetCameraButton.setOnClickListener(v -> sceneControl.getScene().setCamera(originalCamera));
        });
    }

    private void initFunctionComponent() {
        rocker = new Rocker(sceneControl, this);
        //设置功能
        result = (TextView) findViewById(R.id.measureResult);
        measure = new Measure(result, sceneControl);
    }

    private void initGeoComponent() {
        boolean isLicenseAvailable = isLicenseAvailable();
        if (!isLicenseAvailable) {
            return;
        }
        openLocalScene();
        IntroductionDialog landIntroduceDialog = new IntroductionDialog(this);
        flyComponent = new FlyComponent(this, flyRoutePathName, landIntroduceDialog);
        // 场景浏览
        landmarkComponent = new LandmarkComponent(this, landIntroduceDialog);
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
        SearchDialog searchDialog = new SearchDialog(this, view, R.style.DialogTypeTheme);
        searchDialog.stuffWithLandmark(landmarkComponent);
        FloatingActionButton landmarkSearchFloatingActionButton = findViewById(R.id.searchSubMenu);
        landmarkSearchFloatingActionButton.setOnClickListener(v -> {
            searchDialog.show();
            arcMenu.toggleMenu();
        });

        // 从飞行组件中获取路径列表，如果没有就会得到空的，diaLog就没有数据。
        AlertDialog flyRouteAlertDialog = new AlertDialog.Builder(HuxiActivity.this)
                .setTitle("选择要浏览的路线")
                .setItems(flyComponent.getRouteNames(), (dialogInterface, i) -> {
                    flyComponent.doPauseOrFly(i);
                })
                .create();
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
    private void openLocalScene() {
        WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
        // 新建一个工作空间对象
        if (workspace == null) {
            workspace = new Workspace();
        }
        // 根据工作空间类型，设置服务路径和类型信息。
        info.setServer(workspacePath);
        info.setType(WorkspaceType.SXWU);
        // 场景关联工作空间
        if (workspace.open(info)) {
            Scene scene = sceneControl.getScene();
            scene.setWorkspace(workspace);
        }
        // 打开场景
        String name = workspace.getScenes().get(0);
        boolean ok = sceneControl.getScene().open(name);
        if (ok) {
            Toast.makeText(this, "打开场景成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "打开场景失败", Toast.LENGTH_LONG).show();
        }
    }

    // 判断许可是否可用
    @SuppressLint("ShowToast")
    private boolean isLicenseAvailable() {
        LicenseStatus licenseStatus = Environment.getLicenseStatus();
        if (!licenseStatus.isLicenseExsit()) {
            Toast.makeText(this, "许可不存在，场景打开失败，请加入许可", Toast.LENGTH_LONG).show();
            return false;
        } else if (!licenseStatus.isLicenseValid()) {
            Toast.makeText(this, "许可过期，场景打开失败，请更换有效许可", Toast.LENGTH_LONG).show();
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
        buttonExit.setOnClickListener(v -> {
            if (rocker.rockerState == true) {
                rocker.rockerViewLeft.setVisibility(View.GONE);
                rocker.rockerViewRight.setVisibility(View.GONE);
                rocker.rockerState = false;
                viewStateChange();
                rocker.rollVerticalSeekBar.setVisibility(View.GONE);
                rocker.panVerticalSeekBar.setVisibility(View.GONE);
                rocker.altitudeVerticalSeekBar.setVisibility(View.GONE);
                rocker.buttonPitchUp.setVisibility(View.GONE);
                rocker.buttonPitchDown.setVisibility(View.GONE);
                Toast.makeText(HuxiActivity.this, "您已退出摇杆模式", Toast.LENGTH_SHORT).show();
            }
            if (measure.functionState == true) {
                result.setVisibility(View.INVISIBLE);
                sceneControl.setAction(Action3D.PANSELECT3D);
                viewStateChange();
                measure.functionState = false;
                Toast.makeText(HuxiActivity.this, "您已退出测量模式", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //展示路线列表框
    public void showMeasureListDialog() {
        final String[] items3 = new String[]{"距离计算", "面积计算"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                .setTitle("功能选择")
                .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (measure.functionState == false) {
                            measure.functionState = true;
                            result.setVisibility(View.VISIBLE);
                            viewStateChange();
                            Toast.makeText(HuxiActivity.this, "您已进入测量模式", Toast.LENGTH_SHORT).show();
                            switch (i) {
                                case 0:
                                    measure.closeAnalysis();
                                    measure.AnalysisTypeArea = 0;
                                    measure.startMeasureAnalysis();
                                    break;
                                case 1:
                                    measure.closeAnalysis();
                                    measure.AnalysisTypeArea = 1;
                                    measure.startSurearea();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                })
                .create();
        alertDialog3.show();
    }


    public void viewStateChange() {
        if (buttonExit.getVisibility() == View.VISIBLE && arcMenu.getVisibility() == View.INVISIBLE) {
            buttonExit.setVisibility(View.INVISIBLE);
            arcMenu.setVisibility(View.VISIBLE);
        } else {
            buttonExit.setVisibility(View.VISIBLE);
            arcMenu.setVisibility(View.INVISIBLE);
        }
    }
}
