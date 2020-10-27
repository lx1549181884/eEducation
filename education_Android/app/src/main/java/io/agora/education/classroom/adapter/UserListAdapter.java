package io.agora.education.classroom.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.education.R;
import io.agora.education.api.stream.data.EduStreamInfo;
import io.agora.education.api.user.data.EduUserInfo;

public class UserListAdapter extends BaseQuickAdapter<EduUserInfo, UserListAdapter.ViewHolder> {

    private String localUserUuid;
    //    private EduStreamInfo localCameraStream;
//    private int localUserIndex;
    private List<String> grantedUuids = new ArrayList<>();
    List<EduStreamInfo> eduStreamInfos = new ArrayList<>(); // 连麦中用户

    public void setGrantedUuids(List<String> grantedUuids) {
        if (grantedUuids != null && this.grantedUuids.equals(grantedUuids)) {
            this.grantedUuids = grantedUuids;
            notifyDataSetChanged();
        }
    }

    public void setLocalUserUuid(@NonNull String localUserUuid) {
        this.localUserUuid = localUserUuid;
    }

    public void refreshStreamStatus(@NonNull EduStreamInfo streamInfo) {
        for (int i = 0; i < eduStreamInfos.size(); i++) {
            EduStreamInfo element = eduStreamInfos.get(i);
            if (element.same(streamInfo)) {
                eduStreamInfos.set(i, streamInfo);
                break;
            }
        }
        List<EduUserInfo> data = getData();
        for (int i = 0; i < data.size(); i++) {
            EduUserInfo element = data.get(i);
            if (element.getUserUuid().equals(streamInfo.getPublisher().getUserUuid())) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public UserListAdapter() {
        super(R.layout.item_user_list);
        addChildClickViewIds(R.id.iv_btn_mute_audio, R.id.iv_btn_mute_video);
    }

    @Override
    protected void convert(@NonNull ViewHolder viewHolder, EduUserInfo userInfo) {
        boolean hasAudio = false;
        boolean hasVideo = false;
        for (EduStreamInfo streamInfo : eduStreamInfos) {
            if (userInfo.getUserUuid().equals(streamInfo.getPublisher().getUserUuid())) {
                hasAudio = streamInfo.getHasAudio();
                hasVideo = streamInfo.getHasVideo();
                break;
            }
        }
        viewHolder.tv_name.setText(userInfo.getUserName());
        viewHolder.iv_btn_grant_board.setSelected(grantedUuids.contains(userInfo.getUserUuid()));
        viewHolder.iv_btn_mute_audio.setSelected(hasAudio);
        viewHolder.iv_btn_mute_video.setSelected(hasVideo);
        boolean isLocal = userInfo.getUserUuid().equals(localUserUuid);
        viewHolder.iv_btn_mute_audio.setEnabled(isLocal);
        viewHolder.iv_btn_mute_video.setEnabled(isLocal);
        if (!isLocal) {
            viewHolder.iv_btn_mute_audio.setAlpha(0.5f);
            viewHolder.iv_btn_mute_video.setAlpha(0.5f);
        }
    }


    static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.iv_btn_grant_board)
        ImageView iv_btn_grant_board;
        @BindView(R.id.iv_btn_mute_audio)
        ImageView iv_btn_mute_audio;
        @BindView(R.id.iv_btn_mute_video)
        ImageView iv_btn_mute_video;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setNewData(List<EduStreamInfo> covideoAudiences, List<EduUserInfo> audiences) {
        eduStreamInfos.clear();
        eduStreamInfos.addAll(covideoAudiences);
        setNewData(audiences);
    }
}
