package io.agora.education.lx;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

    @StringDef({handUp.TRUE, handUp.FALSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface handUp {
        String TRUE = "true";
        String FALSE = "false";
    }

    public static Object get(EduUserInfo userInfo, Class keyClass) {
        Object value = null;
        Map<String, Object> properties = userInfo.getUserProperties();
        String key = keyClass.getSimpleName();
        if (properties.containsKey(key)) {
            value = properties.get(key);
        }
        return value;
    }
}
