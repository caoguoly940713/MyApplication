package com.example.administrator.myapplication.service;

import android.content.Context;
import android.util.Log;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

public class ReceiveIntentService extends GTIntentService {
    @Override
    public void onReceiveServicePid(Context context, int i) {
        Log.e("MyApplication", "onReceiveServicePid");
    }

    @Override
    public void onReceiveClientId(Context context, String s) {
        Log.e("MyApplication", "onReceiveClientId: "+ s);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        Log.e("MyApplication", "onReceiveMessageData");
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        Log.e("MyApplication", "onReceiveOnlineState");
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.e("MyApplication", "onReceiveCommandResult");
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.e("MyApplication", "onNotificationMessageArrived");
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.e("MyApplication", "onNotificationMessageClicked");
    }
}
