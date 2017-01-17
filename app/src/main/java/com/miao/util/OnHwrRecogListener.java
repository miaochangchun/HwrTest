package com.miao.util;

import com.sinovoice.hcicloudsdk.common.hwr.HwrRecogResult;

/**
 * Created by miaochangchun on 2017/1/17.
 */
public interface OnHwrRecogListener {
    /**
     * 显示识别结果函数
     * @param recogResult   识别结果
     */
    void onHwrResult(HwrRecogResult recogResult);

    /**
     * 识别错误结果回调
     * @param errCode   错误码
     */
    void onError(int errCode);
}
