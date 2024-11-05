package com.kas.authenticationwithfirebase.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.kas.authenticationwithfirebase.R;

public class SettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the fragment to have its own menu
        //setHasOptionsMenu(true);
        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDarkMode();
        // Setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        setupToolbar(toolbar);

        // Set up click listeners for each settings option
        view.findViewById(R.id.chat_option).setOnClickListener(v -> openAppearanceSettings());
        view.findViewById(R.id.storage_data_option).setOnClickListener(v -> openStorageDataSettings());
         view.findViewById(R.id.notifications_option).setOnClickListener(v -> openNotificationSettings());
    }

    private void setupToolbar(Toolbar toolbar) {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("");

            // Set the color of the back button
            Drawable backArrow = getResources().getDrawable(R.drawable.baseline_arrow_back_24);
            backArrow.setColorFilter(getResources().getColor(R.color.top_nav_text_color), PorterDuff.Mode.SRC_ATOP);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setHomeAsUpIndicator(backArrow);
        }
    }
    private void setupDarkMode() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
    private void openAppearanceSettings() {
        startActivity(new Intent(getActivity(), AppearanceActivity.class));
    }

    private void openStorageDataSettings() {
        startActivity(new Intent(getActivity(), StorageAndDataActivity.class));
    }
    private void openNotificationSettings() {
        startActivity(new Intent(getActivity(), NotificationSettingsActivity.class));
    }

    // Handle back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().getSupportFragmentManager().popBackStack(); // Go back to the previous fragment
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
