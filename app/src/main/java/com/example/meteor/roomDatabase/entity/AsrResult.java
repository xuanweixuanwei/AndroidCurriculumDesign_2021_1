package com.example.meteor.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;

/**
 * 语音识别的结果
 */
@Entity(foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "rowid", childColumns = "userId"),
        indices = @Index(value = {"userId"})
)
public class AsrResult {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    private int rowid;
    /**
     * 语音识别结果对应的用户id
     */
    private int userId;
    /**
     * 语音识别结果存储的时间
     * 如果某一次的结果没有存储，用户再一次进行了识别，
     * 那么该次的识别结果不会被保存
     */
    private long createTime;
    /**
     * 语音识别的结果，字符串类型
     */
    private String result;

    public AsrResult() {
    }

    /**
     * 构造方法，传入用户id和识别结果str即可
     */
    @Ignore
    public AsrResult(int userId, String result, String language) {
        this.userId = userId;
        this.result = result;
        this.language = language;
        this.createTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * 语音识别的语言类型 对应language_type
     */
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
