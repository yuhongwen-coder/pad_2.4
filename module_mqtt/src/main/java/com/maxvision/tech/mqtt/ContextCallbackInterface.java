package com.maxvision.tech.mqtt;

import com.maxvision.tech.mqtt.entity.BaseTaskEntity;
import com.maxvision.tech.mqtt.entity.CallDealEntity;
import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.LocationEntity;
import com.maxvision.tech.mqtt.entity.MapEntity;
import com.maxvision.tech.mqtt.entity.NavigationEntity;
import com.maxvision.tech.mqtt.entity.TaskControlResponse;
import com.maxvision.tech.mqtt.entity.VideoCallEntity;

import java.util.List;

/**
 * name: wy
 * date: 2021/4/1
 * desc: 外部接口获取
 */
public interface ContextCallbackInterface {

    void setHeart(Heart heart);

    void setNavigation(NavigationEntity navigation);

    void setLocation(LocationEntity navigation);

    void setMap(MapEntity data);

    void setVideo(VideoCallEntity data);

    void setCallDeal(CallDealEntity data);

    void onNotifyAlarm(String message);

    void setTaskList(String robotSn,List<BaseTaskEntity> task);

    void setControlResponse(String response);
}