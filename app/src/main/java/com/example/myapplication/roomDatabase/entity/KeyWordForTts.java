package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

@Fts4
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
}
)
public class KeyWordForTts {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;

    private int keywordId;

    private int resultId;

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
