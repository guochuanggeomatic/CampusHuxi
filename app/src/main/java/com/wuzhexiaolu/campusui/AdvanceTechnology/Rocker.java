package com.wuzhexiaolu.campusui.AdvanceTechnology;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.kongqw.rockerlibrary.view.RockerView;
import com.supermap.realspace.Camera;
import com.supermap.realspace.SceneControl;
import com.wuzhexiaolu.campusui.HuxiActivity;
import com.wuzhexiaolu.campusui.R;
import com.wuzhexiaolu.campusui.control.VerticalSeekBar;

import java.util.Timer;
import java.util.TimerTask;

public class Rocker {
    public android.support.v7.widget.AppCompatImageButton buttonPitchUp;
    public android.support.v7.widget.AppCompatImageButton buttonPitchDown;
    public RockerView rockerViewLeft;
    public RockerView rockerViewRight;
    public VerticalSeekBar rollVerticalSeekBar;
    public VerticalSeekBar panVerticalSeekBar;
    public VerticalSeekBar altitudeVerticalSeekBar;

    //摇杆

    public boolean rockerState = false;


    private Timer timerPan;
    private TimerTask timerTaskPan;
    private Handler handlerPan;

    private Timer timerAltitudeRoll;
    private TimerTask timerTaskAltitudeRoll;
    private Handler handlerAltitudeRoll;

    private Timer timerPitchUp;
    private TimerTask timerTaskPitchUp;
    private Handler handlerPitchUp;

    private Timer timerPitchDown;
    private TimerTask timerTaskPitchDown;
    private Handler handlerPitchDown;


    private double angleRockerRight;

    private double rollAngleCalValue = 1;
    private double altitudeAngleCalValue = 1;
    private double panAngleCalValue = 1;

    private double scaleRoll = 1;
    private double scalePan = 1;
    private double scaleAltitude = 1;

    private double deltaAltitude = 0.1;
    private double cameraAltitude;
    private Camera camera;

    private HuxiActivity context;
    private SceneControl sceneControl;

    public Rocker(SceneControl sceneControl, HuxiActivity context){
        this.sceneControl = sceneControl;
        this.context = context;
        findViewById();
        setSeekBar();
        setButtonPitch();
        setRocker();
    }

    private void findViewById(){
        buttonPitchDown = context.findViewById(R.id.pitchDown);
        buttonPitchUp = context.findViewById(R.id.pitchUp);
        rockerViewLeft = context.findViewById(R.id.rockerView_left);
        rockerViewRight = context.findViewById(R.id.rockerView_right);
        rollVerticalSeekBar = context.findViewById(R.id.rollVerticalSeekBar);
        altitudeVerticalSeekBar = context.findViewById(R.id.altitudeVerticalSeekBar);
        panVerticalSeekBar = context.findViewById(R.id.panVerticalSeekBar);
    }

    private void setRocker(){
        if (rockerViewRight != null) {
            rockerViewRight.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        rockerViewRight.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void angle(double angle) {
                                angleRockerRight = angle;
                            }

                            @Override
                            public void onFinish() {
                                timerPan.cancel();
                            }
                        });
                        rockerViewRight.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
                            @Override
                            public void onDistanceLevel(int level) {
                                panAngleCalValue = 0.1 * level;
                            }
                        });
                        panLongTouch();

                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    }
                    return false;
                }
            });
        }


        if (rockerViewLeft != null) {
            rockerViewLeft.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        rockerViewLeft.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void angle(double angle) {
                                rollAngleCalValue = -Math.cos((360 - angle) * Math.PI / 180);
                                altitudeAngleCalValue = Math.sin((360 - angle) * Math.PI / 180);
                            }

                            @Override
                            public void onFinish() {
                                timerAltitudeRoll.cancel();
                            }
                        });
                        rockerViewLeft.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
                            @Override
                            public void onDistanceLevel(int level) {
                                altitudeAngleCalValue = 0.1 * level;
                                rollAngleCalValue = 0.1 * level;
                            }
                        });
                        altitudeRollLongTouch();
                    }
                    return false;
                }
            });
        }
    }

    private void setSeekBar(){
        rollVerticalSeekBar.setThumb(R.mipmap.color_seekbar_thum);
        rollVerticalSeekBar.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
                scaleRoll = 0.015 * progress + 0.5;
                Toast.makeText(context, "当前的旋转系数为" + String.valueOf(scaleRoll), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });
        panVerticalSeekBar.setThumb(R.mipmap.star);
        panVerticalSeekBar.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
                scalePan = 0.015 * progress + 0.5;
                Toast.makeText(context, "当前的平移系数为" + String.valueOf(scalePan), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });
        altitudeVerticalSeekBar.setThumb(R.mipmap.color_seekbar_thum);
        altitudeVerticalSeekBar.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {

            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
                scaleAltitude = 0.015 * progress + 0.5;
                Toast.makeText(context, "当前的升降系数为" + String.valueOf(scaleAltitude), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {

            }
        });
    }

    private void setButtonPitch(){
        buttonPitchUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    timerPitchUp = new Timer();

                    handlerPitchUp = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case 0:
                                    sceneControl.getScene().setPitch(-0.25);
                            }
                        }
                    };

                    timerTaskPitchUp = new TimerTask() {
                        @Override
                        public void run() {
                            handlerPitchUp.sendEmptyMessage(0);
                        }
                    };

                    timerPitchUp.schedule(timerTaskPitchUp, 10, 10);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    timerPitchUp.cancel();
                }
                return false;
            }
        });
        buttonPitchDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    timerPitchDown = new Timer();

                    handlerPitchDown = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case 0:
                                    sceneControl.getScene().setPitch(0.25);
                            }
                        }
                    };

                    timerTaskPitchDown = new TimerTask() {
                        @Override
                        public void run() {
                            handlerPitchDown.sendEmptyMessage(0);
                        }
                    };
                    timerPitchDown.schedule(timerTaskPitchDown, 10, 10);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    timerPitchDown.cancel();
                }
                return false;

            }
        });
    }

    private void altitudeRollLongTouch() {
        timerAltitudeRoll = new Timer();

        handlerAltitudeRoll = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        sceneControl.getScene().setRollEye(rollAngleCalValue * scaleRoll * 0.25);
                        setCameraAltitude();

                }
            }
        };

        timerTaskAltitudeRoll = new TimerTask() {
            @Override
            public void run() {
                handlerAltitudeRoll.sendEmptyMessage(0);
            }
        };

        timerAltitudeRoll.schedule(timerTaskAltitudeRoll, 10, 10);
    }

    //设置相机高度
    private void setCameraAltitude() {
        camera = sceneControl.getScene().getCamera();
        cameraAltitude = camera.getAltitude();
        camera.setAltitude(cameraAltitude + scaleAltitude * deltaAltitude * altitudeAngleCalValue);
        sceneControl.getScene().setCamera(camera);
    }

    //长按平移模型
    private void panLongTouch() {
        timerPan = new Timer();

        handlerPan = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        pan(angleRockerRight);
                }
            }
        };

        timerTaskPan = new TimerTask() {
            @Override
            public void run() {
                handlerPan.sendEmptyMessage(0);
            }
        };

        timerPan.schedule(timerTaskPan, 10, 10);
    }

    public void pan(double angle) {
        angle = angle + 90;
        double l = 0.00001;
        double heading = sceneControl.getScene().getCamera().getHeading();
        double longla = scalePan * panAngleCalValue * l * Math.sin((heading + angle) * Math.PI / 180);
        double lan = scalePan * panAngleCalValue * l * Math.cos((heading + angle) * Math.PI / 180);
        sceneControl.getScene().pan(longla, lan);
    }
}
