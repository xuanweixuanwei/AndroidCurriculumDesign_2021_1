package com.example.meteor.bean;

public class AgeGroupCount {
    public String ageGroup; // 年龄段标签
    public int count; // 对应年龄段的用户数量

    public AgeGroupCount(String ageGroup, int count) {
        this.ageGroup = ageGroup;
        this.count = count;
    }
}