package com.aasavari.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URI;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.os.Build.PRODUCT;
import static com.aasavari.inventoryapp.data.InventoryContract.CONTENT_AUTHORITY;
import static com.aasavari.inventoryapp.data.InventoryContract.PATH_PRODUCTS;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SUPPLIER;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_ITEM_TYPE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.TABLE_NAME;

import com.aasavari.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by Aasavari on 3/6/2017.
 */

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbHelper; // global so that all the provider methods can access this object
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    //Uri matcher code for the content Uri in the products table
    private static final int PRODUCTS = 100;
    //Uri matcher code for the content Uri for a single product in the products table
    private static final int PRODUCT_ID = 101;
    //Create a Uri Matcher object to match a content URI to a corresponding code.
    //The input passed into the constructor represents the code to return for the root URI.
    // Its common to use NO_MATCH as the input for this case.

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize.
         */
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS , PRODUCTS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object
     * @return
     */
    @Override
    public boolean onCreate() {
        //use this object to gain access tot he inventory database
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Log.i(LOG_TAG, uri.toString());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                cursor = db.query(TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri" + uri);
        }

        //set notification URI on the cursor,
        //So we know what content uri the cursor was created for.
        //If the data at this URI changes, then we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                return CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + "with match " + match);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Boolean validValues = sanityCheck(contentValues);
        if(!validValues)
            throw new IllegalArgumentException("Content values cannot be null");
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues){
        long rowId;
        //insert a new product into the products table with the given contentValues
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         rowId = db.insert(TABLE_NAME, null, contentValues);
         if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
             return null;
            }

        //Notify all listeners that the data has changed for the product content uri
        getContext().getContentResolver().notifyChange(uri, null);

        //return the new uri with the ID of the newly inserted row appended at the end
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted = -1;
        switch(match){
            case PRODUCTS:
                rowsDeleted =  db.delete(TABLE_NAME, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            //Notify all listeners that the data has changed for the product content uri
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if(contentValues.size()== 0)
            return 0;
        Boolean validValues = sanityCheck(contentValues);
        if(!validValues)
            throw new IllegalArgumentException("Content values cannot be null and/or invalid");
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update operation is not supported for " + uri);
        }
    }

    private int updateProduct( Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated =  db.update(TABLE_NAME,contentValues, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0){
            //Notify all listeners that the data has changed for the product content uri
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /*
    In this helper method,we check to see if the content values object has valid values
     */
    private boolean sanityCheck(ContentValues values){
        String name="";
        String supplier="";
        Integer quantity = 0;
        Float price = 0.0f;
        if(values.containsKey(COLUMN_PROD_NAME))
             name = values.getAsString(COLUMN_PROD_NAME);
        if(values.containsKey(COLUMN_PROD_PRICE))
             price = values.getAsFloat(COLUMN_PROD_PRICE);
        if(values.containsKey(COLUMN_PROD_QUANTITY))
            quantity = values.getAsInteger(COLUMN_PROD_QUANTITY);
        if(values.containsKey(COLUMN_PROD_SUPPLIER))
            supplier = values.getAsString(COLUMN_PROD_SUPPLIER);
        if(name == null || supplier == null || price == null || quantity == null || quantity < 0 || price < 0)
            return false;
        return true;
    }
}
