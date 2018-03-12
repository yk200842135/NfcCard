package com.reformer.cardemulate.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2015-06-17.
 */
public class AccessToken {


    public static String generate(String app, Integer type, Integer life, String user) {
        Date currentDate = new Timestamp(System.currentTimeMillis());
        AbDateUtils abDateUtils = new AbDateUtils();
        Date expires = abDateUtils.getDateByOffset(currentDate, Calendar.MINUTE,life/60);
        String key = getEncryptionKey(currentDate);//"eejr7mb75y3vu2y7pv3nvp01kfb99cir";

        StringBuilder builder = new StringBuilder();
        builder.append(type).append(life).append(expires.getTime()).append(user).append(app).append(key);

        String sign = AESEncrypt.encrypt256(builder.toString());

        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(app).append("|").append(type).append(".").append(sign).append(".")
                .append(life).append(".").append(expires.getTime()).append("-").append(user);
        return tokenBuilder.toString();
    }

    private static String getEncryptionKey(Date date) {
        String dateStr = AbDateUtils.getStringByFormat(date, "yyyyMMddHHmmss");
        return AbMd5.MD5(dateStr);
    }
}
