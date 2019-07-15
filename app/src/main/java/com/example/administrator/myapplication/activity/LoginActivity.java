package com.example.administrator.myapplication.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.BuildConfig;
import com.example.administrator.myapplication.CurrentUser;
import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.service.HeartBeatService;
import com.example.administrator.myapplication.utils.NettyHelper;
import com.umeng.message.PushAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.os.Build.*;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * 登录页面,Bootstrap初始化完成就会自动绑定channelHandlers
 * 无论调用几次connect
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.pass)
    EditText pass;
    @BindView(R.id.login)
    TextView login;
    @BindView(R.id.recon)
    TextView recon;
    @BindView(R.id.check_update)
    TextView checkUpdate;

    private ChannelFuture channelFuture;
    private List<ChannelHandler> channelHandlers;
    private RemoteViews remoteViews;
    private NotificationManager manager;
    private Notification.Builder nb;

    @ChannelHandler.Sharable
    //ChannelHandler不放业务逻辑，只发送message，然后交给Handler处理
    private class LoginHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Message message = handler.obtainMessage();
            message.obj = msg;
            message.what = 100;
            handler.sendMessage(message);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            try {
                JSONObject jsonObject = new JSONObject(msg.obj.toString());
                String type = jsonObject.optString("type");
                if ("version".equals(type)) {
                    int versionCode = Integer.parseInt(jsonObject.optString("message"));
                    if (versionCode > BuildConfig.VERSION_CODE) {
                        downLoadPackage();
                    } else {
                        Toast.makeText(LoginActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                    }
                }
                if ("result".equals(type)) {
                    String message = jsonObject.optString("message");
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void downLoadPackage() {
        String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("download", "下载进度", IMPORTANCE_DEFAULT);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);

            //初始化通知ui
            nb = new Notification.Builder(this, "download");
            nb.setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("下载中");
            nb.setProgress(100, 0, false).setAutoCancel(true);

            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
                    Uri contentUri = FileProvider.getUriForFile(getApplication(), "com.caikeng.app.fileProvider", new File(apkPath));
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        PushAgent.getInstance(this).onAppStart();

        Intent serviceIntent = new Intent(this, HeartBeatService.class);
        startService(serviceIntent);

        initView();
        initData();
        initListener();
    }

    private void initData() {
        //初始化handler
        channelHandlers = new ArrayList<>();
        channelHandlers.add(new StringEncoder());
        channelHandlers.add(new StringDecoder());
        channelHandlers.add(new LoginHandler());

        //初始化ChannelFuture
        channelFuture = NettyHelper.makeConnect(channelHandlers);
    }

    public void initView() {

    }

    public void initListener() {
        login.setOnClickListener(this);
        recon.setOnClickListener(this);
        checkUpdate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recon) {
            channelFuture = NettyHelper.makeConnect(channelHandlers);
        }

        if (v.getId() == R.id.login) {
            String nameStr = name.getText().toString();
            String passStr = pass.getText().toString();

            if (TextUtils.isEmpty(nameStr) || TextUtils.isEmpty(passStr)) {
                Toast.makeText(this, "检查你的输入！", Toast.LENGTH_SHORT).show();
            } else {
                channelFuture.channel().writeAndFlush(makeJson(nameStr, passStr));
                CurrentUser.setUserName(nameStr);

                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
            }
        }

        if (v.getId() == R.id.check_update) {
            channelFuture.channel().writeAndFlush(makeJsonRequest());
        }
    }

    private String makeJson(String s1, String s2) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "login");
            jsonObject.put("name", s1);
            jsonObject.put("pass", s2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String makeJsonRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "check");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
