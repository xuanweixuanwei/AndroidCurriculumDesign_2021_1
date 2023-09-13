package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

/**
 * 语音识别的结果
 * */
@Entity(foreignKeys = @ForeignKey(entity = Account.class,parentColumns = "rowid",childColumns = "userId"))

@Fts4
public class AsrResult {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    private int rowid;

    private int userId;

    private long createTime;

    private String result;

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
