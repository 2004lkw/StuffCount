package com.gwglearning.android.stuffcount.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InvContract {

    // Content authority.  this is for the entire content provider.
    public static final String CONTENT_AUTHORITY = "com.gwglearning.android.stuffcount";
    // Use CONTENT_AUTHORITY to create the base of all URI's which
    // apps will use to contact the provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // path to append to base URI's for the data for this content provider.
    public static final String PATH_BOOKS = "books";

    // private constructor so that this never gets instanced.
    private InvContract() {
    }

    public static final class InvTable implements BaseColumns {
        // Inner class; for the database columns and table name.

        // MIME type of the CONTENT_URI for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // MIME type of the CONTENT_URI for a single book.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Content URI to access the inventory data in the provider.
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        //Table name
        public final static String TABLE_NAME = "bookinventory";

        // ID (INT)
        public final static String _ID = BaseColumns._ID;
        // Product name (STRING)
        public final static String COL_NAME = "name";
        // Price (DOUBLE)
        public final static String COL_PRICE = "price";
        // Quantity (INT)
        public final static String COL_QUANTITY = "quantity";
        // Supplier Name (STRING)
        public final static String COL_SUPPLIER_NAME = "supplier_name";
        // Supplier Phone Number (STRING)
        public final static String COL_SUPPLIER_PHONE_1 = "supplier_phone_1";
    }
}
