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

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.KeyboardUtils;
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
import io.agora.education.lx.Msg;
import io.agora.education.lx.RoomProperty;
import io.agora.education.lx.UserProperty;
import io.agora.education.widget.ConfirmDialog;
import kotlin.Unit;

public class JhbClassActivity extends BaseClassActivity implements TabLayout.OnTabSelectedListener, KeyboardUtils.OnSoftInputChangedListener {
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
    @BindView(R.id.btn_layout)
    View btn_layout;
    @BindView(R.id.btn_layout_hand_up_down)
    View btn_layout_hand_up_down;
    @BindView(R.id.rl_videos2)
    View rl_videos2;
    @BindView(R.id.fl_video_large)
    FrameLayout fl_video_large;
    @BindView(R.id.fl_video_small)
    FrameLayout fl_video_small;
    RtcVideoView rvv_large;
    RtcVideoView rvv_small;
    @BindView(R.id.content_layout)
    View content_layout;
    @BindView(R.id.btn_switch_video)
    Button btn_switch_video;
    @BindView(R.id.btn_layout_linked)
    View btn_layout_linked;
    @BindView(R.id.btn_hung_up)
    View btn_hung_up;
    @BindView(R.id.btn_switch_mic)
    Button btn_switch_mic;
    @BindView(R.id.btn_switch_camera)
    Button btn_switch_camera;


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
        joinRoom(getMainEduRoom(), roomEntry.getUserName(), roomEntry.getUserUuid(), true, false, true,
                new EduCallback<EduStudent>() {
                    @Override
                    public void onSuccess(@org.jetbrains.annotations.Nullable EduStudent res) {
                        runOnUiThread(() -> onJoinSuccess());
                    }

                    @Override
                    public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                        joinFailed(code, reason);
                    }
                }, false);
    }

    private void onJoinSuccess() {
        content_layout.setVisibility(View.VISIBLE);
        refreshBtnLayout();
        boolean showAudienceList;
        switch (roomEntry.getRole()) {
            case UserProperty.jhbRole.ADMIN:
            case UserProperty.jhbRole.HOST:
                showAudienceList = true;
                break;
            default:
                showAudienceList = false;
        }
        layout_tab.setVisibility(showAudienceList ? View.VISIBLE : View.GONE);
        showFragmentWithJoinSuccess();
        onRoomPropertyChanged(getMainEduRoom(), null);
        onLocalUserPropertyUpdated(getLocalUserInfo(), null);
        switch (roomEntry.getRole()) { // 管理员/主持人/嘉宾主动连麦
            case UserProperty.jhbRole.ADMIN:
            case UserProperty.jhbRole.HOST:
            case UserProperty.jhbRole.GUEST:
                setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_adminAccept, new EduCallback() {
                    @Override
                    public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                        ToastUtils.showShort("主动连麦");
                    }

                    @Override
                    public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                        ToastUtils.showShort(code + " " + reason);
                    }
                });
                break;
            case UserProperty.jhbRole.AUDIENCE:
            default:
                hungUp();
                setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_cancel, new EduCallback() {
                    @Override
                    public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                    }

                    @Override
                    public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                    }
                });
                break;
        }
    }

    @Override
    protected void initView() {
        super.initView();

        /*大班课场景不需要计时*/
        title_view.hideTime();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv_videos.setLayoutManager(gridLayoutManager);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getAppScreenWidth() * 2 / 3);
        rv_videos.setLayoutParams(params);
        adapter = new ClassVideoAdapter();
        rv_videos.setAdapter(adapter);
        rl_videos2.setLayoutParams(params);
        if (rvv_large == null) {
            rvv_large = new RtcVideoView(this);
            rvv_large.init(R.layout.layout_video_small_class, false);
        }
        removeFromParent(rvv_large);
        fl_video_large.addView(rvv_large, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (rvv_small == null) {
            rvv_small = new RtcVideoView(this);
            rvv_small.init(R.layout.layout_video_small_class, false);
        }
        removeFromParent(rvv_small);
        fl_video_small.addView(rvv_small, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

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

        KeyboardUtils.registerSoftInputChangedListener(this, this);
    }

    private void refreshBtnLayout() {
        runOnUiThread(() -> {
            EduStreamInfo stream = getLocalCameraStream();
            btn_layout.setVisibility(View.VISIBLE);
            btn_layout_linked.setVisibility(stream == null ? View.GONE : View.VISIBLE);
            btn_hung_up.setVisibility(UserProperty.jhbRole.AUDIENCE == roomEntry.getRole() ? View.VISIBLE : View.GONE);
            btn_layout_hand_up_down.setVisibility(UserProperty.jhbRole.AUDIENCE == roomEntry.getRole() && stream == null ? View.VISIBLE : View.GONE);
            btn_switch_mic.setText(stream != null && stream.getHasAudio() ? "关闭麦克风" : "打开麦克风");
            btn_switch_camera.setText(stream != null && stream.getHasVideo() ? "关闭摄像头" : "打开摄像头");
        });
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
//                                    ToastUtils.showShort("挂断成功");
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
    public void publishStream(EduUserInfo eduUserInfo, boolean hasVideo, boolean hasAudio) {
        /**连麦中，发流*/
        EduStreamInfo streamInfo = new EduStreamInfo(eduUserInfo.getStreamUuid(), null,
                VideoSourceType.CAMERA, hasVideo, hasAudio, eduUserInfo);
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
//            ToastUtils.showShort("举手 " + userInfos.getUserName() + " " + userInfos.getUserUuid());
        } else if (UserProperty.type.applyAudio_cancel.equals(type) || UserProperty.type.applyVideo_cancel.equals(type)) {
//            ToastUtils.showShort("取消举手 " + userInfos.getUserName() + " " + userInfos.getUserUuid());
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
        boolean showToast = cause != null;
        switch (type) {
            case UserProperty.type.applyAudio_adminAccept:
                publishStream(getLocalUserInfo(), false, true);
                break;
            case UserProperty.type.applyVideo_adminAccept:
                publishStream(getLocalUserInfo(), true, true);
                break;
            case UserProperty.type.applyAudio_adminReject:
            case UserProperty.type.applyVideo_adminReject:
                if (showToast) {
                    ToastUtils.showShort(R.string.reject_interactive);
                }
                break;
            case UserProperty.type.applyVideo_cancel:
                hungUp();
                break;
            case UserProperty.type.applyAudio_adminHungUp:
            case UserProperty.type.applyVideo_adminHungUp:
                if (showToast) {
                    ToastUtils.showShort(R.string.abort_interactive);
                }
                hungUp();
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
        refreshBtnLayout();
        ToastUtils.showShort("已连麦");
    }

    @Override
    public void onLocalStreamUpdated(@NotNull EduStreamEvent streamEvent, @NotNull EduStreamStateChangeType type) {
        super.onLocalStreamUpdated(streamEvent, type);
        EduStreamInfo modifiedStream = streamEvent.getModifiedStream();
        setLocalCameraStream(modifiedStream);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
        refreshBtnLayout();
    }

    @Override
    public void onLocalStreamRemoved(@NotNull EduStreamEvent streamEvent) {
        super.onLocalStreamRemoved(streamEvent);
        refreshVideoList();
        audienceListFragment.updateLocalStream(getLocalCameraStream());
        refreshAudienceList();
        refreshBtnLayout();
        ToastUtils.showShort("已挂断");
    }

    private String handUpUserId;

    @Override
    public void onUserMessageReceived(@NotNull EduMsg message) {
        super.onUserMessageReceived(message);
        try {
            switch (GsonUtils.fromJson(message.getMessage(), Msg.class).data.type) {
                case Msg.Type.adminInvite_audioLink:
                    runOnUiThread(() -> ConfirmDialog.normalWithButton("主播邀请语音连麦", "拒绝", "同意", confirm -> {
                        if (confirm) {
                            setApplyCall(getLocalUserInfo(), UserProperty.type.applyVideo_adminAccept, new EduCallback() {
                                @Override
                                public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                                    publishStream(getLocalUserInfo(), false, true);
                                    getMainEduRoom().getLocalUser().sendUserMessage(GsonUtils.toJson(new Msg(Msg.Type.adminInvite_audioLink_audienceAccept)), message.getFromUser(), new EduCallback<EduMsg>() {
                                        @Override
                                        public void onSuccess(@org.jetbrains.annotations.Nullable EduMsg res) {
                                        }

                                        @Override
                                        public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

                                        }
                                    });
                                }

                                @Override
                                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

                                }
                            });
                        } else {
                            getMainEduRoom().getLocalUser().sendUserMessage(GsonUtils.toJson(new Msg(Msg.Type.adminInvite_audioLink_audienceReject)), message.getFromUser(), new EduCallback<EduMsg>() {
                                @Override
                                public void onSuccess(@org.jetbrains.annotations.Nullable EduMsg res) {
                                }

                                @Override
                                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

                                }
                            });
                        }
                    }).showNow(getSupportFragmentManager(), null));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshVideoList() {
        showVideoList(getCurFullStream());
    }

    private void showVideoList(List<EduStreamInfo> list) {
        LogUtil.log("showVideoList", list.size());
        runOnUiThread(() -> {
            btn_switch_video.setVisibility(getLocalCameraStream() == null ? View.GONE : View.VISIBLE);
            if (getLocalCameraStream() == null && !showLiveVideo) {
                btn_switch_video.performClick();
                return;
            }
            for (EduStreamInfo info : list) {
                renderStream(getMainEduRoom(), info, null);
            }
            List<EduStreamInfo> finalList = new ArrayList<>();

            // 按顺序添加视频
            if (liveConfig.data != null) {
                for (RoomProperty.liveConfig.DataBean bean : liveConfig.data) {
                    for (EduStreamInfo info : list) {
                        if (info.getStreamUuid().equals(bean.streamUuid)) {
                            finalList.add(info);
                            break;
                        }
                    }
                }
            }

            for (int i = 0; i < list.size(); i++) {
                EduStreamInfo info = list.get(i);
                boolean isOutput = false;
                for (EduStreamInfo streamInfo : finalList) {
                    if (info.getStreamUuid().equals(streamInfo.getStreamUuid())) {
                        isOutput = true;
                        break;
                    }
                }
                if (isOutput) {
                    getLocalUser().subscribeStream(info, new StreamSubscribeOptions(true, true, VideoStreamType.HIGH), new EduCallback<Unit>() {
                        @Override
                        public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                            LogUtil.log("subscribeStream", "onSuccess", info.getPublisher().getUserName(), res);
                        }

                        @Override
                        public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                            LogUtil.log("subscribeStream", "onFailure", info.getPublisher().getUserName(), reason);
                        }
                    });
                } else {
                    getLocalUser().unSubscribeStream(info, new StreamSubscribeOptions(false, false, VideoStreamType.HIGH), new EduCallback<Unit>() {
                        @Override
                        public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                            LogUtil.log("unSubscribeStream", "onSuccess", info.getPublisher().getUserName(), res);
                        }

                        @Override
                        public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                            LogUtil.log("unSubscribeStream", "onFailure", info.getPublisher().getUserName(), reason);
                        }
                    });
                }
            }
            int cmd = liveConfig.cmd;

            if (!showLiveVideo) {
                finalList.clear();
                EduStreamInfo localCameraStream = getLocalCameraStream();
                if (localCameraStream != null) {
                    finalList.add(localCameraStream);
                }
                cmd = RoomProperty.liveConfig.CMD.CMD_1;
            }

            if (finalList.size() == 0 && cmd == RoomProperty.liveConfig.CMD.CMD_3) {
                cmd = RoomProperty.liveConfig.CMD.CMD_1;
            }

            rvv_large.setName("");
            rvv_large.muteAudio(true);
            rvv_large.muteVideo(true);
            rvv_small.setName("");
            rvv_small.muteAudio(true);
            rvv_small.muteVideo(true);

            switch (cmd) {
                case RoomProperty.liveConfig.CMD.CMD_1:
                case RoomProperty.liveConfig.CMD.CMD_2:
                    rv_videos.setVisibility(View.GONE);
                    rl_videos2.setVisibility(View.VISIBLE);
                    rvv_small.setVisibility(cmd == RoomProperty.liveConfig.CMD.CMD_1 ? View.GONE : View.VISIBLE);
                    adapter.setNewList(new ArrayList<>());
                    for (int i = 0; i < finalList.size(); i++) {
                        EduStreamInfo info = finalList.get(i);
                        if (i == 0) {
                            renderStream(getMainEduRoom(), info, rvv_large.getVideoLayout());
                            rvv_large.setName(info.getPublisher().getUserName());
                            rvv_large.muteAudio(!info.getHasAudio());
                            rvv_large.muteVideo(!info.getHasVideo());
                        } else if (i == 1 && RoomProperty.liveConfig.CMD.CMD_2 == cmd) {
                            renderStream(getMainEduRoom(), info, rvv_small.getVideoLayout());
                            rvv_small.setName(info.getPublisher().getUserName());
                            rvv_small.muteAudio(!info.getHasAudio());
                            rvv_small.muteVideo(!info.getHasVideo());
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
                ToastUtils.showShort("已举手");
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
                ToastUtils.showShort("已取消举手");
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

    RoomProperty.liveConfig liveConfig = new RoomProperty.liveConfig();

    @Override
    protected void onDestroy() {
        KeyboardUtils.unregisterSoftInputChangedListener(getWindow());
        super.onDestroy();
    }

    @Override
    public void onSoftInputChanged(int height) {
        if (height == 0) {
            refreshBtnLayout();
        } else {
            btn_layout.setVisibility(View.GONE);
        }
    }

    boolean showLiveVideo = true;

    @OnClick(R.id.btn_switch_video)
    void switchVideo() {
        showLiveVideo = !showLiveVideo;
        btn_switch_video.setText(showLiveVideo ? "切换至本地画面" : "切换至直播画面");
        refreshVideoList();
    }

    @OnClick(R.id.btn_switch_mic)
    void switchMic() {
        EduStreamInfo stream = getLocalCameraStream();
        if (stream != null) {
            boolean hasAudio = stream.getHasAudio();
            muteLocalAudio(hasAudio, new EduCallback() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                    ToastUtils.showShort(hasAudio ? "已关闭麦克风" : "已关闭麦克风");
                }

                @Override
                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
    }

    @OnClick(R.id.btn_switch_camera)
    void switchCamera() {
        EduStreamInfo stream = getLocalCameraStream();
        if (stream != null) {
            boolean hasVideo = stream.getHasVideo();
            muteLocalVideo(hasVideo, new EduCallback() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                    ToastUtils.showShort(hasVideo ? "已关闭摄像头" : "已打开摄像头");
                }

                @Override
                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
    }
}
