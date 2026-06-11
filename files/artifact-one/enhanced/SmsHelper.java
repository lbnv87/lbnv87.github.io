package com.lorabyrd.inventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/**
 * SmsHelper handles SMS-related functionality for the inventory app.
 * This class centralizes SMS validation and message sending logic.
 */
public class SmsHelper {

    /**
     * Sends a low inventory SMS alert if SMS notifications are enabled
     * and permission has been granted.
     *
     * @param context the application context
     * @param itemName the inventory item name
     * @param quantity the current inventory quantity
     */
    public static void sendLowInventorySms(
            Context context,
            String itemName,
            int quantity
    ) {

        // Check whether SMS alerts are enabled
        if (!(context instanceof androidx.appcompat.app.AppCompatActivity)) {
            return;
        }

        androidx.appcompat.app.AppCompatActivity activity =
                (androidx.appcompat.app.AppCompatActivity) context;

        if (!SmsActivity.isSmsEnabled(activity)) {
            return;
        }

        // Check SMS permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String phoneNumber = "5551234567";

        String message =
                "Low inventory alert: "
                        + itemName
                        + " is low. Current quantity: "
                        + quantity;

        try {

            SmsManager smsManager = SmsManager.getDefault();

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );

            Toast.makeText(
                    context,
                    "Low inventory SMS sent",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception exception) {

            Toast.makeText(
                    context,
                    "SMS failed to send",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}