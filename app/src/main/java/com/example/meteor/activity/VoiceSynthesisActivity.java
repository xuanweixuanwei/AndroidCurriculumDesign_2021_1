package com.example.meteor.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;


import android.app.AlertDialog;

import android.content.Context;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meteor.AppConstant;
import com.example.meteor.action.ToastAction;
import com.example.meteor.dialog.TipsDialog;
import com.example.meteor.roomDatabase.dao.AccountDao;
import com.example.meteor.roomDatabase.dao.UsageLogDao;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.example.meteor.roomDatabase.entity.Account;
import com.example.meteor.roomDatabase.entity.UsageLog;
import com.example.meteor.util.ExtractTextFromFile;
import com.example.myapplication.R;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import timber.log.Timber;

public class VoiceSynthesisActivity extends AppCompatActivity implements ToastAction {

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
    private NestedScrollView scrollView;


    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    private File pcmFile;
    private final Timer timer = new Timer();
    private Account currentUser;
    private UsageLog usageLog;
    private AccountDao accountDao;
    private UsageLogDao usageLogDao;

    private String userEmail;

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
    /*点击播放控制按钮以后，修改播放状态
    --如果语音合成对象不为空
    ---如果当前正在播放，就暂停播放
    ---如果当前未播放，但是合成任务已经设置完成，就继续播放
    ---如果当前未播放，并且合成任务未设置，就调用startSpeaking开启播放任务
    */
    private void changePlayingState() {
        if (mTts == null) {
            showTip("初始化SpeechSynthesizer对象出错，请在真机调试");
        } else {
            Timber.tag("changePlayingState").e(isPrepared + "" + isSpeaking);
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

    /*生命周期函数onCreate，完成主要的初始化操作。
    --设置布局
    --初始化合成对象
    --初始化数据
    --绑定试图
    --编辑框获取焦点
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_synthesis);

        // 初始化合成对象，如果使用虚拟机调试会无法获取合成对象
        // 返回为null，需要用真机调试
        mTts = SpeechSynthesizer.createSynthesizer(VoiceSynthesisActivity.this, mTtsInitListener);

        if (null == mTts) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Timber.e("onCreate: 创建对象失败，请使用真机调试并确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化。");
            this.showTip("创建对象失败，请使用真机调试并确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            finish();
        }
        initData();
        initView();

        focusable();
    }

    /*初始化数据
    --初始化云端合成发音人信息列表、云端合成发音人取值列表
    --获取语音合成的偏好信息
    --获取偏好中存储的用户邮箱
    --根据邮箱查询到对应的Account实体，赋值给currentUser
    */
    private void initData() {
        //初始化云端合成发音人信息列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        //初始化云端合成发音人取值列表
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        //获取默认的偏好信息
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        userEmail = getSharedPreferences(AppConstant.preferenceFileName, Context.MODE_PRIVATE)
                .getString(AppConstant.userEmail, "");


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            AppDatabase appDatabase = AppDatabase.getInstance(getApplicationContext());
            accountDao = appDatabase.AccountDao();
            usageLogDao = appDatabase.UsageLogDao();
            currentUser = accountDao.findAccountByEmail(userEmail);
            usageLog = usageLogDao.getUsageLogForUserOnDate(currentUser.getRowid(), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            if (usageLog == null) {
                usageLog = new UsageLog(currentUser.getRowid(), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                usageLogDao.insert(usageLog);
            }

        });
        executor.shutdown();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /*初始化UI控件
    --设置ActionBar的标题、返回箭头按钮
    --为按钮绑定视图和监听器
    --为编辑框添加监听器，在输入文字前及时调整编辑框的高度（如果输入过长字符串后，EditText的高度也随之改变，导致scrollView无法获取EditText正常的高度并进行滑动）
    --开启定时器任务，每五秒判断一次是否需要更改scrollView的滑动位置
    */
    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("语音合成");
        //在ActionBar最左边显示返回箭头按钮
        actionBar.setDisplayHomeAsUpEnabled(true);

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

            }
        });

        timer.schedule(timerTask, 0, 5000);
        //测试偏好数据
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
                intent.putExtra(getString(R.string.class_name),
                        VoiceSynthesisActivity.class.getSimpleName());
                startActivity(intent);
                Timber.tag(TAG).w("onOptionsItemSelected: " + "点击了Speaker_setting");

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 发音人选择
     * 弹出发音人选择的对话框
     */
    private void showPersonSelectDialog() {

        // 点击单选框后的处理
        new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                        selectedNum, // 默认的选项
                        (dialog, which) -> { // 点击了哪一项
                            voicer = mCloudVoicersValue[which];
                            if ("catherine".equals(voicer) || "henry".equals(voicer) ||
                                    "vimary".equals(voicer)) {
//                                            如果选择的是英文发言人

                                if (ed_input.getText().length() == 0) {
                                    ed_input.setText(R.string.text_tts_source_en);
                                    showTip("英文发音人只能朗读英文，中文无法朗读，已默认导入英文示例");

                                } else {
                                    showTip("英文发音人只能朗读英文，中文无法朗读");
                                }
                            } else {
                                if (ed_input.getText().length() == 0) {
                                    ed_input.setText(R.string.text_tts_source);
                                    showTip("已默认导入中文示例");
                                }
                            }
                            selectedNum = which;
                            dialog.dismiss();
                        }).show();


    }

    /**
     * 参数设置
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
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString(
                    "speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString(
                    "pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString(
                    "volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");

        }

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString(
                "stream_preference", "3"));
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
    private final InitListener mTtsInitListener = code -> {
        Timber.d("InitListener init() code = %s", code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun" +
                    ".cn/document/error-code查询解决方案");
        } else {
            Timber.d("初始化成功");
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
        }
    };

    /**
     * 合成回调监听
     */
    private final SynthesizerListener mTtsListener = new SynthesizerListener() {
        /*-- onSpeakBegin 语音合成开始播放时，回调此方法
        ---将文本编辑框设置为不可获取焦点（不可编辑）
        ---将播放信息playProgressInfo设置为可见
        ---修改播放控制按钮的图片资源
         ---弹窗提示用户“开始播放”
        */
        @Override
        public void onSpeakBegin() {
            unfocused();
            playProgressInfo.setVisibility(View.VISIBLE);
            playProgressInfo.setText("准备播放，缓冲中");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    audioControl.setImageResource(R.drawable.vector_drawable_startplaying);
                    showTip("开始播放");
                }
            });
        }

        /*-- onSpeakPaused 语音合成播放暂停时，回调此方法
            ---修改playProgressInfo的文本为提示信息"点击▷继续播放，点击✖终止播放"
            ---修改播放控制按钮的图片资源
            ---弹窗提示用户“暂停播放”
        */
        @Override
        public void onSpeakPaused() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playProgressInfo.setText("点击▷继续播放，点击✖终止播放");
                    audioControl.setImageResource(R.drawable.vector_drawable_startplaying);
                    showTip("暂停播放");
                }
            });
        }

        /*
        -- onSpeakResumed 语音合成继续播放时，回调此方法
          ---修改playProgressInfo的文本为提示信息“继续播放”
          ---修改播放控制按钮的图片资源
          ---弹窗提示用户“继续播放”
        */
        @Override
        public void onSpeakResumed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playProgressInfo.setText("继续播放");
                    audioControl.setImageResource(R.drawable.vector_drawable_pause);
                    showTip("继续播放");
                }
            });

        }

        /*-- onBufferProgress 缓冲任务执行时，回调此方法
           ---修改全局变量mPercentForBuffering的取值，并打印Log日志
        */
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            Timber.e("percent =%s", percent);
            mPercentForBuffering = percent;

        }

        /*-- onSpeakProgress 播放任务执行时，回调此方法
           ---修改全局变量mPercentForPlaying的取值，并打印Log日志
           ---将正在播放的内容红色高亮显示
           ---修改playProgressInfo的内容为缓冲进度为%d%%，播放进度为%d%%格式字符串
        */
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Timber.e("percent =%s", percent);
            mPercentForPlaying = percent;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playProgressInfo.setText(String.format(getString(R.string.tts_toast_format),
                            mPercentForBuffering, mPercentForPlaying));

                    SpannableStringBuilder style = new SpannableStringBuilder(texts);
                    Timber.e("beginPos = " + beginPos + "  endPos = " + endPos);
                    style.setSpan(new BackgroundColorSpan(Color.RED), beginPos, endPos,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ((EditText) findViewById(R.id.et_input)).setText(style);
                }
            });
        }

        /*-- onCompleted 语音合成任务结束时，回调此方法
           ---将状态位和图片以及提示文本信息都还原，编辑框设置为可点击
           ---弹出完成对话框
           ---如果播放任务出错，将错误信息弹窗显示
        */
        @Override
        public void onCompleted(SpeechError error) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playProgressInfo.setVisibility(View.GONE);
                    audioControl.setImageResource(R.drawable.vector_drawable_start_synthesis);
                }
            });

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

        /*-- onEvent 在语音合成出错时，打印错误日志，便于技术人员定位出错原因*/
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            //	 以下代码用于获取与云端的会话id，
            //	 将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Timber.d("session id =%s", sid);
            }
            // 当设置 SpeechConstant.TTS_DATA_NOTIFY 为1时，抛出buffer数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buffer = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Timber.e("EVENT_TTS_BUFFER = %s", buffer.length);
                // 保存文件
                appendFile(pcmFile, buffer);
            }

        }
    };

    /*自定义方法，在语音合成任务结束时调用，移除文本高亮并且将编辑框设置为可以点击*/
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
                    Timber.e("appendFile: 创建文件失败，无法保存文件");
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

    /*封装弹窗*/
    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    /*开启语音合成播放
    --先增加当前用户的TTS使用次数
    --首先判断当前输入文本是否为空，不为空就无法合成
    --设置语音合成参数、修改播放状态、调用合成播放方法
    */
    private void startSpeaking() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
//                 增加OCR使用次数
            if ((currentUser = accountDao.findAccountByEmail(userEmail)) != null) {

                texts = ed_input.getText().toString().trim();
                if (texts.isEmpty()) {
                    showTip("请先输入文本再点击合成播放~");
                    cancel();
                } else {
                    //设置参数
                    setParam();
                    //修改状态
                    isSpeaking = true;
                    isPrepared = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeEditTextHeight(ed_input);
                        }
                    });
                    //调用合成播放方法
                    mTts.startSpeaking(texts, mTtsListener);
                    accountDao.updateAccount(currentUser.useTts());
                    usageLogDao.update(usageLog.updateTts());
                }
            } else {
                showTip("登陆信息失效，请重新登陆后使用");
            }
        });
        executor.shutdown();
    }

    /*设置定时器任务，在语音合成任务正在播放音频时，根据当前播放进度控制scrollView的位置*/
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (isSpeaking) {
                scrollView.smoothScrollTo(0,
                        ed_input.getHeight() / 100 * mPercentForPlaying);
            }
        }
    };

    /*暂停播放
    --调用语音合成对象的pauseSpeaking方法来实现暂停
    --将标记是否正在播放的isSpeaking修改为false
    */
    private void pause() {
        mTts.pauseSpeaking();
        isSpeaking = false;
    }

    /*恢复播放*/
    private void resume() {
        isSpeaking = true;
        setParam();
        mTts.resumeSpeaking();
    }

    /*取消播放任务*/
    private void cancel() {
        isPrepared = false;
        isSpeaking = false;
        mTts.stopSpeaking();
        playingCompleted();
    }

    /*onDestroy生命周期函数，在Activity被销毁时执行
    --如果mTts（语音合成对象）不为空就暂停播放任务并将其销毁
    --取消定时任务
    */
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

    /*在用户选择结束当前播放任务时，将正在播放的高亮效果取消*/
    private void removeTextBackground() {
        ed_input.setText(ed_input.getText().toString());
    }

    /*将ed_input设置为不可获取焦点，在开始播放任务时被调用，避免用户中途修改文字
    注：事实上即使用户修改了文本内容也不会对正在执行或被暂停的语音合成任务产生影响，只是避免用户修改后发现修改无效，导致不友好的使用体验
    */
    private void unfocused() {
        ed_input.setFocusable(false);
    }

    /*使ed_input获取焦点并将光标置于文本开头，以便用户可以直接在该EditText中输入文本*/
    private void focusable() {
//        设置ed_input可获取焦点
        ed_input.setFocusable(true);
//        设置ed_input在触摸模式下可获取焦点
        ed_input.setFocusableInTouchMode(true);
//        将光标位置设置到文本的开头
        ed_input.requestFocus();
//        将光标位置设置到文本的开头
        ed_input.setSelection(0);
    }

    //获取EditText文本的实际高度
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

    /*根据EditText内容的高度来改变EditText的高度，进行页面重绘，
    避免文本很少但是编辑框高度过高的情况
    */
    private void changeEditTextHeight(EditText editText) {
        ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
        layoutParams.height = getTextHeight(editText);
        editText.setLayoutParams(layoutParams);
    }


    private static final int REQUEST_CODE_PICK_FILE = 1;

    /*使用Intent跳转文件管理器选择TXT/Word文档*/
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String docx_type = "application/vnd.openxmlformats-officedocument.wordprocessingml" +
                ".document";
        String[] mimeTypes = {docx_type, "application/msword", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    /*实现onActivityResult回调，
    用户选取文件失败进行提示，
    选取成功就提取文本并进行显示
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String readFromFile =
                    ExtractTextFromFile.getInstance(getContentResolver()).getText(uri);
            if (readFromFile == null) {
                showTip("文件可能为空");
            } else {
                if (mSharedPreferences.getString("file_input_way_preference", "1").equals("1"))
                    ed_input.append(readFromFile.trim());
                else
                    ed_input.setText(readFromFile.trim());
            }
        }
    }

}