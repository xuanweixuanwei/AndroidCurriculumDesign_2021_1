package com.example.meteor.util.enumType;

import android.os.Build;

import com.example.myapplication.R;

import java.util.Arrays;

public enum Role {
    REGULAR_USER(0,"普通注册用户", R.color.common_accent_color), //普通注册用户 ,0
    MEMBER_USER(1,"会员用户", R.color.common_icon_color),// 会员用户,1
    ADMIN_USER(2,"超级管理员", R.color.common_line_color);// 超级管理员,2
    private final int role;
    private final String roleName;
    private final int color;

    // 可以添加一个静态方法来获取所有描述的数组
    public static String[] getAllDescriptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(Role.values())
                    .map(Role::getRoleName)
                    .toArray(String[]::new);
        }
        return new String[]{"普通注册用户",
                "会员用户",
                "超级管理员"
        };
    }

    Role(int role,String roleName, int color) {
        this.role = role;
        this.roleName = roleName;
        this.color = color;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getColor() {
        return color;
    }

    public int getRole(){
        return role;
    }
}
