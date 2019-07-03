package com.example.administrator.myapplication;

import android.app.Application;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.example.administrator.myapplication.service.HeartBeatService;
import com.example.administrator.myapplication.service.PushTestService;
import com.example.administrator.myapplication.service.ReceiveIntentService;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.igexin.sdk.PushManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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
