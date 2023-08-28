package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import util.FucUtil;
import util.JsonParser;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private Toast mToast;
    private View.OnClickListener listener;
    private EditText et_result;



    private static String TAG = SpeechRecognitionActivity.class.getSimpleName();
    // 语音识别对象
    private SpeechRecognizer mAsr;
    // 缓存
    private SharedPreferences mSharedPreferences;
    // 云端语法文件
    private String mCloudGrammar = null;

    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";

    private String mEngineType = SpeechConstant.TYPE_CLOUD;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
//初始化布局并设置按钮的点击事件监听
        initView();

        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(SpeechRecognitionActivity.this, mInitListener);
        //读取云端语法文件
        mCloudGrammar = FucUtil.readFile(this, "grammar_sample.abnf", "utf-8");


    }
    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }


    private void initView() {

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("语音识别");
        actionBar.setDisplayHomeAsUpEnabled(true);//在ActionBar最左边显示返回箭头按钮

    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                String grammarID = new String(grammarId);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (!TextUtils.isEmpty(grammarId))
                    editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
                editor.commit();
                showTip("语法构建成功：" + grammarId);
            } else {
                showTip("语法构建失败,错误码：" + error.getErrorCode() + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                Log.d(TAG, "recognizer result：" + result.getResultString());
                String text;
                if ("cloud".equalsIgnoreCase(mEngineType)) {
                    text = JsonParser.parseGrammarResult(result.getResultString());
                } else {
                    text = JsonParser.parseLocalGrammarResult(result.getResultString());
                }

                // 显示
                ((EditText) findViewById(R.id.et_result)).setText(text);
            } else {
                Log.d(TAG, "recognizer result : null");
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip("onError Code：" + error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };

    /**
     * 参数设置
     *
     * @return
     *
     *
     */
    public boolean setParam() {
        boolean result = false;
        //设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果为json格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");

        if ("cloud".equalsIgnoreCase(mEngineType)) {
            String grammarId = mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
            if (TextUtils.isEmpty(grammarId)) {
                result = false;
            } else {
                //设置云端识别使用的语法id
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
                result = true;
            }
        }
        // 设置音频保存路径，保存音频格式支持pcm、wav，
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/asr.wav");
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mAsr) {
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speech_recognition_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
               break;
            case R.id.asr_preference_setting:
                //                跳转到音量、音调、语速和音频流类型的页面
                Intent intent = new Intent(SpeechRecognitionActivity.this, SettingsActivity.class);
                intent.putExtra(getString(R.string.class_name), SpeechRecognitionActivity.class.getSimpleName());
                startActivity(intent);
                Log.w(TAG, "onOptionsItemSelected: " + "点击了Speaker_setting");
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}