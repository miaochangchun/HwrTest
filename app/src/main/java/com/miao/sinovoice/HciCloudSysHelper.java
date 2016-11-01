package com.miao.sinovoice;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by miaochangchun on 2016/10/24.
 */
public class HciCloudSysHelper {
    private static final String TAG = HciCloudSysHelper.class.getSimpleName();
    private static HciCloudSysHelper mHciCloudSysHelper = null;

    private HciCloudSysHelper(){
    }

    public static HciCloudSysHelper getInstance() {
        if (mHciCloudSysHelper == null) {
            return  new HciCloudSysHelper();
        }
        return  mHciCloudSysHelper;
    }

    /**
     * 初始化函数
     * @param context
     * @return
     */
    public int init(Context context){
        //配置串参数
        String strConfig = getInitParam(context);
        int errCode = HciCloudSys.hciInit(strConfig, context);
        if (errCode != HciErrorCode.HCI_ERR_NONE){
            Log.e(TAG, "hciInit Failed and return errcode = " + errCode);
            return errCode;
        }

        errCode = checkAuthAndUpdateAuth();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "checkAuthAndUpdateAuth Failed and return errcode = " + errCode);
        }
        return HciErrorCode.HCI_ERR_NONE;
    }

    /**
     * 获取授权
     * @return
     */
    private int checkAuthAndUpdateAuth() {
        // 获取系统授权到期时间
        AuthExpireTime objExpireTime = new AuthExpireTime();
        int initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            // 显示授权日期,如用户不需要关注该值,此处代码可忽略
            Date date = new Date(objExpireTime.getExpireTime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Log.i(TAG, "expire time: " + sdf.format(date));

            if (objExpireTime.getExpireTime() * 1000 > System.currentTimeMillis()) {
                // 已经成功获取了授权,并且距离授权到期有充足的时间(>7天)
                Log.i(TAG, "checkAuth success");
                return initResult;
            }
        }

        // 获取过期时间失败或者已经过期
        initResult = HciCloudSys.hciCheckAuth();
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            Log.i(TAG, "checkAuth success");
            return initResult;
        } else {
            Log.e(TAG, "checkAuth failed: " + initResult);
            return initResult;
        }
    }

    /**
     * 获取配置传参数
     * @param context
     * @return
     */
    private String getInitParam(Context context) {
        InitParam initParam = new InitParam();
        //灵云云服务的接口地址，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, "c85d54f1");
        //灵云云服务的接口地址，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, "712ddd892cf9163e6383aa169e0454e3");
        //灵云云服务的接口地址，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, "http://test.api.hcicloud.com:8888");
        String authPath = context.getFilesDir().getAbsolutePath();
        //授权文件所在路径，此项必填
        initParam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authPath);

        //日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
        initParam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5");
        String logPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "sinovoice" + File.separator
                + context.getPackageName() + File.separator
                + "log" + File.separator;
        File file = new File(logPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        //日志的路径，可选，如果不传或者为空则不生成日志
        initParam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logPath);
        //日志大小，默认一个日志文件写多大，单位为K
        initParam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE, "1024");
        //日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
        initParam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "5");

        return initParam.getStringConfig();
    }

    /**
     * 反初始化
     * @return
     */
    public int release(){
        return HciCloudSys.hciRelease();
    }
}
