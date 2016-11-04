package com.miao.sinovoice;

import android.content.Context;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.hwr.HwrConfig;
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam;
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult;
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResultItem;

import java.util.Iterator;
import java.util.List;

/**
 * Created by miaochangchun on 2016/11/1.
 */
public class HciCloudHwrHelper {
    public static final String TAG = HciCloudHwrHelper.class.getSimpleName();
    private static HciCloudHwrHelper mHciCloudHwrHelper = null;

    private HciCloudHwrHelper(){

    }

    public static HciCloudHwrHelper getInstance(){
        if (mHciCloudHwrHelper == null) {
            return new HciCloudHwrHelper();
        }
        return  mHciCloudHwrHelper;
    }

    /**
     * 手写功能初始化
     * @param context   上下文
     * @param capkey    使用的capkey，手写单字识别为hwr.local.letter，多字识别为hwr.local.freestylus，联想功能为hwr.local.associateword
     * @return  返回0为成功，其他为失败
     */
    public int initHwr(Context context, String capkey){
        String strConfig = getHwrInitParam(context, capkey);
        Log.d(TAG, "strConfig = " + strConfig);
        int errorCode = HciCloudHwr.hciHwrInit(strConfig);
        return errorCode;
    }

    /**
     * 手写识别函数
     * @param data  笔迹坐标
     * @param capkey    使用的capkey，手写单字识别为hwr.local.letter，多字识别为hwr.local.freestylus，联想功能为hwr.local.associateword
     * @return  返回识别的结果
     */
    public String recog(short[] data, String capkey){
        Session session = new Session();
        String sessionConfig = getHwrSessionParam(capkey);
        Log.d(TAG, "sessionConfig = " + sessionConfig);
        //sessionStart开启一个session
        int errorCode = HciCloudHwr.hciHwrSessionStart(sessionConfig, session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrSessionStart failed and return " + errorCode);
            HciCloudHwr.hciHwrSessionStop(session);
            return null;
        }
        HwrRecogResult hwrRecogResult = null;
        //识别的配置串参数可以设置为空，默认就使用sessionConfig配置串参数
        Log.d(TAG, "session = " + session);
        for (short s : data) {
            Log.d(TAG, "s = " + s + ",");
        }
        HwrConfig hwrConfig = new HwrConfig();
        errorCode = HciCloudHwr.hciHwrRecog(session, data, hwrConfig.getStringConfig(), hwrRecogResult);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrRecog failed and return " + errorCode);
            return null;
        }

        //关闭Session
        errorCode = HciCloudHwr.hciHwrSessionStop(session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrSessionStop failed and return " + errorCode);
            return null;
        }
        //返回识别结果
//        StringBuilder sb = new StringBuilder();
//        List<HwrRecogResultItem> lists = hwrRecogResult.getResultItemList();
//        Iterator<HwrRecogResultItem> iterator = lists.iterator();
//        while (iterator.hasNext()) {
//            HwrRecogResultItem item = iterator.next();
//            sb.append(item.getResult());
//        }
//        return sb.toString();
        return "123";
    }

    /**
     * 获取手写识别的配置参数
     * @param capkey    使用的capkey，手写单字识别为hwr.local.letter，多字识别为hwr.local.freestylus，联想功能为hwr.local.associateword
     * @return  返回配置串
     */
    private String getHwrSessionParam(String capkey) {
        HwrConfig hwrConfig = new HwrConfig();
        hwrConfig.addParam(HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY, capkey);
        return hwrConfig.getStringConfig();
    }

    /**
     * 获取手写的初始化配置参数
     * @param context   上下文
     * @param capkey    使用的capkey，手写单字识别为hwr.local.letter，多字识别为hwr.local.freestylus，联想功能为hwr.local.associateword
     * @return  返回配置串
     */
    private String getHwrInitParam(Context context, String capkey) {
        HwrInitParam hwrInitParam = new HwrInitParam();
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_INIT_CAP_KEYS, capkey);
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, "android_so");
        String dataPath = context.getFilesDir().getAbsolutePath().replace("files", "lib");
        hwrInitParam.addParam(HwrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        return hwrInitParam.getStringConfig();
    }

    /**
     * 手写反初始化功能
     * @return  返回0为成功，其他为失败
     */
    public int releaseHwr(){
        return HciCloudHwr.hciHwrRelease();
    }
}
