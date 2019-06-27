package com.example.administrator.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.administrator.myapplication.activity.LoginActivity;
import com.example.administrator.myapplication.utils.NettyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
            online = true;
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            online = false;
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {

        }
    }

    private Runnable worker = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Log.e("HeartBeatService", "run online:" + online);

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
        }
    };


    @Override
    public void onCreate() {
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

        new Thread(worker).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
