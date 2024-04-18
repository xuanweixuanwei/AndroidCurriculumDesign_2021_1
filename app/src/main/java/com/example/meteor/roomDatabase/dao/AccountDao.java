package com.example.meteor.roomDatabase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.meteor.bean.AgeGroupCount;
import com.example.meteor.bean.GenderCount;
import com.example.meteor.bean.UserInfo;
import com.example.meteor.roomDatabase.entity.Account;

import java.util.List;

@Dao
public interface AccountDao {
    //    注册时，通过email查找account
    @Query("SELECT * FROM ACCOUNT WHERE email = :EMAIL AND passwordSHA = :PASSWORD")
    Account findAccountBySHAPassword(String EMAIL, String PASSWORD);

    @Query("SELECT * FROM ACCOUNT WHERE email = :EMAIL ")
    Account findAccountByEmail(String EMAIL);

    @Update
    int updateAccount(Account accountToUpdate);

    @Query("DELETE from ACCOUNT")
    void clearAccount();

    @Delete
    void deleteAccount(Account account);

    //修改密码
// Room 使用主键将传递的实体实例与数据库中的行进行匹配。
// 如果没有具有相同主键的行，Room 不会进行任何更改。
//@Update 方法可以选择性地返回 int 值，该值指示成功更新的行数。
    @Query("UPDATE ACCOUNT SET role = :newRole WHERE rowid = :accountId")
    int updateAccountRole(int accountId, int newRole);

    @Query("UPDATE ACCOUNT SET ttsDailyNum = 0 AND ocrDailyNum = 0 AND asrDailyNum = 0 WHERE " +
            "rowid=:accountId")
    int updateDailyUsage(int accountId);

    @Query("SELECT isLocked FROM ACCOUNT WHERE EMAIL = :email")
    boolean isLocked(String email);

    @Insert
    void insert(Account account);

    // 统计当前非管理员用户数量
    @Query("SELECT COUNT(*) FROM ACCOUNT")
    int countUsers();

    // 统计当前非管理员用户数量
    @Query("SELECT COUNT(*) FROM ACCOUNT WHERE role != :adminRoleId ")
    int countRoleUsers(int adminRoleId);

    // 统计所有用户的ASR总使用次数
    @Query("SELECT SUM(asrUsageCount) FROM ACCOUNT")
    int getTotalAsrUsage();

    // 统计所有用户的OCR总使用次数
    @Query("SELECT SUM(ocrUsageCount) FROM ACCOUNT")
    int getTotalOcrUsage();

    // 统计所有用户的TTS总使用次数
    @Query("SELECT SUM(ttsUsageCount) FROM ACCOUNT")
    int getTotalTtsUsage();


    // 获取不同年龄段用户数量
    // 假设age字段存储的是用户的实际年龄
    @Query("SELECT COUNT(*) AS count, " +
            "CASE WHEN age BETWEEN 0 AND 19 THEN '00后' " +
            "WHEN age BETWEEN 20 AND 29 THEN '90后' " +
            "WHEN age BETWEEN 30 AND 39 THEN '80后' " +
            "WHEN age BETWEEN 40 AND 49 THEN '70后' " +
            "ELSE '50后及以上' END AS ageGroup " +
            "FROM ACCOUNT GROUP BY ageGroup")
    List<AgeGroupCount> getAgeGroupCounts();// AgeGroupCount 是一个自定义数据类，包含ageGroup和count属性

    // 获取性别占比
    @Query("SELECT sex, COUNT(*) as count FROM ACCOUNT GROUP BY sex")
    List<GenderCount> getGenderDistribution(); // GenderCount 是一个自定义数据类，包含sex和count属性

    /**
     * 查询所有用户的基本信息
     *
     * @return LiveData<List < UserInfo>> 包含所有用户基本信息的列表
     */
    @Query("SELECT rowid, email, createTime, isLocked, name, sex, role FROM ACCOUNT")
    List<UserInfo> getAllUserInfo();

    /**
     * 查询所有用户的基本信息，排除当前用户
     *
     * @param userId 当前用户的id
     * @return LiveData<List < UserInfo>> 包含所有用户基本信息的列表
     */
    @Query("SELECT rowid, email, createTime, isLocked, name, sex, role FROM ACCOUNT WHERE rowid " +
            "!= :userId")
    List<UserInfo> getAllUserInfoExceptCurrentUser(long userId);

//    // 自定义插入行为，可以通过Room提供的回调方法获取到插入后的主键
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertWithCallback(Account account, final Callback<Integer> callback);
//
//    // Java 8+ 中的函数式接口
//    @FunctionalInterface
//    interface Callback<T> {
//        void onResult(T result);
//    }

}
