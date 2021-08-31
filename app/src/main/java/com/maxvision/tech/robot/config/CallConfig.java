package com.maxvision.tech.robot.config;

/**
 * @author qs
 * @time 2021/5/7 11:48
 * @describe $ 配置webrtc通话 audioEnable是否开启音频 videoEnable是否开启视频
 */
public class CallConfig {
    private boolean audioEnable;
    private boolean videoEnable;

    public CallConfig(boolean audioEnable, boolean videoEnable) {
        this.audioEnable = audioEnable;
        this.videoEnable = videoEnable;
    }

    public boolean isAudioEnable() {
        return audioEnable;
    }

    public void setAudioEnable(boolean audioEnable) {
        this.audioEnable = audioEnable;
    }

    public boolean isVideoEnable() {
        return videoEnable;
    }

    public void setVideoEnable(boolean videoEnable) {
        this.videoEnable = videoEnable;
    }

}
