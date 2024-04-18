package com.example.meteor.bean;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.example.meteor.AppConstant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserInfo {

    /**
     * 账号id
     */
    private int rowid;

    /**
     * 账号注册时使用的邮箱，是登陆的唯一凭证
     */
    private String email;

    /**
     * 账号的创建时间，7天内不能注销
     */
    private long createTime;

    /**
     * 记录账号状态
     * 1.连续四次输入错误密码,封锁账号
     * 2.注销账号，封锁账号
     * 3.管理员在对账号权限或者数据进行更改时锁定账号
     */
    private boolean isLocked = false;

    /**
     * 用户昵称
     */
    private String name;

    /**
     * 性别，‘F’ or 'M'
     */
    private Integer sex = AppConstant.default_sex;

    /**
     * 用户角色，默认为普通用户
     */
    private int role = AppConstant.REGULAR_USER;

    public UserInfo(int rowid, String email, long createTime, boolean isLocked, String name,
                     Integer sex, int role) {
        this.rowid = rowid;
        this.email = email;
        this.createTime = createTime;
        this.isLocked = isLocked;
        this.name = name;

        this.sex = sex;
        this.role = role;
    }

    public int getRowid() {
        return rowid;
    }

    public String getEmail() {
        return email;
    }

    public String getCreateTime() {
        // 将毫秒值转换为Instant对象
        java.time.Instant instant = java.time.Instant.ofEpochMilli(createTime);
        // 将Instant转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        // 创建一个DateTimeFormatter对象，指定输出格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 将LocalDateTime对象格式化为字符串
        String formattedDate = dateTime.format(formatter);
        // 返回年月日字符串
        return formattedDate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getName() {
        return name;
    }


    public Integer getSex() {
        return sex;
    }

    public int getRole() {
        return role;
    }
}
