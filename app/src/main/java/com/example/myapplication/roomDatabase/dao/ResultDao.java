package com.example.myapplication.roomDatabase.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.myapplication.roomDatabase.entity.AsrResult;
import com.example.myapplication.roomDatabase.entity.OcrResult;
import com.example.myapplication.roomDatabase.entity.TtsResult;

import java.util.List;

@Dao
public interface ResultDao {

    @Query("SELECT * FROM OcrResult WHERE userId = :userId")
    List<OcrResult> findAllOcrResult(int userId);

    @Query("SELECT * FROM AsrResult WHERE userId = :userId")
    List<AsrResult> findAllAsrResult(int userId);

    @Query("SELECT * FROM TtsResult WHERE userId = :userId")
    List<TtsResult> findAllTtsResult(int userId);

    @Query("SELECT * FROM OcrResult WHERE rowid = :resultId")
    OcrResult findOcrResult(int resultId);

    @Query("SELECT * FROM AsrResult WHERE rowid = :resultId")
    AsrResult findAsrResult(int resultId);

    @Query("SELECT * FROM OcrResult WHERE rowid = :resultId")
    OcrResult findTtsResult(int resultId);


}
