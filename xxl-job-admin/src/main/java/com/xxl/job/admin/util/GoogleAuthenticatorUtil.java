package com.xxl.job.admin.util;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Google Authenticator TOTP 工具类
 */
public class GoogleAuthenticatorUtil {

    private static final int SECRET_SIZE = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成密钥
     */
    public static String generateSecretKey() {
        byte[] buffer = new byte[SECRET_SIZE];
        RANDOM.setSeed(System.currentTimeMillis());
        RANDOM.nextBytes(buffer);
        Base32 codec = new Base32();
        return codec.encodeToString(buffer);
    }

    /**
     * 生成二维码内容 (用于Google Authenticator扫码)
     * @param account 用户账号
     * @param secretKey 密钥
     * @param issuer 签发者(如: XXL-JOB)
     */
    public static String getQRCodeUrl(String account, String secretKey, String issuer) {
        String normalizedIssuer = issuer.replace(" ", "");
        String normalizedAccount = account.replace(" ", "");
        return "otpauth://totp/"
                + normalizedIssuer + ":" + normalizedAccount
                + "?secret=" + secretKey
                + "&issuer=" + normalizedIssuer;
    }

    /**
     * 验证验证码是否正确
     * @param secretKey 密钥
     * @param code 用户输入的6位验证码
     */
    public static boolean verifyCode(String secretKey, int code) {
        return verifyCode(secretKey, code, 0);
    }

    /**
     * 验证验证码是否正确 (允许时间偏移)
     * @param secretKey 密钥
     * @param code 用户输入的6位验证码
     * @param window 允许的时间窗口偏移量(通常为0或1)
     */
    public static boolean verifyCode(String secretKey, int code, int window) {
        try {
            Base32 codec = new Base32();
            byte[] decodedKey = codec.decode(secretKey);
            long time = System.currentTimeMillis() / 1000 / 30;
            for (int i = -window; i <= window; i++) {
                long hash = generateCode(decodedKey, time + i);
                if (hash == code) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成6位验证码
     */
    private static int generateCode(byte[] key, long t) throws Exception {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }
}