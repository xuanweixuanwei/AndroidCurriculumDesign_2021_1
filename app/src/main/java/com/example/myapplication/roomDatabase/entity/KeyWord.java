package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class KeyWord {

    @PrimaryKey(autoGenerate = true)
    private int rowid;

/**
 * 关键词字符串，传入前需要trim
 * */
    private String keyword;

    public KeyWord() {
    }

    @Ignore
    public KeyWord(String keyword) {
        this.keyword = keyword;
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}


