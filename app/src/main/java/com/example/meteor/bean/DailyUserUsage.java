package com.example.meteor.bean;

import androidx.annotation.NonNull;

public class DailyUserUsage {
    private int userId;
    @NonNull
    private int ttsCount;
    @NonNull
    private int asrCount;
    @NonNull
    private int ocrCount;
    @NonNull
    private String date;

    public DailyUserUsage(int userId, int ttsCount, int asrCount, int ocrCount, @NonNull String date) {
        this.userId = userId;
        this.ttsCount = ttsCount;
        this.asrCount = asrCount;
        this.ocrCount = ocrCount;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTtsCount() {
        return ttsCount;
    }

    public void setTtsCount(int ttsCount) {
        this.ttsCount = ttsCount;
    }

    public int getAsrCount() {
        return asrCount;
    }

    public void setAsrCount(int asrCount) {
        this.asrCount = asrCount;
    }

    public int getOcrCount() {
        return ocrCount;
    }

    public void setOcrCount(int ocrCount) {
        this.ocrCount = ocrCount;
    }

    // 添加getter和setter方法
}