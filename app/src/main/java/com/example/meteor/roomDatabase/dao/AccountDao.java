package com.example.meteor.roomDatabase.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.meteor.roomDatabase.entity.Account;

@Dao
public interface AccountDao {
//    注册时，通过email查找account
    @Query("SELECT * FROM ACCOUNT WHERE email = :EMAIL AND passwordSHA = :PASSWORD")
    Account findAccountByPassword(String EMAIL, String PASSWORD);

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

    @Query("UPDATE ACCOUNT SET ttsDailyNum = 0 AND ocrDailyNum = 0 AND asrDailyNum = 0 WHERE rowid=:accountId")
    int updateDailyUsage(int accountId);

    @Query("SELECT isLocked FROM ACCOUNT WHERE EMAIL = :email")
    boolean isLocked(String email);

    @Insert
    void insert(Account account);




}
