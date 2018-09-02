package com.example.krokken.storeinventory.data;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class InventoryContract {

    private InventoryContract() {
    }

    // Strings for displaying
    public static final String STOCK_SPECIAL_ORDER_TEXT = "Special Order";
    public static final String STOCK_CUSTOM_ORDER_TEXT = "Custom Order";
    public static final String STOCK_REPLENISH_ORDER_TEXT = "Replenish Order";
    public static final String SHIPPING_BASE_COST_TEXT = "$7 Shipping";
    public static final String SHIPPING_INT_FREE_TEXT = "Free International Shipping";
    public static final String SHIPPING_LOCAL_FREE_TEXT = "Free Local Shipping";

    // Strings to create URI
    public final static String CONTENT_AUTHORITY = "com.example.krokken.storeinventory";
    public final static String CONTENT_PREFIX = "content://";
    public static final Uri BASE_CONTENT_URI = Uri.parse(CONTENT_PREFIX + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";


    public static final class InventoryEntry implements BaseColumns {

        public final static String TABLE_NAME = "inventory";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_INVENTORY_PRODUCT_IMAGE = "product_image";
        public final static String COLUMN_INVENTORY_PRODUCT_NAME = "product_name";
        public final static String COLUMN_INVENTORY_PRODUCT_PRICE = "price";
        public final static String COLUMN_INVENTORY_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_INVENTORY_PRODUCT_SHIPPING_FEE = "shipping_fee";
        public final static String COLUMN_INVENTORY_PRODUCT_STOCK_TYPE = "stock_type";
        public final static String COLUMN_INVENTORY_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_INVENTORY_SUPPLIER_PHONE_NUMBER = "phone_number";

        public final static int SHIPPING_BASE_COST = 0;
        public final static int SHIPPING_INT_FREE = 1;
        public final static int SHIPPING_LOCAL_FREE = 2;

        public final static int STOCK_SPECIAL_ORDER = 0;
        public final static int STOCK_CUSTOM_ORDER = 1;
        public final static int STOCK_REPLENISH_ORDER = 2;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
    }


    public static String getFormattedPhoneNumber(String unformattedNumber) {
        String formattedNumber = PhoneNumberUtils.formatNumber(unformattedNumber);
        return formattedNumber;
    }

    public static String getFormattedPrice(String productPrice, Context context) {
        double price = (Double.parseDouble(productPrice) / 100);
        Locale currentLocale = context.getResources().getConfiguration().locale;
        Currency currency = Currency.getInstance(currentLocale);
        String formattedPrice = currency.getSymbol() + NumberFormat.getNumberInstance(currentLocale).format(price);
        return formattedPrice;
    }

    public static String getShippingFee(int shippingFeeInt) {
        switch (shippingFeeInt) {
            case 1:
                return SHIPPING_INT_FREE_TEXT;
            case 2:
                return SHIPPING_LOCAL_FREE_TEXT;
            default:
                return SHIPPING_BASE_COST_TEXT;
        }
    }

    public static String getStockType(int stockType) {
        switch (stockType) {
            case 1:
                return STOCK_CUSTOM_ORDER_TEXT;
            case 2:
                return STOCK_REPLENISH_ORDER_TEXT;
            default:
                return STOCK_SPECIAL_ORDER_TEXT;
        }
    }


    // Thanks to @crlsndrsjmnz (Forum Mentor Carlos) for this method
    public static Bitmap getBitmapFromUri(Uri uri, Context context) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / 165, photoH / 165);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e("Log image", "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e("Log image", "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}
