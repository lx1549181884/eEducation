package io.agora.education.lx;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import com.blankj.utilcode.util.GsonUtils;

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

    @StringDef({type.applyAudio_apply, type.applyAudio_adminReceived, type.applyAudio_adminAccept, type.applyAudio_adminReject, type.applyAudio_cancel, type.applyAudio_adminHungUp, type.applyAudio_audienceHungUp,
            type.applyVideo_apply, type.applyVideo_adminReceived, type.applyVideo_adminAccept, type.applyVideo_adminReject, type.applyVideo_cancel, type.applyVideo_adminHungUp, type.applyVideo_audienceHungUp})
    @Retention(RetentionPolicy.SOURCE)
    public @interface type {
        String applyAudio_apply = "applyAudio";
        String applyAudio_adminReceived = "applyAudio_adminReceived";
        String applyAudio_adminAccept = "applyAudio_adminAccept";
        String applyAudio_adminReject = "applyAudio_adminReject";
        String applyAudio_cancel = "applyAudio_canceled";
        String applyAudio_adminHungUp = "applyAudio_adminHungUp";
        String applyAudio_audienceHungUp = "applyAudio_audienceHungUp";

        String applyVideo_apply = "applyVideo";
        String applyVideo_adminReceived = "applyVideo_adminReceived";
        String applyVideo_adminAccept = "applyVideo_adminAccept";
        String applyVideo_adminReject = "applyVideo_adminReject";
        String applyVideo_cancel = "applyVideo_canceled";
        String applyVideo_adminHungUp = "applyVideo_adminHungUp";
        String applyVideo_audienceHungUp = "applyVideo_audienceHungUp";

        String adminInvite_audioLink = "adminInvite_audioLink"; //邀请语音链接
        String adminInvite_audioLink_audienceReject = "adminInvite_audioLink_audienceReject"; //观众 拒绝连麦
        String adminInvite_audioLink_audienceAccept = "adminInvite_audioLink_audienceAccept"; //观众同意连麦
        String adminInvite_audioLink_adminCancel = "adminInvite_audioLink_adminCancel"; //管理员取消邀请连麦

        String adminInvite_openAudio = "adminInvite_openAudio"; //邀请打开摄像头
        String adminInvite_openAudio_audienceReject = "adminInvite_openAudio_audienceReject"; //邀请打开麦克风，观众拒绝
        String adminInvite_openAudio_audienceAccept = "adminInvite_openAudio_audienceAccept"; //邀请打开麦克风，观众接受
        String adminInvite_openVideo = "adminInvite_openVideo"; //邀请打开摄像头
        String adminInvite_openVideo_audienceReject = "adminInvite_openVideo_audienceReject"; //邀请打开摄像头，观众拒绝
        String adminInvite_openVideo_audienceAccept = "adminInvite_openVideo_audienceAccept"; //邀请打开摄像头，观众接受
    }

    public static class applyCall {
        public applyCall(String type) {
            this.type = type;
        }

        @type
        public String type;
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

    public static <T> T getProperty(Map<String, Object> properties, Class<T> clazz, T defaultValue) {
        try {
            return GsonUtils.fromJson(properties.get(clazz.getSimpleName()).toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
