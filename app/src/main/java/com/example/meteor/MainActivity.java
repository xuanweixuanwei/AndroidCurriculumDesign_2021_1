package com.example.meteor;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.meteor.activity.SettingsActivity;
import com.example.meteor.service.WakeUpService;
import com.example.meteor.settingFragments.WakeUpSettingsFragment;
import com.example.myapplication.R;
import com.example.meteor.activity.CharacterRecognitionActivity;
import com.example.meteor.activity.UserInfoActivity;
import com.example.meteor.activity.SpeechRecognitionActivity;
import com.example.meteor.activity.VoiceSynthesisActivity;
import com.example.meteor.roomDatabase.database.AppDatabase;
import com.iflytek.cloud.util.ResourceUtil;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Intent intent;
    private View.OnClickListener listener;
    private Button toCharacterRecognition;
    private Button toSpeechRecognition;
    private Button toVoiceSynthesis;
    private LinearLayout tts_view;
    private LinearLayout asr_view;
    private LinearLayout ocr_view;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAccount();
        initWakeUpService();
        initView();
        initListener();
//        申请网络权限等
        requestPermissions();

    }

    private void initWakeUpService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(MainActivity.this, WakeUpService.class);
        intent.putExtra("data", "hello");
        bindService( intent, connection, BIND_AUTO_CREATE);
    }

    private void initAccount() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (AppDatabase.getInstance(getApplicationContext())
                        .AccountDao()
                        .findAccountByEmail(
                                getSharedPreferences(AppConstant.preferenceFileName, MODE_PRIVATE)
                                        .getString(AppConstant.userEmail, "")
                        ) == null) {

                    Toast.makeText(MainActivity.this, "用户登陆信息失效，请重新登陆", Toast.LENGTH_SHORT).show();
                    Logout();
                }
            }
        });
        executorService.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit_account:
                Logout();
                break;
            case R.id.user_info_setting:
                startActivity(new Intent(MainActivity.this, UserInfoActivity.class));
                break;
            case R.id.wake_up_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class).putExtra(getString(R.string.class_name), WakeUpSettingsFragment.class.getSimpleName()));
        }
        return super.onOptionsItemSelected(item);
    }

    private void Logout() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getSharedPreferences(AppConstant.preferenceFileName, MODE_PRIVATE).edit()
                        .putBoolean(AppConstant.loginState, false)
                        .putString(AppConstant.userEmail, "")
                        .putString(AppConstant.userPasswordSHA, "")
                        .apply();
                finish();
            }
        };
        timer.schedule(task, 1000);
    }

    private final ActivityResultLauncher permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.INTERNET) != null
                        && result.get(Manifest.permission.RECORD_AUDIO) != null
                        && result.get(Manifest.permission.CAMERA) != null
                        && result.get(Manifest.permission.READ_PHONE_STATE) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.INTERNET)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission
                            .RECORD_AUDIO)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.CAMERA))
                            .equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.READ_PHONE_STATE)).equals(true)) {
                        Toast.makeText(MainActivity.this, "联网和获取手机状态权限获取成功", Toast.LENGTH_SHORT).show();
                        //权限全部获取到之后的动作
                    } else {
//                        TODO 改成alertdialog，选择手动开启就跳转系统权限设置界面
                        Toast.makeText(MainActivity.this, "可能存在部分权限获取失败，语音合成等功能无法实现",
                                Toast.LENGTH_SHORT).show();

//                          有权限没有获取到的动作
                    }
                }
            });

    //② 在需要的时候启动权限请求（封装一下）
    private void requestPermissions() {
        //权限数组
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };
        permissionLauncher.launch(permissions);
    }

    private void initView() {
        toCharacterRecognition = findViewById(R.id.toCharacterRecognitionActivity);
        toSpeechRecognition = findViewById(R.id.toSpeechRecognitionActivity);
        toVoiceSynthesis = findViewById(R.id.toVoiceSynthesisActivity);
        tts_view = findViewById(R.id.tts_view);
        asr_view = findViewById(R.id.asr_view);
        ocr_view = findViewById(R.id.ocr_view);


    }

    private void initListener() {
        listener = view -> {
            if (view == toCharacterRecognition || view == ocr_view) {
                intent = new Intent(MainActivity.this, CharacterRecognitionActivity.class);
                startActivity(intent);
            } else if (view == toSpeechRecognition || view == asr_view) {
                intent = new Intent(MainActivity.this, SpeechRecognitionActivity.class);
                startActivity(intent);
            } else if (view == toVoiceSynthesis || view == tts_view) {
                intent = new Intent(MainActivity.this, VoiceSynthesisActivity.class);
                startActivity(intent);
            }
        };
        asr_view.setOnClickListener(listener);
        tts_view.setOnClickListener(listener);
        ocr_view.setOnClickListener(listener);
        toCharacterRecognition.setOnClickListener(listener);
        toSpeechRecognition.setOnClickListener(listener);
        toVoiceSynthesis.setOnClickListener(listener);
    }

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(MainActivity.this,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.app_id) + ".jet");
        Timber.e("resPath: " + resPath);
        return resPath;
    }


}



