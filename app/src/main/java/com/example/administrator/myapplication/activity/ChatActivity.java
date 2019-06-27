package com.example.administrator.myapplication.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.CurrentUser;
import com.example.administrator.myapplication.NettyHelper;
import com.example.administrator.myapplication.NotificationUtil;
import com.example.administrator.myapplication.R;
import com.example.administrator.myapplication.UserListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 通信页面
 * 发送报表请求和发送消息
 * 用户列表登录之后需要刷新
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.send)
    TextView send;
    @BindView(R.id.current_user)
    TextView currentUser;
    @BindView(R.id.fresh)
    TextView fresh;
    @BindView(R.id.input)
    EditText input;
    @BindView(R.id.to)
    EditText to;
    @BindView(R.id.users)
    ListView mListView;

    private ChannelFuture channelFuture;
    private UserListAdapter mAdapter;
    private ArrayList<String> mList;
    private NotificationUtil notificationUtil;

    private class MainHandler extends ChannelInboundHandlerAdapter {
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

                //处理服务器发送的聊天消息
                if ("chat".equals(type)) {
                    String message = jsonObject.optString("message");
                    String from = jsonObject.optString("from");
                    String content = from + " 发来消息: " + message;

                    Toast.makeText(ChatActivity.this, content, Toast.LENGTH_SHORT).show();
                    notificationUtil.postNotification(content);
                }

                //处理服务器发送的用户列表
                if ("response".equals(type)) {
                    JSONArray array = jsonObject.optJSONArray("message");
                    mList.clear();
                    for (int i = 0; i < array.length(); i++) {
                        mList.add(array.optString(i));
                    }
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initView();
        initData();
        initListener();
    }

    public void initView() {
    }

    public void initData() {
        //初始化handler
        ArrayList<ChannelHandler> channelHandlers = new ArrayList<>();
        channelHandlers.add(new StringEncoder());
        channelHandlers.add(new StringDecoder());
        channelHandlers.add(new MainHandler());

        channelFuture = NettyHelper.makeConnect(channelHandlers);
        try {
            Thread.sleep(500);
            channelFuture.channel().writeAndFlush(makeJsonRequest());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mList = new ArrayList<>();
        mAdapter = new UserListAdapter(mList);
        mListView.setAdapter(mAdapter);

        currentUser.setText("当前用户：" + CurrentUser.getUserName());
        notificationUtil = new NotificationUtil(this);
    }

    public void initListener() {
        send.setOnClickListener(this);
        fresh.setOnClickListener(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                to.setText(mList.get(position));
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send) {
            String inputStr = input.getText().toString();
            String toStr = to.getText().toString();
            channelFuture.channel().writeAndFlush(makeJson(toStr, inputStr));
        }

        if (v.getId() == R.id.fresh) {
            channelFuture.channel().writeAndFlush(makeJsonRequest());
        }
    }

    /**
     * 将数据封装成json
     *
     * @param s1 接收人
     * @param s2 消息体
     * @return json字符串
     */
    private String makeJson(String s1, String s2) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "chat");
            jsonObject.put("from", CurrentUser.getUserName());
            jsonObject.put("to", s1);
            jsonObject.put("message", s2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String makeJsonRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String makeJsonRequest2() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "test");
            jsonObject.put("who", CurrentUser.getUserName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
