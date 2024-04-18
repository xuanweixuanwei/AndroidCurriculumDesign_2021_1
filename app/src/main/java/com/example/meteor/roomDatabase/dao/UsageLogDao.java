package com.example.meteor.roomDatabase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.meteor.bean.DailyUserUsage;
import com.example.meteor.roomDatabase.entity.UsageLog;

import java.util.List;

@Dao
public interface UsageLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsageLog usageLog);

    @Update
    void update(UsageLog usageLog);

    @Query("SELECT count(*) FROM usage_logs WHERE  date = :date AND ttsCount != 0 OR asrCount !=0 OR ocrCount != 0")
    int getDailyActiveUser(String date);

    @Query("SELECT * FROM usage_logs WHERE userId = :userId AND date = :date")
    UsageLog getUsageLogForUserOnDate(int userId, String date);

    @Query("SELECT * FROM usage_logs WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    UsageLog getLastUsageLogForUser(int userId);

//    // 查询某用户总使用次数
//    @Query("SELECT SUM(ttsCount), SUM(asrCount), SUM(ocrCount) FROM usage_logs WHERE userId = :userId ")
//    ArrayList<UsageLog> getTotalUsageCountForUserOnDate(int userId);

    /**
     * 根据指定日期获取所有用户的每日使用统计数据
     */
    @Query("SELECT userId, SUM(ttsCount) AS ttsCount, SUM(asrCount) AS asrCount, SUM(ocrCount) AS ocrCount ,date FROM usage_logs WHERE date = :targetDate GROUP BY userId")
    List<DailyUserUsage> getDailyUsageListForAllUsers(String targetDate);

    /**
     * 根据指定日期获取当天全部用户的使用统计数据，与id无关
     */
    @Query("SELECT 0 as userId , SUM(ttsCount) AS ttsCount, SUM(asrCount) AS asrCount, SUM(ocrCount) AS ocrCount ,date " +
            "FROM usage_logs WHERE date = :targetDate ")
    DailyUserUsage getDailyUsageSum(String targetDate);
}