package com.kas.authenticationwithfirebase.ui.settings;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AppearanceViewModel extends AndroidViewModel {

    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String TEXT_SIZE_KEY = "text_size";

    public static final float SMALL_TEXT_SIZE = 14f;
    public static final float MEDIUM_TEXT_SIZE = 16f;
    public static final float LARGE_TEXT_SIZE = 18f;

    private final MutableLiveData<Boolean> isDarkMode = new MutableLiveData<>();
    private final MutableLiveData<Float> textSize = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;

    @Inject
    public AppearanceViewModel(Application application, SharedPreferences sharedPreferences) {
        super(application);
        this.sharedPreferences = sharedPreferences;

        // Load the current dark mode setting and text size
        boolean currentMode = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        isDarkMode.setValue(currentMode);

        float currentTextSize = sharedPreferences.getFloat(TEXT_SIZE_KEY, MEDIUM_TEXT_SIZE);
        textSize.setValue(currentTextSize);
    }

    public LiveData<Boolean> getIsDarkMode() {
        return isDarkMode;
    }

    public LiveData<Float> getTextSize() {
        return textSize;
    }

    public void toggleDarkMode(boolean enabled) {
        // Update the LiveData
        isDarkMode.setValue(enabled);

        // Save the preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DARK_MODE_KEY, enabled);
        editor.apply();

        // Apply the new theme
        AppCompatDelegate.setDefaultNightMode(enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void setTextSize(float size) {
        textSize.setValue(size);
        // Save the preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(TEXT_SIZE_KEY, size);
        editor.apply();
    }
}
