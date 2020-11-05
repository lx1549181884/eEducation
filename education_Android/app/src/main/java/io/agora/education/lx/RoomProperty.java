package io.agora.education.lx;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class RoomProperty {
    public static class liveConfig {

        @IntDef({CMD.CMD_1, CMD.CMD_2, CMD.CMD_3})
        @Retention(RetentionPolicy.SOURCE)
        public @interface CMD {
            int CMD_1 = 1;
            int CMD_2 = 2;
            int CMD_3 = 3;
        }

        @CMD
        public int cmd = CMD.CMD_3;
        public List<liveConfig.DataBean> data;

        public static class DataBean {
            public DataBean(String streamUuid) {
                this.streamUuid = streamUuid;
            }

            public String streamUuid;
        }
    }
}
