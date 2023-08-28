package com.example.myapplication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.activity.CharacterRecognitionActivity;
import com.example.myapplication.activity.SpeechRecognitionActivity;
import com.example.myapplication.activity.VoiceSynthesisActivity;

import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        requestPermissions();


    }


    private final ActivityResultLauncher permissionLauncher2 = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.INTERNET) != null
                        && result.get(Manifest.permission.RECORD_AUDIO) != null
                        && result.get(Manifest.permission.CAMERA) != null
                        && result.get(Manifest.permission.READ_PHONE_STATE) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.INTERNET)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.RECORD_AUDIO)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.CAMERA)).equals(true)
                            && Objects.requireNonNull(result.get(Manifest.permission.READ_PHONE_STATE)).equals(true)) {
                        Toast.makeText(MainActivity.this, "权限获取成功", Toast.LENGTH_SHORT).show();
                        //权限全部获取到之后的动作
                    } else {
                        Toast.makeText(MainActivity.this, "权限获取失败，可能存在部分功能无法实现", Toast.LENGTH_SHORT).show();

//                          有权限没有获取到的动作
                    }
                }
            });

    //② 在需要的时候启动权限请求（封装一下）
    private void requestPermissions() {
        //权限数组
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE
        };

        permissionLauncher2.launch(permissions);
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

