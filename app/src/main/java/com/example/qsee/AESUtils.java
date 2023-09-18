package com.example.qsee;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static final String AES_MODE = "AES/CBC/PKCS7Padding";

    private static final byte[] HARD_CODED_SECRET_KEY = {
            (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
            (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
            (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
            (byte) 0x76, (byte) 0x54, (byte) 0x32, (byte) 0x10
    };

    private static final byte[] HARD_CODED_IV = {
            (byte) 0x0F, (byte) 0xE1, (byte) 0xD2, (byte) 0xC3,
            (byte) 0xB4, (byte) 0xA5, (byte) 0x96, (byte) 0x87,
            (byte) 0x78, (byte) 0x69, (byte) 0x5A, (byte) 0x4B,
            (byte) 0x3C, (byte) 0x2D, (byte) 0x1E, (byte) 0x0F
    };

    private static SecretKey secretKey;
    private static IvParameterSpec iv;

    public static SecretKey generateSecretKey() {
        if (secretKey == null) {
            try {
                // Use the hardcoded secret key bytes instead of generating one
                secretKey = new SecretKeySpec(HARD_CODED_SECRET_KEY, "AES");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return secretKey;
    }

    public static IvParameterSpec generateIV() {
        if (iv == null) {
            try {
                // Use the hardcoded IV bytes
                iv = new IvParameterSpec(HARD_CODED_IV);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return iv;
    }

    public static String encrypt(String input) {
        SecretKey secretKey = generateSecretKey();
        IvParameterSpec iv = generateIV();

        if (secretKey != null && iv != null) {
            try {
                Cipher cipher = Cipher.getInstance(AES_MODE);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

                byte[] encryptedBytes = cipher.doFinal(input.getBytes("UTF-8"));
                return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String decrypt(String encrypted) {
        SecretKey secretKey = generateSecretKey();
        IvParameterSpec iv = generateIV();

        if (secretKey != null && iv != null) {
            try {
                Cipher cipher = Cipher.getInstance(AES_MODE);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

                byte[] encryptedBytes = Base64.decode(encrypted, Base64.DEFAULT);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

                return new String(decryptedBytes, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}



