package com.example.krokken.storeinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

import com.example.krokken.storeinventory.R;
import com.example.krokken.storeinventory.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper inventoryDbHelper;

    private Resources res;

    // URI matcher code for the content URI for the inventory table
    private static final int INVENTORY = 100;

    // URI matcher code for the content URI for a single item in the inventory table
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        res = getContext().getResources();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrderBy) {
        SQLiteDatabase databaseReadable = inventoryDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = databaseReadable.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrderBy);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = databaseReadable.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrderBy);
                break;
            default:
                throw new IllegalArgumentException(
                        res.getString(R.string.IAE_query_query_not_supported) + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(
                        res.getString(R.string.IAE_get_type_unknown_uri) + uri +
                                res.getString(R.string.IAE_get_type_with_match) + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventoryItem(uri, contentValues);
            default:
                throw new IllegalArgumentException(
                        res.getString(R.string.IAE_insert_insertion_not_supported) + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase databaseWritable = inventoryDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                rowsDeleted = databaseWritable.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = databaseWritable.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(
                        res.getString(R.string.IAE_delete_delete_not_supported) + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues,
                      String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(
                        res.getString(R.string.IAE_update_update_not_supported) + uri);
        }
    }

    private Uri insertInventoryItem(Uri uri, ContentValues contentValues) {
        // Checks to make sure app has something to get before inserting
        String productNameString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        if (productNameString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_name));
        }
        Integer productPriceInt =
                contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE);
        if (productPriceInt == null || productPriceInt < 0) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_price));
        }
        Integer productQuantityInt =
                contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY);
        if (productQuantityInt == null || productQuantityInt < 0) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_quantity));
        }
        String supplierNameString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        if (supplierNameString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_supplier_name));
        }
        String supplierPhoneNumberString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumberString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_supplier_phone_number));
        }

        SQLiteDatabase databaseWritable = inventoryDbHelper.getWritableDatabase();

        long id = databaseWritable.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Toast.makeText(getContext(), res.getString(R.string.toast_insert_item_fail_text), Toast.LENGTH_LONG).show();
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private int updateInventory(Uri uri, ContentValues contentValues,
                                String selection, String[] selectionArgs) {
        /**
         * Note to reviewer person. I'm not entirely sure I understand why we have the following checks.
         * It was in the pet app/course and so I wanted to have it. Checks are good, but:
         * It seems to work without it, and when I'm wanting to update a single item, like with
         * [@link StoreActivity] Order/Sell button to update quantity:
         * It would see the contentValue for productName as null (since I only 'put' quantity?),
         * and it would throw the exception and crash.
         * Since I may not remember to add this when submitting, I'm hoping you can send me some info on what
         * I should be doing differently or understanding better.
         **/
        String productNameString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        if (productNameString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_name));
        }
        Integer productPriceInt =
                contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE);
        if (productPriceInt == null || productPriceInt < 0) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_price));
        }
        Integer productQuantityInt =
                contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY);
        if (productQuantityInt == null || productQuantityInt < 0) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_product_quantity));
        }
        String supplierNameString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        if (supplierNameString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_supplier_name));
        }
        String supplierPhoneNumberString =
                contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER);
        if (supplierPhoneNumberString == null) {
            throw new IllegalArgumentException(
                    res.getString(R.string.IAE_insert_inventory_item_supplier_phone_number));
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
