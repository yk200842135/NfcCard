package com.reformer.cardemulate.util;

/**
 * Created by Administrator on 2017-12-28.
 */

public class ByteUtils {
    public static String bytesToString(byte b[]) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String plainText = Integer.toHexString(0xff & b[i]);
            if (plainText.length() < 2)
                plainText = "0" + plainText;
            hexString.append(plainText);
        }
        return hexString.toString().toUpperCase();
    }

    public static byte[] stringToBytes(String outStr){
        if (outStr.length()!=18)
            return null;
        int len = outStr.length()/2;
        byte[] mac = new byte[len];
        for (int i = 0; i < len; i++){
            String s = outStr.substring(i*2,i*2+2);
            if (Integer.valueOf(s, 16)>0x7F) {
                mac[i] = (byte)(Integer.valueOf(s, 16) - 0xFF - 1);
            }else {
                mac[i] = Byte.valueOf(s, 16);
            }
        }
        return mac;
    }
}
