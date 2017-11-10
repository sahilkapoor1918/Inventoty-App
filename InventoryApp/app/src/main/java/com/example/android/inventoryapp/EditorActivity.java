package com.example.android.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_LOADER = 0;
    private static final int GALLERY_CODE = 1;
    private File mTempFile;
    private String pathway;
    private String imagePathway;
    private final String[] mPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri mCurrentUri;
    private EditText mProductName;
    private EditText mProductQuantity;
    private EditText mProductPrice;
    private ImageView mProductImage;
    private EditText mSupplierName;
    private EditText mSupplierEmail;
    private EditText mSupplierPhone;
    private Button mIncrement;
    private Button mDecrement;
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
        mCurrentUri = intent.getData();

        if (mCurrentUri == null) {
            setTitle("Add Item");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        mProductName = (EditText) findViewById(R.id.product_name);
        mProductQuantity = (EditText) findViewById(R.id.product_quantity);
        mProductPrice = (EditText) findViewById(R.id.product_price);
        mProductImage = (ImageView) findViewById(R.id.product_image);
        mSupplierName = (EditText) findViewById(R.id.supplier_name);
        mSupplierEmail = (EditText) findViewById(R.id.supplier_email);
        mSupplierPhone = (EditText) findViewById(R.id.supplier_phone);
        mIncrement = (Button) findViewById(R.id.increment_quantity);
        mDecrement = (Button) findViewById(R.id.decrement_quantity);

        mDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractOneFromQuantity();
                mItemHasChanged = true;
            }
        });

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneToQuantity();
                mItemHasChanged = true;
            }
        });

        mProductName.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductImage.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        Button selectImgButton = (Button) findViewById(R.id.select_image_button);
        selectImgButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                checkForPermissions();
                mItemHasChanged = true;
            }
        });

        Button orderMoreButton = (Button) findViewById(R.id.order_more_button);
        orderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String supName = mSupplierName.getText().toString().trim();
                String supEmail = mSupplierEmail.getText().toString().trim();
                String supPhone = mSupplierPhone.getText().toString().trim();
                String pdtName = mProductName.getText().toString().trim();
                String pdtQuantity = mProductQuantity.getText().toString().trim();
                String pdtPrice = mProductPrice.getText().toString().trim();

                String orderMsg = orderSummary(supName, pdtName, pdtQuantity, pdtPrice, supPhone);

                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:"));
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{supEmail});
                i.putExtra(Intent.EXTRA_SUBJECT, "Order for: " + supName);
                i.putExtra(Intent.EXTRA_TEXT, orderMsg);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(EditorActivity.this, mPermission[0]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(mPermission, 456);
        } else {
            openImageSelector();
        }
    }

    private void openImageSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent.createChooser(intent, "Select File"), GALLERY_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 456) {
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRotionale = shouldShowRequestPermissionRationale(permission);
                    if (!showRotionale) {
                        showDialogOK("storage permission requested", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                        Uri uri = Uri.fromParts("111", EditorActivity.this.getPackageName(),
                                                null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, 456);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        dialogInterface.dismiss();
                                        break;
                                }
                            }
                        }, "NEVER_ASK");
                    } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showDialogOK("storage permission request", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        checkForPermissions();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        dialogInterface.dismiss();
                                        break;
                                }
                            }
                        }, "DENY");
                    }
                }
            }
        }
    }

    private void showDialogOK(String msg, DialogInterface.OnClickListener okListener, String from) {

        AlertDialog.Builder alertDialogBox = new AlertDialog.Builder(EditorActivity.this).setMessage(msg);

        if (from.equals("DENY")) {
            alertDialogBox.setTitle("Gallery Permission")
                    .setPositiveButton("OK", okListener)
                    .setNegativeButton("CANCEL", okListener)
                    .create()
                    .show();
        } else if (from.equals("NEVER_ASK")) {
            alertDialogBox.setTitle("Gallery Permission")
                    .setPositiveButton("OK", okListener)
                    .setNegativeButton("CANCEL", okListener)
                    .create()
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Cursor c = getContentResolver().query(data.getData(), null, null, null, null);
            c.moveToFirst();
            String doc_id = c.getString(0);
            doc_id = doc_id.substring(doc_id.lastIndexOf(":") + 1);
            c.close();
            c = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Images.Media._ID + " = ? ",
                    new String[]{doc_id},
                    null);
            c.moveToFirst();
            pathway = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            c.close();

            mTempFile = new File(pathway);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Glide.with(EditorActivity.this).load(mTempFile)
                .placeholder(R.drawable.ic_content_paste_black_24dp)
                .error(R.mipmap.ic_launcher)
                .crossFade()
                .centerCrop()
                .into(mProductImage);
    }

    /**
     * this method is called when minus button is pressed
     */
    private void subtractOneFromQuantity() {
        String oldValueString = mProductQuantity.getText().toString();
        int oldValue;
        if (oldValueString.isEmpty()) {
            return;
        } else if (oldValueString.equals("0")) {
            return;
        } else {
            oldValue = Integer.parseInt(oldValueString);
            mProductQuantity.setText(String.valueOf(oldValue - 1));
        }
    }

    /**
     * this method is called when plus button is pressed
     */
    private void addOneToQuantity() {
        String oldValueString = mProductQuantity.getText().toString();
        int oldValue;
        if (oldValueString.isEmpty()) {
            oldValue = 0;
        } else {
            oldValue = Integer.parseInt(oldValueString);
        }
        mProductQuantity.setText(String.valueOf(oldValue + 1));
    }

    /**
     * @param name     --name of the customer
     * @param pdtname  --name of the product
     * @param quantity --quantity of the product
     * @param price    --price of the product
     * @return --message to be delivered in mail
     */
    private String orderSummary(String name, String pdtname, String quantity, String price, String phone) {
        String orderMsg = "Supplier Name: " + name;
        orderMsg += "\nProduct Name: " + pdtname;
        orderMsg += "\nQuantity: " + quantity;
        orderMsg += "\nTotal Price: " + price;
        orderMsg += "\nPhone number :" + phone;

        return orderMsg;
    }

    /**
     * this method is called when save button i.e. tick mark is pressed
     */
    private void saveItem() {

        String pdtNameString = mProductName.getText().toString().trim();
        String pdtQuantityString = mProductQuantity.getText().toString().trim();
        String pdtPriceString = mProductPrice.getText().toString().trim();
        String supName = mSupplierName.getText().toString().trim();
        String supEmail = mSupplierEmail.getText().toString().trim();
        String supPhone = mSupplierPhone.getText().toString().trim();

        if (TextUtils.isEmpty(pdtNameString) && TextUtils.isEmpty(pdtQuantityString) &&
                TextUtils.isEmpty(pdtPriceString) && pathway == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pdtNameString)) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pdtQuantityString)) {
            Toast.makeText(this, "Quantity is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pdtPriceString)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supName)) {
            Toast.makeText(this, "Supplier Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supEmail)) {
            Toast.makeText(this, "Supplier Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supPhone)) {
            Toast.makeText(this, "Supplier Phone Number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, pdtNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, pdtQuantityString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, pdtPriceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(pathway));
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supName);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supEmail);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supPhone);

        if (mCurrentUri == null) {
            if (mTempFile == null) {
                Toast.makeText(this, "Image is required", Toast.LENGTH_SHORT).show();
            }
            values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(pathway));

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error inserting item", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item Saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mTempFile != null) {
                values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(mTempFile));
            } else if (imagePathway != null) {
                values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(imagePathway));
            }

            int rowAffected = getContentResolver().update(mCurrentUri, values, null, null);
            if (rowAffected == 0) {
                Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButton =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButton);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * to display an alert dialog box to confirm your deletion
     */
    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Item Deleted");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * checking whether your deletion was successful or not
     */
    private void deleteItem() {
        if (mCurrentUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * to display an alert dialog box, on pressing the back key.
     * to confirm your response
     *
     * @param discardButton
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your messages and quit editing?");
        builder.setPositiveButton("Discard", discardButton);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButton =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButton);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE};

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int pdtNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int pdtQuantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int pdtPriceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int pdtImageColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int supNameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supEmailColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int supPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            String pdtName = data.getString(pdtNameColumnIndex);
            Integer pdtQuantity = data.getInt(pdtQuantityColumnIndex);
            Float pdtPrice = data.getFloat(pdtPriceColumnIndex);
            imagePathway = data.getString(pdtImageColumnIndex);
            String supName = data.getString(supNameColumnIndex);
            String supEmail = data.getString(supEmailColumnIndex);
            String supPhone = data.getString(supPhoneColumnIndex);

            mProductName.setText(pdtName);
            mProductQuantity.setText(Integer.toString(pdtQuantity));
            mProductPrice.setText(Float.toString(pdtPrice));
            Glide.with(EditorActivity.this).load(imagePathway)
                    .placeholder(R.drawable.ic_content_paste_black_24dp)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(mProductImage);
            mSupplierName.setText(supName);
            mSupplierEmail.setText(supEmail);
            mSupplierPhone.setText(supPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mProductName.setText("");
        mProductQuantity.setText("0");
        mProductPrice.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mSupplierPhone.setText("");
    }
}
