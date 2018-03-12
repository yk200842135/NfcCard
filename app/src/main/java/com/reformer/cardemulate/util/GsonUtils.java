package com.reformer.cardemulate.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Created by Administrator on 2016-11-03.
 */
public class GsonUtils {
    private static Gson gson = new Gson();
    public static Gson getInstance(){
        return gson ;
    }

    public static <T> T fromJson(String content, Class<T> clz) {
        if (!isGoodJson(content))
            return null;
        return gson.fromJson(content,clz);
    }

    private static boolean isGoodJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }
}
