package com.example.administrator.myapplication;

/**
 * 静态实体类
 * 表示当前登录此app的用户
 */
public class CurrentUser {
    private static String userName;

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        CurrentUser.userName = userName;
    }
}
