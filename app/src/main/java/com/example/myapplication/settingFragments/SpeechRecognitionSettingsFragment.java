package com.example.myapplication.settingFragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.myapplication.R;

import com.example.myapplication.util.InputFilterMinMax;

public class SpeechRecognitionSettingsFragment extends PreferenceFragmentCompat {

    private final static SpeechRecognitionSettingsFragment instance = new SpeechRecognitionSettingsFragment();

    private androidx.preference.EditTextPreference mVadbosPreference;
    private androidx.preference.EditTextPreference mVadeosPreference;

    public static SpeechRecognitionSettingsFragment getInstance(){
        return instance;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_speech_recognition, rootKey);

        mVadbosPreference = (EditTextPreference)findPreference("iat_vadbos_preference");
        mVadeosPreference = (EditTextPreference) findPreference("iat_vadeos_preference");

        setEditListener();
    }

    private void setEditListener() {

        mVadbosPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 10000)});
            }
        });

        mVadeosPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 10000)});
            }
        });
    }
}