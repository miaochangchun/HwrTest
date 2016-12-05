package com.miao.util;

/**
 * 灵云配置信息
 * Created by 10048 on 2016/12/3.
 */
public class ConfigUtil {
    /**
     * 灵云APP_KEY
     */
    public static final String APP_KEY = "c85d54f1";

    /**
     * 开发者密钥
     */
    public static final String DEVELOPER_KEY = "712ddd892cf9163e6383aa169e0454e3";

    /**
     * 灵云云服务的接口地址
     */
    public static final String CLOUD_URL = "test.api.hcicloud.com:8888";

    /**
     * 需要运行的灵云能力
     */
    //云端多字识别功能
    public static final String CAP_KEY_HWR_CLOUD_FREETALK = "hwr.cloud.freetalk";
    //云端单字识别功能
    public static final String CAP_KEY_HWR_CLOUD_LETTER = "hwr.cloud.letter";
    //离线单字识别功能
    public static final String CAP_KEY_HWR_LOCAL_LETTER = "hwr.local.letter";
    //离线多字识别功能
    public static final String CAP_KEY_HWR_LOCAL_FREESTYLUS = "hwr.local.freestylus";
    //离线联想功能
    public static final String CAP_KEY_HWR_LOCAL_ASSOCIATE_WORD = "hwr.local.associateword";
    //离线笔形功能
    public static final String CAP_KEY_HWR_LOCAL_PENSCRIPT = "hwr.local.penscript";
}
