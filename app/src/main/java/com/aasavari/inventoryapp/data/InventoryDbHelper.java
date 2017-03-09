package com.aasavari.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.R.attr.name;
import static android.R.attr.version;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_IMAGE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SALE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SUPPLIER;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.TABLE_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry._ID;

/**
 * Created by Aasavari on 3/6/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PROD_NAME + " TEXT NOT NULL, "
                + COLUMN_PROD_PRICE + " FLOAT NOT NULL, "
                + COLUMN_PROD_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_PROD_SUPPLIER + " TEXT NOT NULL, "
                + COLUMN_PROD_SALE + " INTEGER DEFAULT 0 ,"
                + COLUMN_PROD_IMAGE + " TEXT " +
                ");";
        Log.i("SQL STRING: ",SQL_CREATE_PRODUCTS_TABLE );
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // The database is still at version 1, so there's nothing to do be done here.
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }
}
