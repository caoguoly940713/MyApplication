package com.example.administrator.myapplication.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 工具页面
 * 1.包含一个裁剪头像带控件
 * 2.包含一个获取视频关键帧及相关信息的控件
 * 3.包含一个视频转码控件
 */
public class UtilActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Context mContext;
    //开始转码的时间
    private long start;
    //转码结束的时间
    private long end;
    private String targetPath;
    private String sourcePath;
    //视频时长
    private int duration;
    private MediaMetadataRetriever retriever;
    //开始剪切的时间
    private long select;

    //视频分辨率
    private int width;
    private int height;

    @BindView(R.id.video)
    TextView video;
    @BindView(R.id.portrait)
    TextView portrait;
    @BindView(R.id.control)
    SeekBar control;
    @BindView(R.id.submit)
    TextView submit;
    @BindView(R.id.thumbnail)
    ImageView thumbnail;
    @BindView(R.id.label)
    TextView label;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.crop)
    ImageView crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_util);
        ButterKnife.bind(this);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        label.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        label.setVisibility(View.GONE);
        info.setVisibility(View.GONE);
    }

    private void initData() {
        mContext = this;
        retriever = new MediaMetadataRetriever();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("提示");
            builder.setMessage("应用需要通讯录，定位，相机权限方可运行，是否跳转至设置?");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
            builder.create().show();
        }
    }

    //将开始的时间转换为ffmpeg可用的参数
    public List<String> secondToTime(long seconds) {
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

    //转码的实际操作
    private void compress(String src, String tag, long startTime, String resolution) {
        FFmpeg fFmpeg = FFmpeg.getInstance(mContext);
        List<String> times = secondToTime(startTime);
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
                "-s", resolution,
                tag
        };

        try {
            fFmpeg.execute(commands, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String msg) {
                    //记录转码结束的时间，单位毫秒
                    end = System.currentTimeMillis();
                    String time = "转码用时：" + (end - start) / 1000 + "秒\n\n";
                    long oldSize = new File(sourcePath).length();
                    long newSize = new File(targetPath).length();
                    String srcfile = "源文件路径：" + sourcePath + "\n\n";
                    String tagfile = "目标文件路径：" + targetPath + "\n\n";

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

    private void initListener() {
        video.setOnClickListener(this);
        submit.setOnClickListener(this);
        portrait.setOnClickListener(this);

        //拖动进度条的会生成对应的预览图和时间
        control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (duration != 0) {
                    //进度条的长度要转换成对应的视频时长
                    select = Math.round(seekBar.getProgress() * 1.0 / 100 * duration);
                    label.setText("当前位置：" + select + "秒");
                    long selectFrame = Math.round(select * 1000000);
                    //获取关键帧的截图，注意单位是毫秒
                    thumbnail.setImageBitmap(retriever.getFrameAtTime(selectFrame));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        //使用Android原生的文件浏览器选择视频文件
        if (v.getId() == R.id.video) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 1);
        }

        //使用Android原生的文件浏览器选择图片文件
        if (v.getId() == R.id.portrait) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 2);
        }

        //点击开始转码弹出提示
        if (v.getId() == R.id.submit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("提示");

            String msg = "将剪切" + select + "秒到" + (select + 10) + "秒的内容，分辨率设置为原视频的0.8倍";
            builder.setMessage(msg);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //将原视频的分辨率设置为0.8倍
                    String reso = Math.round(0.8) * width + "*" + Math.round(0.8 * height);
                    compress(sourcePath, targetPath, select, reso);
                    message.setVisibility(View.VISIBLE);
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //选择视频文件之后获取视频文件的相关信息
        //并把进度条和文本框设为可见
        //防止在选择文件的时候退出，增加data空值判断
        if (data != null) {
            if (requestCode == 1) {
                submit.setVisibility(View.VISIBLE);
                control.setVisibility(View.VISIBLE);
                label.setVisibility(View.VISIBLE);
                info.setVisibility(View.VISIBLE);
                try {
                    //使用MediaPlayer获取视频的时长
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(mContext, data.getData());
                    mediaPlayer.prepare();
                    duration = mediaPlayer.getDuration() / 1000;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //使用第三方框架Ucrop的FileUtils工具类将uri转换为真实路径
                sourcePath = FileUtils.getPath(mContext, data.getData());
                String name = "new_" + new File(sourcePath).getName();
                targetPath = Environment.getExternalStorageDirectory() + "/1/" + name;

                //初始化MediaMetadataRetriever并生成默认预览图
                retriever.setDataSource(sourcePath);
                Bitmap bm = retriever.getFrameAtTime(0);
                thumbnail.setImageBitmap(bm);

                width = bm.getWidth();
                height = bm.getHeight();
                String infoStr = "分辨率：" + width + "*" + height + "    视频长度：" + duration + "秒";
                info.setText(infoStr);

                super.onActivityResult(requestCode, resultCode, data);
            }

            if (requestCode == 2) {
                sourcePath = FileUtils.getPath(mContext, data.getData());
                String name = "new_" + new File(sourcePath).getName();
                String target = Environment.getExternalStorageDirectory() + "/1/" + name;
                //传入目标文件uri，和源文件uri
                UCrop uCrop = UCrop.of(data.getData(), Uri.parse(target));
                //使用默认设置跳转到裁剪页
                uCrop.withAspectRatio(1,1);

                uCrop.start(this, 3);
            }
        }
        if (requestCode == 3) {
            String name = "new_" + new File(sourcePath).getName();
            String target = Environment.getExternalStorageDirectory() + "/1/" + name;
            //从目标文件生成Bitmap
            Bitmap bm = BitmapFactory.decodeFile(target);
            crop.setImageBitmap(bm);
        }
    }
}
