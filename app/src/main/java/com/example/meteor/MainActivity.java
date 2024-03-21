package com.example.meteor;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.meteor.activity.CharacterRecognitionActivity;
import com.example.meteor.activity.InfoActivity;
import com.example.meteor.activity.SpeechRecognitionActivity;
import com.example.meteor.activity.VoiceSynthesisActivity;
import com.example.meteor.roomDatabase.database.AppDatabase;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private View.OnClickListener listener;
    private Button toCharacterRecognition;
    private Button toSpeechRecognition;
    private Button toVoiceSynthesis;
    private LinearLayout tts_view;
    private LinearLayout asr_view;
    private LinearLayout ocr_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAccount();
        initView();
        initListener();
//        申请网络权限等
        requestPermissions();

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
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
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
//                        && result.get(Manifest.permission.RECORD_AUDIO) != null
//                        && result.get(Manifest.permission.CAMERA) != null
                        && result.get(Manifest.permission.READ_PHONE_STATE) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.INTERNET)).equals(true)
//                            && Objects.requireNonNull(result.get(Manifest.permission
//                            .RECORD_AUDIO)).equals(true)
//                            && Objects.requireNonNull(result.get(Manifest.permission.CAMERA))
//                            .equals(true)
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
                Manifest.permission.READ_PHONE_STATE
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
}



