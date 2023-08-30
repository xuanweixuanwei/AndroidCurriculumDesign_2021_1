package com.example.myapplication.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;


import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.action.ToastAction;
import com.example.myapplication.dialog.TipsDialog;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;



import java.io.File;
import java.io.IOException;

import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;



import util.ExtractTextFromFile;

public class VoiceSynthesisActivity extends AppCompatActivity implements ToastAction{

    public static String TAG = "VoiceSynthesisActivity";

    // 语音合成对象
    private SpeechSynthesizer mTts;



    // 默认发音人
    private String voicer = "xiaoyan";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    private EditText ed_input;
    private String texts = "";

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    private int selectedNum = 0;
    private boolean isPrepared = false;
    private boolean isSpeaking = false;


    private ImageButton audioControl;
    private TextView playProgressInfo;
    private NestedScrollView scrollView ;


    private String docx_type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    private File pcmFile;
    private Timer timer = new Timer();

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.open_folder_for_text:
                    pickFile();
                    break;
                case R.id.delete_input_text:
                    ed_input.setText("");
                    changeEditTextHeight(ed_input);

                    showTip("清空已输入的文本");
                    break;

                case R.id.audio_control:
                    changePlayingState();
                    break;

                case R.id.cancel_synthesis_task:
                    cancel();
                    showTip("取消了播放任务");
                    playProgressInfo.setText("");
                    playProgressInfo.setVisibility(View.GONE);
                    break;

                default:
                    break;

            }
        }
    };


    //改变播放状态
    private void changePlayingState() {
        if (mTts == null) {
            showTip("初始化SpeechSynthesizer对象出错，请在真机调试");
        } else {
            if (isSpeaking) {
                pause();
            } else {
                if (isPrepared) {
                    resume();
                } else {
                    startSpeaking();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_synthesis);
        // 初始化合成对象，如果使用虚拟机调试会无法获取合成对象
        // 返回为null，需要用真机调试
        mTts = SpeechSynthesizer.createSynthesizer(VoiceSynthesisActivity.this, mTtsInitListener);

        if (null == mTts) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Log.e(TAG, "onCreate: 创建对象失败，请使用真机调试并确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化。");
            this.showTip("创建对象失败，请使用真机调试并确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            finish();
        }



        initData();
        initView();

//        ed_input.setHint(R.string.speech_synthesis_hint);

        focusable();

    }

    private void initData() {
        //初始化云端合成发音人信息列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        //初始化云端合成发音人取值列表
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        //获取默认的偏好信息
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("语音合成");
        actionBar.setDisplayHomeAsUpEnabled(true);//在ActionBar最左边显示返回箭头按钮

        ed_input = findViewById(R.id.et_input);
        audioControl = findViewById(R.id.audio_control);
        playProgressInfo = findViewById(R.id.playProgressInfo);
        scrollView = findViewById(R.id.scrollView);
        //给底部四个按钮设置监听器
        findViewById(R.id.open_folder_for_text).setOnClickListener(listener);
        findViewById(R.id.delete_input_text).setOnClickListener(listener);
        findViewById(R.id.audio_control).setOnClickListener(listener);
        findViewById(R.id.cancel_synthesis_task).setOnClickListener(listener);

        ed_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    changeEditTextHeight(ed_input);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

//                updateEditTextHeight();
            }
        });

        timer.schedule(timerTask,0,5000);
        //测试偏好数据
        //TODO 上线前需要增加更丰富的提示信息
    }

    private void updateEditTextHeight() {
        showTip("hh");
        // 获取EditText的LayoutParams
        ViewGroup.LayoutParams layoutParams = ed_input.getLayoutParams();

        // 测量EditText的宽度和高度
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ed_input.getWidth(), View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ed_input.getHeight(), View.MeasureSpec.UNSPECIFIED);
        ed_input.measure(widthMeasureSpec, heightMeasureSpec);

        layoutParams.width = View.MeasureSpec.makeMeasureSpec(ed_input.getWidth(), View.MeasureSpec.EXACTLY);
        layoutParams.height =  View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

//        // 获取测量后的高度
//        int measuredHeight = ed_input.getMeasuredHeight();
//
//        // 更新EditText的高度
//        layoutParams.height = measuredHeight;
//        ed_input.setLayoutParams(layoutParams);
    }

    //设置选项菜单的menu资源
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.voice_synthesis_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //设置选项菜单Item的点击事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.speaker_setting:
                showPersonSelectDialog();
                break;

            case R.id.tts_preference_setting:
//                跳转到音量、音调、语速和音频流类型的页面
                Intent intent = new Intent(VoiceSynthesisActivity.this, SettingsActivity.class);
                intent.putExtra(getString(R.string.class_name), VoiceSynthesisActivity.class.getSimpleName());
                startActivity(intent);
                Log.w(TAG, "onOptionsItemSelected: " + "点击了Speaker_setting");

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 发音人选择
     */
    private void showPersonSelectDialog() {

        new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                        selectedNum, // 默认的选项
                        new DialogInterface.OnClickListener() { // 点击单选框后的处理
                            public void onClick(DialogInterface dialog,
                                                int which) { // 点击了哪一项
                                voicer = mCloudVoicersValue[which];
                                if ("catherine".equals(voicer) || "henry".equals(voicer) || "vimary".equals(voicer)) {
//                                            如果选择的是英文发言人

                                    if(ed_input.getText().length()==0){
                                        ed_input.setText(R.string.text_tts_source_en);
                                        showTip("英文发音人只能朗读英文，中文无法朗读，已默认导入英文示例");

                                    }else{
                                        showTip("英文发音人只能朗读英文，中文无法朗读");
                                    }
                                } else {
                                    if(ed_input.getText().length()==0){
                                        ed_input.setText(R.string.text_tts_source);
                                        showTip("已默认导入中文示例");
                                    }
                                }
                                selectedNum = which;
                                dialog.dismiss();
                            }
                        }).show();


    }

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
            mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");

        }

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/tts.pcm");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            unfocused();
            playProgressInfo.setVisibility(View.VISIBLE);
            playProgressInfo.setText("准备播放，缓冲中");
            audioControl.setImageResource(R.drawable.vector_drawable_startplaying);
            showTip("开始播放");


        }

        @Override
        public void onSpeakPaused() {
            playProgressInfo.setText("点击▷继续播放，点击✖终止播放");

            audioControl.setImageResource(R.drawable.vector_drawable_startplaying);
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            playProgressInfo.setText("继续播放");
//            isSpeaking =  true;
            audioControl.setImageResource(R.drawable.vector_drawable_pause);
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            Log.e("MscSpeechLog_", "percent =" + percent);
            mPercentForBuffering = percent;
           /* showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));*/

        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Log.e("MscSpeechLog_", "percent =" + percent);
            mPercentForPlaying = percent;
            playProgressInfo.setText(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));


//
//showTip(ed_input.getHeight()/1000*percent+"");

                //todo 加一个计时器任务




            SpannableStringBuilder style = new SpannableStringBuilder(texts);
            Log.e(TAG, "beginPos = " + beginPos + "  endPos = " + endPos);
            style.setSpan(new BackgroundColorSpan(Color.RED), beginPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((EditText) findViewById(R.id.et_input)).setText(style);
        }

        @Override
        public void onCompleted(SpeechError error) {

            playProgressInfo.setVisibility(View.GONE);
            audioControl.setImageResource(R.drawable.vector_drawable_start_synthesis);
            isPrepared = false;
            isSpeaking = false;
            mPercentForPlaying = 0;
            mPercentForBuffering = 0;
            playingCompleted();

            // 成功对话框
            new TipsDialog.Builder(VoiceSynthesisActivity.this)
                    .setIcon(TipsDialog.ICON_FINISH)
                    .setMessage("完成")
                    .show();

            if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
            // 当设置 SpeechConstant.TTS_DATA_NOTIFY 为1时，抛出buffer数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buffer = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Log.e(TAG, "EVENT_TTS_BUFFER = " + buffer.length);
                // 保存文件
                appendFile(pcmFile, buffer);
            }

        }
    };

    private void playingCompleted() {
        removeTextBackground();
        focusable();
    }


    /**
     * 给file追加数据，用于在业务出错时将buffer区数据写入文件尾
     */
    private void appendFile(File file, byte[] buffer) {
        try {
            if (!file.exists()) {
                boolean b = file.createNewFile();
                if (!b) {
                    Log.e(TAG, "appendFile: 创建文件失败，无法保存文件");
                }
            }
            RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
            /*创建一个RandomAccessFile对象用来读写文件。传入的参数为文件名和"rw"，表示既可以读取也可以写入*/
            randomFile.seek(file.length());
            /*将读写指针移动到文件末尾*/
            randomFile.write(buffer);
            /*将buffer写入文件末尾*/
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void startSpeaking() {
//        changeToTextView();

//        changeEditTextHeight(ed_input);

        texts = ed_input.getText().toString().trim();


        if (texts.isEmpty()) {
            showTip("请先输入文本再点击合成播放~");
        } else {
            //设置参数
            setParam();
            //修改状态
            isSpeaking = true;
            isPrepared = true;
/*            //修改按钮图标
            audio_control.setImageResource(R.drawable.vector_drawable_pause);*/
            //调用合成播放方法
            mTts.startSpeaking(texts, mTtsListener);





        }


    }


    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(isSpeaking)
            scrollView.smoothScrollTo(0,ed_input.getHeight()/100*mPercentForPlaying);
        }
    };


    private void pause() {

        mTts.pauseSpeaking();
        isSpeaking = false;
    }


    private void resume() {
        isSpeaking = true;
        setParam();
        mTts.resumeSpeaking();


    }


    private void cancel() {
        isPrepared = false;
        isSpeaking = false;
        mTts.stopSpeaking();
        playingCompleted();

    }


    @Override
    protected void onDestroy() {
        if (null != mTts) {
           cancel();
           timer.cancel();
            // 退出时释放连接
            mTts.destroy();
        }
        super.onDestroy();
    }


    private void removeTextBackground() {
        ed_input.setText(ed_input.getText().toString());
    }


    private void unfocused(){
//        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        ed_input.setFocusable(false);
    }


    private void focusable(){
        ed_input.setFocusable(true);
        ed_input.setFocusableInTouchMode(true);
        ed_input.requestFocus();
        ed_input.setSelection(0);

    }


    private int getTextHeight(EditText editText) {
        Layout layout = editText.getLayout();
        if (layout != null) {
            int totalLineCount = layout.getLineCount();
            int paddingTop = editText.getPaddingTop();
            int paddingBottom = editText.getPaddingBottom();
            int lineHeight = layout.getLineBottom(totalLineCount - 1) - layout.getLineTop(0);
            return lineHeight + paddingTop + paddingBottom;
        }
        return 0;
    }


    private void changeEditTextHeight(EditText editText){
    ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
    layoutParams.height = getTextHeight(editText);
    editText.setLayoutParams(layoutParams);

}


    private static final int REQUEST_CODE_PICK_FILE = 1;


    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/msword", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String readFromFile = new ExtractTextFromFile(getContentResolver()).getText(uri);
            if (readFromFile.isEmpty()) {
                showTip("文件可能为空");
            }else{
                if(mSharedPreferences.getString("file_input_way_preference","1").equals("1"))
                ed_input.append(readFromFile);
                else
                    ed_input.setText(readFromFile);
            }
        }
    }

}