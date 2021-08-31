package com.maxvision.tech.robot.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.maxvision.tech.robot.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import top.zibin.luban.Luban;

/**
 * Created by yuhongwen
 * on 2021/4/7
 * 图片加载框架
 */
public class ImageEngineUtils {
    private ImageEngineUtils() {
    }
    private static ImageEngineUtils instance;

    public static ImageEngineUtils createImageEngine() {
        if (null == instance) {
            synchronized (ImageEngineUtils.class) {
                if (null == instance) {
                    instance = new ImageEngineUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 加载人脸上传本地图片
     * @param context 上下文
     * @param url 图片地址
     * @param imageView 图片控件
     */
    public void loadLocalImage(@NonNull Context context, String url, ImageView imageView){
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .bitmapTransform(new RoundedCorners(10))
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public String imageToBase64Sync(String srcPath,Context context){
        if(TextUtils.isEmpty(srcPath)){
            return null;
        }
        // 将图片原样压缩
        if (!TextUtils.isEmpty(getPicture(srcPath))) {
            compressImageForSync(context, srcPath, srcPath);
            return imageToBase64ForCompress(srcPath);
        }
        return imageToBase64ForCompress(srcPath);
    }


    /**
     * 同步压缩图片
     * @param appContext
     * @param srcPath
     * @param destImage
     * @return
     */
    public static List<File> compressImageForSync(Context appContext, String srcPath, String destImage) {
        String reFileName = "";
        if (!TextUtils.isEmpty(srcPath)) {
            String[] imagePaths = srcPath.split(destImage);
            if (imagePaths != null && imagePaths.length >= 2) {
                reFileName = imagePaths[1];
                if (reFileName.startsWith("/")) {
                    // 删掉 "/"
                    reFileName = reFileName.substring(1);
                }
            }
        }
        final String finalReFileName = reFileName;
        try {
            return Luban.with(appContext)
                    .load(srcPath)
                    .ignoreBy(100)
                    .setRenameListener(filePath -> finalReFileName)
                    .setTargetDir(destImage)
                    .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"))).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String imageToBase64ForCompress(String srcPath) {
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(srcPath);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.NO_CLOSE);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    // 现有的算法只能压缩图片
    private static String getPicture(String strPath) {
        File file = new File(strPath);
        if (file.isFile()) {
            int idx = file.getPath().lastIndexOf(".");
            if (idx <= 0) {
                return "";
            }
            String suffix = file.getPath().substring(idx);
            if (suffix.toLowerCase().equals(".jpg") ||
                    suffix.toLowerCase().equals(".jpeg") ||
                    suffix.toLowerCase().equals(".bmp") ||
                    suffix.toLowerCase().equals(".png") ||
                    suffix.toLowerCase().equals(".gif")) {
                return file.getPath();
            }
        }
        return "";
    }

    public void loadImage(Context context,String imagePath,ImageView iv) {
        Glide.with(context)
                .load(imagePath)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.default_image)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(iv);
    }

    public boolean decodeImage(String sourceImagePic,String filePath) {
        if (sourceImagePic == null) {
            return false;
        }
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            byte[] decodeByte = Base64.decode(sourceImagePic,Base64.NO_CLOSE);
            File photoFile = new File(filePath);
            photoFile.createNewFile();
            outputStream = new FileOutputStream(photoFile);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(decodeByte);
            bufferedOutputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return false;
    }
}
