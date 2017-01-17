package com.miao.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

import com.sinovoice.hcicloudsdk.api.hwr.HciCloudHwr;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.hwr.HwrAssociateWordsResult;
import com.sinovoice.hcicloudsdk.common.hwr.HwrConfig;
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam;
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult;
import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResultItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
     * @param listener  识别结果回调函数
     */
    public void recog(short[] data, String capkey, OnHwrRecogListener listener){
        Session session = new Session();
        String sessionConfig = getHwrSessionParam(capkey);
        Log.d(TAG, "sessionConfig = " + sessionConfig);
        //sessionStart开启一个session
        int errorCode = HciCloudHwr.hciHwrSessionStart(sessionConfig, session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrSessionStart failed and return " + errorCode);
            listener.onError(errorCode);
            HciCloudHwr.hciHwrSessionStop(session);
            return;
        }

        //识别结果类进行初始化
        HwrRecogResult recogResult = new HwrRecogResult();
        //识别的配置串参数可以设置为空，默认就使用sessionConfig配置串参数
        errorCode = HciCloudHwr.hciHwrRecog(session, data, "", recogResult);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrRecog failed and return " + errorCode);
            listener.onError(errorCode);
            return;
        } else {
            listener.onHwrResult(recogResult);  //把识别结果传给回调监听
        }

        //关闭Session
        errorCode = HciCloudHwr.hciHwrSessionStop(session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "hciHwrSessionStop failed and return " + errorCode);
            listener.onError(errorCode);
            return ;
        }
    }

    /**
     * 联想词功能，对str进行联想，返回联想结果
     * @param str   需要联想的字符串
     * @param assCapkey 联想功能对应的capkey。
     * @return
     */
    public String associateWord(String str, String assCapkey){
        Session session = new Session();
        String sessionConfig = getAssociateWordSessionParam(assCapkey);
        int errorCode = HciCloudHwr.hciHwrSessionStart(sessionConfig, session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "HciCloudHwr.hciHwrSessionStart failed and return " + errorCode);
        }
        HwrAssociateWordsResult hwrAssociateWordsResult = new HwrAssociateWordsResult();
        errorCode = HciCloudHwr.hciHwrAssociateWords(session, str, "", hwrAssociateWordsResult);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "HciCloudHwr.hciHwrAssociateWords failed and return " + errorCode);
        }
        errorCode = HciCloudHwr.hciHwrSessionStop(session);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "HciCloudHwr.hciHwrSessionStop failed and return " + errorCode);
        }
        ArrayList<String> lists = hwrAssociateWordsResult.getResultList();
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = lists.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            sb.append(string).append(",");
        }
        return  sb.toString();
    }

    /**
     * 设置联想词的配置参数
     * @param assCapkey 联想词功能所需的capkey，需要设置为 assCapkey=hwr.local.associateword
     * @return  联想词配置的字符串
     */
    private String getAssociateWordSessionParam(String assCapkey) {
        HwrConfig hwrConfig = new HwrConfig();
        hwrConfig.addParam(HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY, assCapkey);
        return hwrConfig.getStringConfig();
    }

    /**
     * 获取手写识别的配置参数
     * @param hwrCapkey    使用的capkey，手写单字识别为hwr.local.letter，多字识别为hwr.local.freestylus
     * @return  返回配置串
     */
    private String getHwrSessionParam(String hwrCapkey) {
        HwrConfig hwrConfig = new HwrConfig();
        hwrConfig.addParam(HwrConfig.SessionConfig.PARAM_KEY_CAP_KEY, hwrCapkey);
        //设置识别结果的候选个数
        hwrConfig.addParam(HwrConfig.ResultConfig.PARAM_KEY_CAND_NUM, "10");
        //设置识别结果的范围
        hwrConfig.addParam(HwrConfig.ResultConfig.PARAM_KEY_RECOG_RANGE, "gbk");
        //设置叠写还是行写 line是行写，overlap是叠写
        hwrConfig.addParam(HwrConfig.InputConfig.PARAM_KEY_SPLIT_MODE, "line");
        return hwrConfig.getStringConfig();
    }

    /**
     * 获取手写的初始化配置参数
     * @param context   上下文
     * @param initCapkeys    使用的capkey，手写单字识别为hwr.local.letter，
     *                       多字识别为hwr.local.freestylus，
     *                       联想功能为hwr.local.associateword
     *                       可以设置多个，中间以分号隔开
     * @return  返回配置串
     */
    private String getHwrInitParam(Context context, String initCapkeys) {
        HwrInitParam initParam = new HwrInitParam();
        initParam.addParam(HwrInitParam.PARAM_KEY_INIT_CAP_KEYS, initCapkeys);
        initParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, "none");
        String dataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                        + "sinovoice" + File.separator
                        + context.getPackageName() + File.separator
                        + "data" + File.separator;
        //多字识别所需的字典文件
        String[] strings = {"fwlib.dic", "wwlib.dic", "letter.dic", "letter.conf"};
        for (String data : strings) {
            copyAssetsToSdcard(context, data, dataPath);
        }
        initParam.addParam(HwrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        return initParam.getStringConfig();
    }

    /**
     * 把手写字典文件拷贝到sd卡
     * @param context   上下文
     * @param s         字典文件名称
     * @param dataPath  sd卡路径
     */
    private void copyAssetsToSdcard(Context context, String s, String dataPath) {
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            File path = new File(dataPath);
            if (!path.exists()) {
                path.mkdirs();
            }
            fos = new FileOutputStream(dataPath + s);
            inputStream = context.getAssets().open(s);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 手写反初始化功能
     * @return  返回0为成功，其他为失败
     */
    public int releaseHwr(){
        return HciCloudHwr.hciHwrRelease();
    }
}
