package com.example.meteor.util.enumType;

import android.os.Build;

import com.example.myapplication.R;

import java.util.Arrays;

public enum AccountStatus {

    ACTIVE("活跃", R.color.common_accent_color), //普通注册用户 ,0
    LOCKED("被锁定", R.color.common_icon_color),// 会员用户,1
    LOGOUT("已注销", R.color.common_line_color);// 超级管理员,2
    private final String status;
    private final int color;

    // 可以添加一个静态方法来获取所有描述的数组
    public static String[] getAllDescriptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(Role.values())
                    .map(Role::getRoleName)
                    .toArray(String[]::new);
        }
        return new String[]{"活跃",
                "被锁定",
                "已注销"
        };
    }

    AccountStatus(String status, int color) {
        this.status = status;
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public int getColor() {
        return color;
    }

}
