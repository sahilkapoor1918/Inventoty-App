package com.example.android.inventoryapp.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SahilKapoor on 24-09-2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL,"
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL,"
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL ,"
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE + " TEXT, "
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL,"
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL,"
                + InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
