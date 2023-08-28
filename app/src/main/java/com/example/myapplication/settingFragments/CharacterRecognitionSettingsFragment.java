package com.example.myapplication.settingFragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import com.example.myapplication.R;

public class CharacterRecognitionSettingsFragment extends PreferenceFragmentCompat {

    private final static CharacterRecognitionSettingsFragment instance = new CharacterRecognitionSettingsFragment();

    public static CharacterRecognitionSettingsFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_character_recognition, rootKey);
    }
}