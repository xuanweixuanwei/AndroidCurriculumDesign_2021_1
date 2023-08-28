package com.example.myapplication.settingFragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.myapplication.R;

import util.InputFilterMinMax;

public class VoiceSynthesisSettingsFragment extends PreferenceFragmentCompat {

    private final static VoiceSynthesisSettingsFragment instance = new VoiceSynthesisSettingsFragment();

    private androidx.preference.EditTextPreference mSpeedPreference;
    private androidx.preference.EditTextPreference mPitchPreference;
    private androidx.preference.EditTextPreference mVolumePreference;

    public static VoiceSynthesisSettingsFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_voice_synthesis, rootKey);

        mSpeedPreference = (EditTextPreference)findPreference("speed_preference");
        mPitchPreference = (EditTextPreference) findPreference("pitch_preference");
        mVolumePreference = (EditTextPreference) findPreference("volume_preference");

        setEditListener();


    }

    private void setEditListener() {
        assert mSpeedPreference != null;
        mSpeedPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 200)});
            }
        });

        assert mPitchPreference != null;
        mPitchPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
            }
        });

        assert mVolumePreference != null;
        mVolumePreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
            }
        });

    }
}