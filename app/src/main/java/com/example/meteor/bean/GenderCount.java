package com.example.meteor.bean;

public class GenderCount {
    public Integer sex = 0; // 性别（假设0代表未知或未填，1代表男，2代表女）
    public int count = 0; // 对应性别的用户数量

    public GenderCount(Integer sex, int count) {
        this.sex = sex;
        this.count = count;
    }
}