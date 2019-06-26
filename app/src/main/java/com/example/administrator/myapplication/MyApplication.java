package com.example.administrator.myapplication;

import android.app.Application;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.loadBinary(null);
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
