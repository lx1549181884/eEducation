package io.agora.education.classroom;

import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.education.R;
import io.agora.education.api.EduCallback;
import io.agora.education.api.message.EduChatMsg;
import io.agora.education.api.message.EduMsg;
import io.agora.education.api.room.EduRoom;
import io.agora.education.api.room.data.EduRoomChangeType;
import io.agora.education.api.statistics.ConnectionState;
import io.agora.education.api.statistics.NetworkQuality;
import io.agora.education.api.stream.data.EduStreamEvent;
import io.agora.education.api.stream.data.EduStreamInfo;
import io.agora.education.api.stream.data.EduStreamStateChangeType;
import io.agora.education.api.stream.data.LocalStreamInitOptions;
import io.agora.education.api.stream.data.StreamSubscribeOptions;
import io.agora.education.api.stream.data.VideoSourceType;
import io.agora.education.api.stream.data.VideoStreamType;
import io.agora.education.api.user.EduStudent;
import io.agora.education.api.user.EduUser;
import io.agora.education.api.user.data.EduUserEvent;
import io.agora.education.api.user.data.EduUserInfo;
import io.agora.education.api.user.data.EduUserStateChangeType;
import io.agora.education.classroom.adapter.ClassVideoAdapter;
import io.agora.education.classroom.bean.channel.Room;
import io.agora.education.classroom.fragment.UserListFragment;
import io.agora.education.classroom.widget.RtcVideoView;
import io.agora.education.lx.LogUtil;
import io.agora.education.lx.RoomProperty;
import io.agora.education.lx.UserProperty;
import kotlin.Unit;

public class JhbClassActivity extends BaseClassActivity implements TabLayout.OnTabSelectedListener {
    private static final String TAG = "JhbClassActivity";

    @Nullable
    @BindView(R.id.layout_tab)
    protected TabLayout layout_tab;
    @BindView(R.id.layout_chat_room)
    protected FrameLayout layout_chat_room;
    @BindView(R.id.layout_audience_list)
    protected FrameLayout layout_audience_list;
    @Nullable
    @BindView(R.id.layout_materials)
    protected FrameLayout layout_materials;
    @BindView(R.id.layout_hand_up)
    protected CardView layout_hand_up;
    @BindView(R.id.rv_videos)
    RecyclerView rv_videos;
    @BindView(R.id.btn_layout_1)
    View btn_layout_1;
    @BindView(R.id.btn_layout_2)
    View btn_layout_2;
    @BindView(R.id.rl_videos2)
    View rl_videos2;
    @BindView(R.id.rvv_large)
    RtcVideoView rvv_large;
    @BindView(R.id.rvv_small)
    RtcVideoView rvv_small;
    @BindView(R.id.btn_mute_chat_all)
    Button btn_mute_chat_all;


    private AppCompatTextView textView_unRead;
    private ConstraintLayout layout_unRead;
    private ClassVideoAdapter adapter;
    protected UserListFragment audienceListFragment;

    private int unReadCount = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_jhb_class;
    }

    @Override
    protected void initData() {
        super.initData();
        final boolean isAdmin = UserProperty.jhbRole.ADMIN == roomEntry.getRole();
        joinRoom(getMainEduRoom(), roomEntry.getUserName(), roomEntry.getUserUuid(), true, false, true,
                new EduCallback<EduStudent>() {
                    @Override
                    public void onSuccess(@org.jetbrains.annotations.Nullable EduStudent res) {
                        runOnUiThread(() -> onJoinSuccess(isAdmin));
                    }

                    @Override
                    public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                        joinFailed(code, reason);
                    }
                }, false);
    }

    private void onJoinSuccess(boolean isAdmin) {
        showFragmentWithJoinSuccess();
        onRoomPropertyChanged(getMainEduRoom(), null);
        onLocalUserPropertyUpdated(getLocalUserInfo(), null);
        if (isAdmin) { // 管理员主动连麦
            setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_adminAccept, new EduCallback() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                    ToastUtils.showShort("管理员主动连麦");
                }

                @Override
                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
    }

    @Override
    protected void initView() {
        super.initView();
        /*大班课场景不需要计时*/
        title_view.hideTime();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv_videos.setLayoutManager(gridLayoutManager);
        rv_videos.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (ScreenUtils.getAppScreenWidth() * 0.7)));
        adapter = new ClassVideoAdapter();
        rv_videos.setAdapter(adapter);

        if (layout_tab != null) {
            /*不为空说明是竖屏*/
            layout_tab.addOnTabSelectedListener(this);
            layout_unRead = findViewById(R.id.layout_unRead);
            textView_unRead = findViewById(R.id.textView_unRead);
        }

        audienceListFragment = new UserListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_audience_list, audienceListFragment)
                .show(audienceListFragment)
                .commitNow();

        if (UserProperty.jhbRole.ADMIN == roomEntry.getRole()) {
            btn_layout_2.setVisibility(View.VISIBLE);
        } else {
            btn_layout_1.setVisibility(View.VISIBLE);
        }

        // disable operation in large class
        whiteboardFragment.disableDeviceInputs(true);
        whiteboardFragment.setWritable(false);

        EduStreamInfo streamInfo = getScreenShareStream();
        if (streamInfo != null) {
            layout_whiteboard.setVisibility(View.GONE);
            layout_share_video.setVisibility(View.VISIBLE);
            layout_share_video.removeAllViews();
            renderStream(getMainEduRoom(), streamInfo, layout_share_video);
        }
    }

    @Override
    protected int getClassType() {
        return Room.Type.LARGE;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(getLayoutResId());
        ButterKnife.bind(this);
        initView();
        recoveryFragmentWithConfigChanged();
    }

    /**
     * 挂断
     */
    @OnClick(R.id.btn_hung_up)
    void hungUp() {
        /*连麦过程中取消
         * 1：关闭本地流
         * 2：更新流信息到服务器
         * 3：发送取消的点对点消息给老师
         * 4：更新本地记录的连麦状态*/
        if (getLocalCameraStream() != null) {
            LocalStreamInitOptions options = new LocalStreamInitOptions(getLocalCameraStream().getStreamUuid(), false, false);
            options.setStreamName(getLocalCameraStream().getStreamName());
            getLocalUser().initOrUpdateLocalStream(options, new EduCallback<EduStreamInfo>() {
                @Override
                public void onSuccess(@Nullable EduStreamInfo res) {
                    getLocalUser().unPublishStream(res, new EduCallback<Boolean>() {
                        @Override
                        public void onSuccess(@Nullable Boolean res) {
                            EduUser localUser = getLocalUser();
                            Map.Entry<String, Object> property = new AbstractMap.SimpleEntry<>(UserProperty.applyCall.class.getSimpleName(), new UserProperty.applyCall(UserProperty.type.applyVideo_audienceHungUp));
                            localUser.setUserProperty(property, new HashMap<>(), localUser.getUserInfo(), new EduCallback<Unit>() {
                                @Override
                                public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                                    ToastUtils.showShort("挂断成功");
                                }

                                @Override
                                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                                    ToastUtils.showShort(code + " " + reason);
                                }
                            });
                        }

                        @Override
                        public void onFailure(int code, @Nullable String reason) {
                            ToastUtils.showShort(code + " " + reason);
                        }
                    });
                }

                @Override
                public void onFailure(int code, @Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
    }

    /**
     * 用户推流(暂时只能本地用户推流，远端用户推流有问题)
     */
    public void publishStream(EduUserInfo eduUserInfo) {
        /**连麦中，发流*/
        EduStreamInfo streamInfo = new EduStreamInfo(eduUserInfo.getStreamUuid(), null,
                VideoSourceType.CAMERA, true, true, eduUserInfo);
        /**举手连麦，需要新建流信息*/
        getLocalUser().publishStream(streamInfo, new EduCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean res) {
                Log.e(TAG, "发流成功");
                if (streamInfo.getPublisher().getUserUuid().equals(getLocalUserInfo().getUserUuid())) {
                    setLocalCameraStream(streamInfo);
                }
            }

            @Override
            public void onFailure(int code, @Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    private boolean chatRoomShowing() {
        return layout_chat_room.getVisibility() == View.VISIBLE;
    }

    private void updateUnReadCount(boolean gone) {
        if (gone) {
            unReadCount = 0;
        } else {
            unReadCount++;
            if (textView_unRead != null) {
                textView_unRead.setText(String.valueOf(unReadCount));
            }
        }
        if (textView_unRead != null) {
            textView_unRead.setVisibility(gone ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        layout_chat_room.setVisibility(tab.getPosition() == 0 ? View.VISIBLE : View.GONE);
        layout_audience_list.setVisibility(tab.getPosition() == 1 ? View.VISIBLE : View.GONE);
        layout_materials.setVisibility(tab.getPosition() == 2 ? View.VISIBLE : View.GONE);
        if (tab.getPosition() == 1) {
            updateUnReadCount(true);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    public void onRemoteUsersInitialized(@NotNull List<? extends EduUserInfo> users, @NotNull EduRoom classRoom) {
        super.onRemoteUsersInitialized(users, classRoom);
        title_view.setTitle(String.format(Locale.getDefault(), "%s", getMediaRoomName()));
    }

    @Override
    public void onRemoteUsersJoined(@NotNull List<? extends EduUserInfo> users, @NotNull EduRoom classRoom) {
        super.onRemoteUsersJoined(users, classRoom);
        title_view.setTitle(String.format(Locale.getDefault(), "%s", getMediaRoomName()));
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRemoteUserLeft(@NotNull EduUserEvent userEvent, @NotNull EduRoom classRoom) {
        super.onRemoteUserLeft(userEvent, classRoom);
        title_view.setTitle(String.format(Locale.getDefault(), "%s", getMediaRoomName()));
    }

    @Override
    public void onRemoteUserUpdated(@NotNull EduUserEvent userEvent, @NotNull EduUserStateChangeType type,
                                    @NotNull EduRoom classRoom) {
        super.onRemoteUserUpdated(userEvent, type, classRoom);
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRoomMessageReceived(@NotNull EduMsg message, @NotNull EduRoom classRoom) {
        super.onRoomMessageReceived(message, classRoom);
    }

    @Override
    public void onRoomChatMessageReceived(@NotNull EduChatMsg eduChatMsg, @NotNull EduRoom classRoom) {
        super.onRoomChatMessageReceived(eduChatMsg, classRoom);
        runOnUiThread(() -> updateUnReadCount(chatRoomShowing()));
    }

    @Override
    public void onRemoteStreamsInitialized(@NotNull List<? extends EduStreamInfo> streams, @NotNull EduRoom classRoom) {
        super.onRemoteStreamsInitialized(streams, classRoom);
        refreshVideoList();
        audienceListFragment.setLocalUserUuid(classRoom.getLocalUser().getUserInfo().getUserUuid());
        refreshAudienceList();
    }

    @Override
    public void onRemoteStreamsAdded(@NotNull List<EduStreamEvent> streamEvents, @NotNull EduRoom classRoom) {
        super.onRemoteStreamsAdded(streamEvents, classRoom);
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRemoteStreamUpdated(@NotNull EduStreamEvent streamEvent, @NotNull EduStreamStateChangeType type,
                                      @NotNull EduRoom classRoom) {
        super.onRemoteStreamUpdated(streamEvent, type, classRoom);
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRemoteStreamsRemoved(@NotNull List<EduStreamEvent> streamEvents, @NotNull EduRoom classRoom) {
        super.onRemoteStreamsRemoved(streamEvents, classRoom);
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRoomStatusChanged(@NotNull EduRoomChangeType event, @NotNull EduUserInfo operatorUser, @NotNull EduRoom classRoom) {
        super.onRoomStatusChanged(event, operatorUser, classRoom);
    }

    @Override
    public void onMuteChanged(boolean muteAll, boolean muteSelf) {
        super.onMuteChanged(muteAll, muteSelf);
        btn_mute_chat_all.setSelected(muteAll);
        btn_mute_chat_all.setText(muteAll ? "取消全体禁言" : "全体禁言");
    }

    @Override
    public void onRoomPropertyChanged(@NotNull EduRoom classRoom, @Nullable Map<String, Object> cause) {
        super.onRoomPropertyChanged(classRoom, cause);
        /*处理可能收到的录制的消息*/
        runOnUiThread(() -> {
            if (revRecordMsg) {
                revRecordMsg = false;
                updateUnReadCount(chatRoomShowing());
            }
            liveConfig = UserProperty.getProperty(classRoom.getRoomProperties(), RoomProperty.liveConfig.class, liveConfig);
            LogUtil.log("onRoomPropertyChanged", liveConfig);
            refreshVideoList();
        });
    }

    @Override
    public void onRemoteUserPropertyUpdated(@NotNull EduUserInfo userInfos, @NotNull EduRoom classRoom,
                                            @Nullable Map<String, Object> cause) {
        super.onRemoteUserPropertyUpdated(userInfos, classRoom, cause);
        Object type = UserProperty.get(userInfos, UserProperty.applyCall.class, UserProperty.type.class);
        if (UserProperty.type.applyAudio_apply.equals(type) || UserProperty.type.applyVideo_apply.equals(type)) {
            handUpUserId = userInfos.getUserUuid();
            ToastUtils.showShort("举手 " + userInfos.getUserName() + " " + userInfos.getUserUuid());
        } else if (UserProperty.type.applyAudio_cancel.equals(type) || UserProperty.type.applyVideo_cancel.equals(type)) {
            ToastUtils.showShort("取消举手 " + userInfos.getUserName() + " " + userInfos.getUserUuid());
            if (handUpUserId == userInfos.getUserUuid()) {
                handUpUserId = null;
            }
        }
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onNetworkQualityChanged(@NotNull NetworkQuality quality, @NotNull EduUserInfo user,
                                        @NotNull EduRoom classRoom) {
        super.onNetworkQualityChanged(quality, user, classRoom);
        title_view.setNetworkQuality(quality);
    }

    @Override
    public void onConnectionStateChanged(@NotNull ConnectionState state, @NotNull EduRoom classRoom) {
        super.onConnectionStateChanged(state, classRoom);
    }

    @Override
    public void onLocalUserUpdated(@NotNull EduUserEvent userEvent, @NotNull EduUserStateChangeType type) {
        super.onLocalUserUpdated(userEvent, type);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
    }

    @Override
    public void onLocalUserPropertyUpdated(@NotNull EduUserInfo userInfo, @Nullable Map<String, Object> cause) {
        super.onLocalUserPropertyUpdated(userInfo, cause);
        refreshVideoList();
        refreshAudienceList();
        String type = (String) UserProperty.get(userInfo, UserProperty.applyCall.class, UserProperty.type.class);
        if (type == null) {
            type = UserProperty.type.applyVideo_cancel;
        }
        switch (type) {
            case UserProperty.type.applyAudio_adminAccept:
            case UserProperty.type.applyVideo_adminAccept:
                ToastUtils.showShort(R.string.accept_interactive);
                publishStream(getLocalUserInfo());
                break;
            case UserProperty.type.applyAudio_adminReject:
            case UserProperty.type.applyVideo_adminReject:
                ToastUtils.showShort(R.string.reject_interactive);
                break;
            case UserProperty.type.applyAudio_adminHungUp:
            case UserProperty.type.applyVideo_adminHungUp:
                ToastUtils.showShort(R.string.abort_interactive);
                break;
        }
    }

    @Override
    public void onLocalStreamAdded(@NotNull EduStreamEvent streamEvent) {
        super.onLocalStreamAdded(streamEvent);
        EduStreamInfo modifiedStream = streamEvent.getModifiedStream();
        setLocalCameraStream(modifiedStream);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
    }

    @Override
    public void onLocalStreamUpdated(@NotNull EduStreamEvent streamEvent, @NotNull EduStreamStateChangeType type) {
        super.onLocalStreamUpdated(streamEvent, type);
        EduStreamInfo modifiedStream = streamEvent.getModifiedStream();
        setLocalCameraStream(modifiedStream);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
    }

    @Override
    public void onLocalStreamRemoved(@NotNull EduStreamEvent streamEvent) {
        super.onLocalStreamRemoved(streamEvent);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
    }

    private String handUpUserId;

    @Override
    public void onUserMessageReceived(@NotNull EduMsg message) {
        super.onUserMessageReceived(message);
    }

    @Override
    public void onUserChatMessageReceived(@NotNull EduChatMsg chatMsg) {
        super.onUserChatMessageReceived(chatMsg);
    }

    private void refreshVideoList() {
        showVideoList(getCurFullStream());
    }

    private void showVideoList(List<EduStreamInfo> list) {
        LogUtil.log("showVideoList", list.size());
        runOnUiThread(() -> {
            for (EduStreamInfo info : list) {
                renderStream(getMainEduRoom(), info, null);
            }
            List<EduStreamInfo> finalList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                EduStreamInfo info = list.get(i);
                boolean isOutput = false;
                if (liveConfig.data != null) {
                    for (RoomProperty.liveConfig.DataBean bean : liveConfig.data) {
                        if (info.getStreamUuid().equals(bean.streamUuid)) {
                            finalList.add(info);
                            isOutput = true;
                            break;
                        }
                    }
                }
                if (isOutput) {
                    getLocalUser().subscribeStream(info, new StreamSubscribeOptions(true, true, VideoStreamType.HIGH), new EduCallback<Unit>() {
                        @Override
                        public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {

                        }

                        @Override
                        public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

                        }
                    });
                } else {
                    getLocalUser().unSubscribeStream(info, new StreamSubscribeOptions(false, false, VideoStreamType.HIGH), new EduCallback<Unit>() {
                        @Override
                        public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {

                        }

                        @Override
                        public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

                        }
                    });
                }
            }


            switch (liveConfig.cmd) {
                case RoomProperty.liveConfig.CMD.CMD_1:
                case RoomProperty.liveConfig.CMD.CMD_2:
                    rv_videos.setVisibility(View.GONE);
                    rl_videos2.setVisibility(View.VISIBLE);
                    rvv_small.setViewVisibility(liveConfig.cmd == RoomProperty.liveConfig.CMD.CMD_1 ? View.GONE : View.VISIBLE);
                    adapter.setNewList(new ArrayList<>());
                    for (int i = 0; i < finalList.size(); i++) {
                        EduStreamInfo info = finalList.get(i);
                        if (i == 0) {
                            renderStream(getMainEduRoom(), info, rvv_large);
                        } else if (i == 1 && RoomProperty.liveConfig.CMD.CMD_2 == liveConfig.cmd) {
                            renderStream(getMainEduRoom(), info, rvv_small);
                        }
                    }
                    break;
                case RoomProperty.liveConfig.CMD.CMD_3:
                default:
                    rv_videos.setVisibility(View.VISIBLE);
                    rl_videos2.setVisibility(View.GONE);
                    adapter.setNewList(finalList);
                    break;
            }
        });
    }

    private void refreshAudienceList() {
        runOnUiThread(() -> audienceListFragment.setUserList(getCurFullStream(), getCurFullUser()));
    }

    @OnClick(R.id.btn_hand_up)
    void handUp() {
        if (!getMainEduRoom().getRoomStatus().isStudentChatAllowed()) {
            ToastUtils.showShort("全体禁言中！");
            return;
        } else {
            EduUserInfo localUserInfo = getLocalUserInfo();
            if (!localUserInfo.isChatAllowed()) {
                ToastUtils.showShort("个人禁言中！");
                return;
            }
        }

        setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_apply, new EduCallback() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                ToastUtils.showShort("举手成功");
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    @OnClick(R.id.btn_hand_down)
    void handDown() {
        setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_cancel, new EduCallback() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                ToastUtils.showShort("取消成功");
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    @OnClick(R.id.btn_accept)
    void accept() {
        setApplyCall(getUserInfo(handUpUserId), UserProperty.type.applyAudio_adminAccept, new EduCallback() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                ToastUtils.showShort("接收成功");
                publishStream(getUserInfo(handUpUserId));
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    @OnClick(R.id.btn_reject)
    void reject() {
        setApplyCall(getUserInfo(handUpUserId), UserProperty.type.applyAudio_adminReject, new EduCallback() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                ToastUtils.showShort("拒绝成功");
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    /**
     * 设置举手连麦状态
     */
    private void setApplyCall(EduUserInfo eduUserInfo, @UserProperty.type String type, EduCallback callback) {
        Map.Entry<String, Object> property = new AbstractMap.SimpleEntry<>(UserProperty.applyCall.class.getSimpleName(), new UserProperty.applyCall(type));
        getLocalUser().setUserProperty(property, new HashMap<>(), eduUserInfo, callback);
    }

    @OnClick(R.id.btn_video_layout_1)
    void videoLayout1() {
        ArrayList<RoomProperty.liveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new RoomProperty.liveConfig.DataBean(info.getStreamUuid()));
        }
    }

    @OnClick(R.id.btn_video_layout_2)
    void videoLayout2() {
        ArrayList<RoomProperty.liveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new RoomProperty.liveConfig.DataBean(info.getStreamUuid()));
        }
    }

    @OnClick(R.id.btn_video_layout_3)
    void videoLayout3() {
        ArrayList<RoomProperty.liveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new RoomProperty.liveConfig.DataBean(info.getStreamUuid()));
        }
    }

    RoomProperty.liveConfig liveConfig = new RoomProperty.liveConfig();

    @OnClick(R.id.btn_mute_chat_all)
    void muteChatAll() {
        boolean allow = !btn_mute_chat_all.isSelected();
        getLocalUser().allowStudentChat(!allow, new EduCallback<Unit>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                ToastUtils.showShort("禁言成功");
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }
}
