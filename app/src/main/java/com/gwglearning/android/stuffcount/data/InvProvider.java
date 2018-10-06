package com.gwglearning.android.stuffcount.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class InvProvider extends ContentProvider {

    // Constant for URI creation (SQL *).
    public static final int BOOKS = 20;

    // Constant for URI Creation (not SQL *) specific book.
    public static final int BOOKS_ID = 201;

    // UriMatcher object.  Matches URI content to a corresponding code.
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer which is ran the first time this class is used.
    static {
        // This is where the calls to addURI() go to create the patterns that
        // the provider should recognize.  All paths listed here should return a code
        // when they are found.
        // Domain/Authority + path + ??
        mUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_BOOKS, BOOKS); // ALL
        mUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_BOOKS + "/#", BOOKS_ID); // just one
    }

    // Database Helper for the books database.
    private DBInterface mDBHelper;

    @Override
    public boolean onCreate() {
        // Make the DBHelper Usable
        mDBHelper = new DBInterface(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get the DB
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // Cursor creation.
        Cursor cursor = null;

        // What does the URI matcher say?
        int match = mUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // for the books * return a cursor with everything.
                cursor = db.query(InvContract.InvTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOKS_ID:
                // for the ID version, get the ID from the URI.
                selection = InvContract.InvTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InvContract.InvTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                // Huh?  Dunno what this is...
                throw new IllegalArgumentException("Invalid URI " + uri);
        }
        // if we made it this far, we have a cursor to return!!!!
        // First we have to let the content resolver know.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // returns the mime type.
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return InvContract.InvTable.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return InvContract.InvTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Uknown URI " + uri);
        }
    }

    // ******************************************************INSERT
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertIntoDB(uri, values);
            default:
                throw new IllegalArgumentException("Cannot insert into DB with " + uri);
        }
    }

    private Uri insertIntoDB(Uri uri, ContentValues values) {
        // Checks...

        // Check Name, Quantity, Price, Supplier name, Supplier phone1 of which
        //      NONE of these are allowed to be empty.  Quantity can be 0.
        //      I suppose price could be 0 and set later as well.
        // Call custom function to check the values.
        // DEBUG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Log.d("URI passed", "B4 Check: " + uri);
        checkData(values);

        // Ok we made it to here, lets continue.  If there was a format error we would
        // not make it to here by now.
        // Get the DB.
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // Insert into DB.
        long result = db.insert(InvContract.InvTable.TABLE_NAME, null, values);

        // Look to see if this was a success...
        if (result == -1) {
            // OH NO!!  Failure.   It's horrid!  O... M... gosh....
            Log.e("Failed INSERT", "URI failed : " + uri);
            return null;
        }

        // Still here ?  Oh ok.  Lets notify.
        getContext().getContentResolver().notifyChange(uri, null);

        // Ok.  return the URI with an ID.
        return ContentUris.withAppendedId(uri, result);
    } // ************************************************INSERT END

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get the database.
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int result;
        switch (match) {
            case BOOKS:
                // delete the entire database!
                result = db.delete(InvContract.InvTable.TABLE_NAME, selection, selectionArgs);
                if (result != 0) {
                    // success!  Notify the resolver.
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return result;

            case BOOKS_ID:
                // delete the single row by ID
                selection = InvContract.InvTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                result = db.delete(InvContract.InvTable.TABLE_NAME, selection, selectionArgs);
                if (result != 0) {
                    // Success!  Notify the resolver.
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return result;

            default:
                throw new IllegalArgumentException("Cannot delete item with uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Update.  This is the public method to Update.
        // Do the work in the private method.
        // get database
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateItem(uri, values, selection, selectionArgs);
            case BOOKS_ID:
                // got an ID ...
                selection = InvContract.InvTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update cannot proceed with uri: " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check data...
        checkData(values);

        // get the database
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // return the number of rows.
        int result = db.update(InvContract.InvTable.TABLE_NAME, values, selection, selectionArgs);

        // if we did anything..
        if (result != 0) {
            // Notify of change.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }


    private void checkData(ContentValues values) {
        // Check name.
        String name = values.getAsString(InvContract.InvTable.COL_NAME);
        if (name == null) {
            throw new IllegalArgumentException("This item requires a NAME");
        }

        // Check quantity for not a negative.
        int quantity = values.getAsInteger(InvContract.InvTable.COL_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("There cannot be a negative QUANTITY");
        }

        // Check Price.
        double price = values.getAsInteger(InvContract.InvTable.COL_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException("Price must be at least 0");
        }

        // Check Supplier name.
        String supplyName = values.getAsString(InvContract.InvTable.COL_SUPPLIER_NAME);
        if (supplyName == null) {
            throw new IllegalArgumentException("There must be a supplier name.");
        }

        // Check supplier number.
        // Should only contain numbers, as a string, and 10 numbers.
        String supplyNumber = values.getAsString(InvContract.InvTable.COL_SUPPLIER_PHONE_1);
        if (supplyNumber == null) {
            throw new IllegalArgumentException("There must be a supplier number");
        }
        if (supplyNumber.length() != 10) {
            throw new IllegalArgumentException("The phone number must be 10 digits long");
        }
        if (!TextUtils.isDigitsOnly(supplyNumber)) {
            throw new IllegalArgumentException("The phone number must be numbers only.");
        }

    }
}
