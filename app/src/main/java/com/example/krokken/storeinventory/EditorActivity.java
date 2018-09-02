package com.example.krokken.storeinventory;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.krokken.storeinventory.data.InventoryContract;
import com.example.krokken.storeinventory.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 100;

    private static final int CAMERA_PERMISSION_CODE = 200;

    private static final int CAMERA_REQUEST_CODE = 288;

    private ImageView mProductImageView;
    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private Spinner mShippingSpinner;
    private Spinner mStockSpinner;
    private Uri mCurrentItemUri;
    private Uri mPhotoUri;
    private Bitmap mPhotoBitmap;
    private Bitmap addImage;
    private int mShippingFee;
    private int mStockType;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        addImage = BitmapFactory.decodeResource(getResources(), R.drawable.add_image);
        mPhotoUri = Uri.parse("android.resource://" + getPackageName() + "/drawable/add_image");

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.editor_title_add_new_item));
            mPhotoBitmap = addImage;
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_title_edit_old_item));
            getLoaderManager().initLoader(0, null, this);
        }

        setupViews();
        setupListeners();
        setupSpinners();
        setupFloatingLabelErrors();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_edit_text_info:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
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
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int productImageColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE);
            int productNameColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
            int productPriceColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE);
            int productQuantityColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY);
            int productShippingFeeColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE);
            int productStockTypeColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE);
            int supplierNameColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex =
                    cursor.getColumnIndex(
                            InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER);

            String productImageString = cursor.getString(productImageColumnIndex);
            String productNameString = cursor.getString(productNameColumnIndex);
            String productPriceString =
                    (InventoryContract.getFormattedPrice(
                            cursor.getString(productPriceColumnIndex), this)).replaceAll("[$,]", "");
            String productQuantityInt = cursor.getInt(productQuantityColumnIndex) + "";
            int productShippingFeeInt = cursor.getInt(productShippingFeeColumnIndex);
            int productStockTypeInt = cursor.getInt(productStockTypeColumnIndex);
            String supplierNameString = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumberString = cursor.getString(supplierPhoneNumberColumnIndex);

            productImageString = "";
            if (!productImageString.isEmpty()) {
                mPhotoUri = Uri.parse(productImageString);
            }

            mPhotoBitmap = InventoryContract.getBitmapFromUri(mPhotoUri, this);

            mProductImageView.setImageBitmap(mPhotoBitmap);
            mProductNameEditText.setText(productNameString);
            mProductPriceEditText.setText(productPriceString);
            mProductQuantityEditText.setText(productQuantityInt);
            mSupplierNameEditText.setText(supplierNameString);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumberString);

            switch (productShippingFeeInt) {
                case InventoryEntry.SHIPPING_INT_FREE:
                    mShippingSpinner.setSelection(1);
                    break;
                case InventoryEntry.SHIPPING_LOCAL_FREE:
                    mShippingSpinner.setSelection(2);
                    break;
                default:
                    mShippingSpinner.setSelection(0); // SHIPPING_BASE_COST
                    break;
            }

            switch (productStockTypeInt) {
                case InventoryEntry.STOCK_CUSTOM_ORDER:
                    mStockSpinner.setSelection(1);
                    break;
                case InventoryEntry.STOCK_REPLENISH_ORDER:
                    mShippingSpinner.setSelection(2);
                    break;
                default:
                    mStockSpinner.setSelection(0); // STOCK_SPECIAL_ORDER
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductImageView.setImageBitmap(addImage);
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
        mShippingSpinner.setSelection(0); // Base Shipping Fee
        mStockSpinner.setSelection(0); // Special order
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mPhotoUri = data.getData();
            mPhotoBitmap = InventoryContract.getBitmapFromUri(mPhotoUri, this);
            mProductImageView.setImageBitmap(mPhotoBitmap);
        }
    }

    private void setupViews() {
        mProductImageView = findViewById(R.id.product_image);
        mProductNameEditText = findViewById(R.id.product_name);
        mProductPriceEditText = findViewById(R.id.product_price);
        mProductQuantityEditText = findViewById(R.id.product_quantity);
        mSupplierNameEditText = findViewById(R.id.supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.supplier_phone_number);
        mShippingSpinner = findViewById(R.id.spinner_shipping);
        mStockSpinner = findViewById(R.id.spinner_stock);
    }

    private void setupListeners() {
        /**
         * Note to reviewer
         * I wanted to add the perform click for the following listeners,
         * but I was having trouble understanding exactly how they're supposed to be implemented
         * and where I put it, or doing it for each one seperately?
         */
        mProductImageView.setOnTouchListener(mTouchListener);
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
        mShippingSpinner.setOnTouchListener(mTouchListener);
        mStockSpinner.setOnTouchListener(mTouchListener);

        mProductImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoBitmap.sameAs(addImage)) {
                    cameraIntent();
                } else {
                    showImageConfirmationDialog();
                }
            }
        });
    }

    // First spinner is for shipping charges
    // Second spinner is for how the item is stocked/ordered
    private void setupSpinners() {
        // First spinner
        ArrayAdapter spinnerShippingAdapter =
                ArrayAdapter.createFromResource(this, R.array.spinner_1, android.R.layout.simple_spinner_item);
        spinnerShippingAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mShippingSpinner.setAdapter(spinnerShippingAdapter);
        mShippingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selection = (String) adapterView.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.spinner_shipping_international_free))) {
                        mShippingFee = InventoryEntry.SHIPPING_INT_FREE;
                    } else if (selection.equals(getString(R.string.spinner_shipping_local_free))) {
                        mShippingFee = InventoryEntry.SHIPPING_LOCAL_FREE;
                    } else {
                        mShippingFee = InventoryEntry.SHIPPING_BASE_COST;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mShippingFee = InventoryEntry.SHIPPING_BASE_COST;
            }
        });

        // Second spinner
        ArrayAdapter spinnerStockAdapter =
                ArrayAdapter.createFromResource(this, R.array.spinner_2, android.R.layout.simple_spinner_item);
        spinnerStockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStockSpinner.setAdapter(spinnerStockAdapter);
        mStockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selection = (String) adapterView.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.spinner_special_order))) {
                        mStockType = InventoryEntry.STOCK_SPECIAL_ORDER;
                    } else if (selection.equals(getString(R.string.spinner_custom_order))) {
                        mStockType = InventoryEntry.STOCK_CUSTOM_ORDER;
                    } else {
                        mStockType = InventoryEntry.STOCK_REPLENISH_ORDER;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mStockType = InventoryEntry.STOCK_SPECIAL_ORDER;
            }
        });
    }

    private void setupFloatingLabelErrors() {
        final TextInputLayout floatingSupplierPhoneNumberLabel = (TextInputLayout) findViewById(R.id.supplier_phone_number_text_input_layout);
        floatingSupplierPhoneNumberLabel.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do yet
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && charSequence.length() < 10) {
                    floatingSupplierPhoneNumberLabel.setError(getString(R.string.floating_label_phone_number_error));
                    floatingSupplierPhoneNumberLabel.setErrorEnabled(true);
                } else {
                    floatingSupplierPhoneNumberLabel.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Nothing to do yet
            }
        });
    }

    private void saveItem() {
        String productImageString = "";
        if (mPhotoBitmap != null &&
                !mPhotoBitmap.sameAs(addImage) &&
                mPhotoUri != null) {
            productImageString = mPhotoUri.toString();
        }

        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();

        int productPriceInt = 0;
        if (!TextUtils.isEmpty(productPriceString)) {
            productPriceInt = (int) (Double.parseDouble(productPriceString) * 100);
        }

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(productPriceString) &&
                TextUtils.isEmpty(productQuantityString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString) &&
                mPhotoBitmap.sameAs(addImage) &&
                mShippingFee == InventoryEntry.SHIPPING_BASE_COST &&
                mStockType == InventoryEntry.STOCK_SPECIAL_ORDER) {
            return;
        }

        int productQuantityInt = 0;

        if (!TextUtils.isEmpty(productQuantityString)) {
            productQuantityInt = Integer.parseInt(productQuantityString);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_IMAGE, productImageString); // String input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME, productNameString); // String input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE, productPriceInt); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY, productQuantityInt); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE, mShippingFee); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_PRODUCT_STOCK_TYPE, mStockType); // int input
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierNameString); // String input
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString); // String input

        if (mCurrentItemUri == null) {
            // Insert the new row, returning the primary key value of the new row
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, R.string.toast_new_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_new_item_saved, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.toast_update_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_item_update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteItem() {
        int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.toast_delete_item_success, Toast.LENGTH_SHORT);
            finish();
        }
    }

    private void cameraIntent() {
        if (ContextCompat.checkSelfPermission(
                EditorActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    EditorActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(
                EditorActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    EditorActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes_message);
        builder.setPositiveButton(R.string.dialog_unsaved_changes_positive, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_unsaved_changes_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_confirm_message);
        builder.setPositiveButton(R.string.dialog_delete_confirm_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_confirm_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showImageConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_image_confirm_message);
        builder.setPositiveButton(R.string.dialog_image_confirm_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cameraIntent();
            }
        });
        builder.setNegativeButton(R.string.dialog_image_confirm_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}