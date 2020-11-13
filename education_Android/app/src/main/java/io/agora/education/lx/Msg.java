package io.agora.education.lx;

public class Msg {
    public int cmd = 99;

    public Msg(String type) {
        this.data = new Data(type);
    }

    public Data data;

    public static class Data {
        public Data(String type) {
            this.type = type;
        }

        public String type;
    }

    public interface Type {
        String adminInvite_audioLink = "adminInvite_audioLink"; //邀请语音链接
        String adminInvite_audioLink_audienceReject = "adminInvite_audioLink_audienceReject"; //观众 拒绝连麦
        String adminInvite_audioLink_audienceAccept = "adminInvite_audioLink_audienceAccept"; //观众同意连麦
        String adminInvite_audioLink_adminCancel = "adminInvite_audioLink_adminCancel"; //管理员取消邀请连麦
        String adminInvite_openAudio = "adminInvite_openAudio"; //邀请打开麦克风
        String adminInvite_openAudio_audienceReject = "adminInvite_openAudio_audienceReject"; //邀请打开麦克风，观众拒绝
        String adminInvite_openAudio_audienceAccept = "adminInvite_openAudio_audienceAccept"; //邀请打开麦克风，观众接受
        String adminInvite_openVideo = "adminInvite_openVideo"; //邀请打开摄像头
        String adminInvite_openVideo_audienceReject = "adminInvite_openVideo_audienceReject"; //邀请打开摄像头，观众拒绝
        String adminInvite_openVideo_audienceAccept = "adminInvite_openVideo_audienceAccept"; //邀请打开摄像头，观众接受
    }
}
