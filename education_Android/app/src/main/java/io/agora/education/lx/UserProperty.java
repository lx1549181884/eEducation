package io.agora.education.lx;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UserProperty {

    @StringDef({role.AUDIENCE, role.ADMIN, role.HOST, role.GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface role {
        String AUDIENCE = "audience"; // 观众
        String ADMIN = "admin"; // 管理员
        String HOST = "host"; // 主持人
        String GUEST = "guest"; // 嘉宾
    }
}
