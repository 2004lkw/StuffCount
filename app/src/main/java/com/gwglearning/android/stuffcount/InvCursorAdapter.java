package com.gwglearning.android.stuffcount;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gwglearning.android.stuffcount.data.InvContract;

import java.text.NumberFormat;


public class InvCursorAdapter extends CursorAdapter {

    // The constructor
    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // The layout to inflate.
        // I can never remember how to code this darn thing.
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // get the views.
        TextView tvName = view.findViewById(R.id.listItem_textView_name);
        TextView tvPrice = view.findViewById(R.id.listItem_textView_price);
        TextView tvQuantity = view.findViewById(R.id.listItem_textView_quantity);
        Button bSale = view.findViewById(R.id.listItem_button_sale);

        // Insert the info!
        int nameCol = cursor.getColumnIndex(InvContract.InvTable.COL_NAME);
        int priceCol = cursor.getColumnIndex(InvContract.InvTable.COL_PRICE);
        int quantityCol = cursor.getColumnIndex(InvContract.InvTable.COL_QUANTITY);
        // Get the rest of the fields for if we update.
        final int supNameCol = cursor.getColumnIndex(InvContract.InvTable.COL_SUPPLIER_NAME);
        final int supPhoneCol = cursor.getColumnIndex(InvContract.InvTable.COL_SUPPLIER_PHONE_1);
        final int idCol = cursor.getColumnIndex(InvContract.InvTable._ID);

        // data we need for the view
        final String name = cursor.getString(nameCol);
        final double price = cursor.getDouble(priceCol);
        final int quantity = cursor.getInt(quantityCol);
        final String supPhone = cursor.getString(supPhoneCol);
        final String supName = cursor.getString(supNameCol);
        final int id = cursor.getInt(idCol);

        //      ----    Format the price
        //  The price is in the DB as a INT so we divide by 100 to get the decimal.
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String finalPrice = "$0.00";
        if (price != 0) {
            finalPrice = format.format(price);
        }
        String finalQuantity = context.getString(R.string.adapter_quantity) + Integer.toString(quantity);

        tvName.setText(name);
        tvQuantity.setText(finalQuantity);
        tvPrice.setText(finalPrice);

        // Set a listener on the button for the item.
        bSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    // Build the Uri
                    Uri currentUri = ContentUris.withAppendedId(InvContract.InvTable.CONTENT_URI, id);

                    // build the values.
                    int newQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(InvContract.InvTable.COL_NAME, name);
                    values.put(InvContract.InvTable.COL_SUPPLIER_PHONE_1, supPhone);
                    values.put(InvContract.InvTable.COL_SUPPLIER_NAME, supName);
                    values.put(InvContract.InvTable.COL_PRICE, price);
                    values.put(InvContract.InvTable.COL_QUANTITY, newQuantity);

                    // Attempt to update.
                    int result = context.getContentResolver().update(currentUri, values, null, null);
                    if (result == 0) {
                        // Failed.
                        Toast.makeText(context, context.getString(R.string.list_view_failedSale), Toast.LENGTH_SHORT).show();
                    } else {
                        // SUCCESS!
                        Toast.makeText(context, context.getString(R.string.list_view_successSale), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Nothing here to sell.
                    Toast.makeText(context, R.string.list_view_noneLeft, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
