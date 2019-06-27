package com.example.administrator.myapplication.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.activity.ChatActivity;

public class NotificationUtil {
    private Context context;
    private NotificationManager notificationManager;

    public NotificationUtil(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 普通的Notification
     */
    public void postNotification(String content) {
        Notification.Builder builder = new Notification.Builder(context);
        Intent intent = new Intent(context, ChatActivity.class);  //需要跳转指定的页面
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);// 设置图标
        builder.setContentTitle("新的消息");// 设置通知的标题
        builder.setContentText(content);// 设置通知的内容
        builder.setWhen(System.currentTimeMillis());// 设置通知来到的时间
        builder.setAutoCancel(true); //自己维护通知的消失
        builder.setTicker("new message");// 第一次提示消失的时候显示在通知栏上的
        builder.setOngoing(true);
        builder.setNumber(20);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("MyApplication");
        }

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;  //只有全部清除时，Notification才会清除
        notificationManager.notify("message", 1, notification);
    }

}
