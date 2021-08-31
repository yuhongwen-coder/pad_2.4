package com.maxvision.tech.mqtt;

import android.text.TextUtils;
import android.util.Log;

import com.maxvision.tech.mqtt.entity.Heart;
import com.maxvision.tech.mqtt.entity.state.RobotChargeState;
import com.maxvision.tech.mqtt.entity.state.RobotCmdType;
import com.maxvision.tech.mqtt.entity.state.RobotInitState;
import com.maxvision.tech.mqtt.entity.state.RobotIntercomState;
import com.maxvision.tech.mqtt.entity.state.RobotNavState;
import com.maxvision.tech.mqtt.entity.state.RobotDoorState;
import com.maxvision.tech.mqtt.entity.state.RobotDrugState;
import com.maxvision.tech.mqtt.entity.state.RobotStateHint;
import com.maxvision.tech.mqtt.entity.state.RobotStopState;
import com.maxvision.tech.mqtt.entity.state.RobotTaskState;
import com.maxvision.tech.mqtt.entity.state.RobotWaterState;
import com.maxvision.tech.mqtt.entity.state.RobotXdzyState;
import com.maxvision.tech.mqtt.entity.state.RobotZnxyState;

/**
 * name: wy
 * date: 2021/4/13
 * desc: 任务状态判断类
 * <p>
 * 注意:
 * 1. private修饰的为公用逻辑,请务必检查后修改(包括文字内容)
 * 2. 所有状态前必须判断 机器人是否在线
 * 3. 运动状态前必须判断 急停按钮是否按下
 * 4. 运动状态(除充电)前必须判断 机器人是否初始化\
 * <p>
 */
public class RobotState {

    // 判断是否能初始化
    public static RobotStateHint isCanInit(Heart heart) {
        RobotStateHint condition = getFirstCondition(heart);
        if (null != condition) {
            return condition;
        }
        // 判断是否正在前往充电
        condition = isGoCharge(heart, RobotCmdType.CMD_INIT);
        if (null != condition) {
            return condition;
        }
        condition = isCharging(heart, RobotCmdType.CMD_INIT);
        return condition;
    }

    // 判断是否能充电
    public static RobotStateHint isCanCharge(Heart heart) {
        RobotStateHint condition = getFirstCondition(heart);
        if (null != condition) {
            return condition;
        }
        condition = isInit(heart);
        if (null != condition) {
            return condition;
        }
        if (RobotChargeState.NAV == heart.chargeState) {
            // 正在前往充电
            return getHint("正在前往充电中,请等待");
        }
        if (RobotChargeState.ING == heart.chargeState || RobotChargeState.FULL == heart.chargeState) {
            return getHint("正在充电中");
        }
        if (RobotNavState.FOLLOW == heart.navState) {
            return getHint("机器人正在跟随,请稍候再试");
        }
        // 是否在执行任务
        condition = isTasking(heart, RobotCmdType.CMD_CHARGE);
        if (null != condition) {
            return condition;
        }
        // 是否在执行消毒作业
        condition = isXdzy(heart,RobotCmdType.CMD_CHARGE);
        if (null != condition) {
            return condition;
        }
        // 是否在执行协运任务
        condition = isZnxyRunning(heart,RobotCmdType.CMD_CHARGE);
        if (condition != null) {
            return condition;
        }
        return isNaving(heart,RobotCmdType.CMD_CHARGE);
    }

    // 判断是否能停止充电
    public static RobotStateHint isCanStopCharge(Heart heart) {
        RobotStateHint condition = getFirstCondition(heart);
        if (null != condition) {
            return condition;
        }
        // 判断是否在前往充电
        condition = isGoCharge(heart, RobotCmdType.CMD_NONE);
        if (null != condition) {
            return condition;
        }
        return isCharging(heart, RobotCmdType.CMD_NONE);
    }

    // 判断是否能导航
    // 1. 在充电中或者正在前往的过程中-->判断是否能停止充电
    // 2. 在导航到加水点的过程中 --> 判断是否能停止加水
    // 3. 在加水或者加药
    // 4. 在导航中(包含2) --> 是否能停止导航
    public static RobotStateHint isCanNav(Heart heart) {
        RobotStateHint condition = getSecondCondition(heart);
        if (null != condition) {
            return condition;
        }
        // 判断是否在前往充电
        condition = isGoCharge(heart, RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        // 判断是否在充电
        condition = isCharging(heart, RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        condition = isWater(heart, RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        condition = isDrug(heart);
        if (null != condition) {
            return condition;
        }
        condition = isTasking(heart,RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        condition = isXdzy(heart,RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        condition = isZnxyRunning(heart,RobotCmdType.CMD_NAV);
        if (condition != null) {
            return condition;
        }
        condition = isNaving(heart,RobotCmdType.CMD_NAV);
        if (null != condition) {
            return condition;
        }
        return null;
    }

    // 判断任务调度
    public static RobotStateHint isCanTask(Heart heart) {
        RobotStateHint condition = getSecondCondition(heart);
        if (null != condition) {
            return condition;
        }
        condition = isBatLow(heart);
        if (null != condition) {
            return condition;
        }
        // 判断是否在前往充电
        condition = isGoCharge(heart, RobotCmdType.CMD_TASK);
        if (null != condition) {
            return condition;
        }
        // 判断是否在充电
        condition = isCharging(heart, RobotCmdType.CMD_TASK);
        if (null != condition) {
            return condition;
        }
        condition = isTasking(heart,RobotCmdType.CMD_TASK);
        if (null != condition) {
            return condition;
        }
        return isNaving(heart,RobotCmdType.CMD_TASK);
    }

    // 判断消毒作业
    public static RobotStateHint isCanXdzy(Heart heart) {
        RobotStateHint condition = getSecondCondition(heart);
        if (null != condition) {
            return condition;
        }
        condition = isBatLow(heart);
        if (null != condition) {
            return condition;
        }
        condition = isWaterLow(heart);
        if (null != condition) {
            return condition;
        }
        condition = isGoCharge(heart,RobotCmdType.CMD_XDZY);
        if (null != condition) {
          return condition;
        }
        condition = isCharging(heart,RobotCmdType.CMD_XDZY);
        if (null != condition) {
            return condition;
        }
        condition = isXdzy(heart,RobotCmdType.CMD_XDZY);
        if (null != condition) {
            return condition;
        }
        return isNaving(heart,RobotCmdType.CMD_XDZY);
    }

    // 判断停止导航
    public static RobotStateHint isCanStop(Heart heart) {
        RobotStateHint condition = isOnline(heart);
        if (null != condition) {
            return condition;
        }
        condition = isGoCharge(heart,RobotCmdType.CMD_NONE);
        if (null != condition) {
            return condition;
        }
        condition = isTasking(heart,RobotCmdType.CMD_NONE);
        if (null != condition) {
            return condition;
        }
        condition = isXdzy(heart,RobotCmdType.CMD_NONE);
        if (null != condition) {
            return condition;
        }
        condition = isZnxyRunning(heart,RobotCmdType.CMD_NONE);
        if (null != condition) {
            return condition;
        }
        condition = isNaving(heart,RobotCmdType.CMD_NONE);
        return condition;
    }


    /**
     * 判断智能协运是否能执行运动
     *
     * @param isShowStop 是否显示停止对话框
     * @param heart      心跳
     * @return
     */
    public static RobotStateHint isCanRunningZnxy(boolean isShowStop, Heart heart) {
        RobotStateHint secondCondition = getSecondCondition(heart);
        if (null != secondCondition) {
            return secondCondition;
        }
        RobotStateHint batHint = isBatLow(heart);
        if (batHint != null) {
            return batHint;
        }
        RobotStateHint doorHint = isDoorClose(heart);
        if (doorHint != null) {
            return doorHint;
        }
        if (isShowStop) {
            RobotStateHint runningHint = isZnxyRunning(heart,RobotCmdType.CMD_NONE);
            if (runningHint != null) {
                return runningHint;
            }
        }
        RobotStateHint chargeHint = isChargingXy(heart,RobotCmdType.CMD_ZNXY);
        if (chargeHint != null) {
            return chargeHint;
        }
        return isNaving(heart,RobotCmdType.CMD_ZNXY);
    }

    //机器人状态描述
    public static String state(Heart heart) {
        if (!heart.isLine) {
            return "离线";
        }
        //1、判断是否急停
        if (heart.stop == 1) {
            return "机器人急停按钮已被按下";
        }

        //2、判断充电状态
        if (heart.chargeState == RobotChargeState.NAV) {
            return "正在前往充电桩";
        }
        if (heart.chargeState == RobotChargeState.ING) {
            return "正在充电";
        }
        if (heart.chargeState == RobotChargeState.FULL) {
            return "电量已充满";
        }

        //3、初始化状态
        if (heart.initState == RobotInitState.EMPTY) {
            return "地图未初始化";
        }
        if (heart.initState == RobotInitState.FAIL) {
            return "地图初始化失败";
        }
        if (heart.initState == RobotInitState.ROTATING) {
            return "正在地图初始化";
        }

        //4、任务调度状态
        if (heart.taskState == RobotTaskState.ING) {
            return "正在执行任务";
        }

        //4、消毒作业状态
        if (heart.xdzyState == RobotXdzyState.ING) {
            return "正在执行消毒作业";
        }
        if (heart.xdzyState == RobotXdzyState.BREAK) {
            return "消毒作业已暂停";
        }

        if ("2".equals(heart.type) && heart.xyMode == 0) {
            String state = getZnxyState(heart);
            if (!TextUtils.isEmpty(state)) {
                return state;
            }
        }

        //5、上水状态
        if (1 == heart.xyMode) {
            if (RobotWaterState.NAV == heart.waterState) {
                return "正在导航到加水点";
            }
            if (RobotWaterState.ING == heart.waterState) {
                return "正在执行加水";
            }
            if (RobotDrugState.ING == heart.drugState) {
                return "正在执行加药";
            }
        }

        //6、判断导航状态
        if (heart.navState == RobotNavState.ING) {
            return "正在导航";
        }
        if (heart.navState == RobotNavState.FOLLOW) {
            return "正在跟随";
        }

        //7、对讲状态
        if (RobotIntercomState.INTERCOM_CALL_ON == heart.intercomState||RobotIntercomState.INTERCOM_ANSWER_ON== heart.intercomState) {
            return "正在通话";
        }
        if (RobotIntercomState.INTERCOM_CALL_ING == heart.intercomState) {
            return "呼叫中";
        }
        if (RobotIntercomState.INTERCOM_ANSWER_ING == heart.intercomState) {
            return "接听中";
        }
        if (RobotIntercomState.INTERCOM_HANGEUP_ING == heart.intercomState) {
            return "挂断中";
        }

        //8、报警状态
        if (heart.sufaState.hwcw == 1) {
            return "红外测温已开启";
        }
        if (heart.sufaState.kzjc == 1) {
            return "口罩检测已开启";
        }
        if (heart.sufaState.face == 1) {
            return "人脸识别已开启";
        }
        if (heart.sufaState.qybk == 1) {
            return "区域布控已开启";
        }

        return "空闲";
    }

    private static String getZnxyState(Heart heart) {
        String robotState = "";
        if (heart.doorState == RobotDoorState.CLOSE_ING) {
            return RobotDoorState.CLOSE_ING_STRING;
        }
        if (heart.doorState == RobotDoorState.OPEN_ING) {
            return RobotDoorState.OPEN_ING_STRING;
        }
        if (heart.doorState == RobotDoorState.OPEND) {
            return RobotDoorState.OPEND_STRING;
        }
        if (TextUtils.equals(heart.znxyNextState,"任务已经停止，您的物品未取出")) {
            return "任务已经停止，您的物品未取出";
        }
        // 针对 小车被障碍物挡住，又不能自己避障，或者 地图 http失败
        if (TextUtils.equals(heart.znxyNextState,"未知地点，请检查")) {
            return "未知地点，请检查";
        }
        if (TextUtils.equals(heart.znxyNextState,"目标点不可到达，请检查")) {
            return "目标点不可到达，请检查";
        }
        if (TextUtils.equals(heart.znxyNextState,"网络请求失败，请检查")) {
            return "网络请求失败，请检查";
        }
        if (heart.znxyState == RobotZnxyState.TASK_PAUSE) {
            return "协运任务已暂停";
        }
        if (!TextUtils.isEmpty(heart.znxyNextState)) {
            return heart.znxyNextState;
        }
        if (heart.znxyState == RobotZnxyState.TASK_EXECUTING) {
            return "正在执行协运任务";
        }
        return robotState;
    }


    /**--------------------------------------------------------------------------------------------*/

    /**
     * 第一种条件:  运动状态前判断
     * 1. 判断是否在线
     * 2. 判断是否急停
     * 3. 判断是否有加密狗
     */
    private static RobotStateHint getFirstCondition(Heart heart) {
        RobotStateHint online = isOnline(heart);
        if (null != online) {
            return online;
        }
        RobotStateHint stop = isStop(heart);
        if (null != stop) {
            return stop;
        }
        return hasUsb(heart);
    }

    /**
     * 第二种条件
     * 判断是否初始化成功(未成功的情况下不允许除了充电的其它运动操作)
     */
    private static RobotStateHint getSecondCondition(Heart heart) {
        RobotStateHint firstCondition = getFirstCondition(heart);
        if (null != firstCondition) {
            return firstCondition;
        }
        return isInitSuccess(heart);
    }

    /**
     * 一. 分两种提示
     * 1. 文字提示  --------> Toast
     * 2. 文字提示 + 弹窗 --------> DialogFragment  包含是否,说明有缓冲的余地
     * <p>
     * 统一返回RobotStateHint
     */

    private static final RobotStateHint robotStateHint = new RobotStateHint();

    private static RobotStateHint getHint(String msg) {
        return getHint(false, msg, RobotCmdType.CMD_NONE);
    }

    private static RobotStateHint getHint(boolean isDialog, String msg, String firstCmd) {
        return getHint(isDialog, msg, firstCmd, RobotCmdType.CMD_NONE);
    }

    private static RobotStateHint getHint(boolean isDialog, String msg, String firstCmd, String secondCmd) {
        return getHint(isDialog,msg,firstCmd,secondCmd,false);
    }

    private static RobotStateHint getHint(boolean isDialog, String msg, String firstCmd, String secondCmd,boolean cmdSync) {
        if (isDialog && RobotCmdType.CMD_NONE.equals(firstCmd)) {
            Log.e("ZJJHINT", "请检查cmdType参数");
        }
        robotStateHint.isDialog = isDialog;
        robotStateHint.msg = msg;
        robotStateHint.firstCmd = firstCmd;
        robotStateHint.secondCmd = secondCmd;
        robotStateHint.cmdSync = cmdSync;
        return robotStateHint;
    }

    // 1. 判断是否在线
    private static RobotStateHint isOnline(Heart heart) {
        if (!heart.isLine) {
            return getHint("您选择的机器人已离线,请先启动机器人");
        }
        return null;
    }

    // 2. 判断是否急停
    private static RobotStateHint isStop(Heart heart) {
        if (RobotStopState.STOP == heart.stop) {
            return getHint("急停按钮已被按下,请先手动松开急停按钮");
        }
        return null;
    }

    // 3. 判断是否有加密狗
    private static RobotStateHint hasUsb(Heart heart) {
        if (!heart.hasUsb) {
            return getHint("请检查机器人加密狗设备,并重启机器人");
        }
        return null;
    }

    // 4. 判断是否初始化
    private static RobotStateHint isInit(Heart heart) {
        if (RobotInitState.EMPTY == heart.initState) {
            return getHint("机器人未初始化,请先初始化地图");
        }
        return null;
    }

    // 判断是否在前往充电
    private static RobotStateHint isGoCharge(Heart heart, String secondCmd) {
        if (RobotChargeState.NAV == heart.chargeState) {
            // 正在前往充电
            return getHint(true, "正在前往充电中,是否停止充电?", RobotCmdType.CMD_STOP_CHARGE, secondCmd);
        }
        return null;
    }

    // 判断是否在充电
    private static RobotStateHint isCharging(Heart heart, String secondCmd) {
        if (RobotChargeState.ING == heart.chargeState || RobotChargeState.FULL == heart.chargeState) {
            if (heart.bat <= 5) {
                return getHint("机器人电量低于5%,无法退出充电");
            }
            return getHint(true, "正在充电中,是否停止充电?", RobotCmdType.CMD_STOP_CHARGE, secondCmd);
        }
        return null;
    }

    // 5. 判断是否初始化成功
    private static RobotStateHint isInitSuccess(Heart heart) {
        if (RobotInitState.SUCCESS != heart.initState) {
            return getHint("机器人初始化未成功,请重新初始化地图");
        }
        return null;
    }

    // 6. 判断是否在执行任务
    private static RobotStateHint isTasking(Heart heart,String secondCmd) {
        if (RobotTaskState.ING == heart.taskState) {
            return getHint(true, "机器人正在执行任务,是否停止当前任务?", RobotCmdType.CMD_STOP_TASK,secondCmd);
        }
        return null;
    }

    // 7. 判断是否在执行消毒作业
    private static RobotStateHint isXdzy(Heart heart,String secondCmd) {
        if (0 == heart.xyMode) {
            return null;
        }
        RobotStateHint doorClose = isDoorClose(heart);
        if (null != doorClose) {
            return doorClose;
        }
        if (!TextUtils.isEmpty(heart.runningId)) {
            return getHint(true, "机器人正在执行消毒作业,是否停止消毒作业?", RobotCmdType.CMD_STOP_XDZY,secondCmd);
        }
        return null;
    }

    // 判断水量低
    private static RobotStateHint isWaterLow(Heart heart) {
        if (0 == heart.xyMode) {
            return null;
        }
        if (20 > heart.waterValue) {
            return getHint("机器人水量低,请先加水再执行消毒作业");
        }
        return null;
    }

    // 判断电量低
    private static RobotStateHint isBatLow(Heart heart) {
        if (20 > heart.bat) {
            return getHint("机器人电量低,请先充电再执行操作");
        }
        return null;
    }

    // 8. 判断是否在加水
    private static RobotStateHint isWater(Heart heart, String secondCmd) {
        if (0 == heart.xyMode) {
            return null;
        }
        if (RobotWaterState.NAV == heart.waterState) {
            return getHint(true, "机器人正在前往加水,是否停止导航到加水点?", RobotCmdType.CMD_STOP_NAV, secondCmd);
        }
        if (RobotWaterState.ING == heart.waterState) {
            return getHint("机器人正在加水,请等待");
        }
        return null;
    }

    // 9. 判断是否在加药
    private static RobotStateHint isDrug(Heart heart) {
        if (0 == heart.xyMode) {
            return null;
        }
        if (RobotDrugState.ING == heart.drugState) {
            return getHint("机器人正在加药,请等待");
        }
        return null;
    }

    // 10. 判断是否关门
    private static RobotStateHint isDoorClose(Heart heart) {
        if ("2".equals(heart.type) || "3".equals(heart.type) ) {
            if (RobotDoorState.OPEN_ING == heart.doorState ||
                    RobotDoorState.CLOSE_ING == heart.doorState ||
                    RobotDoorState.OPEND == heart.doorState) {
                return getHint("机器人舱门未关,请先关闭舱门");
            }
        }
        return null;
    }

    // 11. 判断是否在执行智能协运作业
    private static RobotStateHint isZnxyRunning(Heart heart,String secondCmd) {
        if (!"2".equals(heart.type)) {
            return null;
        }
        RobotStateHint doorHint = isDoorClose(heart);
        if (doorHint != null) {
            return doorHint;
        }
        if (heart.znxyState == RobotZnxyState.TASK_EXECUTING || heart.znxyState == RobotZnxyState.TASK_PAUSE) {
            return getHint(true, "机器人正在执行协运任务,是否停止当前任务？", RobotCmdType.CMD_STOP_ZNXY,secondCmd);
        }
        return null;
    }

    // 12. 判断是否在执行导航
    private static RobotStateHint isNaving(Heart heart,String secondCmd) {
        if (RobotNavState.ING == heart.navState) {
            return getHint(true, "正在导航中,是否停止导航?", RobotCmdType.CMD_STOP_NAV,secondCmd);
        }
        return null;
    }

    //判断是否在智能跟随状态
    public static boolean isFollow(Heart heart) {
        if(heart==null) return false;
        if(RobotNavState.FOLLOW == heart.navState){
            return true;
        }
        return false;
    }


    private static RobotStateHint isChargingXy(Heart heart, String secondCmd) {
        if (RobotChargeState.ING == heart.chargeState || RobotChargeState.FULL == heart.chargeState
                 || RobotChargeState.NAV == heart.chargeState) {
            if (heart.bat < 20) {
                return getHint("机器人电量低于20%,无法退出充电");
            }
            return getHint(true, "正在充电中,是否停止充电?", RobotCmdType.CMD_STOP_CHARGE, secondCmd);
        }
        return null;
    }
}