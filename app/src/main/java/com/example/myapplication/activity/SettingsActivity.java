package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.settingFragments.CharacterRecognitionSettingsFragment;
import com.example.myapplication.settingFragments.SpeechRecognitionSettingsFragment;
import com.example.myapplication.settingFragments.VoiceSynthesisSettingsFragment;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsActivity extends AppCompatActivity {

    private PreferenceFragmentCompat preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        if (intent != null) {
            preferenceFragment = getFragment(intent.getStringExtra("className"));
            setFragment();
        } else {
            back();
        }
    }


    @SuppressWarnings("unchecked")
    private <F extends PreferenceFragmentCompat> F getFragment(String className) {
        if (className.equals(VoiceSynthesisActivity.class.getSimpleName())) {
            return (F) VoiceSynthesisSettingsFragment.getInstance();
        } else if (className.equals(SpeechRecognitionActivity.class.getSimpleName())) {
            return (F) SpeechRecognitionSettingsFragment.getInstance();
        } else if (className.equals(CharacterRecognitionActivity.class.getSimpleName())) {
            return (F) CharacterRecognitionSettingsFragment.getInstance();
        } else {
            return null;
        }
    }

    private void setFragment() {
        if (preferenceFragment != null) {
            this.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.setting_fragment, preferenceFragment)
                    .commit();
        } else {
            back();
        }
    }

    public void back() {
        Toast.makeText(
                SettingsActivity.this,
                "程序员开小差啦，携带参数出错，即将跳转回上一个页面",
                Toast.LENGTH_SHORT)
                .show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

}