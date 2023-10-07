package com.example.myapplication;


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
import android.widget.Toast;

import com.example.myapplication.activity.CharacterRecognitionActivity;
import com.example.myapplication.activity.InfoActivity;
import com.example.myapplication.activity.SpeechRecognitionActivity;
import com.example.myapplication.activity.VoiceSynthesisActivity;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
//        申请网络权限等
        requestPermissions();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {


            case R.id.exit_account:
//           TODO      exit();
                break;
            case R.id.user_info_setting:
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private final ActivityResultLauncher permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.INTERNET) != null
//                        && result.get(Manifest.permission.RECORD_AUDIO) != null
//                        && result.get(Manifest.permission.CAMERA) != null
                        && result.get(Manifest.permission.READ_PHONE_STATE) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.INTERNET)).equals(true)
//                            && Objects.requireNonNull(result.get(Manifest.permission.RECORD_AUDIO)).equals(true)
//                            && Objects.requireNonNull(result.get(Manifest.permission.CAMERA)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.READ_PHONE_STATE)).equals(true)) {
                        Toast.makeText(MainActivity.this, "联网和获取手机状态权限获取成功", Toast.LENGTH_SHORT).show();
                        //权限全部获取到之后的动作
                    } else {
//                        TODO 改成alertdialog，选择手动开启就跳转系统权限设置界面
                        Toast.makeText(MainActivity.this, "可能存在部分权限获取失败，语音合成等功能无法实现", Toast.LENGTH_SHORT).show();

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
        Button toCharacterRecognition = findViewById(R.id.toCharacterRecognitionActivity);
        Button toSpeechRecognition = findViewById(R.id.toSpeechRecognitionActivity);
        Button toVoiceSynthesis = findViewById(R.id.toVoiceSynthesisActivity);

        View.OnClickListener listener = view -> {
            switch (view.getId()) {
                case R.id.toCharacterRecognitionActivity:
                    intent = new Intent(MainActivity.this, CharacterRecognitionActivity.class);
                    startActivity(intent);
                    break;
                case R.id.toSpeechRecognitionActivity:
                    intent = new Intent(MainActivity.this, SpeechRecognitionActivity.class);
                    startActivity(intent);
                    break;
                case R.id.toVoiceSynthesisActivity:
                    intent = new Intent(MainActivity.this, VoiceSynthesisActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        };

        toVoiceSynthesis.setOnClickListener(listener);
        toSpeechRecognition.setOnClickListener(listener);
        toCharacterRecognition.setOnClickListener(listener);
    }
}

