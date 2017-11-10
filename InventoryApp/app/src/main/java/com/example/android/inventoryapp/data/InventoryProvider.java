package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.R.attr.name;

/**
 * Created by SahilKapoor on 24-09-2017.
 */

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int ITEMS = 100;
    private static final int ITEMS_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEMS_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            case ITEMS_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + "with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not possible for " + uri);
        }
    }

    /**
     * method to insert a product with valid properties
     *
     * @param uri           --the uri passed to this method contains all the information about the product
     *                      in the form of a uri
     * @param contentValues --this contains the product values
     * @return --ContentUris
     */
    private Uri insertItem(Uri uri, ContentValues contentValues) {

        String pdtName = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (pdtName == null) {
            throw new IllegalArgumentException("Product name is required.");
        }

        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity.");
        }

        Float price = contentValues.getAsFloat(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires a valid price.");
        }

        String image = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Product image is required.");
        }

        String supplier_name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplier_name == null) {
            throw new IllegalArgumentException("Supplier name is required.");
        }

        String supplier_email = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        if (supplier_email == null) {
            throw new IllegalArgumentException("Supplier email is required.");
        }

        String supplier_phone = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplier_phone == null) {
            throw new IllegalArgumentException("Supplier phone is required.");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert new row for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEMS_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not possible for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEMS_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update on not possible for " + uri);
        }
    }

    /**
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return -- the number of rows updated
     */
    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product name is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("A valid product quantity is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Float price = contentValues.getAsFloat(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("A valid product price is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_IMAGE)) {
            String image = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("A valid product image is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            String supplier_name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplier_name == null) {
                throw new IllegalArgumentException("A valid supplier name is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)) {
            String supplier_email = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            if (supplier_email == null) {
                throw new IllegalArgumentException("A valid supplier email is req.");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)) {
            String supplier_phone = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            if (supplier_phone == null) {
                throw new IllegalArgumentException("A valid supplier phone number is req.");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
