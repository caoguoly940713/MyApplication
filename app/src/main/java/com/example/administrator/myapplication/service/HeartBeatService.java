package com.example.administrator.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.myapplication.utils.NettyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 心跳机制，定时给服务器发送请求
 */
public class HeartBeatService extends Service {

    private boolean online;
    private ChannelFuture channelFuture;
    private List<ChannelHandler> channelHandlers;

    @ChannelHandler.Sharable
    private class HeartBeatHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            Toast.makeText(HeartBeatService.this, "连接成功", Toast.LENGTH_SHORT).show();
            online = true;
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            Toast.makeText(HeartBeatService.this, "连接断开", Toast.LENGTH_SHORT).show();
            online = false;
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {

        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            int i = 0;

            while (i < 5) {
                i++;
                Log.e("HeartBeatService", new Date() + " run online: " + online);
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (online) {
                    channelFuture.channel().writeAndFlush(makeJson());
                } else {
                    channelFuture = NettyHelper.makeConnect(channelHandlers);
                }
            }

            stopSelf();
        }
    }


    @Override
    public void onCreate() {
        Log.e("HeartBeatService", "onCreate");
        //初始化handler
        channelHandlers = new ArrayList<>();
        channelHandlers.add(new StringEncoder());
        channelHandlers.add(new StringDecoder());
        channelHandlers.add(new HeartBeatHandler());

        //初始化ChannelFuture
        channelFuture = NettyHelper.makeConnect(channelHandlers);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("HeartBeatService", "onStartCommand");
        new Worker().start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("HeartBeatService", "onDestroy");
        Intent intent = new Intent(this, HeartBeatService.class);
        startService(intent);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("HeartBeatService", "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("HeartBeatService", "onUnbind");
        return super.onUnbind(intent);
    }

    private String makeJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "hello");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
