package com.kas.authenticationwithfirebase.ui.settings;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.kas.authenticationwithfirebase.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppearanceActivity extends AppCompatActivity {

    private Switch switchDarkMode;
    private Spinner spinnerTextSize;
    private AppearanceViewModel appearanceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");

            // Set the color of the back button
            Drawable backArrow = getResources().getDrawable(R.drawable.baseline_arrow_back_24); // or use `AppCompatResources.getDrawable()` for better compatibility
            backArrow.setColorFilter(getResources().getColor(R.color.top_nav_text_color), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(backArrow);
        }

        // Initialize the ViewModel
        appearanceViewModel = new ViewModelProvider(this).get(AppearanceViewModel.class);

        // Initialize the switch and spinner
        switchDarkMode = findViewById(R.id.switchDarkMode);
        spinnerTextSize = findViewById(R.id.spinnerTextSize);

        // Set up spinner options for text size
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.text_size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTextSize.setAdapter(adapter);

        // Observe text size and update spinner selection
        appearanceViewModel.getTextSize().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float textSize) {
                int position = textSize.equals(AppearanceViewModel.SMALL_TEXT_SIZE) ? 0 :
                        textSize.equals(AppearanceViewModel.MEDIUM_TEXT_SIZE) ? 1 : 2;
                spinnerTextSize.setSelection(position);
            }
        });

        // Set listener for text size changes
        spinnerTextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float selectedSize = position == 0 ? AppearanceViewModel.SMALL_TEXT_SIZE :
                        position == 1 ? AppearanceViewModel.MEDIUM_TEXT_SIZE : AppearanceViewModel.LARGE_TEXT_SIZE;
                appearanceViewModel.setTextSize(selectedSize);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // You can leave this empty if there's no specific action when nothing is selected
            }
        });


        // Observe dark mode setting
        appearanceViewModel.getIsDarkMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDarkMode) {
                switchDarkMode.setChecked(isDarkMode != null && isDarkMode);
            }
        });

        // Set listener for dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                appearanceViewModel.toggleDarkMode(isChecked)
        );
    }

    // Handle back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
