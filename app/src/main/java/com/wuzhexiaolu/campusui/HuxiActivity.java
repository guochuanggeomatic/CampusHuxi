package com.wuzhexiaolu.campusui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kongqw.rockerlibrary.view.RockerView;
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
import com.supermap.realspace.Sightline;
import com.wuzhexiaolu.campusui.AdvanceTechnology.Rocker;
import com.wuzhexiaolu.campusui.control.*;
import com.wuzhexiaolu.campusui.function.Measure;
import com.wuzhexiaolu.campusui.geocomponent.FlyComponent;
import com.wuzhexiaolu.campusui.geocomponent.GestureListenerForLandmark;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;

import java.util.Timer;
import java.util.TimerTask;


public class HuxiActivity extends AppCompatActivity {
    private Workspace workspace;
    private Scene scene;
    private SceneControl sceneControl;
    // 离线三维场景数据名称
    private String workspacePath;

    // 三维场景名称
    private WorkspaceConnectionInfo info;
    private WorkspaceType workspaceTypetemp = null;
    private boolean isLicenseAvailable = false;

    //手机根目录
    private static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String localSceneDirPath = rootPath + "/SuperMap/demo/CBD_android/";
    public static final String routePathName = "CBD_android";

    //view声明
    private SearchDialog searchDialog;
    private IntroduceDialog introduceDialog;
    private ListView listView;
    private SearchView searchView;
    private ArcMenu arcMenu;
    private Button buttonExit;
    private Button buttonBack;

    private TextView result;


    //测量功能
    private Measure measure;
    //摇杆
    private Rocker rocker;
    //飞行管理与地标
    private FlyComponent flyComponent;
    private LandmarkComponent landmarkComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化SuperMap环境
        initSuperMapEnvironment();

        setContentView(R.layout.activity_huxi);
        sceneControl = findViewById(R.id.sceneControl);
        sceneControl.sceneControlInitedComplete(success -> {
            initAllComponent();
        });
    }


    private void initAllComponent() {
        initGeoComponent();
        initUIComponent();
        initFunctionComponent();
    }

    private void initFunctionComponent() {
        rocker = new Rocker(sceneControl, this);
        //设置功能
        result = (TextView) findViewById(R.id.measureResult);
        measure = new Measure(result, sceneControl);
    }

    private void initGeoComponent() {
        isLicenseAvailable = isLicenseAvailable();
        if (!isLicenseAvailable) {
            return;
        }
        openLocalScene();
        flyComponent = new FlyComponent(sceneControl);
        landmarkComponent = new LandmarkComponent(this);
        // 场景浏览
        flyComponent.prepareFly(HuxiActivity.this, routePathName, localSceneDirPath);
        // 设置长按监听，地标相关操作
        GestureListenerForLandmark gestureListenerForLandmark = new GestureListenerForLandmark(HuxiActivity.this, landmarkComponent);
        GestureDetector gestureDetector = new GestureDetector(HuxiActivity.this, gestureListenerForLandmark);
        sceneControl.setGestureDetector(gestureDetector);
    }

    //初始化超图场景
    private void initUIComponent() {
        //设置返回和退出按钮监听器
        setButtonBackAndExitListen();
        //设置搜索框
        setSearchDialog(landmarkComponent.getLandmarkNames());
        //设置菜单
        setMenu();
    }

    //设置按钮
    private void setMenu() {
        arcMenu = (ArcMenu) findViewById(R.id.arcMenu);
        arcMenu.setRadius(getResources().getDimension(R.dimen.radius));

        arcMenu.setStateChangeListener(new StateChangeListener() {
            @Override
            public void onMenuOpened() {
            }

            @Override
            public void onMenuClosed() {
            }
        });

        findViewById(R.id.searchSubMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialog.show();
                arcMenu.toggleMenu();
            }
        });
        findViewById(R.id.showRouteSubMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arcMenu.toggleMenu();
                showRouteListDialog();
            }
        });
        findViewById(R.id.functionSubMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arcMenu.toggleMenu();
                showMeasureListDialog();
            }
        });
        findViewById(R.id.advanceTechnologySubMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }

    //初始化SuperMap环境
    private void initSuperMapEnvironment() {
        Environment.setLicensePath(rootPath + "/SuperMap/license/");
        Environment.setWebCacheDirectory(rootPath + "/SuperMap/WebCache/");
        Environment.setTemporaryPath(rootPath + "/SuperMap/temp/");
        Environment.initialization(this);
    }

    // 打开一个本地场景
    private void openLocalScene() {
        workspacePath = rootPath + "/SuperMap/demo/CBD_android/CBD_android.sxwu";
        info = new WorkspaceConnectionInfo();
        // 新建一个工作空间对象
        if (workspace == null) {
            workspace = new Workspace();
        }
        // 根据工作空间类型，设置服务路径和类型信息。
        workspaceTypetemp = WorkspaceType.SXWU;
        info.setServer(workspacePath);
        info.setType(workspaceTypetemp);
        // 场景关联工作空间
        if (workspace.open(info)) {
            scene = sceneControl.getScene();
            scene.setWorkspace(workspace);
        }
        // 打开场景
        String name = workspace.getScenes().get(0);
        boolean successed = sceneControl.getScene().open(name);
        if (successed) {
            Toast.makeText(HuxiActivity.this, "打开场景成功", Toast.LENGTH_LONG);
        }
    }

    // 判断许可是否可用
    @SuppressLint("ShowToast")
    private boolean isLicenseAvailable() {
        LicenseStatus licenseStatus = Environment.getLicenseStatus();
        if (!licenseStatus.isLicenseExsit()) {
            Toast.makeText(HuxiActivity.this, "许可不存在，场景打开失败，请加入许可", Toast.LENGTH_LONG).show();
            return false;
        } else if (!licenseStatus.isLicenseValid()) {
            Toast.makeText(HuxiActivity.this, "许可过期，场景打开失败，请更换有效许可", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    //设置搜索框
    private void setSearchDialog(String[] listViewItems) {
        //获取searchdialog并且对类进行初始化
        View v = getLayoutInflater().inflate(R.layout.search_dialog, null);
        searchDialog = new SearchDialog(this, 0, 0, v, R.style.DialogTypeTheme);
        searchDialog.setCancelable(true);
        SearchView searchView = searchDialog.getSearchView();
        ListView listView = searchDialog.getListView();
        final ArrayAdapter arrayAdapter;
        //对布局内的控件进行设置
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listViewItems);
        listView.setAdapter(arrayAdapter);
        //listview启动过滤
        listView.setTextFilterEnabled(true);
        //一开始不显示
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            landmarkComponent.flyToSpecifiedLand((String) arrayAdapter.getItem(position));
            searchDialog.hide();
        });
        //显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //单击搜索按钮的监听
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //输入字符的监听
            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    //返回和退出监听器
    private void setButtonBackAndExitListen() {
        buttonBack = (Button) findViewById(R.id.button_back);
        buttonExit = (Button) findViewById(R.id.button_exit);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
    }

    //展示路线列表框
    public void showRouteListDialog() {
        final String[] items3 = new String[]{"Route1", "Route2", "Route3", "Route4"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                .setTitle("选择要浏览的路线")
                .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                flyComponent.startOrPauseFly(HuxiActivity.this);
                        }
                    }
                })
                .create();
        alertDialog3.show();
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
