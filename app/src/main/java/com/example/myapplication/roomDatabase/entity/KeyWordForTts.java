package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 语音合成tts的结果和keyword的关联表
 * resultId参照TtsResult表的rowid
 * keywordId参照KeyWord表的rowid
 * <p>
 * 不提供直接删除和修改keyword的操作
 */
@Entity(foreignKeys = {
        @ForeignKey(
                entity = KeyWord.class,
                parentColumns = "rowid",
                childColumns = "keywordId",
                onDelete = CASCADE,
                onUpdate = CASCADE),
        @ForeignKey(
                entity = TtsResult.class,
                parentColumns = "rowid",
                childColumns = "resultId",
                onDelete = CASCADE,
                onUpdate = CASCADE)
        },
            indices = @Index(value = {"rowid"})

)
public class KeyWordForTts {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;

    /**
     * 关键词id
     */
    @ColumnInfo(index = true)
    private int keywordId;
    /**
     * tts语音合成的结果id
     */
    @ColumnInfo(index = true)
    private int resultId;

    public KeyWordForTts() {
    }

    @Ignore
    public KeyWordForTts(int keywordId, int resultId) {
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
