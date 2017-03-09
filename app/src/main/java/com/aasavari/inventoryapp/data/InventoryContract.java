package com.aasavari.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Aasavari on 3/6/2017.
 */

public  final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.aasavari.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static abstract class ProductEntry implements BaseColumns{

        public static final String TABLE_NAME = "products";
        //The MIME type for a list of products, for the content Uri
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        //The MIME type for a single product
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PROD_NAME = "name";
        public static final String COLUMN_PROD_PRICE = "price";
        public static final String COLUMN_PROD_QUANTITY = "quantity";
        public static final String COLUMN_PROD_SUPPLIER = "supplier";
        public static final String COLUMN_PROD_SALE = "sale";
        public static final String COLUMN_PROD_IMAGE = "picture";



    }
}
