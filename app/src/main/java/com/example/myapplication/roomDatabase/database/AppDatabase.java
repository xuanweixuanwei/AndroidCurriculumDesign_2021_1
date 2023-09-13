package com.example.myapplication.roomDatabase.database;

import androidx.room.Database;

import com.example.myapplication.roomDatabase.entity.Account;
import com.example.myapplication.roomDatabase.entity.Application;
import com.example.myapplication.roomDatabase.entity.AsrResult;
import com.example.myapplication.roomDatabase.entity.KeyWord;
import com.example.myapplication.roomDatabase.entity.KeywordForOcr;
import com.example.myapplication.roomDatabase.entity.OcrResult;
import com.example.myapplication.roomDatabase.entity.TtsResult;

@Database(entities = {
        Account.class,
        Application.class,
        AsrResult.class,
        OcrResult.class,
        TtsResult.class,
        KeyWord.class,
        KeywordForOcr.class
        },
        version = 0,
        exportSchema = false)
public class AppDatabase {



}
