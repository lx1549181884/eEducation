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
import io.agora.base.ToastManager;
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
import io.agora.education.api.stream.data.VideoSourceType;
import io.agora.education.api.user.EduStudent;
import io.agora.education.api.user.EduUser;
import io.agora.education.api.user.data.EduBaseUserInfo;
import io.agora.education.api.user.data.EduLocalUserInfo;
import io.agora.education.api.user.data.EduUserEvent;
import io.agora.education.api.user.data.EduUserInfo;
import io.agora.education.api.user.data.EduUserStateChangeType;
import io.agora.education.classroom.adapter.ClassVideoAdapter;
import io.agora.education.classroom.bean.channel.Room;
import io.agora.education.classroom.bean.msg.PeerMsg;
import io.agora.education.classroom.fragment.UserListFragment;
import io.agora.education.classroom.widget.RtcVideoView;
import io.agora.education.lx.LiveConfig;
import io.agora.education.lx.LogUtil;
import io.agora.education.lx.UserProperty;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannelAttribute;
import kotlin.Unit;

import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Status.Applying;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Status.CoVideoing;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Status.DisCoVideo;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.ABORT;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.ACCEPT;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.APPLY;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.CANCEL;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.EXIT;
import static io.agora.education.classroom.bean.msg.PeerMsg.CoVideoMsg.Type.REJECT;

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

    /**
     * 当前本地用户是否在连麦中
     */
    private int localCoVideoStatus = DisCoVideo;
    /**
     * 当前连麦用户
     */
    private EduBaseUserInfo curLinkedUser;

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
                        runOnUiThread(() -> showFragmentWithJoinSuccess());
                        if (isAdmin) { // 管理员主动连麦
                            onLinkMediaChanged(true);
                        }
                    }

                    @Override
                    public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                        joinFailed(code, reason);
                    }
                }, isAdmin);
    }

    @Override
    protected void initView() {
        super.initView();
        /*大班课场景不需要计时*/
        title_view.hideTime();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv_videos.setLayoutManager(gridLayoutManager);
        rv_videos.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

        resetHandState();
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
     * 申请举手连麦
     */
    private void applyCoVideo() {
        EduUser localUser = getLocalUser();
        Map.Entry<String, String> property = new AbstractMap.SimpleEntry<>(UserProperty.handUp.class.getSimpleName(), UserProperty.handUp.TRUE);
        localUser.setUserProperty(property, new HashMap<>(), localUser.getUserInfo(), new EduCallback() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object res) {
                localCoVideoStatus = Applying;
                resetHandState();
                ToastUtils.showShort("成功");
            }

            @Override
            public void onFailure(int code, @Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    /**
     * 取消举手(包括在老师处理前主动取消和老师同意后主动退出)
     */
    private void cancelCoVideo(EduCallback callback) {
        if (localCoVideoStatus == CoVideoing) {
            /*连麦过程中取消
             * 1：关闭本地流
             * 2：更新流信息到服务器
             * 3：发送取消的点对点消息给老师
             * 4：更新本地记录的连麦状态*/
            if (getLocalCameraStream() != null) {
                LocalStreamInitOptions options = new LocalStreamInitOptions(
                        getLocalCameraStream().getStreamUuid(), false, false);
                options.setStreamName(getLocalCameraStream().getStreamName());
                getLocalUser().initOrUpdateLocalStream(options, new EduCallback<EduStreamInfo>() {
                    @Override
                    public void onSuccess(@Nullable EduStreamInfo res) {
                        localCoVideoStatus = DisCoVideo;
                        curLinkedUser = null;
                        resetHandState();
                        EduLocalUserInfo userInfo = getLocalUser().getUserInfo();
                        PeerMsg.CoVideoMsg coVideoMsg = new PeerMsg.CoVideoMsg(EXIT, userInfo.getUserUuid(), userInfo.getUserName());
                        PeerMsg peerMsg = new PeerMsg(PeerMsg.Cmd.CO_VIDEO, coVideoMsg);
                        getLocalUser().sendUserMessage(peerMsg.toJsonString(), getTeacher(), callback);
                        getLocalUser().unPublishStream(res, new EduCallback<Boolean>() {
                            @Override
                            public void onSuccess(@Nullable Boolean res) {
                            }

                            @Override
                            public void onFailure(int code, @Nullable String reason) {
                                callback.onFailure(code, reason);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code, @Nullable String reason) {
                        Log.e(TAG, "举手过程中取消失败");
                        callback.onFailure(code, reason);
                    }
                });
            }
        } else {
            /*举手过程中取消(老师还未处理)*/
            EduUser localUser = getLocalUser();
            Map.Entry<String, String> property = new AbstractMap.SimpleEntry<>(UserProperty.handUp.class.getSimpleName(), UserProperty.handUp.FALSE);
            localUser.setUserProperty(property, new HashMap<>(), localUser.getUserInfo(), new EduCallback<Unit>() {
                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                    localCoVideoStatus = DisCoVideo;
                    resetHandState();
                    ToastUtils.showShort("成功");
                }

                @Override
                public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
    }

    /**
     * 本地用户(举手、连麦)被老师同意/(拒绝、打断)
     *
     * @param coVideoing 是否正在连麦过程中
     */
    public void onLinkMediaChanged(boolean coVideoing) {
        if (!coVideoing) {
            /**正在连麦中时才会记录本地流；申请中取消或被拒绝本地不会记录流*/
            if (localCoVideoStatus == CoVideoing) {
                Log.e(TAG, "连麦过程中被打断");
                /**连麦被打断，停止发流*/
                LocalStreamInitOptions options = new LocalStreamInitOptions(
                        getLocalCameraStream().getStreamUuid(), false, false);
                options.setStreamName(getLocalCameraStream().getStreamName());
                getLocalUser().initOrUpdateLocalStream(options, new EduCallback<EduStreamInfo>() {
                    @Override
                    public void onSuccess(@Nullable EduStreamInfo res) {
                        getLocalUser().unPublishStream(res, new EduCallback<Boolean>() {
                            @Override
                            public void onSuccess(@Nullable Boolean res) {
                                Log.e(TAG, "连麦过程中被打断，停止发流成功");
                            }

                            @Override
                            public void onFailure(int code, @Nullable String reason) {
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code, @Nullable String reason) {
                    }
                });
            }
        } else {
            /**连麦中，发流*/
            EduStreamInfo streamInfo = new EduStreamInfo(getLocalUserInfo().getStreamUuid(), null,
                    VideoSourceType.CAMERA, true, true, getLocalUserInfo());
            /**举手连麦，需要新建流信息*/
            getLocalUser().publishStream(streamInfo, new EduCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean res) {
                    Log.e(TAG, "发流成功");
                    setLocalCameraStream(streamInfo);
                }

                @Override
                public void onFailure(int code, @Nullable String reason) {
                    ToastUtils.showShort(code + " " + reason);
                }
            });
        }
        localCoVideoStatus = coVideoing ? CoVideoing : DisCoVideo;
        curLinkedUser = coVideoing ? getLocalUserInfo() : null;
        resetHandState();
    }

    /**
     * 被取消连麦
     */
    private void resetHandState() {
        runOnUiThread(() -> {
            boolean hasTeacher = getTeacher() != null;
            /**有老师的情况下才显示*/
//            layout_hand_up.setVisibility(hasTeacher ? View.VISIBLE : View.GONE);
            /**当前连麦用户不是本地用户时，隐藏*/
            if (curLinkedUser != null) {
//                layout_hand_up.setVisibility((curLinkedUser.equals(getLocalUserInfo()) ?
//                        View.VISIBLE : View.GONE));
                layout_hand_up.setEnabled(curLinkedUser.equals(getLocalUserInfo()));
                layout_hand_up.setSelected(true);
            } else {
                layout_hand_up.setEnabled(true);
                layout_hand_up.setSelected(false);
            }
//            if (hasTeacher) {
//                layout_hand_up.setSelected(localCoVideoStatus != DisCoVideo);
//            }
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
        /**老师不在的时候不能举手*/
        resetHandState();
    }

    @Override
    public void onRemoteUsersJoined(@NotNull List<? extends EduUserInfo> users, @NotNull EduRoom classRoom) {
        super.onRemoteUsersJoined(users, classRoom);
        title_view.setTitle(String.format(Locale.getDefault(), "%s", getMediaRoomName()));
        /**老师不在的时候不能举手*/
        resetHandState();
        refreshVideoList();
        refreshAudienceList();
    }

    @Override
    public void onRemoteUserLeft(@NotNull EduUserEvent userEvent, @NotNull EduRoom classRoom) {
        super.onRemoteUserLeft(userEvent, classRoom);
        title_view.setTitle(String.format(Locale.getDefault(), "%s", getMediaRoomName()));
        /**老师不在的时候不能举手*/
        resetHandState();
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
        });
    }

    @Override
    public void onRemoteUserPropertyUpdated(@NotNull EduUserInfo userInfos, @NotNull EduRoom classRoom,
                                            @Nullable Map<String, Object> cause) {
        super.onRemoteUserPropertyUpdated(userInfos, classRoom, cause);
        if (UserProperty.handUp.TRUE.equals(UserProperty.get(userInfos, UserProperty.handUp.class))) {
            handUpUserId = userInfos.getUserUuid();
        } else {
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
        PeerMsg peerMsg = PeerMsg.fromJson(message.getMessage(), PeerMsg.class);
        if (peerMsg.cmd == PeerMsg.Cmd.CO_VIDEO) {
            PeerMsg.CoVideoMsg coVideoMsg = peerMsg.getMsg(PeerMsg.CoVideoMsg.class);
            switch (coVideoMsg.type) {
                case REJECT:
                    onLinkMediaChanged(false);
                    ToastManager.showShort(R.string.reject_interactive);
                    break;
                case ACCEPT:
                    onLinkMediaChanged(true);
                    ToastManager.showShort(R.string.accept_interactive);
                    break;
                case ABORT:
                    onLinkMediaChanged(false);
                    ToastManager.showShort(R.string.abort_interactive);
                    break;
                case APPLY:
                    handUpUserId = coVideoMsg.userId;
                    ToastUtils.showShort("举手 " + coVideoMsg.userName + " " + coVideoMsg.userId);
                    break;
                case CANCEL:
                    handUpUserId = null;
                    ToastUtils.showShort("取消举手 " + coVideoMsg.userName + " " + coVideoMsg.userId);
                    break;
            }
        }
    }

    @Override
    public void onUserChatMessageReceived(@NotNull EduChatMsg chatMsg) {
        super.onUserChatMessageReceived(chatMsg);
    }

    private void refreshVideoList() {
        showVideoList(getCurFullStream());
    }

    private void showVideoList(List<EduStreamInfo> list) {
        LogUtil.log("showVideoList", list);
        runOnUiThread(() -> {
            for (EduStreamInfo info : list) {
                renderStream(getMainEduRoom(), info, null);
            }
            List<EduStreamInfo> finalList = new ArrayList<>();
            if (liveConfig.data != null) {
                for (LiveConfig.DataBean bean : liveConfig.data) {
                    for (EduStreamInfo info : list) {
                        if (info.getStreamUuid().equals(bean.uid)) {
                            finalList.add(info);
                            break;
                        }
                    }
                }
            }
            switch (liveConfig.cmd) {
                case LiveConfig.CMD.CMD_1:
                case LiveConfig.CMD.CMD_2:
                    rv_videos.setVisibility(View.GONE);
                    rl_videos2.setVisibility(View.VISIBLE);
                    rvv_small.setViewVisibility(LiveConfig.CMD.CMD_1.equals(liveConfig.cmd) ? View.GONE : View.VISIBLE);
                    adapter.setNewList(new ArrayList<>());
                    for (int i = 0; i < list.size(); i++) {
                        EduStreamInfo info = list.get(i);
                        if (i == 0) {
                            renderStream(getMainEduRoom(), info, rvv_large);
                        } else if (i == 1 && LiveConfig.CMD.CMD_2.equals(liveConfig.cmd)) {
                            renderStream(getMainEduRoom(), info, rvv_small);
                        }
                    }
                    break;
                case LiveConfig.CMD.CMD_3:
                default:
                    rv_videos.setVisibility(View.VISIBLE);
                    rl_videos2.setVisibility(View.GONE);
                    adapter.setNewList(list);
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
        /*举手*/
        applyCoVideo();
    }

    @OnClick(R.id.btn_hand_down)
    void handDown() {
        /*取消举手(包括在老师处理前主动取消和老师同意后主动退出)*/
        cancelCoVideo(new EduCallback<EduMsg>() {
            @Override
            public void onSuccess(@Nullable EduMsg res) {
                ToastUtils.showShort("成功");
            }

            @Override
            public void onFailure(int code, @Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }

    @OnClick(R.id.btn_accept)
    void accept() {
        accept(new EduCallback<EduMsg>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable EduMsg res) {

            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

            }
        });
    }

    @OnClick(R.id.btn_reject)
    void reject() {
        reject(new EduCallback<EduMsg>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable EduMsg res) {

            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {

            }
        });
    }

    EduUserInfo getUserInfo(String userId) {
        for (EduUserInfo userInfo : getCurFullUser()) {
            if (userInfo.getUserUuid().equals(userId)) {
                return userInfo;
            }
        }
        return null;
    }

    /**
     * 同意连麦
     */
    private void accept(EduCallback<EduMsg> callback) {
        EduUserInfo userInfo = getUserInfo(handUpUserId);
        if (userInfo == null) {
            return;
        }
        PeerMsg.CoVideoMsg coVideoMsg = new PeerMsg.CoVideoMsg(
                ACCEPT,
                userInfo.getUserUuid(),
                userInfo.getUserName());
        PeerMsg peerMsg = new PeerMsg(PeerMsg.Cmd.CO_VIDEO, coVideoMsg);
        getLocalUser().sendUserMessage(peerMsg.toJsonString(), userInfo, callback);
    }

    /**
     * 拒绝连麦
     */
    private void reject(EduCallback<EduMsg> callback) {
        EduUserInfo userInfo = getUserInfo(handUpUserId);
        if (userInfo == null) {
            return;
        }
        PeerMsg.CoVideoMsg coVideoMsg = new PeerMsg.CoVideoMsg(
                REJECT,
                userInfo.getUserUuid(),
                userInfo.getUserName());
        PeerMsg peerMsg = new PeerMsg(PeerMsg.Cmd.CO_VIDEO, coVideoMsg);
        getLocalUser().sendUserMessage(peerMsg.toJsonString(), userInfo, callback);
    }

    @OnClick(R.id.btn_video_layout_1)
    void videoLayout1() {
        ArrayList<LiveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new LiveConfig.DataBean(info.getStreamUuid()));
        }
        setChannelVideoLayout(new LiveConfig(LiveConfig.CMD.CMD_1, data));
    }

    @OnClick(R.id.btn_video_layout_2)
    void videoLayout2() {
        ArrayList<LiveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new LiveConfig.DataBean(info.getStreamUuid()));
        }
        setChannelVideoLayout(new LiveConfig(LiveConfig.CMD.CMD_2, data));
    }

    @OnClick(R.id.btn_video_layout_3)
    void videoLayout3() {
        ArrayList<LiveConfig.DataBean> data = new ArrayList<>();
        for (EduStreamInfo info : getCurFullStream()) {
            data.add(new LiveConfig.DataBean(info.getStreamUuid()));
        }
        setChannelVideoLayout(new LiveConfig(LiveConfig.CMD.CMD_3, data));
    }

    private void setChannelVideoLayout(LiveConfig liveConfig) {
        ArrayList<RtmChannelAttribute> attributes = new ArrayList<>();
        attributes.add(new RtmChannelAttribute(LiveConfig.KEY, GsonUtils.toJson(liveConfig)));
        updateAttributes(attributes, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ToastUtils.showShort("设置成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                ToastUtils.showShort(errorInfo.toString());
            }
        });
    }

    LiveConfig liveConfig = new LiveConfig();

    @Override
    public void onAttributesUpdated(@Nullable List<RtmChannelAttribute> p0) {
        super.onAttributesUpdated(p0);
        runOnUiThread(() -> {
            for (RtmChannelAttribute attribute : p0) {
                if (LiveConfig.KEY.equals(attribute.getKey())) {
                    liveConfig = GsonUtils.fromJson(attribute.getValue(), LiveConfig.class);
                    break;
                }
            }
            refreshVideoList();
        });
    }

    @OnClick(R.id.btn_mute_chat_all)
    void muteChatAll() {
        boolean allow = !btn_mute_chat_all.isSelected();
        getLocalUser().allowStudentChat(!allow, new EduCallback<Unit>() {
            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Unit res) {
                ToastUtils.showShort("成功");
            }

            @Override
            public void onFailure(int code, @org.jetbrains.annotations.Nullable String reason) {
                ToastUtils.showShort(code + " " + reason);
            }
        });
    }
}
