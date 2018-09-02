package com.example.krokken.storeinventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.krokken.storeinventory.data.InventoryContract;
import com.example.krokken.storeinventory.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder vh = new ViewHolder();
        vh.productName = (TextView) view.findViewById(R.id.name);
        vh.productPrice = (TextView) view.findViewById(R.id.price);
        vh.productQuantity = (TextView) view.findViewById(R.id.quantity);

        // Figure out the index of each column
        int productNameIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT_NAME);
        int productPriceIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT_PRICE);
        int productQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRODUCT_QUANTITY);

        // Get values related to the index
        String currentProductName = cursor.getString(productNameIndex);
        String currentProductPrice = InventoryContract.getFormattedPrice(cursor.getString(productPriceIndex), context);
        String currentProductQuantity = context.getResources().getString(R.string.popup_quantity_on_hand_text) + cursor.getString(productQuantityIndex);

        // Update views to display the values
        vh.productName.setText(currentProductName);
        vh.productPrice.setText(currentProductPrice);
        vh.productQuantity.setText(currentProductQuantity);
    }

    static class ViewHolder {
        private TextView productName;
        private TextView productPrice;
        private TextView productQuantity;
    }
}
