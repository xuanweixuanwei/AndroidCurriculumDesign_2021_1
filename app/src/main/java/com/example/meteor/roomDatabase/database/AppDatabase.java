package com.example.meteor.roomDatabase.database;

import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.dao.ApplicationDao;
import com.example.meteor.roomDatabase.dao.ResultDao;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.roomDatabase.entity.Application;
import com.example.meteor.roomDatabase.entity.AsrResult;
import com.example.meteor.roomDatabase.entity.KeyWord;
import com.example.meteor.roomDatabase.entity.KeywordForOcr;
import com.example.meteor.roomDatabase.entity.OcrResult;
import com.example.meteor.roomDatabase.entity.TtsResult;

@Database(entities = {
        Account.class,
        Application.class,
        AsrResult.class,
        OcrResult.class,
        TtsResult.class,
        KeyWord.class,
        KeywordForOcr.class
        },
        version = 5,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
        public static final String DB_NAME = "AppDatabase.db";
        private static volatile AppDatabase instance;
        //通过getInstance方法创建数据库，每次都得到同一个实例
        public static synchronized AppDatabase getInstance(Context context) {
                if (instance == null) {
                        instance = create(context);
                }
                return instance;
        }

        private static AppDatabase create(final Context context) {
                return Room.databaseBuilder(
                        context,
                        AppDatabase.class,
                        DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
          /*如果从旧版本到新版本的迁移规则无法找到，
          就会触发错误，如果要避免该错误，可以使用fallbackToDestructiveMigration
          ，这样就可以告诉Room，在找不到迁移规则时，可以破坏性重建数据库，注意这会删除所有数据库表数据。
作者：crossroads  链接：https://www.jianshu.com/p/6242ee48b777  来源：简书    著作权归作者所有。*/
        }
        //两个表的Dao接口，用于对数据进行增删改查操作
        public abstract AccountDao AccountDao();
        public abstract ApplicationDao ApplicationDao();
        public abstract ResultDao ResultDao();
}
