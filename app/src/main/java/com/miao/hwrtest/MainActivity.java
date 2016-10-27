package com.miao.hwrtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaRouter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private Paint paint;
    private Path path;
    private int top;
    private int bottom;
    private boolean isRunning = true;
    private Canvas canvas;
    private String TAG = MainActivity.class.getSimpleName();
    private boolean start = true;
    private long init;
    private long now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        surface = (SurfaceView) findViewById(R.id.surface);

        surfaceHolder = surface.getHolder();        // 获得SurfaceHolder对象
        surface.setZOrderOnTop(true);               //使surface可见
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);   //设置背景透明
        surfaceHolder.addCallback(this);          // 为SurfaceView添加状态监听

        paint = new Paint();          // 创建一个画笔对象
        path = new Path();
    }

    /**
     * 当SurfaceView创建的时候，调用此函数
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int[] location = new int[2];
        surface.getLocationOnScreen(location);
        top = location[1];
        Log.d(TAG, "top = " + top);
        bottom = top + surface.getHeight();
        Log.d(TAG, "bottom = " + bottom);
        //开启一个绘画线程
        new Thread(wlineThread).start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getY() >= top && event.getY() <= bottom) {
            start = true;
            init = now;
            now = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (now - init >= 100 && now - init <= 1000) {      //抬笔操作，加上(-1,0)
                        Log.d(TAG, "X坐标=" + "-1" + "\tY坐标=" + "0");
                    }
                    path.moveTo(event.getX(), event.getY() - top);
                    Log.d(TAG, "X坐标=" + event.getX() + "\tY坐标=" + event.getY());

                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(event.getX(), event.getY() - top);
                    Log.d(TAG, "X坐标=" + event.getX() + "\tY坐标=" + event.getY());
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isRunning = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 当SurfaceView的视图发生改变的时候，调用此函数
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 当SurfaceView销毁的时候，调用此函数
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface Destroyed");
        isRunning = false;
    }

    Runnable wlineThread =new Runnable(){

        @Override
        public void run() {
            while (isRunning) {
                drawView();
                if (start) {
                    long temp = System.currentTimeMillis() - now;
                    if (temp > 1000) {      //抬笔时间超过1秒，加上坐标(-1,-1)
                        Log.d(TAG, "X坐标=" + "-1" + "\tY坐标=" + "-1");


                        start = false;

                        clearCanvas();
                    }
                }
//                try {
//                    Thread.sleep(80);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };

    /**
     * 清空Canvas上的笔迹
     */
    private void clearCanvas() {
        for (int i = 0; i < 4; i++) {
            try {
                if (surfaceHolder != null) {

                    canvas = surfaceHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    path.reset();

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null)
                    surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 再Canvas上画
     */
    private void drawView() {
        try {
            if (surfaceHolder != null) {
                canvas = surfaceHolder.lockCanvas();
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

                canvas.drawPath(path, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
