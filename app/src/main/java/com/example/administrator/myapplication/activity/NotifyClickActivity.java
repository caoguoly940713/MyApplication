package com.example.administrator.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.administrator.myapplication.R;
import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;

public class NotifyClickActivity extends UmengNotifyClickActivity {

        @Override
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            setContentView(R.layout.activity_mipush);
        }

        @Override
        public void onMessage(Intent intent) {
            super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
            String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
            Log.i("MyApplication", body);
        }
}
