package io.agora.education.lx;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import io.agora.education.api.user.data.EduUserInfo;

public class UserProperty {

    @StringDef({role.AUDIENCE, role.ADMIN, role.HOST, role.GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface role {
        String AUDIENCE = "audience"; // 观众
        String ADMIN = "admin"; // 管理员
        String HOST = "host"; // 主持人
        String GUEST = "guest"; // 嘉宾
    }

    @StringDef({handUp.TRUE, handUp.FALSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface handUp {
        String TRUE = "true";
        String FALSE = "false";
    }

    public static String get(EduUserInfo userInfo, Class keyClass) {
        String value = null;
        Map<String, Object> properties = userInfo.getUserProperties();
        String key = keyClass.getSimpleName();
        if (properties.containsKey(key)) {
            value = (String) properties.get(key);
        }
        return value;
    }
}
