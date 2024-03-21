package com.example.meteor.roomDatabase.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;

/**
 * userId参照account表的rowid
 * adminId参照account表的rowid
 * <p>
 * 未设置onDelete = CASCADE
 * 查询到的管理员和用户id对应的账号可能已经注销
 */

@Entity(tableName = "APPLICATION",
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "rowid",
                        childColumns = "userId",
                        onUpdate = CASCADE),
                @ForeignKey(entity = Account.class,
                        parentColumns = "rowid",
                        childColumns = "adminId",
                        onUpdate = CASCADE)
        },
        indices = @Index(value = {"userId", "adminId"})
)
public class Application {

    /**
     * Application表的主码
     * 1.rowid属于添加fts4注解的要求，
     * 2.由于一个用户可以提交多个申请，
     * //多个管理员可以同时处理不同的申请
     * 所以主码直接设置为id
     * 否则需要以{userId,createTime}为主码
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;

    /**
     * 申请的提交时间
     */
    private long createTime;
    /**
     * 申请处理完毕的时间,未完成则为null
     */
    private long processedTime;
    /**
     * 标记是否正在处理，取值为false表明未开始处理或者已经处理完毕
     */
    private boolean isProcess = false;
    /**
     * 标记是否通过
     */
    private boolean isPassed = false;
    /**
     * 提交申请的用户id
     */
    @ColumnInfo(index = true)
    private int userId;
    /**
     * 处理申请的管理员id，
     * 在管理员处选择进行处理时需要及时修改application，
     * 在管理员提交处理结果时也需要再一次查询application状态，
     * 如果已经被处理，就不再处理
     * 如果未被处理就提交update application
     */
    @ColumnInfo(index = true)
    private int adminId;

    /**
     * 申请的信息
     */
    private String applicationMessage;

    /**
     * 标记是否包含图片信息
     */
    private boolean withPicture = false;
    /**
     * 如果包含图片，pictureBitmap存储图片bitmap转换的string值
     */
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

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
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

    public String getApplicationMessage() {
        return applicationMessage;
    }

    public void setApplicationMessage(String applicationmMessage) {
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

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
}
