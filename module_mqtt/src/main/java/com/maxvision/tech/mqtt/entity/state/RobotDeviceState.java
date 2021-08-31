package com.maxvision.tech.mqtt.entity.state;

/**
 * name: wy
 * date: 2021/4/1
 * desc: 设备状态
 * -1：不显示
 * 0：正常
 * 1：异常
 */
public class RobotDeviceState {

    //网络摄像头
    public int xmCamear = 0;
    //USB摄像头
    public int usbCamear = 0;
    //深度摄像头
    public int sdCamear = 0;
    //红外摄像头
    public int hwCamear = 0;
    //TX2链接状态
    public int tx2Camear = 0;
    //雷达
    public int leiDa = 0;
    //访问外网状态
    public int wifi = 0;
    //访问外网状态
    public int server = 0;
    //打印机设备链接
    public int printer = 0;
    //OCR设备链接
    public int ocr = 0;
    //扫描枪设备链接
    public int scan = 0;


    @Override
    public String toString() {
        return "RobotDeviceState{" +
                "xmCamear=" + xmCamear +
                ", usbCamear=" + usbCamear +
                ", sdCamear=" + sdCamear +
                ", hwCamear=" + hwCamear +
                ", tx2Camear=" + tx2Camear +
                ", leiDa=" + leiDa +
                ", wifi=" + wifi +
                ", server=" + server +
                ", printer=" + printer +
                ", ocr=" + ocr +
                ", scan=" + scan +
                '}';
    }
}