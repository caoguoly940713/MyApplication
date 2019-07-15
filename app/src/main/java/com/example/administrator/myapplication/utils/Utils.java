package com.example.administrator.myapplication.utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Utils {

    private static final String TAG = "Utils";

    //转码的实际操作
    public static void compress(Context context, final String src, final String tag, long startTime, final TextView message, long duration) {

        List<String> times = secondToTime(startTime, duration);
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        Log.e(TAG, times.toString());

        //ffmpeg需要的命令参数
        String[] commands = new String[]{
                "-threads", "10",
                "-i", src,
                "-c:v", "libx264",
                "-crf", "30",
                "-preset", "superfast",
                "-y",
                "-acodec", "libmp3lame",
                "-ss", times.get(0),
                "-to", times.get(1),
                tag
        };

        try {
            fFmpeg.execute(commands, new ExecuteBinaryResponseHandler() {
                private long end;
                private long start;

                @Override
                public void onSuccess(String msg) {
                    //记录转码结束的时间，单位毫秒
                    end = System.currentTimeMillis();
                    String time = "转码用时：" + (end - start) / 1000 + "秒\n\n";
                    long oldSize = new File(src).length();
                    long newSize = new File(tag).length();
                    String srcfile = "源文件路径：" + src + "\n\n";
                    String tagfile = "目标文件路径：" + tag + "\n\n";

                    double percent = (newSize * 1.0 / oldSize) * 100;
                    String percentStr = "压缩比：百分之" + Math.round(percent);

                    message.setText(time + srcfile + tagfile + percentStr);
                }

                @Override
                public void onProgress(String msg) {
                    message.setText(msg);
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "onFailure:" + message);
                }

                @Override
                public void onStart() {
                    //记录开始转码的时间，单位毫秒
                    start = System.currentTimeMillis();
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    //将开始的时间转换为ffmpeg可用的参数,默认转换10s
    private static List<String> secondToTime(long seconds, long duration) {
        List<String> times = new ArrayList<>();
        //如果所选时间大于3600秒则从0开始剪切
        if (seconds < 10) {
            times.add("00:00:00");
            String endTime = "00:00:" + duration;
            times.add(endTime);
        }
        if (seconds > 3600) {
            times.add("00:00:00");
            times.add("00:00:10");
        } else {
            long startMinute = seconds / 60;
            long startSecond = seconds % 60;
            String startTime = "00:" + startMinute + ":" + startSecond;
            times.add(startTime);

            long endMinute = (seconds + 10) / 60;
            long endSecond = (seconds + 10) % 60;
            String endTime = "00:" + endMinute + ":" + endSecond;
            times.add(endTime);
        }
        return times;
    }

    public static Bitmap getVideoCapture(Context context, Intent data, long time) {
        try {
            //使用MediaPlayer获取视频的时长
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, data.getData());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化MediaMetadataRetriever并生成默认预览图
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, data.getData());
        return retriever.getFrameAtTime(time);
    }


    public static Map<String, Object> getVideoInfo(Context context, Intent data) {
        Map<String, Object> map = new ArrayMap<>();
        long duration = 0;
        int width = 0, height = 0;
        try {
            //使用MediaPlayer获取视频的时长
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, data.getData());
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
            width = mediaPlayer.getVideoWidth();
            height = mediaPlayer.getVideoHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.put("duration", duration);
        map.put("width", width);
        map.put("height", height);
        return map;
    }

    public static void checkPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示");
            builder.setMessage("应用需要通讯录，定位，相机权限方可运行，是否跳转至设置?");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(context, "取消", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            });
            builder.create().show();
        }
    }

    public static void update(final Context context) {
        String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("download", "下载进度", IMPORTANCE_DEFAULT);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);

            //初始化通知ui
            final Notification.Builder nb = new Notification.Builder(context, "download");
            nb.setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("下载中");
            nb.setProgress(100, 0, false).setAutoCancel(true);

            final NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            //创建通道，注意这一步很重要
            manager.createNotificationChannel(channel);

            //使用okhttp发送get请求
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(url).get().build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    InputStream inputStream = response.body().byteStream();
                    File espd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
                    //缓存的路径以及文件名
                    String apkPath = espd.getPath() + "/" + "BLiao" + System.currentTimeMillis() + ".apk";
                    FileOutputStream fos = new FileOutputStream(new File(apkPath));

                    //目标文件的总大小
                    long contentLength = response.body().contentLength();

                    int length = 0;
                    //字节数组缓冲区要设置为足够大，过小会导致io速度慢
                    byte[] bytes = new byte[10240];
                    while ((length = inputStream.read(bytes)) != -1) {
                        fos.write(bytes, 0, length);

                        //已经写入的文件内容所占百分数
                        double percent = fos.getChannel().position() * 100.0 / contentLength;
                        //手动刷新进度条
                        nb.setProgress(100, (int) percent, false);
                        manager.notify(1, nb.build());
                    }

                    fos.flush();
                    fos.close();
                    response.body().close();
                    //下载完毕关闭要隐藏通知
                    manager.cancel(1);

                    //调出系统安装应用页面
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //fileprovider要在清单文件中声明
                    Uri contentUri = FileProvider.getUriForFile(context, "com.caikeng.app.fileProvider", new File(apkPath));
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    context.startActivity(intent);
                }
            });
        }
    }
}
