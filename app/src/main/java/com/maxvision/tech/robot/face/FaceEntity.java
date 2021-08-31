package com.maxvision.tech.robot.face;

/**
 * 人脸上传
 */
public class FaceEntity {

    private String faceName;

    /**
     * 男 1  女 2
     */
    private int faceSex;
    private String faceCardNum;
    /**
     *  选择名单类型 1是白名单，2是黑名单，3是员工名单
     */
    private int faceType;
    private String faceImage;


    //平板序列号
    public String padSn;
    //人脸ID
    public String faceId;

    public FaceEntity(String name, int sex, String cardNumber, int listType, String image,String faceId,String padSn) {
        this.faceName = name;
        this.faceSex = sex;
        this.faceCardNum = cardNumber;
        this.faceType = listType;
        this.faceImage = image;
        this.faceId = faceId;
        this.padSn = padSn;
    }

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

    public String getFaceCardNum() {
        return faceCardNum;
    }

    public void setFaceCardNum(String faceCardNum) {
        this.faceCardNum = faceCardNum;
    }

    public int getFaceSex() {
        return faceSex;
    }

    public void setFaceSex(int faceSex) {
        this.faceSex = faceSex;
    }

    public int getFaceType() {
        return faceType;
    }

    public void setFaceType(int faceType) {
        this.faceType = faceType;
    }

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public String getPadSn() {
        return padSn;
    }

    public void setPadSn(String padSn) {
        this.padSn = padSn;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }
}
