package com.example.meteor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.meteor.AppConstant;
import com.example.meteor.MainActivity;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class WakeUpService extends Service {
    private static final String TAG = "WakeUpService";
//    private final IBinder binder = new WakeUpService();
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    // 唤醒结果内容
    private String resultString;

    private String threshStr = "门限值：";
    private String keep_alive = "1";
    private String ivwNetMode = "0";
    private int curThresh = 1450;


//    public class WakeUpService extends Binder{
//        public WakeUpService getService(){
//            return WakeUpService.this;
//        }
//    }
    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Timber.d("onResult");
//            if (!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
//            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                String buffer = "【RAW】 " + text +
                        "\n" +
                        "【操作类型】" + object.optString("sst") +
                        "\n" +
                        "【唤醒词id】" + object.optString("id") +
                        "\n" +
                        "【得分】" + object.optString("score") +
                        "\n" +
                        "【前端点】" + object.optString("bos") +
                        "\n" +
                        "【尾端点】" + object.optString("eos");
                resultString = buffer;
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
//            textView.setText(resultString);
        }

        @Override
        public void onError(SpeechError error) {
//            showTip(error.getPlainDescription(true));
//            setRadioEnable(true);
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };


    private void startWakeUp(){
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {

            resultString = "";
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, AppConstant.path);
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
                    getExternalFilesDir("msc").getAbsolutePath() + "/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

            mIvw.startListening(mWakeuperListener);
        } else {
//            showTip("唤醒未初始化");
//            Toast.makeText(, "jjjj", Toast.LENGTH_SHORT).show();
        }
    }

    public WakeUpService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIvw = VoiceWakeuper.createWakeuper(this, null);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        IBinder binder = null;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        TODO : RELEASE
        return super.onUnbind(intent);
    }
}