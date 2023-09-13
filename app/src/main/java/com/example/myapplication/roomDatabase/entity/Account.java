package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.myapplication.AppConstant;

import java.util.Calendar;

import okio.ByteString;

/**
 * 账号信息
 */
@Entity(tableName = "ACCOUNT")
@Fts4
public class Account {

    /**
     * 账号id
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private  int rowid;

    /**
     * 账号注册时使用的邮箱，是登陆的唯一凭证
     */
    private String email;
    /**
     * 账号密码的MD5值，避免意外的数据库泄露导致用户密码被窃取
     */
    private String passwordMD5;

    /**
     * 用户的密保问题
     */
    private String question;
    /**
     * 用户密保问题答案的MD5值
     */
    private String answerMD5;

    /**
     * 账号的创建时间，7天内不能注销
     */
    private long createTime;

    /**
     * 注销时间，可为空。
     * 如果用户注销账号，记录注销时间并封锁账号，7天内保留用户数据，但是无法使用当前邮箱再次注册
     * 如果用户进行登陆，清空注销时间
     */
    private long logoutTime;
    /**
     * 记录账号状态
     * 1.连续四次输入错误密码,封锁账号
     * 2.注销账号，封锁账号
     * 3.管理员在对账号权限或者数据进行更改时锁定账号
     */
    private boolean isLocked = false;
    /**
     * 记录连续输入错误密码的次数
     */
    private short errorTimes = 0;
    /**
     * 用户昵称
     */
    private String name;
    /**
     * 用户生日
     */
    private long birthday;

    /**
     * 性别，‘F’ or 'M'
     */
    private char sex;
    /**
     * 用户年龄
     * 0-150，在输入时限制
     */
    private int age;
    /**
     * 用户角色，默认为普通用户
     */
    private int role = AppConstant.REGULAR_USER;

    /**
     * asr-语音识别的日最大使用量
     */
    private int asrDailyMaxNum = 10;
    /**
     * ocr-图像文字识别的日最大使用量
     */
    private int ocrDailyMaxNum = 10;

    /**
     * tts-文字合成的日最大使用量
     */
    private int ttsDailyMaxNum = 10;

    /**
     * 记录当前账号今日的asr语音识别使用量
     */
    private int asrDailyNum = 0;
    /**
     * 记录当前账号今日的ocr图像文字识别语音识别使用量
     */
    private int ocrDailyNum = 0;
    /**
     * 记录当前账号今日的tts语音合成使用量
     */
    private int ttsDailyNum = 0;
    /**
     * 记录当前账号tts语音合成的总使用量，
     * tts结果的存储次数不在此统计，每次通过dao接口来查询获得
     */
    private int ttsUsageCount = 0;
    /**
     * 记录当前账号ocr图像文字识别的总使用量，
     * ocr结果的存储次数不在此统计，每次通过dao接口来查询获得
     */
    private int ocrUsageCount = 0;
    /**
     * 记录当前账号asr语音识别的总使用量，
     * asr结果的存储次数不在此统计，每次通过dao接口来查询获得
     */
    private int asrUsageCount = 0;
    /**
     * 关键词总使用次数
     * */
    private int KeywordExtractionUsageCount = 0;


    //必须有无参构造方法
    public Account() {
    }

    /**
     * 注册账号时，未设置密保问题时调用的构造方法
     */
    @Ignore
    public Account(String email, String password) {
        this.email = email;
        this.passwordMD5 = ByteString.encodeUtf8(password).md5().toString();
        this.createTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * 注册账号时，设置了密保问题和答案时调用的构造方法
     *
     * @Param answer 用户输入的问题答案，存储时会转换为MD5值
     */
    @Ignore
    public Account(String email, String password, String question, String answer) {
        this.email = email;
        this.passwordMD5 = ByteString.encodeUtf8(password).md5().toString();
        this.createTime = Calendar.getInstance().getTimeInMillis();
        this.question = question;
        this.answerMD5 = ByteString.encodeUtf8(answer).md5().toString();
    }

    public int getId() {
        return rowid;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordMD5() {
        return passwordMD5;
    }

    public void setPasswordMD5(String passwordMD5) {
        this.passwordMD5 = passwordMD5;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void Logout() {
        logoutTime = Calendar.getInstance().getTimeInMillis();
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public short getErrorTimes() {
        return errorTimes;
    }

    public void passwordError() {
        if (errorTimes != 3) {
            errorTimes++;
        } else {
            setLocked(true);
        }
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question, String answer) {
        this.question = question;
        this.answerMD5 = ByteString.encodeUtf8(answer).md5().toString();
    }

    public String getAnswerMD5() {
        return answerMD5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getAsrDailyMaxNum() {
        return asrDailyMaxNum;
    }

    public void setAsrDailyMaxNum(int asrDailyMaxNum) {
        this.asrDailyMaxNum = asrDailyMaxNum;
    }

    public int getOcrDailyMaxNum() {
        return ocrDailyMaxNum;
    }

    public void setOcrDailyMaxNum(int ocrDailyMaxNum) {
        this.ocrDailyMaxNum = ocrDailyMaxNum;
    }

    public int getTtsDailyMaxNum() {
        return ttsDailyMaxNum;
    }

    public void setTtsDailyMaxNum(int ttsDailyMaxNum) {
        this.ttsDailyMaxNum = ttsDailyMaxNum;
    }

    public int getAsrDailyNum() {
        return asrDailyNum;
    }

    public void setAsrDailyNum(int asrDailyNum) {
        this.asrDailyNum = asrDailyNum;
    }

    public int getOcrDailyNum() {
        return ocrDailyNum;
    }

    public void setOcrDailyNum(int ocrDailyNum) {
        this.ocrDailyNum = ocrDailyNum;
    }

    public int getTtsDailyNum() {
        return ttsDailyNum;
    }

    public void setTtsDailyNum(int ttsDailyNum) {
        this.ttsDailyNum = ttsDailyNum;
    }

    public void updateDailyUsageCount() {
        ttsDailyNum = 0;
        ocrDailyNum = 0;
        asrDailyNum = 0;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
    }

    public int getTtsUsageCount() {
        return ttsUsageCount;
    }

    public void setTtsUsageCount(int ttsUsageCount) {
        this.ttsUsageCount = ttsUsageCount;
    }

    public int getOcrUsageCount() {
        return ocrUsageCount;
    }

    public void setOcrUsageCount(int ocrUsageCount) {
        this.ocrUsageCount = ocrUsageCount;
    }

    public int getAsrUsageCount() {
        return asrUsageCount;
    }

    public void setAsrUsageCount(int asrUsageCount) {
        this.asrUsageCount = asrUsageCount;
    }

    public int getKeywordExtractionUsageCount() {
        return KeywordExtractionUsageCount;
    }

    public void setKeywordExtractionUsageCount(int keywordExtractionUsageCount) {
        KeywordExtractionUsageCount = keywordExtractionUsageCount;
    }
}
