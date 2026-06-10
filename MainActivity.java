package com.lorabyrd.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity handles user login and account creation for the inventory app.
 * Users can create a new account or log in with existing credentials.
 */
public class MainActivity extends AppCompatActivity {

    // User input fields
    private EditText usernameEditText;
    private EditText passwordEditText;

    // Action buttons
    private Button loginButton;
    private Button createAccountButton;

    // Database helper for user account operations
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeDatabase();
        setupClickListeners();
    }

    /**
     * Finds and stores references to the views used in this activity.
     */
    private void initializeViews() {
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        createAccountButton = findViewById(R.id.buttonCreateAccount);
    }

    /**
     * Initializes the database helper used for authentication tasks.
     */
    private void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    /**
     * Assigns click behavior to the login and account creation buttons.
     */
    private void setupClickListeners() {
        createAccountButton.setOnClickListener(v -> createAccount());
        loginButton.setOnClickListener(v -> loginUser());
    }

    /**
     * Creates a new user account if the username does not already exist
     * and both fields contain valid input.
     */
    private void createAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (isInputInvalid(username, password)) {
            showToast("Enter username and password");
            return;
        }

        if (databaseHelper.userExists(username)) {
            showToast("Username already exists");
            return;
        }

        boolean wasUserInserted = databaseHelper.insertUser(username, password);

        if (wasUserInserted) {
            showToast("Account created successfully");
            clearInputFields();
        } else {
            showToast("Account creation failed");
        }
    }

    /**
     * Attempts to log the user in using the provided username and password.
     * If successful, the app opens the inventory screen.
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (isInputInvalid(username, password)) {
            return;
        }

        boolean isValidLogin = databaseHelper.checkUser(username, password);

        if (isValidLogin) {
            showToast("Login successful");

            Intent inventoryIntent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(inventoryIntent);
        } else {
            showToast("Invalid username or password");
        }
    }

    /**
     * Validates username and password input.
     * Username requirements:
     * - Cannot be empty
     * - Must be at least 4 characters
     * - Cannot contain spaces
     * Password requirements:
     * - Cannot be empty
     * - Must be at least 6 characters
     *
     * @param username entered username
     * @param password entered password
     * @return true if input is invalid; false otherwise
     */
    private boolean isInputInvalid(String username, String password) {

        // Check for empty fields
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToast("Username and password are required");
            return true;
        }

        // Username length validation
        if (username.length() < 4) {
            showToast("Username must be at least 4 characters");
            return true;
        }

        // Prevent spaces in usernames
        if (username.contains(" ")) {
            showToast("Username cannot contain spaces");
            return true;
        }

        // Password length validation
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return true;
        }

        return false;
    }

    /**
     * Clears the username and password input fields after account creation.
     */
    private void clearInputFields() {
        usernameEditText.setText("");
        passwordEditText.setText("");
    }

    /**
     * Displays a short toast message to the user.
     *
     * @param message the message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}