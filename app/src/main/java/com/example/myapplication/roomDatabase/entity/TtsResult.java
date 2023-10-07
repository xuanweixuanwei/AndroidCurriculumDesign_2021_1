package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;

/**
 * 语音合成的请求结果信息
 */
@Entity(foreignKeys = @ForeignKey(
        entity = Account.class,
        parentColumns = "rowid",
        childColumns = "userId"
        ),
        indices = @Index({"userId"})
)
public class TtsResult {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;
    /**
     * 用户id
     */
    private int userId;

    /**
     * 进行语音合成的内容
     */
    private String requestText;

    /**
     * 发音人信息
     */
    private String voicer;

    /**
     * 创建文件的时间，设想：根据创建时间来查找手机内存中的文件
     */
    private long createTime;

    public TtsResult() {
    }

    @Ignore
    public TtsResult(int userId, String requestText, String voicer) {
        this.userId = userId;
        this.requestText = requestText;
        this.voicer = voicer;
        this.createTime = Calendar.getInstance().getTimeInMillis();
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

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getVoicer() {
        return voicer;
    }

    public void setVoicer(String voicer) {
        this.voicer = voicer;
    }
}
