package com.gwglearning.android.stuffcount;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gwglearning.android.stuffcount.data.InvContract.InvTable;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Loader number.
    final int LOADER_ID = 0;
    // Global cursor adapter
    InvCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the list view by ID.
        ListView booksList = findViewById(R.id.main_list_view);

        // Set the eptyview.
        View emptyView = findViewById(R.id.empty_view_text);
        booksList.setEmptyView(emptyView);

        // Set up Loader and adapter.
        mCursorAdapter = new InvCursorAdapter(this, null);
        booksList.setAdapter(mCursorAdapter);

        // create a loader.
        getLoaderManager().initLoader(LOADER_ID, null, this);

        // Set on click listener for the list view.
        booksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create an intent to the editor activity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // create a Uri for the item selected.
                Uri currentItem = ContentUris.withAppendedId(InvTable.CONTENT_URI, id);

                // Debugging ...
                Log.e("INTENT!", "Intent created on ID : " + id + " URI " + currentItem);

                // Put the Uri in the data field for the Intent.
                intent.setData(currentItem);

                // Launch!
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the settings menu.  Located at /res/menu/settings_menu.xml
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_data) {
            // Create a false entry.
            createFalseEntry();
            return true;
        }
        if (item.getItemId() == R.id.delete_data) {
            // delete all entries in the database.
            deleteAllBooksDialog();
        }
        if (item.getItemId() == R.id.create_new) {
            // create a new item!
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllBooksDialog() {
        // create an alert dialog to be sure we want to delete everything.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.main_dialog_delete_db_msg));
        builder.setPositiveButton(getString(R.string.main_dialog_delete_db_button_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lets delete....
                deleteAllBooks();
            }
        });
        builder.setNegativeButton(getString(R.string.main_dialog_delete_db_button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete all books in the database.
     */
    private void deleteAllBooks() {
        // Delete EVERYTHING!!
        int result = getContentResolver().delete(InvTable.CONTENT_URI, null, null);
        Toast.makeText(this, result + "items deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // Create a false entry.
    private void createFalseEntry() {
        // array of strings for names of suppliers.
        final String[] SUPPLIER_NAMES = {
                "General Bob's",
                "Unhandy Reads",
                "Known Knowing",
                "Macatronics",
                "Battery Text",
                "Funny Finds",
                "Base ick"
        };
        // array of strings for names of books.
        final String[] NAME_OF = {
                "How to Hammer",
                "My Precious",
                "Unspoken",
                "Center Ground",
                "Steal ME",
                "How to Tick",
                "Potluck Life",
                "Never Once",
                "Have a Shot",
                "Be a Friend",
                "Normally I Do",
                "The Band of None",
                "Spanish in 7 Days",
                "French in 9 Days",
                "Making Leather",
                "Four Score More",
                "Spring Scrubbing"
        };

        // Random number generator!!!!!!!!!!!!
        Random rand = new Random();

        // Declare our output items.
        String outputName = "";
        double outputPrice = 0;
        int outputQuantity = 0;
        String outputSupplierName = "";
        String outputSupplierPhone1 = "";

        // Create a random name.
        outputName = NAME_OF[rand.nextInt(NAME_OF.length - 1) + 1];

        // Create a random Price.  max 125$ for the examples here.
        outputPrice = rand.nextInt((125 - 1) + 1);

        // Create a random Quantity.  Max 100
        outputQuantity = rand.nextInt(100);

        // Create a random Supplier name from the string constant.
        outputSupplierName = SUPPLIER_NAMES[rand.nextInt((SUPPLIER_NAMES.length - 1) + 1)];

        // Create a supplier phone number.
        for (int counter = 0; counter < 10; counter++) {
            int nextNumber = rand.nextInt((9 - 1) + 1);
            outputSupplierPhone1 += nextNumber;
        }

        // Create values to insert this into the table.
        ContentValues values = new ContentValues();

        // use columns to put the row in the DB.
        values.put(InvTable.COL_NAME, outputName);
        values.put(InvTable.COL_PRICE, outputPrice);
        values.put(InvTable.COL_QUANTITY, outputQuantity);
        values.put(InvTable.COL_SUPPLIER_NAME, outputSupplierName);
        values.put(InvTable.COL_SUPPLIER_PHONE_1, outputSupplierPhone1);

        // Alright.  Now lets put this in the DB.
        Uri uriResult = getContentResolver().insert(InvTable.CONTENT_URI, values);
        Log.v("NewRowLog: ", "Returned URI: " + uriResult);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                InvTable.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}

