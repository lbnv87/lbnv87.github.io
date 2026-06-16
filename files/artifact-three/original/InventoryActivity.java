package com.lorabyrd.inventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * InventoryActivity displays and manages the inventory list.
 * Users can add, update, and delete items, as well as receive
 * SMS alerts when inventory levels are low.
 */
public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    // UI components
    private RecyclerView recyclerViewInventory;
    private android.widget.Button buttonAddItem;
    private android.widget.Button buttonSmsSettings;

    // Data and adapter
    private DatabaseHelper databaseHelper;
    private InventoryAdapter adapter;
    private ArrayList<InventoryItem> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupClickListeners();
    }

    /**
     * Initializes UI components from the layout.
     */
    private void initializeViews() {
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonSmsSettings = findViewById(R.id.buttonSmsSettings);
    }

    /**
     * Initializes the database helper instance.
     */
    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    /**
     * Sets up the RecyclerView with layout manager and adapter.
     */
    private void setupRecyclerView() {
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));

        itemList = databaseHelper.getAllItems();
        adapter = new InventoryAdapter(itemList, this);
        recyclerViewInventory.setAdapter(adapter);
    }

    /**
     * Assigns click listeners to buttons.
     */
    private void setupClickListeners() {
        buttonAddItem.setOnClickListener(v -> showAddItemDialog());

        buttonSmsSettings.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, SmsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Displays a dialog allowing the user to add a new inventory item.
     */
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Inventory Item");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        // Input field for item name
        final EditText editTextName = new EditText(this);
        editTextName.setHint("Item name");
        layout.addView(editTextName);

        // Input field for quantity
        final EditText editTextQuantity = new EditText(this);
        editTextQuantity.setHint("Quantity");
        editTextQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(editTextQuantity);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = editTextName.getText().toString().trim();
            String quantityText = editTextQuantity.getText().toString().trim();

            if (itemName.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            boolean inserted = databaseHelper.insertItem(itemName, quantity);

            if (inserted) {
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
                refreshInventory();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Reloads inventory data from the database and updates the adapter.
     */
    private void refreshInventory() {
        itemList = databaseHelper.getAllItems();
        adapter.updateList(itemList);
    }

    /**
     * Increases the quantity of an item by 1.
     *
     * @param item the selected inventory item
     */
    @Override
    public void onIncreaseClick(InventoryItem item) {
        int newQuantity = item.getQuantity() + 1;
        boolean success = databaseHelper.updateQuantity(item.getId(), newQuantity);

        if (success) {
            refreshInventory();
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Decreases the quantity of an item by 1.
     * Sends an SMS alert if the quantity falls below or equal to 5.
     *
     * @param item the selected inventory item
     */
    @Override
    public void onDecreaseClick(InventoryItem item) {
        int currentQuantity = item.getQuantity();

        if (currentQuantity > 0) {
            int newQuantity = currentQuantity - 1;
            boolean success = databaseHelper.updateQuantity(item.getId(), newQuantity);

            if (success) {
                refreshInventory();

                // Trigger SMS alert when inventory is low
                if (newQuantity <= 5) {
                    sendLowInventorySms(item.getName(), newQuantity);
                }
            } else {
                Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Quantity cannot go below 0", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes an item from the inventory.
     *
     * @param item the selected inventory item
     */
    @Override
    public void onDeleteClick(InventoryItem item) {
        boolean success = databaseHelper.deleteItem(item.getId());

        if (success) {
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
            refreshInventory();
        } else {
            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends an SMS alert when inventory is low.
     * Checks if SMS is enabled and permission is granted before sending.
     *
     * @param itemName the name of the item
     * @param quantity the current quantity
     */
    private void sendLowInventorySms(String itemName, int quantity) {

        // Check if user has enabled SMS alerts
        if (!SmsActivity.isSmsEnabled(this)) {
            return;
        }

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String phoneNumber = "5551234567"; // Emulator/test number
        String message = "Low inventory alert: " + itemName
                + " is low. Current quantity: " + quantity;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Low inventory SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
        }
    }
}