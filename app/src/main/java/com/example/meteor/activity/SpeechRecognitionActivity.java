package com.example.meteor.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.meteor.AppConstant;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.util.FileUtils;
import com.example.meteor.util.JsonParser;
import com.example.myapplication.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import timber.log.Timber;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private Toast mToast;
    private EditText et_result;
    private ImageButton ib_audio_record;

    private static final String TAG = SpeechRecognitionActivity.class.getSimpleName();

    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private boolean isListening = false;

    private SharedPreferences mSharedPreferences;

    private String language = "zh_cn";

    private String resultType = "json";

    private StringBuffer buffer = new StringBuffer();
    private int ret = 0; // 函数调用返回值

    private String userEmail;
    private Account currentUser;
    private AccountDao accountDao;
    /**
     * 初始化监听器
     */
    private final InitListener mInitListener = code -> {
        Timber.d("SpeechRecognizer init() code = %s", code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun" +
                    ".cn/document/error-code查询解决方案");
        }
    };

    /**
     * 听写监听器
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Timber.d("onError %s", error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));
            isListening = false;
            ib_audio_record.setImageResource(R.drawable.vector_drawable_voice_1);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Timber.d(results.getResultString());
            if (isLast) {
                Timber.d("onResult 结束");
                isListening = false;
                ib_audio_record.setImageResource(R.drawable.vector_drawable_voice_1);

            }
            if (resultType.equals("json")) {
                printResult(results);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                et_result.setText(buffer.toString());
                et_result.setSelection(et_result.length());
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
//             以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
//             若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        // 返回结果
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        // 识别回调错误
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };


    /*生命周期函数onCreate，完成主要的初始化操作。
    --设置布局
    --初始化布局并设置按钮的点击事件监听
    --初始化识别无UI识别对象
    --初始化听写Dialog
    --初始化并绑定mIatDialog（语音听写UI）的dismiss消失监听器
      ---将isListening设置为false
      ---改变录音控制按钮的图标
      ---调用语音听写对象mIat的cancel方法，取消听写任务
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
        //初始化布局并设置按钮的点击事件监听
        init();
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(SpeechRecognitionActivity.this, mInitListener);
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(SpeechRecognitionActivity.this, mInitListener);

        DialogInterface.OnDismissListener dismissListener =
                dialogInterface -> {
                    isListening = false;
                    ib_audio_record.setImageResource(R.drawable.vector_drawable_voice_1);
                    mIat.cancel();
                };
        mIatDialog.setOnDismissListener(dismissListener);
    }

    /**
     * 封装弹窗功能
     */
    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    /**
     * 初始化ActionBar
     * *设置title
     * *设置返回箭头
     * 初始化UI控件
     * 初始化监听器
     * 绑定监听器
     */
    private void init() {

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("语音识别");
        actionBar.setDisplayHomeAsUpEnabled(true);//在ActionBar最左边显示返回箭头按钮

        et_result = findViewById(R.id.et_result);
        ib_audio_record = findViewById(R.id.audio_record);

        // 开始听写2
        // 如何判断一次听写结束：OnResult isLast=true 或者 onError
        View.OnClickListener listener = view -> {
            switch (view.getId()) {
                // 开始听写2
                // 如何判断一次听写结束：OnResult isLast=true 或者 onError
                case R.id.audio_record:
                    if (isListening) {
                        ib_audio_record.setImageResource(R.drawable.vector_drawable_voice_1);
                        mIat.cancel();

                    } else {
                        ib_audio_record.setImageResource(R.drawable.vector_drawable_unabled_voice);
                        recognize();

                    }
                    break;
                case R.id.copy_text:
                    copyText(et_result);
                    break;
                case R.id.delete_result:
                    new AlertDialog.Builder(SpeechRecognitionActivity.this)
                            .setTitle("是否要删除当前的识别结果？")
                            .setMessage("删除后无法恢复")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("是的", (dialogInterface, i) -> et_result.setText(""))
                            .show();
                    break;
                case R.id.open_folder_for_audio:
                    pickAudioToRecognize();
                    break;
            }
        };

        findViewById(R.id.delete_result).setOnClickListener(listener);
        findViewById(R.id.audio_record).setOnClickListener(listener);
        findViewById(R.id.copy_text).setOnClickListener(listener);
        findViewById(R.id.open_folder_for_audio).setOnClickListener(listener);
        initData();

    }

    private void initData() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        userEmail = getSharedPreferences(AppConstant.preferenceFileName, Context.MODE_PRIVATE)
                .getString(AppConstant.userEmail, "");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            accountDao = AppDatabase.getInstance(getApplicationContext()).AccountDao();
            currentUser = accountDao.findAccountByEmail(userEmail);
        });
        executor.shutdown();
    }

    /**
     * 将识别结果复制到手机剪切板中
     * 写入剪切板不需要动态获取权限
     */
    private void copyText(EditText et_result) {

        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("recognition result", et_result.getText());

        // Set the clipboard's primary clip.
        clipboard.setPrimaryClip(clip);
        // Only show a toast for Android 12 and lower.
        //        因为测试过程发现即使真机是安卓13，在调用剪切板复制以后也没有弹窗，所以统一不做判断直接弹窗复制的信息
        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){  }

        String str = et_result.getText().toString();
        if (str.length() < 20)
            showTip("复制了“" + str + "”");
        else {
            //            在复制的文本过多时，做一个字符串的截取和拼接
            String limitedStr = String.format("%s...%s", str.substring(0, 7),
                    str.substring(str.length() - 7));
            showTip("复制了“" + limitedStr + "”");
        }

    }

    /**
     * 有（无）UI语音听写
     **/
    private void recognize() {
        if (currentUser != null) {

            isListening = true;
            Timber.e("recognize");
            buffer.setLength(0);
            et_result.setText(null);// 清空显示内容
            mIatResults.clear();
            // 设置参数
            setParam();
            boolean isShowDialog = mSharedPreferences.getBoolean(
                    getString(R.string.pref_key_iat_show), true);
            if (isShowDialog) {
                // 显示听写对话框
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                showTip(getString(R.string.text_begin));
            } else {
                // 不显示听写对话框
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun" +
                            ".cn/document/error-code查询解决方案");
                } else {
                    showTip(getString(R.string.text_begin));
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(() -> {
                        accountDao.updateAccount(currentUser.useAsr());
                    });
                    executorService.shutdown();
                }
            }
            isListening = true;
        } else {
            showTip("登陆信息失效，请重新登陆后使用");
        }
    }

    /**
     * 设置听写参数
     */
    public void setParam() {
        Timber.d("setParam");
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);
        //TODO 需要测试
        language = mSharedPreferences.getString("iat_language_type_preference", "zh_cn");
        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            // 设置语言
            Timber.e("language = %s", language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
            mIat.setParameter("dwa", "wpgs");
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Timber.e("last language:%s", mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString(
                "iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString(
                "iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString(
                "iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }


    /**
     * 显示结果
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        et_result.setText(resultBuffer.toString());
        et_result.setSelection(et_result.length());
    }

    /**
     * 创建选项菜单,设置菜单menu资源
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speech_recognition_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 实现选项菜单点击事件
     * --语音识别参数设置
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.asr_preference_setting:
//                跳转到音量、音调、语速和音频流类型的页面
                Intent intent = new Intent(SpeechRecognitionActivity.this, SettingsActivity.class);
                intent.putExtra(getString(R.string.class_name),
                        SpeechRecognitionActivity.class.getSimpleName());
                startActivity(intent);
                Timber.d("onOptionsItemSelected: " + "点击了Speaker_setting");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private final ActivityResultLauncher pickAudioLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    String extensionName = FileUtils.getFileExtension(getContentResolver(), result);
                    if (extensionName.equals("pcm") || extensionName.equals("wav")) {
                        executeStream(result);
                    } else {
                        showTip("文件格式有误");
                    }

                } else {
                    showTip("请选择文件~");
                }
            }
    );


    private void executeStream(Uri uri) {
        buffer.setLength(0);
        et_result.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();
        // 设置音频来源为外部文件
        //        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        //         也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        //         mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, filePath);
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            return;
        }
        try {
            InputStream open = getContentResolver().openInputStream(uri);
            byte[] buff = new byte[1280];
            while (open.available() > 0) {
                int read = open.read(buff);
                mIat.writeAudio(buff, 0, read);
            }
            mIat.stopListening();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    accountDao.updateAccount(currentUser.useAsr());
                }
            });
            executorService.shutdown();
        } catch (IOException e) {
            mIat.cancel();
            showTip("读取音频流失败");
        }
    }

    private void pickAudioToRecognize() {
        pickAudioLauncher.launch("*/");
    }

}
