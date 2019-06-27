package com.example.administrator.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.CurrentUser;
import com.example.administrator.myapplication.NettyHelper;
import com.example.administrator.myapplication.R;

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

    private ChannelFuture channelFuture;
    private List<ChannelHandler> channelHandlers;

    @ChannelHandler.Sharable
    //ChannelHandler不放业务逻辑，只发送message，然后交给Handler处理
    private class LoginHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            Message message = handler.obtainMessage();
            message.what = 100;
            message.obj = "连接成功";
            handler.sendMessage(message);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Message message = handler.obtainMessage();
            message.obj = msg;
            message.what = 100;
            handler.sendMessage(message);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            Message message = handler.obtainMessage();
            message.what = 100;
            message.obj = "连接断开";
            handler.sendMessage(message);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(LoginActivity.this, "msg:" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

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
    }

    /**
     * 将数据封装成json
     *
     * @param s1 用户名
     * @param s2 密码
     * @return json字符串
     */
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
}
