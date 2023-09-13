package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

/**
 * 图像文字识别结果
 * */
@Fts4
@Entity(foreignKeys = @ForeignKey(entity = Account.class,parentColumns = "rowid",childColumns = "userId"))

public class OcrResult {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;


    private int userId;

    @ColumnInfo(name = "picture")
    private String bitmapStr;

    private String result;

    public int getResultId() {
        return rowid;
    }

    public void setResultId(int resultId) {
        this.rowid = resultId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBitmapStr() {
        return bitmapStr;
    }

    public void setBitmapStr(String bitmapStr) {
        this.bitmapStr = bitmapStr;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
