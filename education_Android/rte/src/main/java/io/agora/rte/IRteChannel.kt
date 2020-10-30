package io.agora.rte

import androidx.annotation.NonNull
import io.agora.rtc.models.ChannelMediaOptions
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmChannelAttribute

interface IRteChannel {

    fun join(rtcOptionalInfo: String, rtcToken: String, rtcUid: Long, mediaOptions: ChannelMediaOptions,
             @NonNull callback: ResultCallback<Void>)

    fun leave()

    fun release()

    fun updateAttributes(attributes: MutableList<RtmChannelAttribute>, @NonNull callback: ResultCallback<Void>)
}
