package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.myapplication.AppConstant;

/**
 * 图像文字识别结果
 */

@Entity(foreignKeys = @ForeignKey(
        entity = Account.class,
        parentColumns = "rowid",
        childColumns = "userId",
        onDelete = CASCADE,
        onUpdate = CASCADE
        ),
        indices = @Index(value = {"userId"})
)

public class OcrResult {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;

    /**
     * ocr图像文字识别结果对应用户的id
     */
    private int userId;
    /**
     * 进行图像文字识别传入的图片str
     */
    @ColumnInfo(name = "picture")
    private String bitmapStr;
    /**
     * 图像文字识别生成结果后，用户选择保存的结果
     * （因为结果可以修改，所以可能与Web API返回的结果不一致）
     * 传入时需要trim去除空格
     */
    private String result;

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

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
