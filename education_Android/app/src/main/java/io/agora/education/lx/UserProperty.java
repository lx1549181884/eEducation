package io.agora.education.lx;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Map;

import io.agora.education.api.user.data.EduUserInfo;

public class UserProperty {

    @IntDef({jhbRole.AUDIENCE, jhbRole.ADMIN, jhbRole.HOST, jhbRole.GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface jhbRole {
        int ADMIN = 1; // 管理员
        int AUDIENCE = 2; // 观众
        int HOST = 3; // 主持人
        int GUEST = 4; // 嘉宾
    }

    @StringDef({type.applyAudio_apply, type.applyAudio_adminDidReceived, type.applyAudio_adminAccept, type.applyAudio_adminReject, type.applyAudio_cancel, type.applyAudio_adminHungUp, type.applyAudio_audienceHungUp,
            type.applyVideo_apply, type.applyVideo_adminDidReceived, type.applyVideo_adminAccept, type.applyVideo_adminReject, type.applyVideo_cancel, type.applyVideo_adminHungUp, type.applyVideo_audienceHungUp})
    @Retention(RetentionPolicy.SOURCE)
    public @interface type {
        String applyAudio_apply = "applyAudio";
        String applyAudio_adminDidReceived = "applyAudio_adminReceived";
        String applyAudio_adminAccept = "applyAudio_adminAccept";
        String applyAudio_adminReject = "applyAudio_adminReject";
        String applyAudio_cancel = "applyAudio_canceled";
        String applyAudio_adminHungUp = "applyAudio_adminHungUp";
        String applyAudio_audienceHungUp = "applyAudio_audienceHungUp";

        String applyVideo_apply = "applyVideo";
        String applyVideo_adminDidReceived = "applyVideo_adminReceived";
        String applyVideo_adminAccept = "applyVideo_adminAccept";
        String applyVideo_adminReject = "applyVideo_adminReject";
        String applyVideo_cancel = "applyVideo_canceled";
        String applyVideo_adminHungUp = "applyVideo_adminHungUp";
        String applyVideo_audienceHungUp = "applyVideo_audienceHungUp";
    }

    public static class applyCall {
        public applyCall(String type) {
            this.type = type;
        }

        @type
        String type;
    }

    public static Object get(EduUserInfo userInfo, Class... classes) {
        String[] keys = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            keys[i] = classes[i].getSimpleName();
        }
        return get(userInfo.getUserProperties(), keys);
    }

    public static Object get(Map<String, Object> map, String... keys) {
        String key = keys[0];
        Object value = null;
        if (map.containsKey(key)) {
            value = map.get(key);
        }
        if (value instanceof Map) {
            return get((Map) value, Arrays.copyOfRange(keys, 1, keys.length));
        } else {
            return value;
        }
    }
}
