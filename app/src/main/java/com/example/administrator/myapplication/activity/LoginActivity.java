package com.example.administrator.myapplication.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.BuildConfig;
import com.example.administrator.myapplication.CurrentUser;
import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.service.HeartBeatService;
import com.example.administrator.myapplication.utils.NettyHelper;
import com.umeng.message.PushAgent;
import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppManager;

import org.json.JSONException;
import org.json.JSONObject;

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

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

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
                    downLoadPackage();
//                    int versionCode = Integer.parseInt(jsonObject.optString("message"));

//                    if (versionCode > BuildConfig.VERSION_CODE) {
//                        downLoadPackage();
//                    } else {
//                        Toast.makeText(LoginActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
//                    }
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

    //下载安装包
    private void downLoadPackage() {
        final String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("download", "下载进度", IMPORTANCE_DEFAULT);
            Notification.Builder builder = new Notification.Builder(this, "download");
            builder.setContentText("下载进度度");
            builder.setContentTitle("标题");
            builder.setSmallIcon(R.drawable.ic_launcher_background);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            manager.notify(1, builder.build());
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
