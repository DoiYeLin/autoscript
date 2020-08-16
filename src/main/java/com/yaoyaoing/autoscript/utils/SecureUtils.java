package com.yaoyaoing.autoscript.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DESede;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class SecureUtils {

    // 加密Key 每次启动生成，清理已登录的用户信息
    private static byte[] key = {88, 41, 35, 103, 28, 84, -125, 52, 110, 81, 32, 124, 107, 81, 8, -99, 88, 41, 35,
            103, 28, 84, -125, 52};
    private static DESede deSede = null;

    static {
        deSede = SecureUtil.desede(key);
    }

    public static String encryptBase64(String value) {
        String encryptBase64 = deSede.encryptBase64(value);
        return encryptBase64;
    }

    public static String encryptHex(String value) {
        String encryptHex = deSede.encryptHex(value);
        return encryptHex;
    }

    public static String decryptStr(String value) {
        String decryptStr = deSede.decryptStr(value);
        return decryptStr;
    }


}
