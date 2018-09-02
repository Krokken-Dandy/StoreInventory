package com.example.krokken.storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.krokken.storeinventory.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "store.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE + " STRING NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE + " INTEGER NOT NULL,"
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // When the version needs to be updated, nothing to add yet
    }
}
