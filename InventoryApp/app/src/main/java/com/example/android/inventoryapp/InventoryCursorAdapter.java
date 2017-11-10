package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

/**
 * Created by SahilKapoor on 24-09-2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView productNameView = (TextView) view.findViewById(R.id.list_product_name);
        TextView productPriceView = (TextView) view.findViewById(R.id.list_product_price);
        final TextView productQuantityView = (TextView) view.findViewById(R.id.list_product_quantity);
        ImageView productImageView = (ImageView) view.findViewById(R.id.product_image);

        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int productImageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        int rowIndex = cursor.getColumnIndex(InventoryEntry._ID);

        final String productName = cursor.getString(productNameColumnIndex);
        final int productPrice = cursor.getInt(productPriceColumnIndex);
        final int productQuantity = cursor.getInt(productQuantityColumnIndex);
        final int rowId = cursor.getInt(rowIndex);

        productNameView.setText(productName);
        productPriceView.setText(Integer.toString(productPrice));
        productQuantityView.setText(Integer.toString(productQuantity));
        Glide.with(context).load(cursor.getString(productImageColumnIndex))
                .placeholder(R.drawable.ic_content_paste_black_24dp)
                .error(R.mipmap.ic_launcher)
                .crossFade()
                .centerCrop()
                .into(productImageView);

        Button sellButton = (Button) view.findViewById(R.id.sell_product);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InventoryDbHelper dbHelper = new InventoryDbHelper(context);
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                int items = Integer.parseInt(productQuantityView.getText().toString());

                if (items > 0) {

                    int quantitySold = items - 1;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantitySold);
                    String selection = InventoryEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(rowId)};
                    int rowAffected = database.update(InventoryEntry.TABLE_NAME, values,
                            selection, selectionArgs);
                    if (rowAffected != -1) {
                        productQuantityView.setText(Integer.toString(quantitySold));
                    }
                } else {
                    Toast.makeText(context, "Out of Stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
