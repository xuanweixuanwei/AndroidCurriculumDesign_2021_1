package com.example.myapplication.roomDatabase.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.myapplication.AppConstant;

import java.util.Calendar;

import okio.ByteString;

/**
 * 账号信息
 */
@Entity(tableName = "ACCOUNT")
public class Account {

    /**
     * 账号id
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    private int rowid;

    /**
     * 账号注册时使用的邮箱，是登陆的唯一凭证
     */
    private String email;
    /**
     * 账号密码的SHA256值，避免意外的数据库泄露导致用户密码被窃取
     */
    private String passwordSHA;

    private String avatarStr;
    /**
     * 用户的密保问题
     */
    private String question;
    /**
     * 用户密保问题答案的SHA256值
     */
    private String answerSHA;

    /**
     * 账号的创建时间，7天内不能注销
     */
    private long createTime;

    /**
     * 注销时间，可为空。
     * 如果用户注销账号，记录注销时间并封锁账号，5天内保留用户数据，但是无法使用当前邮箱再次注册
     * 如果用户进行登陆，清空注销时间
     */
    private long logoutTime=0;

    private long lockedTime=0;

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
    private long lastErrorTime;

    private short errorTimes = 0;
    /**
     * 用户昵称
     */
    private String name;
    /**
     * 用户生日
     */
    private String birthday;

    /**
     * 性别，‘F’ or 'M'
     */
    private Integer sex = AppConstant.default_sex;
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
     */
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
        this.passwordSHA = ByteString.encodeUtf8(password).sha256().toString();
        this.createTime = Calendar.getInstance().getTimeInMillis();
        this.name = "Meteor_" + this.createTime;
        this.ocrUsageCount=0;
        this.asrUsageCount=0;
        this.ttsUsageCount=0;
        this.errorTimes=0;
        this.isLocked=false;
    }

    /**
     * 注册账号时，设置了密保问题和答案时调用的构造方法
     *
     *  answer 用户输入的问题答案，存储时会转换为SHA256值
     */
    @Ignore
    public Account(String email, String password, String question, String answer) {
        this(email,password);
        this.question = question;
        this.answerSHA = ByteString.encodeUtf8(answer).sha256().toString();
    }


    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordSHA() {
        return passwordSHA;
    }

    public void setPasswordSHA(String passwordSHA) {
        this.passwordSHA = passwordSHA;
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
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,5);//偏移五天
        logoutTime = instance.getTimeInMillis();
    }

    public boolean isLockedCauseErrorPassword() {
        if (this.isLocked && this.errorTimes > 3 && (Calendar.getInstance().getTimeInMillis() - lastErrorTime) > 5 * 60 * 1000) {//如果当前输错密码时间距离上一次输错时间大于5min
            isLocked = false;
            errorTimes = 0;
        }
        return isLocked;
    }

    public long getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(long lockedTime) {
        this.lockedTime = lockedTime;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public long getLastErrorTime() {
        return lastErrorTime;
    }

    public void setLastErrorTime(long lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public short getErrorTimes() {
        return errorTimes;
    }

    public void passwordError() {

        if (lastErrorTime!=0) {//上一次密码输入错误过
            Calendar instance = Calendar.getInstance();
            Calendar lastTime = Calendar.getInstance();//转换为上一次错误的日期
            lastTime.setTimeInMillis(lastErrorTime);
            if (instance.get(Calendar.YEAR) == lastTime.get(Calendar.YEAR) &&
                    instance.get(Calendar.MONTH) == lastTime.get(Calendar.MONTH) &&
                    instance.get(Calendar.DAY_OF_MONTH) == lastTime.get(Calendar.DAY_OF_MONTH)) {//如果上一次错误是今天
                errorTimes++;//错误次数+1
                lastErrorTime = instance.getTimeInMillis();//并将上一次错误时间设置为今天
                if (errorTimes > 4) {
                    setLocked(true);
                    instance.add(Calendar.DATE,5);
                    setLockedTime(instance.getTimeInMillis());//设置解锁时间为5天后
                }
                return;
            }
        }
            errorTimes = 1;
            lastErrorTime = Calendar.getInstance().getTimeInMillis();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question, String answer) {
        this.question = question;
        this.answerSHA = ByteString.encodeUtf8(answer).sha256().toString();
    }

    public String getAnswerSHA() {
        return answerSHA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
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

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswerSHA(String answerSHA) {
        this.answerSHA = answerSHA;
    }
/*    public void setAnswer(String answer) {
        this.answerSHA = ByteString.encodeUtf8(answer).sha256().toString();
    }*/

    public Account setPassword(String password){
        this.passwordSHA = ByteString.encodeUtf8(password).sha256().toString();
        return this;
    }
    public void setErrorTimes(short errorTimes) {
        this.errorTimes = errorTimes;
    }

    public String getAvatarStr() {
        return avatarStr;
    }

    public void setAvatarStr(String avatarStr) {
        this.avatarStr = avatarStr;
    }

    public Account useOcr(){
        this.ocrUsageCount++;
        return this;
    }
    public Account useAsr(){
        this.asrUsageCount++;
        return this;

    }
    public Account useTts(){
        this.ttsUsageCount++;
        return this;

    }
}
