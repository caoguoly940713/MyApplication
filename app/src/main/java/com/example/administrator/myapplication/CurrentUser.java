package com.example.administrator.myapplication;

import io.netty.channel.Channel;

public class CurrentUser {
    private static String userName;

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        CurrentUser.userName = userName;
    }
}
