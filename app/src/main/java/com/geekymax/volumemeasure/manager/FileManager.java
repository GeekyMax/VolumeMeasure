package com.geekymax.volumemeasure.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.geekymax.volumemeasure.entity.MyPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

public class FileManager {
    private static final String TAG = "Geeky-FileManager";
    private static final String imageFilePath = "/sdcard/";

    private FileManager instance;

    public FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    private FileManager() {
    }

    public void outputPoint(Map<Integer, MyPoint> map) {
        String fileName = "point.csv";
        clearFile(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        map.forEach((id, point) -> {
            stringBuilder.append("" + id + ", " + point.x + ", " + point.y + ", " + point.z + ", " + point.confidence + "\n");
        });
        stringBuilder.append("\n");
        writeTxtToFile(fileName, stringBuilder.toString());
    }


    public static void outputBitmap(String id, Bitmap bitmap) {
        File file = new File(imageFilePath + id + ".jpg");
        Log.d("TestFile", "start output bitmap");

        try {
//            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + file.getAbsolutePath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    public static Bitmap loadBitmap(String id) {
        File file = new File(imageFilePath + id + ".jpg");
        Log.d("TestFile", "start output bitmap");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            return bitmap;
        } else {
            return null;
        }
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String fileName, String strcontent) {
        //生成文件夹之后，再生成文件，不然会出错
//        makeFilePath(imageFilePath, fileName);
        File file = newFile(fileName);
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
//            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + file.getAbsolutePath());
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    private void clearFile(String fileName) {
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
            Log.d(TAG, "file.exists():" + file.exists() + " file.getAbsolutePath():" + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            Log.d(TAG, "SD卡目录下创建文件成功...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File newFile(String fileName) {
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
            Log.d(TAG, "file.exists():" + file.exists() + " file.getAbsolutePath():" + file.getAbsolutePath());
            if (file.exists()) {
//                file.delete();
            } else {
                file.createNewFile();
            }

            Log.d(TAG, "SD卡目录下创建文件成功...");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    /**
     * Bitmap转换成byte[]并且进行压缩,压缩到不大于maxkb
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap, int maxkb) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb * 1024 && options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        // 传值 byte 大于200*1024就会报错
        if (options == 10 && output.toByteArray().length > 200000) {
            while (output.toByteArray().length > 200000&& options != 1) {
                output.reset(); //清空output
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
                options -= 1;
            }
        }
        return output.toByteArray();
    }
}
