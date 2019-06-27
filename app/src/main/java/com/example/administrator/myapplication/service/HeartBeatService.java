package com.example.administrator.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 心跳机制，定时给服务器发送请求
 */
public class HeartBeatService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
