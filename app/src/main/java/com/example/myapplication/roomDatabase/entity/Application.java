package com.example.myapplication.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Fts4
@Entity(tableName = "APPLICATION",
        foreignKeys = {
            @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "userId",
                onUpdate = CASCADE,
                onDelete = CASCADE),
             @ForeignKey(entity = Account.class,
                    parentColumns = "rowid",
                    childColumns = "adminId",
                    onUpdate = CASCADE,
                    onDelete = CASCADE)
        }
)
public class Application {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int id;

    private long createTime;

    private long processedTime;

    private boolean isProcess = false;

    private boolean isPassed = false;

    private int userId;

    private int adminId;

    private String applicationMessage;

    private boolean withPicture = false;

    private String pictureBitmap;

    public Application() {
    }

    @Ignore
    public Application(int userId, String applicationMessage, String pictureBitmap) {
        this.createTime = Calendar.getInstance().getTimeInMillis();
        this.userId = userId;
        this.applicationMessage = applicationMessage;
        this.withPicture = true;
        this.pictureBitmap = pictureBitmap;

    }

    @Ignore
    public Application(int userId, String applicationMessage) {
        this.createTime = Calendar.getInstance().getTimeInMillis();
        this.userId = userId;
        this.applicationMessage = applicationMessage;
    }

    public int getId() {
        return id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(long processedTime) {
        this.processedTime = processedTime;
    }

    public boolean isProcess() {
        return isProcess;
    }

    public void setProcess(boolean process) {
        isProcess = process;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getApplicationmMessage() {
        return applicationMessage;
    }

    public void setApplicationmMessage(String applicationmMessage) {
        this.applicationMessage = applicationmMessage;
    }

    public boolean isWithPicture() {
        return withPicture;
    }

    public void setWithPicture(boolean withPicture) {
        this.withPicture = withPicture;
    }

    public String getPictureBitmap() {
        return pictureBitmap;
    }

    public void setPictureBitmap(String pictureBitmap) {
        this.pictureBitmap = pictureBitmap;
    }
}
