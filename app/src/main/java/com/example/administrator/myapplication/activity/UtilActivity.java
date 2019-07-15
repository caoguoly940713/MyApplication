package com.example.administrator.myapplication.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.utils.Utils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;

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
    private String tag;
    private String src;
    //视频时长
    private int duration;

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
    private long select;

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

            String msg = "将剪切视频内容";
            builder.setMessage(msg);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.compress(mContext, src, tag, 0, message, duration);
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

                duration = (int) Utils.getVideoInfo(mContext, data).get("duration");
                src = FileUtils.getPath(mContext, data.getData());
                tag = Environment.getExternalStorageDirectory() + "/1/";

                super.onActivityResult(requestCode, resultCode, data);
            }

            if (requestCode == 2) {
                src = FileUtils.getPath(mContext, data.getData());
                String name = "new_" + new File(src).getName();
                String target = Environment.getExternalStorageDirectory() + "/1/" + name;
                //传入目标文件uri，和源文件uri
                UCrop uCrop = UCrop.of(data.getData(), Uri.parse(target));
                //使用默认设置跳转到裁剪页
                uCrop.withAspectRatio(1, 1);

                uCrop.start(this, 3);
            }
        }
        if (requestCode == 3) {
            String name = "new_" + new File(src).getName();
            String target = Environment.getExternalStorageDirectory() + "/1/" + name;
            //从目标文件生成Bitmap
            Bitmap bm = BitmapFactory.decodeFile(target);
            crop.setImageBitmap(bm);
        }
    }
}
