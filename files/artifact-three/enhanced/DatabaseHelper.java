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
    private static final int DATABASE_VERSION = 2;

    // Users table and column names
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Inventory table and column names
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_INVENTORY_USER_ID = "user_id";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_ITEM_QUANTITY = "quantity";

    // Inventory history table and column names
    public static final String TABLE_HISTORY = "inventory_history";
    public static final String COLUMN_HISTORY_ID = "history_id";
    public static final String COLUMN_HISTORY_ITEM_ID = "item_id";
    public static final String COLUMN_HISTORY_ACTION = "action_type";
    public static final String COLUMN_HISTORY_OLD_QTY = "old_quantity";
    public static final String COLUMN_HISTORY_NEW_QTY = "new_quantity";
    public static final String COLUMN_HISTORY_TIMESTAMP = "timestamp";

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
                COLUMN_INVENTORY_USER_ID + " INTEGER, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_ITEM_QUANTITY + " INTEGER)";

        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HISTORY_ITEM_ID + " INTEGER, " +
                COLUMN_HISTORY_ACTION + " TEXT, " +
                COLUMN_HISTORY_OLD_QTY + " INTEGER, " +
                COLUMN_HISTORY_NEW_QTY + " INTEGER, " +
                COLUMN_HISTORY_TIMESTAMP + " TEXT)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
        db.execSQL(createHistoryTable);
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
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

    /**
     * Retrieves the database ID for a user based on username.
     *
     * @param username the username to search for
     * @return the user's ID if found; -1 otherwise
     */
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username.trim()},
                null,
                null,
                null
        );

        int userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }

        cursor.close();
        return userId;
    }

    // ---------------- INVENTORY METHODS ----------------

    /**
     * Inserts a new inventory item into the inventory table.
     *
     * @param itemName the name of the inventory item
     * @param quantity the quantity of the item
     * @return true if the item was inserted successfully; false otherwise
     */
    public boolean insertItem(int userId, String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_INVENTORY_USER_ID, userId);
        values.put(COLUMN_ITEM_NAME, itemName.trim());
        values.put(COLUMN_ITEM_QUANTITY, quantity);

        long result = db.insert(TABLE_INVENTORY, null, values);

        if (result != -1) {
            logInventoryHistory((int) result, "Item Added", 0, quantity);
            return true;
        }

        return false;
    }

    /**
     * Retrieves all inventory items from the database and returns them
     * as a list sorted alphabetically by item name.
     *
     * @return an ArrayList containing all inventory items
     */
    public ArrayList<InventoryItem> getAllItems(int userId) {
        ArrayList<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query all inventory records ordered by item name
        Cursor cursor = db.query(
                TABLE_INVENTORY,
                null,
                COLUMN_INVENTORY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_ITEM_NAME + " ASC"
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
     * Records an inventory action in the inventory history table.
     *
     * @param itemId the ID of the inventory item
     * @param actionType the type of action performed
     * @param oldQuantity the quantity before the action
     * @param newQuantity the quantity after the action
     */
    private void logInventoryHistory(int itemId, String actionType, int oldQuantity, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_ITEM_ID, itemId);
        values.put(COLUMN_HISTORY_ACTION, actionType);
        values.put(COLUMN_HISTORY_OLD_QTY, oldQuantity);
        values.put(COLUMN_HISTORY_NEW_QTY, newQuantity);
        values.put(COLUMN_HISTORY_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        db.insert(TABLE_HISTORY, null, values);
    }

    /**
     * Retrieves the current quantity for an inventory item.
     *
     * @param itemId the ID of the inventory item
     * @return the current quantity if found; 0 otherwise
     */
    private int getItemQuantity(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_QUANTITY},
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)},
                null,
                null,
                null
        );

        int quantity = 0;

        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
        }

        cursor.close();
        return quantity;
    }

    /**
     * Updates the quantity of an existing inventory item and logs the change.
     *
     * @param itemId      the unique ID of the item to update
     * @param newQuantity the new quantity value to store
     * @return true if at least one row was updated; false otherwise
     */
    public boolean updateQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        int oldQuantity = getItemQuantity(itemId);

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        int rowsAffected = db.update(
                TABLE_INVENTORY,
                values,
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        if (rowsAffected > 0) {
            logInventoryHistory(itemId, "Quantity Updated", oldQuantity, newQuantity);
            return true;
        }

        return false;
    }

    /**
     * Deletes an inventory item from the database.
     *
     * @param itemId the unique ID of the item to delete
     * @return true if at least one row was deleted; false otherwise
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int oldQuantity = getItemQuantity(itemId);

        int rowsDeleted = db.delete(
                TABLE_INVENTORY,
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(itemId)}
        );

        if (rowsDeleted > 0) {
            logInventoryHistory(itemId, "Item Deleted", oldQuantity, 0);
            return true;
        }

        return false;
    }
}