package com.example.administrator.myapplication;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.example.administrator.myapplication.service.PushTestService;
import com.example.administrator.myapplication.service.ReceiveIntentService;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.igexin.sdk.PushManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this, "5d2452304ca3575579000c8d", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "45fc1e7296124e9b8c79d8b4953fe697");

        PushAgent.getInstance(this).setResourcePackageName("com.example.administrator.myapplication");
        PushAgent.getInstance(this).register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.i("MyApplication","注册成功：deviceToken：" + deviceToken);
            }
            @Override
            public void onFailure(String s, String s1) {
                Log.e("MyApplication","注册失败：" + "s:" + s + ",s1:" + s1);
            }
        });

        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.loadBinary(null);
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

        MultiDex.install(this);
        PushManager.getInstance().initialize(this, PushTestService.class);
        PushManager.getInstance().registerPushIntentService(this, ReceiveIntentService.class);
    }
}
