package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.ColorLong;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;



@Entity(foreignKeys = {
        @ForeignKey(
                entity = KeyWord.class,
                parentColumns = "rowid",
                childColumns = "keywordId",
                onDelete = CASCADE,
                onUpdate = CASCADE),
        @ForeignKey(
                entity = OcrResult.class,
                parentColumns = "rowid",
                childColumns = "resultId",
                onDelete = CASCADE,
                onUpdate = CASCADE)
        },
        indices = @Index(value = {"rowid"})
)
public class KeywordForOcr {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;
    /**
     * 关键词id
     */
    @ColumnInfo(index = true)
    private int keywordId;
    /**
     * ocr图片识文结果result的id
     */
    @ColumnInfo(index = true)
    private int resultId;

    public KeywordForOcr() {
    }

    @Ignore
    public KeywordForOcr(int keywordId, int resultId) {
        this.keywordId = keywordId;
        this.resultId = resultId;
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public int getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(int keywordId) {
        this.keywordId = keywordId;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }
}
