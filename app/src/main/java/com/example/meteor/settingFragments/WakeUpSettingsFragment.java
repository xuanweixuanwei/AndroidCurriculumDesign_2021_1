package com.example.meteor.settingFragments;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.meteor.util.*;
import com.example.myapplication.R;

public class WakeUpSettingsFragment extends PreferenceFragmentCompat {

    private final static WakeUpSettingsFragment instance = new WakeUpSettingsFragment();
    EditTextPreference wakeUpEnablePreference;

    public static WakeUpSettingsFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_wake_up, rootKey);

         wakeUpEnablePreference = (EditTextPreference) findPreference("wake_up_thresh");

        setEditListener();
    }

    private void setEditListener() {
        wakeUpEnablePreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 3000)});
            }
        });
    }
}