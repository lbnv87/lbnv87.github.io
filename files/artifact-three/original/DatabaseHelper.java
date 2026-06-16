package com.lorabyrd.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * DatabaseHelper manages the local SQLite database for the Inventory App.
 * This class handles database creation, upgrades, user account operations,
 * and inventory item operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "InventoryApp.db";
    private static final int DATABASE_VERSION = 1;

    // Users table and column names
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Inventory table and column names
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_ITEM_QUANTITY = "quantity";

    /**
     * Constructs a new DatabaseHelper instance.
     *
     * @param context the application context used to access the database
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database tables when the database is first initialized.
     *
     * @param db the SQLite database being created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";

        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_ITEM_QUANTITY + " INTEGER)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
    }

    /**
     * Upgrades the database when the version number changes.
     * This implementation drops existing tables and recreates them.
     *
     * @param db         the SQLite database being upgraded
     * @param oldVersion the previous database version
     * @param newVersion the new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ---------------- USER METHODS ----------------

    /**
     * Inserts a new user into the users table.
     * <p>
     * Note: For this project, passwords are stored as plain text.
     * In a production application, passwords should be hashed
     * before being stored for security.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     * @return true if the user was inserted successfully; false otherwise
     */
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.trim());
        values.put(COLUMN_PASSWORD, password.trim());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Checks whether a username already exists in the users table.
     *
     * @param username the username to search for
     * @return true if the username exists; false otherwise
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USERNAME + "=?",
                new String[]{username.trim()},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * Validates a user's login credentials by checking whether
     * the provided username and password match a record in the database.
     *
     * @param username the username entered by the user
     * @param password the password entered by the user
     * @return true if the username and password match a stored user; false otherwise
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username.trim(), password.trim()},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // ---------------- INVENTORY METHODS ----------------

    /**
     * Inserts a new inventory item into the inventory table.
     *
     * @param itemName the name of the inventory item
     * @param quantity the quantity of the item
     * @return true if the item was inserted successfully; false otherwise
     */
    public boolean insertItem(String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName.trim());
        values.put(COLUMN_ITEM_QUANTITY, quantity);

        long result = db.insert(TABLE_INVENTORY, null, values);
        return result != -1;
    }

    /**
     * Retrieves all inventory items from the database and returns them
     * as a list sorted alphabetically by item name.
     *
     * @return an ArrayList containing all inventory items
     */
    public ArrayList<InventoryItem> getAllItems() {
        ArrayList<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query all inventory records ordered by item name
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + COLUMN_ITEM_NAME + " ASC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));

                itemList.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return itemList;
    }

    /**
     * Updates the quantity of an existing inventory item.
     *
     * @param itemId      the unique ID of the item to update
     * @param newQuantity the new quantity value to store
     * @return true if at least one row was updated; false otherwise
     */
    public boolean updateQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        int rowsAffected = db.update(
                TABLE_INVENTORY,
                values,
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        return rowsAffected > 0;
    }

    /**
     * Deletes an inventory item from the database.
     *
     * @param itemId the unique ID of the item to delete
     * @return true if at least one row was deleted; false otherwise
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsDeleted = db.delete(
                TABLE_INVENTORY,
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        return rowsDeleted > 0;
    }
}