package com.maxvision.tech.robot.face;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.maxvision.tech.mqtt.MQTTManager;
import com.maxvision.tech.robot.AppHolder;
import com.maxvision.tech.robot.utils.ImageEngineUtils;
import com.maxvision.tech.robot.R;
import com.maxvision.tech.robot.utils.CustomToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.maxvision.tech.robot.utils.CustomToast.TIP_ERROR;
import static com.maxvision.tech.robot.utils.CustomToast.TIP_SUCCESS;

/**
 * Created by yuhongwen
 * on 2021/4/6
 * 人脸上传
 */
public class FaceUploadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int UPLOAD_SUCCESS = 1;
    private ImageView uoloadUserImage;
    private TextView tvListType;
    private EditText etListName;
    private TextView tvListSex;
    private EditText etListCardNumber;
    private Button uploadInfoBtn;
    private List<LocalMedia> selectList = new ArrayList<>();
    private String mSelectpath;
    private final String[] faceTypeArray = {"白名单","黑名单","员工名单"};
    private final String[] faceSexArray = {"男","女"};
    private int faceType = 1; // 默认类型
    private int faceSex = 1; // 默认性别
    private String faceId;
    private Disposable timeOutDispose;
    private BuildBean mLoadingDialog;
    private ImageView back;
    private String robotSn;
    private long lastClickTime = 0;
    private boolean isShowing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_upload_activity);
        uoloadUserImage = findViewById(R.id.ic_user_image);
        tvListType = findViewById(R.id.tv_list_type);
        etListName = findViewById(R.id.et_list_name);
        tvListSex = findViewById(R.id.tv_list_sex);
        etListCardNumber = findViewById(R.id.et_list_card_number);
        uploadInfoBtn = findViewById(R.id.but_list_updata);
        back = findViewById(R.id.face_upload_back);
        Intent intent = getIntent();
        // 当前操作机器人sn
        robotSn = intent.getStringExtra("RobotSn");
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String onMessageFace) {
        hindLoading();
        if (TextUtils.isEmpty(onMessageFace)) return;
        if (timeOutDispose != null) {
            timeOutDispose.dispose();
        }
        JSONObject jsonObject = JSONObject.parseObject(onMessageFace);
        int faceResult = (int) jsonObject.get("faceResult");
        String onRobotSn = (String) jsonObject.get("robotSn");
        Log.i("yhw_face","onMessageFace = " + faceResult + "--onRobotSn = " + onRobotSn);
        if (TextUtils.equals(onRobotSn,robotSn)) {
            if (faceResult == UPLOAD_SUCCESS) {
                CustomToast.toastLong(TIP_SUCCESS, "上传成功");
            } else  {
                CustomToast.toastLong(TIP_ERROR, "上传失败");
            }
        }
    }

    private void initListener() {
        uoloadUserImage.setOnClickListener(this);
        tvListType.setOnClickListener(this);
        etListName.setOnClickListener(this);
        tvListSex.setOnClickListener(this);
        etListCardNumber.setOnClickListener(this);
        uploadInfoBtn.setOnClickListener(this);
        back.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ic_user_image) {
            // 选择上传的图片
            openPhotos();
        } else if (id == R.id.tv_list_type) {
            // 名单类型
            showTypeDialog();
        } else if (id == R.id.tv_list_sex) {
            // 名单性别
            showSexDialog();
        } else if (id == R.id.but_list_updata) {
            // 提交人脸信息
            uploadFaceInfo();
        } else if (id == R.id.face_upload_back) {
            finish();
        }
    }

    private void showSexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SelectDialog);
        builder.setSingleChoiceItems(faceSexArray,0, (dialog, which) -> {
            faceSex = which + 1;
            refreshFaceSex();
            dialog.dismiss();
        });
        builder.setTitle("");
        builder.setNegativeButton("关闭", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void refreshFaceSex() {
        tvListSex.setText(faceSexArray[faceSex-1]);
    }

    private void showTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.SelectDialog);
        builder.setSingleChoiceItems(faceTypeArray,0, (dialog, which) -> {
            faceType = which + 1;
            refreshFaceType();
            dialog.dismiss();
        });
        builder.setNegativeButton("关闭", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void refreshFaceType() {
        tvListType.setText(faceTypeArray[faceType-1]);
    }

    private void uploadFaceInfo() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 500) {
            lastClickTime = now;
            Log.e("yhw_face", "user click too quick");
            return;
        }
        lastClickTime = now;
        if (isShowing) {
            Log.e("yhw_face", "user 正在提交");
            return;
        }
        String name = etListName.getText().toString().trim();
        String cardNumber = etListCardNumber.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(cardNumber)) {
            Toast.makeText(this,R.string.please_input_info,Toast.LENGTH_SHORT).show();
            return;
        }
        if (mSelectpath == null){
            Toast.makeText(this,R.string.please_upload_image,Toast.LENGTH_SHORT).show();
            return;
        }
        showRequestDialog();
        long time = System.currentTimeMillis();
        if (faceType == 1) {
            faceId = "w" + time;
        } else if (faceType == 2) {
            faceId = "b" + time;
        } else if (faceType == 3) {
            faceId = "y" + time;
        } else {
            faceId = "w" + time;
        }
        //上传图片
        FaceEntity listUploadBean = new FaceEntity(name, faceSex,cardNumber,faceType,
                ImageEngineUtils.createImageEngine().imageToBase64Sync(mSelectpath,this),faceId, MQTTManager.PAD_SN);
        Log.e("yhw_face","开始上传人脸");
        timeOutDispose = Observable.timer(15, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    hindLoading();
                    Log.e("yhw_face","开始上传人脸超时");
                    Toast.makeText(this,R.string.face_upload_fail,Toast.LENGTH_SHORT).show();
                    timeOutDispose.dispose();
                });
        AppHolder.getInstance().getMqtt().getUploadFace(robotSn, new Gson().toJson(listUploadBean));
    }

    private void showRequestDialog() {
        mLoadingDialog = DialogUIUtils.showLoading(
                this,getString(R.string.loading), true, true, true, true);
        mLoadingDialog.show();
        isShowing = true;
    }

    private void hindLoading() {
        DialogUIUtils.dismiss(mLoadingDialog);
        isShowing = false;
    }

    private void openPhotos(){
        PictureSelector.create(FaceUploadActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.picture_default_style)
                .maxSelectNum(1)
                .minSelectNum(0)
                .imageSpanCount(4)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)
                .previewVideo(false)
                .isCamera(true)
                .imageFormat(PictureMimeType.PNG)
                .isZoomAnim(true)
                .sizeMultiplier(0.5f)
                .setOutputCameraPath("/CustomPath")
                .enableCrop(true)
                .compress(false)
                .glideOverride(150, 150)
                .withAspectRatio(3, 4)
                .hideBottomControls(true)
                .selectionMedia(selectList)
                .isGif(false)
                .openClickSound(true)
                .previewEggs(true)
                .minimumCompressSize(100)
                .loadImageEngine(PictureSelectGlideEngine.createGlideEngine())
                .synOrAsy(true)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PictureConfig.CHOOSE_REQUEST){
            List<LocalMedia> mSelectImage = PictureSelector.obtainMultipleResult(data);
            if (mSelectImage != null && mSelectImage.size() >0) {
                mSelectpath = mSelectImage.get(0).getCutPath();
                uoloadUserImage.setVisibility(View.VISIBLE);
                ImageEngineUtils.createImageEngine().loadLocalImage(this,mSelectpath,uoloadUserImage);
            }
        }
    }

    public static void startActivity(Context context,String sn) {
        Intent intent = new Intent(context, FaceUploadActivity.class);
        intent.putExtra("RobotSn",sn);
        context.startActivity(intent);
    }
}
