package io.agora.education.lx;

import android.util.Log;

import com.google.gson.Gson;

public class LogUtil {
    private static final Gson gson = new Gson();

    public static void log(Object... objs) {
        String s = "";
        try {
            for (Object obj : objs) {
                s += gson.toJson(obj) + " ";
            }
        } catch (Exception e) {
            e.printStackTrace();
            s = e.getMessage();
        }
        Log.i("lx", s);
    }
}
