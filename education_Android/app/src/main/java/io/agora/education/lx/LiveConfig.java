package io.agora.education.lx;

import androidx.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class LiveConfig implements Serializable {
    public static final String KEY = "liveConfig";
    @CMD
    public String cmd;
    public List<DataBean> data;

    public LiveConfig() {
        this(CMD.CMD_3, null);
    }

    public LiveConfig(@CMD String cmd, List<DataBean> data) {
        this.cmd = cmd;
        this.data = data;
    }

    @StringDef({CMD.CMD_1, CMD.CMD_2, CMD.CMD_3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CMD {
        String CMD_1 = "1";
        String CMD_2 = "2";
        String CMD_3 = "3";
    }

    public static class DataBean implements Serializable {

        public String uid;

        public DataBean(String uid) {
            this.uid = uid;
        }
    }
}
