package com.example.krokken.storeinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krokken.storeinventory.data.InventoryContract;
import com.example.krokken.storeinventory.data.InventoryContract.InventoryEntry;
import com.example.krokken.storeinventory.data.InventoryDbHelper;

public class StoreActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    private static final int DUMMY_PRODUCT_QUANTITY = 5;

    private static final int DUMMY_PRODUCT_SHIPPING_FEE = 2;

    private static final int DUMMY_PRODUCT_STOCK_TYPE = 1;

    private static final String NO_IMAGE_STRING = "android.resource://com.example.krokken.storeinventory/drawable/no_image";

    private static final String ADD_IMAGE_STRING = "android.resource://com.example.krokken.storeinventory/drawable/add_image";

    private int lightPurpleColor;
    private int darkPurpleColor;

    private ImageView popupDeleteItem;
    private ImageView popupEditItem;
    private ImageView popupProductImageView;
    private ImageView popupSupplierPhoneNumberIcon;
    private TextView popupProductNameView;
    private TextView popupProductPriceView;
    private TextView popupProductQuantityView;
    private TextView popupProductShippingFeeSpinner;
    private TextView popupProductStockTypeSpinner;
    private TextView popupSupplierNameView;
    private TextView popupSupplierPhoneNumberView;
    private Button popupSellItemButton;
    private Button popupOrderItemButton;

    private InventoryDbHelper mDbHelper;

    private View emptyView;

    private FloatingActionButton fabAddItem;

    private InventoryCursorAdapter mInventoryCursorAdapter;

    private ListView inventoryListView;

    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVariables();
        onCreateClickListeners();
        inventoryListView.setEmptyView(emptyView);
        inventoryListView.setAdapter(mInventoryCursorAdapter);
        getLoaderManager().initLoader(INVENTORY_LOADER, null, StoreActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_edit_text_info:
                insertDummyData();
                return true;
            case R.id.action_delete_all_items:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeVariables() {
        mDbHelper = new InventoryDbHelper(this);
        fabAddItem = findViewById(R.id.fab_add_item);
        emptyView = findViewById(R.id.empty_view);
        inventoryListView = findViewById(R.id.list);
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        lightPurpleColor = ContextCompat.getColor(this, R.color.main_light_purple);
        darkPurpleColor = ContextCompat.getColor(this, R.color.main_dark_purple);
    }

    private void onCreateClickListeners() {
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                inventoryPopup(view, currentItemUri);
            }
        });
    }

    private void insertDummyData() {
        ContentValues values = new ContentValues();
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE,
                "");
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                getString(R.string.dummy_product_name));
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE,
                getString(R.string.dummy_product_price));
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY,
                DUMMY_PRODUCT_QUANTITY);
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE,
                DUMMY_PRODUCT_SHIPPING_FEE);
        values.put(
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE,
                DUMMY_PRODUCT_STOCK_TYPE);
        values.put(
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                getString(R.string.dummy_supplier_name));
        values.put(
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER,
                getString(R.string.dummy_supplier_phone_number));

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        if (newUri == null) {
            Toast.makeText(this, R.string.dummy_toast_failed_insert, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_all_question);
        builder.setPositiveButton(R.string.dialog_delete_all_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAll();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_all_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Toast.makeText(this, rowsDeleted + getString(R.string.toast_delete_all_rows_deleted), Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mInventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }

    private void inventoryPopup(View anchorView, final Uri currentItemUri) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE,
                InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER
        };

        Cursor currentItemCursor =
                getContentResolver().query(
                        currentItemUri,
                        projection,
                        null,
                        null,
                        null);

        currentItemCursor.moveToFirst();

        // Column Indices
        int currentProductImageColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE);
        int currentProductNameColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        int currentProductPriceColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE);
        int currentProductQuantityColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY);
        int currentProductShippingFeeColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE);
        int currentProductStockTypeColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE);
        int currentSupplierNameColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        int currentSupplierPhoneNumberColumnIndex =
                currentItemCursor.getColumnIndex(
                        InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER);

        // Retrieving values from the columns
        String currentProductImageString =
                currentItemCursor.getString(
                        currentProductImageColumnIndex);
        final String currentProductNameString =
                currentItemCursor.getString(
                        currentProductNameColumnIndex);
        String currentProductPriceString =
                InventoryContract.getFormattedPrice(
                        currentItemCursor.getString(
                                currentProductPriceColumnIndex), this);
        final int currentProductQuantityInt =
                currentItemCursor.getInt(
                        currentProductQuantityColumnIndex);
        int currentProductShippingFeeInt =
                currentItemCursor.getInt(
                        currentProductShippingFeeColumnIndex);
        int currentProductStockTypeInt =
                currentItemCursor.getInt(
                        currentProductStockTypeColumnIndex);
        String currentSupplierNameString =
                currentItemCursor.getString(
                        currentSupplierNameColumnIndex);
        final String currentSupplierPhoneNumberString =
                InventoryContract.getFormattedPhoneNumber(
                        currentItemCursor.getString(
                                currentSupplierPhoneNumberColumnIndex));
        String currentProductQuantityString =
                getString(R.string.popup_quantity_on_hand_text) +
                        currentProductQuantityInt;

        View popupView = getLayoutInflater().inflate(R.layout.popup_inventory_item, null);
        // Finds all the views used in the popup window
        setPopupViews(popupView);

        Uri photoUri = Uri.parse(NO_IMAGE_STRING);
        if (!currentProductImageString.isEmpty() && !currentProductImageString.equals(ADD_IMAGE_STRING)) {
            photoUri = Uri.parse(currentProductImageString);
        }

        popupProductImageView.setImageBitmap(InventoryContract.getBitmapFromUri(photoUri, this));
        popupProductNameView.setText(currentProductNameString);
        popupProductPriceView.setText(currentProductPriceString);
        popupProductQuantityView.setText(currentProductQuantityString);
        popupProductShippingFeeSpinner.setText(InventoryContract.getShippingFee(currentProductShippingFeeInt));
        popupProductStockTypeSpinner.setText(InventoryContract.getStockType(currentProductStockTypeInt));
        popupSupplierNameView.setText(currentSupplierNameString);
        popupSupplierPhoneNumberView.setText(currentSupplierPhoneNumberString);

        // Sets the views gone or fills with text if no information was provided for related columns
        setViewsIfEmpty(currentProductNameString,
                currentProductPriceString,
                currentProductQuantityInt,
                currentSupplierNameString,
                currentSupplierPhoneNumberString);

        // Popup window creation and initializing the views
        popupWindow = new PopupWindow(popupView,
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        //Dismiss popup window when clicked outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        popupWindow.setAnimationStyle(R.style.AnimationPopup);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        popupDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rowsDelete = getContentResolver().delete(currentItemUri, null, null);
                if (rowsDelete > 0) {
                    Toast.makeText(StoreActivity.this, currentProductNameString +
                            getString(R.string.toast_popup_deleted_item),
                            Toast.LENGTH_SHORT).show();
                }
                popupWindow.dismiss();
            }
        });

        popupEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreActivity.this, EditorActivity.class);
                intent.setData(currentItemUri);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });

        popupSellItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = currentProductQuantityInt;
                int whichButton = 2;
                showAmountDialog(currentItemUri, quantity, whichButton);
            }
        });

        popupOrderItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = currentProductQuantityInt;
                int whichButton = 1;
                showAmountDialog(currentItemUri, quantity, whichButton);
            }
        });

        popupSupplierPhoneNumberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "tel:" + currentSupplierPhoneNumberString;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
        currentItemCursor.close();
    }

    private void changeQuantity(Uri currentItemUri, int productQuantityInt, int whichButton) {

        String productNameString =
                popupProductNameView
                        .getText()
                        .toString()
                        .trim();
        String productPriceString =
                (popupProductPriceView
                        .getText()
                        .toString()
                        .trim())
                        .replaceAll("[$,]", "");
        String supplierNameString =
                popupSupplierNameView
                        .getText()
                        .toString()
                        .trim();
        String supplierPhoneNumberString =
                popupSupplierPhoneNumberView
                        .getText()
                        .toString()
                        .trim();

        int productPriceInt = 0;
        if (!TextUtils.isEmpty(productPriceString)) {
            if (productPriceString.equals(getString(R.string.popup_no_price_text))) {
                productPriceString = "0";
            }
            productPriceInt = (int) (Double.parseDouble(productPriceString) * 100);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME, productNameString); // String input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE, productPriceInt); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY, productQuantityInt); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierNameString); // String input
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString); // String input

        int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);

        if (rowsAffected > 0) {
            switch (whichButton) {
                case 1:
                    Toast.makeText(this, R.string.popup_quantity_case_ordered, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, R.string.popup_quantity_case_sold, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, R.string.popup_case_quantity_default, Toast.LENGTH_SHORT).show();
            }
        }
        popupWindow.dismiss();
    }

    private void setPopupViews(View popupView) {
        popupDeleteItem = popupView.findViewById(R.id.popup_delete_item);
        popupEditItem = popupView.findViewById(R.id.popup_edit_item);
        popupProductImageView = popupView.findViewById(R.id.popup_item_image);
        popupProductNameView = popupView.findViewById(R.id.popup_product_name);
        popupProductPriceView = popupView.findViewById(R.id.popup_product_price);
        popupProductQuantityView = popupView.findViewById(R.id.popup_product_quantity);
        popupProductShippingFeeSpinner = popupView.findViewById(R.id.popup_product_shipping_fee);
        popupProductStockTypeSpinner = popupView.findViewById(R.id.popup_product_stock_type);
        popupSupplierNameView = popupView.findViewById(R.id.popup_supplier_name);
        popupSupplierPhoneNumberView = popupView.findViewById(R.id.popup_supplier_phone_number);
        popupSupplierPhoneNumberIcon = popupView.findViewById(R.id.popup_supplier_phone_number_icon);
        popupSellItemButton = popupView.findViewById(R.id.popup_sell_item);
        popupOrderItemButton = popupView.findViewById(R.id.popup_order_item);
    }

    private void setViewsIfEmpty(String productName,
                                 String productPrice,
                                 int productQuantity,
                                 String supplierName,
                                 String supplierPhoneNumber) {

        productPrice = productPrice.replaceAll("[$]", "");

        if (productName.isEmpty()) {
            popupProductNameView.setText(R.string.popup_no_product_name_text);
        }

        if (productPrice.isEmpty() || productPrice.equals("0")) {
            popupProductPriceView.setText(getString(R.string.popup_no_price_text));
        }

        if (productQuantity < 1) {
            popupSellItemButton.setBackgroundResource(R.drawable.popup_button_background);
            popupSellItemButton.setTextColor(darkPurpleColor);
            popupSellItemButton.setEnabled(false);
            popupOrderItemButton.setBackgroundResource(R.drawable.popup_button_background_alternate);
            popupOrderItemButton.setTextColor(lightPurpleColor);
        } else {
            popupSellItemButton.setEnabled(true);
            popupSellItemButton.setBackgroundResource(R.drawable.popup_button_background_alternate);
            popupSellItemButton.setTextColor(lightPurpleColor);
            popupOrderItemButton.setBackgroundResource(R.drawable.popup_button_background);
            popupOrderItemButton.setTextColor(darkPurpleColor);
        }

        if (supplierName.isEmpty()) {
            popupSupplierNameView.setText(R.string.popup_no_supplier_name_text);
        }

        if (supplierPhoneNumber.isEmpty()) {
            popupSupplierPhoneNumberView.setVisibility(View.GONE);
            popupSupplierPhoneNumberIcon.setVisibility(View.GONE);
        } else {
            popupSupplierPhoneNumberView.setVisibility(View.VISIBLE);
            popupSupplierPhoneNumberIcon.setVisibility(View.VISIBLE);
        }
    }

    private void showAmountDialog(final Uri currentItemUri, final int quantity, final int whichButton) {
        LayoutInflater inflater = this.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = inflater.inflate(R.layout.alert_order_edit_text_layout, null);
        String message;
        final int newQuantity;
        switch (whichButton) {
            case 1:
                message = getString(R.string.dialog_case_order);
                newQuantity = quantity;
                break;
            case 2:
                message = getString(R.string.dialog_case_sell);
                newQuantity = quantity * -1;
                break;
            default:
                message = getString(R.string.dialog_case_default);
                newQuantity = 0;
        }
        builder.setMessage(getString(
                R.string.dialog_show_amount_message_pt_1) +
                message +
                getString(R.string.dialog_show_amount_message_pt_2));

        builder.setView(dialogView);
        final EditText taskEditText = dialogView.findViewById(R.id.alert_order_edit_text);
        builder.setPositiveButton(message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(taskEditText.getWindowToken(), 0);
                String amount = taskEditText.getText().toString().trim();
                if (amount.isEmpty()) {
                    Toast.makeText(StoreActivity.this, R.string.toast_dialog_show_amount_quantity_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                int amountInt = Integer.parseInt(amount);
                if (whichButton == 2 && quantity < amountInt) {
                    Toast.makeText(StoreActivity.this, R.string.toast_dialog_show_amount_oversell, Toast.LENGTH_SHORT).show();
                    return;
                }
                int updateQuantityInt = newQuantity + amountInt;
                if (updateQuantityInt < 0) {
                    updateQuantityInt *= -1;
                }
                changeQuantity(currentItemUri, updateQuantityInt, whichButton);
            }
        });
        builder.setNegativeButton(R.string.dialog_show_amount_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(taskEditText.getWindowToken(), 0);
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }
}

