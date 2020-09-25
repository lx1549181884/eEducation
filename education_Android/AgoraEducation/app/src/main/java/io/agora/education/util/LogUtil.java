package io.agora.education.util;

import android.util.Log;

import com.google.gson.Gson;

public class LogUtil {
    private static final Gson gson = new Gson();

    public static void log(Object... objs) {
        String s = "";
        for (Object obj : objs) {
            s += gson.toJson(obj) + " ";
        }
        Log.i("lx", s);
    }
}
