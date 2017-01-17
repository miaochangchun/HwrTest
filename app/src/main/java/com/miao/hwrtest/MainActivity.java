package com.miao.hwrtest;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.miao.util.ConfigUtil;
import com.miao.util.HciCloudHwrHelper;
import com.miao.util.HciCloudSysHelper;
import com.miao.util.OnHwrRecogListener;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final int MAX_POINT = 2048;
    private static final int RECOG_RESULT = 10000;
    private static final int RECOG_ERROR = 10001;
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
    private int mCurX, mCurY;
    private int mCurIndex;
    private short[] mPoints = null;
    private boolean mEnd;
    private HciCloudSysHelper mHciCloudSysHelper;
    private HciCloudHwrHelper mHciCloudHwrHelper;
    private TextView tvResult;

    private Handler myHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case RECOG_RESULT:
                    Bundle resultBundle = msg.getData();
                    String result = resultBundle.getString("result");
                    tvResult.setText(result);
                    break;

                case RECOG_ERROR:
                    Bundle errorBundle = msg.getData();
                    String error = errorBundle.getString("error");
                    Toast.makeText(MainActivity.this, "识别出错，错误码=" + error, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        int errCode = initSinovoice();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            Toast.makeText(this, "初始化失败，错误码是" + errCode, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        surface = (SurfaceView) findViewById(R.id.surface);
        tvResult = (TextView) findViewById(R.id.text);

        surfaceHolder = surface.getHolder();        // 获得SurfaceHolder对象
        surface.setZOrderOnTop(true);               //使surface可见
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);   //设置背景透明
        surfaceHolder.addCallback(this);          // 为SurfaceView添加状态监听

        paint = new Paint();          // 创建一个画笔对象
        path = new Path();

        mPoints = new short[MAX_POINT * 2];
        mCurIndex = 0;
    }

    /**
     * 灵云系统初始化
     * @return 0成功，其他失败
     */
    private int initSinovoice() {
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudHwrHelper = HciCloudHwrHelper.getInstance();
        int errorCode = mHciCloudSysHelper.init(this);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            return errorCode;
        }
        errorCode = mHciCloudHwrHelper.initHwr(this, ConfigUtil.CAP_KEY_HWR_LOCAL_FREESTYLUS);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            return errorCode;
        }
        return HciErrorCode.HCI_ERR_NONE;
    }

    /**
     *  把坐标添加到short数组
     * @param x
     * @param y
     * @return
     */
    private boolean addStroke(short x, short y) {
        if (mCurX >= 0 && mCurY >= 0) {
            if ((mCurIndex / 2) < (MAX_POINT - 2)) {
                mPoints[mCurIndex] = x;
                mCurIndex++;
                mPoints[mCurIndex] = y;
                mCurIndex++;
                return true;
            } else if ((mCurIndex / 2) == (MAX_POINT - 2)) {
                mPoints[mCurIndex] = -1;
                mCurIndex++;
                mPoints[mCurIndex] = 0;
                mCurIndex++;
                return true;
            }
        }
        return false;
    }

    /**
     * 最后一笔添加坐标(-1,-1)
     */
    public void addLastStrokePoint() {
        if(mCurIndex < 2 ||(mPoints[mCurIndex-1] == -1 && mPoints[mCurIndex-2] == -1))
        {
            return;
        }
        mPoints[mCurIndex] = -1;
        mCurIndex++;
        mPoints[mCurIndex] = -1;
        mCurIndex++;
    }

    /**
     * 每次抬笔添加坐标(-1,0)
     */
    private void addStrokeEnd() {
        mPoints[mCurIndex] = -1;
        mCurIndex++;
        mPoints[mCurIndex] = 0;
        mCurIndex++;
    }

    /**
     * 重置笔迹点数据
     */
    private void resetStroke() {
        mPoints = new short[MAX_POINT * 2];
        mCurIndex = 0;
    }

    /**
     * 获取笔迹点数组
     * @return 笔迹点数组
     */
    public short[] getStroke() {
        mEnd = true;
        addStrokeEnd();
        addLastStrokePoint();
        short[] stroke = new short[mCurIndex];
        System.arraycopy(mPoints, 0, stroke, 0, mCurIndex);

        return stroke;
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
                        addStrokeEnd();
                    }
                    path.moveTo(event.getX(), event.getY() - top);
                    break;

                case MotionEvent.ACTION_MOVE:
                    path.lineTo(event.getX(), event.getY() - top);
                    addStroke((short) event.getX(), (short) event.getY());
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

    Runnable wlineThread = new Runnable(){

        @Override
        public void run() {
            while (isRunning) {
                drawView();
                if (start) {
                    long temp = System.currentTimeMillis() - now;
//                    Log.d(TAG, "temp=" + temp);
                    if (temp > 1000 && temp < 100000) {      //抬笔时间超过1秒，加上坐标(-1,-1),过滤第一次时间的计算
                        short[] data = getStroke();

                        mHciCloudHwrHelper.recog(data, ConfigUtil.CAP_KEY_HWR_LOCAL_FREESTYLUS, new OnHwrRecogListener() {
                            @Override
                            public void onHwrResult(HwrRecogResult recogResult) {
                                StringBuilder sBuilder = new StringBuilder();
                                for (int i=0; i<recogResult.getResultItemList().size(); i++) {
                                    sBuilder.append(recogResult.getResultItemList().get(i).getResult()).append(";");
                                }
                                Message message = new Message();
                                message.arg1 = RECOG_RESULT;
                                Bundle bundle = new Bundle();
                                bundle.putString("result", sBuilder.toString());
                                message.setData(bundle);
                                myHander.sendMessage(message);
                            }

                            @Override
                            public void onError(int errCode) {
//                                Toast.makeText(MainActivity.this, "识别出错，错误码是" + errCode, Toast.LENGTH_SHORT).show();
                                Message message = new Message();
                                message.arg1 = RECOG_ERROR;
                                Bundle bundle = new Bundle();
                                bundle.putString("error", errCode + "");
                                message.setData(bundle);
                                myHander.sendMessage(message);
                            }
                        });

                        start = false;

                        clearCanvas();
                    }
                }
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
                    resetStroke();
                    mEnd = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null){
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
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
        releaseSinovoice();
        super.onDestroy();
    }

    /**
     * 灵云系统反初始化
     */
    private void releaseSinovoice() {
        if (mHciCloudHwrHelper != null) {
            mHciCloudHwrHelper.releaseHwr();
        }
        if (mHciCloudSysHelper != null) {
            mHciCloudSysHelper.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
