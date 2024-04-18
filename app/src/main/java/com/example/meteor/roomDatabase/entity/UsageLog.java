package com.example.meteor.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.meteor.util.enumType.AccountStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
// 外键约束，关联User表

@Entity(tableName = "usage_logs", foreignKeys = @ForeignKey(entity = Account.class,
        parentColumns = "rowid", childColumns = "userId"),
        indices = {@Index(value = "userId")})
public class UsageLog {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;
    private int userId; // 用户ID
    private String date; // 使用日期
    private int ttsCount; // TTS使用次数
    private int asrCount; // ASR使用次数
    private int ocrCount; // OCR使用次数

    public UsageLog() {
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Ignore
    public UsageLog(int userId, LocalDate date) {
        this.userId = userId;
        this.date = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.ocrCount = 0;
        this.asrCount = 0;
        this.ttsCount = 0;
    }

    @Ignore
    public UsageLog(int userId, String date) {
        this.userId = userId;
        this.date = date;
        this.ocrCount = 0;
        this.asrCount = 0;
        this.ttsCount = 0;
    }

    public UsageLog updateTts() {
        ttsCount++;
        return this;
    }

    public UsageLog updateAsr() {
        asrCount++;
        return this;
    }

    public UsageLog updateOcr() {
        ocrCount++;
        return this;
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

//    @Ignore 无效
//    // 添加一个方法将LocalDate转换为String
//    public UsageLog setDate(LocalDate date) {
//        this.date = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
//        return this;
//    }


//    public LocalDate getDate() {
//        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
//    }

    // 添加一个方法将LocalDate转换为String
    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

}