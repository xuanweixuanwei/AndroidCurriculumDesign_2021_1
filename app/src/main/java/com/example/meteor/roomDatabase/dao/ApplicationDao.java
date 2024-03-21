package com.example.meteor.roomDatabase.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.example.meteor.roomDatabase.entity.Application;

import java.util.List;

@Dao
public interface ApplicationDao {
    @Query("SELECT * FROM APPLICATION WHERE userId =:userId")
    List<Application> user_findAllApplication(int userId);

    @Query("SELECT * FROM APPLICATION GROUP BY userId  ORDER BY createTime ")
    List<Application> admin_findAllApplicationOrderByUser();

    @Query("SELECT * FROM APPLICATION WHERE isProcess =0  GROUP BY userId  ORDER BY createTime ")
    List<Application> admin_findApplicationToProcess();

    @Query("SELECT * FROM APPLICATION WHERE isProcess =1  GROUP BY userId  ORDER BY createTime ")
    List<Application> admin_findApplicationProcessed();
}
