package com.example.myapplication.roomDatabase.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.myapplication.roomDatabase.entity.OcrResult;

import java.util.List;

@Dao
public interface ResultDao {

    @Query("SELECT * FROM OcrResult WHERE userId = :userId")
    List<OcrResult> findAllOcrResult(int userId);

    @Query("SELECT * FROM AsrResult WHERE userId = :userId")
    List<OcrResult> findAllAsrResult(int userId);

    @Query("SELECT * FROM OcrResult WHERE userId = :userId")
    List<OcrResult> findAllTtsResult(int userId);

    @Query("SELECT * FROM OcrResult WHERE rowid = :resultId")
    List<OcrResult> findOcrResult(int resultId);

    @Query("SELECT * FROM AsrResult WHERE rowid = :resultId")
    List<OcrResult> findAsrResult(int resultId);

    @Query("SELECT * FROM OcrResult WHERE rowid = :resultId")
    List<OcrResult> findTtsResult(int resultId);


}
