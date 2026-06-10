package com.lorabyrd.inventoryapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Activity that allows the user to enable or disable SMS inventory alerts.
 * This screen also requests SMS permission when needed and stores the user's
 * alert preference in SharedPreferences.
 */
public class SmsActivity extends AppCompatActivity {

    // UI components
    private TextView smsStatusTextView;
    private Button smsToggleButton;
    private Button backButton;
    private TextView thresholdStatusTextView;
    private Button changeThresholdButton;

    // SharedPreferences file name and key for storing SMS alert preference
    private static final String PREFS_NAME = "sms_prefs";
    private static final String KEY_SMS_ENABLED = "sms_enabled";
    private static final String KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold";
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    /**
     * Handles the runtime permission request for SEND_SMS.
     * If permission is granted, SMS alerts are enabled automatically.
     * If permission is denied, SMS alerts remain disabled.
     */
    private final ActivityResultLauncher<String> requestSmsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveSmsEnabledPreference(true);
                    updateSmsStatusDisplay(true);
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    saveSmsEnabledPreference(false);
                    updateSmsStatusDisplay(false);
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        initializeViews();
        setupClickListeners();

        // Display the user's saved SMS alert preference when the screen opens
        updateSmsStatusDisplay(isSmsEnabled());

        updateThresholdStatusDisplay();

        // Ensure a default low-stock threshold exists
        saveLowStockThreshold(
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .getInt(KEY_LOW_STOCK_THRESHOLD,
                                DEFAULT_LOW_STOCK_THRESHOLD)
        );
    }

    /**
     * Finds and stores references to the views used in this activity.
     */
    private void initializeViews() {
        smsStatusTextView = findViewById(R.id.textViewSmsStatus);
        smsToggleButton = findViewById(R.id.buttonEnableSms);
        backButton = findViewById(R.id.buttonBack);
        thresholdStatusTextView = findViewById(R.id.textViewThresholdStatus);
        changeThresholdButton = findViewById(R.id.buttonChangeThreshold);
    }

    /**
     * Sets up button click behavior for the SMS toggle and back button.
     */
    private void setupClickListeners() {
        smsToggleButton.setOnClickListener(v -> handleSmsToggleClick());
        backButton.setOnClickListener(v -> finish());
        changeThresholdButton.setOnClickListener(v -> showThresholdDialog());
    }

    /**
     * Handles the SMS toggle button click.
     * If SMS permission has not been granted, requests permission first.
     * Otherwise, toggles the saved SMS alert setting on or off.
     */
    private void handleSmsToggleClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            return;
        }

        boolean isCurrentlyEnabled = isSmsEnabled();
        boolean newSmsState = !isCurrentlyEnabled;

        saveSmsEnabledPreference(newSmsState);
        updateSmsStatusDisplay(newSmsState);

        if (newSmsState) {
            Toast.makeText(this, "SMS alerts enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS alerts disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the on-screen SMS status text, color, and button label
     * based on whether SMS alerts are currently enabled.
     *
     * @param isEnabled true if SMS alerts are enabled; false otherwise
     */
    private void updateSmsStatusDisplay(boolean isEnabled) {
        if (isEnabled) {
            smsStatusTextView.setText(getString(R.string.sms_status_granted));
            smsStatusTextView.setTextColor(
                    ContextCompat.getColor(this, R.color.status_green)
            );
            smsToggleButton.setText(getString(R.string.button_disable_sms));
        } else {
            smsStatusTextView.setText(getString(R.string.sms_status_denied));
            smsStatusTextView.setTextColor(
                    ContextCompat.getColor(this, R.color.status_red)
            );
            smsToggleButton.setText(getString(R.string.button_enable_sms));
        }
    }

    /**
     * Saves the user's SMS alert preference in SharedPreferences.
     *
     * @param isEnabled true to enable SMS alerts; false to disable them
     */
    private void saveSmsEnabledPreference(boolean isEnabled) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_SMS_ENABLED, isEnabled).apply();
    }

    /**
     * Returns the saved SMS alert preference for this activity.
     *
     * @return true if SMS alerts are enabled; false otherwise
     */
    private boolean isSmsEnabled() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_SMS_ENABLED, false);
    }

    /**
     * Saves the user's preferred low inventory threshold.
     *
     * @param threshold the inventory level that triggers alerts
     */
    private void saveLowStockThreshold(int threshold) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        preferences.edit()
                .putInt(KEY_LOW_STOCK_THRESHOLD, threshold)
                .apply();
    }

    /**
     * Retrieves the saved low inventory threshold.
     *
     * @return the saved threshold value or the default value
     */
    public static int getLowStockThreshold(AppCompatActivity activity) {
        SharedPreferences preferences =
                activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        return preferences.getInt(
                KEY_LOW_STOCK_THRESHOLD,
                DEFAULT_LOW_STOCK_THRESHOLD
        );
    }

    /**
     * Shows a dialog that allows the user to change the low inventory threshold.
     */
    private void showThresholdDialog() {
        EditText thresholdInput = new EditText(this);
        thresholdInput.setHint("Enter threshold number");
        thresholdInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        thresholdInput.setText(String.valueOf(getLowStockThreshold(this)));

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Change Low Inventory Threshold")
                .setMessage("Enter the inventory quantity that should trigger a low-stock alert.")
                .setView(thresholdInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String thresholdText = thresholdInput.getText().toString().trim();

                    if (thresholdText.isEmpty()) {
                        Toast.makeText(this,
                                "Threshold is required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int threshold;

                    try {
                        threshold = Integer.parseInt(thresholdText);

                        if (threshold < 0) {
                            Toast.makeText(this,
                                    "Threshold cannot be negative",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } catch (NumberFormatException exception) {
                        Toast.makeText(this,
                                "Enter a valid number",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveLowStockThreshold(threshold);
                    updateThresholdStatusDisplay();

                    Toast.makeText(this,
                            "Low inventory threshold updated",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates the displayed low inventory threshold value.
     */
    private void updateThresholdStatusDisplay() {
        int threshold = getLowStockThreshold(this);

        thresholdStatusTextView.setText(
                getString(R.string.current_low_inventory_threshold, threshold)
        );
    }

    /**
     * Static helper method that allows other activities to check whether
     * SMS alerts are currently enabled.
     *
     * @param activity the calling activity
     * @return true if SMS alerts are enabled; false otherwise
     */
    public static boolean isSmsEnabled(AppCompatActivity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_SMS_ENABLED, false);
    }


}