package com.gwglearning.android.stuffcount;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gwglearning.android.stuffcount.data.InvContract.InvTable;

import java.text.NumberFormat;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Loader number.
    final int LOADER_ID = 0;

    // Uri passed to me.
    private Uri mCurrentUri;

    // fields we will be handling.
    private EditText mNameET;
    private EditText mQuantityET;
    private EditText mSupplierET;
    private EditText mSupplierPhoneET;
    private EditText mPriceET;
    private Button mMinusButton;
    private Button mPlusButton;
    private Button mLeftSideButton;
    private Button mRightSideButton;
    private Button mCallButton;

    // Book on screen changed boolean.
    private boolean mDataChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        // In case something on the screen changes...
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDataChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get the intent data passed this way..
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // Setup the views.
        mNameET = findViewById(R.id.editor_title); // name
        mQuantityET = findViewById(R.id.editor_quantity_editText); // quantity
        mSupplierET = findViewById(R.id.editor_supplier_name); // supplier name.
        mSupplierPhoneET = findViewById(R.id.editor_supplier_phone); // Supplier phone.
        mPriceET = findViewById(R.id.editor_price);  // price
        mMinusButton = findViewById(R.id.editor_quantity_buttonMinus); // minus button
        mPlusButton = findViewById(R.id.editor_quantity_buttonPlus); // plus button
        mLeftSideButton = findViewById(R.id.editor_button_deleteCancel); // left side button
        mRightSideButton = findViewById(R.id.editor_button_saveUpdate); // right side button
        mCallButton = findViewById(R.id.editor_button_call); // Call supplier button

        // check out this intent info.
        if (mCurrentUri == null) {
            // nothing on the intent.  NEW ITEM.
            setTitle(R.string.editor_label_adding);
            mLeftSideButton.setVisibility(View.GONE);
            mRightSideButton.setText(getString(R.string.editor_button_save));
            mQuantityET.setText("0");
            mCallButton.setVisibility(View.GONE);
        } else {
            // Sent an item.  Fill stuff, add...
            setTitle(R.string.editor_label);
            getLoaderManager().initLoader(LOADER_ID, null, this);
            mRightSideButton.setText(getString(R.string.editor_button_save));
            mLeftSideButton.setText(getString(R.string.editor_button_delete));
        }

        // Create the touch listener so we can't leave unsaved data.
        mNameET.setOnTouchListener(mTouchListener);
        mQuantityET.setOnTouchListener(mTouchListener);
        mSupplierET.setOnTouchListener(mTouchListener);
        mSupplierPhoneET.setOnTouchListener(mTouchListener);
        mPriceET.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);

        // create the onClickListeners for the buttons...
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editQuantity = mQuantityET.getText().toString().trim();
                if (TextUtils.isEmpty(editQuantity)) {
                    // If the value got deleted some how...
                    mQuantityET.setText("0");
                }
                int currentValue = Integer.valueOf(mQuantityET.getText().toString().trim());
                if (currentValue > 0) {
                    currentValue--;
                } else {
                    Toast.makeText(EditorActivity.this, "You cannot have negative inventory", Toast.LENGTH_SHORT).show();
                }
                mQuantityET.setText(Integer.toString(currentValue));
            }
        });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editQuantity = mQuantityET.getText().toString().trim();
                if (TextUtils.isEmpty(editQuantity)) {
                    // If the value was deleted some how...
                    mQuantityET.setText("0");
                }
                int currentValue = Integer.valueOf(mQuantityET.getText().toString().trim());
                currentValue++;
                mQuantityET.setText(Integer.toString(currentValue));
            }
        });

        // On click listeners for the bottom two buttons.
        // Save button.
        mRightSideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On click listener for Save/Delete.
                if (mCurrentUri == null) {
                    // New Item save routine.
                    saveItemDialog();
                }
                if (mCurrentUri != null) {
                    // Saving a book and it already exists.
                    saveItemDialog();
                }
            }
        });

        mLeftSideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUri != null) {
                    // DELETE THIS!
                    deleteItemDialog();
                }
            }
        });
    }

    // This overrides the Back button...
    public void onBackPressed() {
        if (mDataChanged) {
            // If data changed.
            // I had to copy saveItemDialog becuase it has a different
            // behavior here and final's can't be used to adjust the branching
            // in an imposter class.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.editor_ask_to_save));
            builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Yes, save.
                    if (saveItem()) {
                        // Save was successful.
                        finish();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // NO, do not save.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    finish();
                }
            });

            // make this dialog appear.
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            super.onBackPressed();
            return;
        }
    }

    private void deleteItemDialog() {
        // this is called if they want to delete an item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.editor_ask_to_delete));
        builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // YES, delete this item.
                deleteItem();
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NO, don't do that!
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show....
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItemDialog() {
        // this is called if they hit the back button
        // and there is an item to save.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.editor_ask_to_save));
        builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Yes, save.
                if (saveItem()) {
                    // Save was successful.
                    finish();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NO, do not save.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // make this dialog appear.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This routine checks data from the edit text's before
     * saving the information to the database.  Anything that
     * wrong will return a FALSE and show a Toast
     * as to why.
     */
    private boolean checkDataBeforeSaving() {
        // Test the name.
        if (TextUtils.isEmpty(mNameET.getText().toString().trim())) {
            Toast.makeText(this, "There must be a title", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Test the price.  pull this early for testing to remove all special characters
        // like the $ or some might put a ,
        String priceTestStr = mPriceET.getText().toString().trim();
        if (TextUtils.isEmpty(priceTestStr)) {
            Toast.makeText(this, "The price is invalid", Toast.LENGTH_SHORT).show();
            return false;
        } else {

            String newPriceTestStr = priceTestStr.replaceAll("[,$]", "");
            Log.e("Str Out: ", "A: " + priceTestStr + "     /      B: " + newPriceTestStr);
            Double priceOut = Double.valueOf(newPriceTestStr);
            if (priceOut < 0) {
                Toast.makeText(this, "The price is invalid", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Test quantity.
        if (TextUtils.isEmpty(mQuantityET.getText().toString().trim())) {
            Toast.makeText(this, "The quantity is invalid", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            int quantityOut = Integer.valueOf(mQuantityET.getText().toString().trim());
            if (quantityOut < 0) {
                Toast.makeText(this, "The quantity is invalid", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Test supplier name.
        if (TextUtils.isEmpty(mSupplierET.getText().toString().trim())) {
            Toast.makeText(this, "The name of the supplier is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Test Supplier Phone Number.
        if (TextUtils.isEmpty(mSupplierPhoneET.getText().toString().trim())) {
            Toast.makeText(this, "The supplier's phone number is missing", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String textCheck = mSupplierPhoneET.getText().toString().trim();
            if (textCheck.length() != 10) {
                Toast.makeText(this, "Supplier Phone Number must be 10 digits", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    // Delete this item....
    private void deleteItem() {
        if (mCurrentUri != null) {
            int result = getContentResolver().delete(mCurrentUri, null, null);
            if (result > 0) {
                Toast.makeText(this, getString(R.string.editor_delete_success), Toast.LENGTH_SHORT).show();
                Log.e("DEL_ITEM: ", "  " + result);
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Saves an item or updates an item into the database.
     */
    private boolean saveItem() {
        if (mCurrentUri != null) {
            //Saving an existing item (update).
            if (checkDataBeforeSaving()) {
                // this is reached if the data is valid.
                String nameOut = mNameET.getText().toString().trim();
                // these lines remove any $ or , that might be in the price edit text so it's safe to put
                // into a double value.
                String priceConversionString = mPriceET.getText().toString().trim();
                String priceConvertedString = priceConversionString.replaceAll("[,$]", "");
                Double priceOut = Double.valueOf(priceConvertedString);
                int quantityOut = Integer.valueOf(mQuantityET.getText().toString().trim());
                String supplierNameOut = mSupplierET.getText().toString().trim();
                String supplierPhoneOut = mSupplierPhoneET.getText().toString().trim();

                // Create values to insert this into the table.
                ContentValues values = new ContentValues();

                // use columns to put the row in the DB.
                values.put(InvTable.COL_NAME, nameOut);
                values.put(InvTable.COL_PRICE, priceOut);
                values.put(InvTable.COL_QUANTITY, quantityOut);
                values.put(InvTable.COL_SUPPLIER_NAME, supplierNameOut);
                values.put(InvTable.COL_SUPPLIER_PHONE_1, supplierPhoneOut);

                // Alright.  Now lets put this in the DB.
                int result = getContentResolver().update(mCurrentUri, values, null, null);

                if (result != 0) {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_save_success), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_save_failed), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else {
            //Saving a new item.
            if (checkDataBeforeSaving()) {
                // this is reached if the data is valid.
                String nameOut = mNameET.getText().toString().trim();
                // these lines remove any $ that might be in the price edit text so it's safe to put
                // into a double value.
                String priceConversionString = mPriceET.getText().toString().trim();
                String priceConvertedString = priceConversionString.replaceAll("[,$]", "");
                Double priceOut = Double.valueOf(priceConvertedString);
                int quantityOut = Integer.valueOf(mQuantityET.getText().toString().trim());
                String supplierNameOut = mSupplierET.getText().toString().trim();
                String supplierPhoneOut = mSupplierPhoneET.getText().toString().trim();

                // Create values to insert this into the table.
                ContentValues values = new ContentValues();

                // use columns to put the row in the DB.
                values.put(InvTable.COL_NAME, nameOut);
                values.put(InvTable.COL_PRICE, priceOut);
                values.put(InvTable.COL_QUANTITY, quantityOut);
                values.put(InvTable.COL_SUPPLIER_NAME, supplierNameOut);
                values.put(InvTable.COL_SUPPLIER_PHONE_1, supplierPhoneOut);

                // Alright.  Now lets put this in the DB.
                Uri result = getContentResolver().insert(InvTable.CONTENT_URI, values);

                if (result != null) {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_save_success), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_save_failed), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create the projection
        // Projection for the cursor.
        String[] projection = {
                InvTable._ID,
                InvTable.COL_SUPPLIER_NAME,
                InvTable.COL_QUANTITY,
                InvTable.COL_PRICE,
                InvTable.COL_SUPPLIER_PHONE_1,
                InvTable.COL_NAME
        };
        // Cursor returned.
        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Fill stuffs in.
        if (data.moveToFirst()) {
            int nameCol = data.getColumnIndex(InvTable.COL_NAME);
            int priceCol = data.getColumnIndex(InvTable.COL_PRICE);
            int quantityCol = data.getColumnIndex(InvTable.COL_QUANTITY);
            int supplierCol = data.getColumnIndex(InvTable.COL_SUPPLIER_NAME);
            int supplierPhCol = data.getColumnIndex(InvTable.COL_SUPPLIER_PHONE_1);

            // format the data and get it ready for display.
            String name = data.getString(nameCol);
            Double price = data.getDouble(priceCol);
            int quantity = data.getInt(quantityCol);
            String supplier = data.getString(supplierCol);
            final String supplierPhone = data.getString(supplierPhCol);

            // Check price isn't 0, make it a double for precision.
            // format for the text edit
            NumberFormat format = NumberFormat.getCurrencyInstance();
            String outPrice = "0.00";
            if (price != 0) {
                outPrice = format.format(price);
            }

            // Phone must always be stored into the DB as
            // 10 digits so this has been checked many times before.
            // Phone number formatting for the button
            // 0-2 (###)
            // 3-5 ###-
            // 6-9 ####
            String callText = "Call: (" + supplierPhone.substring(0, 3) + ") "
                    + supplierPhone.substring(3, 6) + "-" + supplierPhone.substring(6, 10);

            // On click listener to call the supplier from the editor display.
            mCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhone));
                    startActivity(intent);
                }
            });

            // Finally, display the data.
            mNameET.setText(name);
            mPriceET.setText(outPrice);
            mQuantityET.setText(Integer.toString(quantity));
            mSupplierET.setText(supplier);
            mSupplierPhoneET.setText(supplierPhone);
            mCallButton.setText(callText);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // reset the views.
        mNameET.setText("");
        mPriceET.setText("");
        mQuantityET.setText("");
        mSupplierET.setText("");
        mSupplierPhoneET.setText("");
    }
}
