package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

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
                onUpdate = CASCADE
        ),
        @ForeignKey(
                entity = AsrResult.class,
                parentColumns = "rowid",
                childColumns = "resultId",
                onDelete = CASCADE,
                onUpdate = CASCADE
        )
        },
        indices = @Index(value = {"rowid"})
)
public class KeyWordForAsr {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;
    /**
     * 关键词id
     */
    private int keywordId;
    /**
     * asr结果的id
     */
    private int resultId;

    public KeyWordForAsr() {
    }

    @Ignore
    public KeyWordForAsr(int keywordId, int resultId) {
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
