package com.aasavari.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_IMAGE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SALE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SUPPLIER;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_URI;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry._ID;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    ProductCursorAdapter mCursorAdapter;
    private static int PRODUCT_LOADER = 0;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //define a projection that specifies the columns that we care about
        String [] projection = {
                _ID,
                COLUMN_PROD_NAME,
                COLUMN_PROD_PRICE,
                COLUMN_PROD_QUANTITY,
                COLUMN_PROD_SUPPLIER,
                COLUMN_PROD_SALE,
                COLUMN_PROD_IMAGE
        };

        //The loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                  this,                  // Parent activity context
                  CONTENT_URI,           //Provider content uri as input to query
                  projection,            // columns to include in the resulting cursor
                  null,                  // no selection clause
                  null,                  // no selection arguments
                  null                   // default sort order
                );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //update the mCursorAdapter with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //Set up FAB to open DetailActivity
        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(addIntent);
            }
        });
        //Find listView to populate
        ListView productsList = (ListView)findViewById(R.id.products_list);

        //Find and set the empty view on the list view, so that it only shows up
        //when the list has 0 items
        View emptyView = findViewById(R.id.empty_inventory_text);
        productsList.setEmptyView(emptyView);
        //Set up a cursor adapter using cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        //attach the cursor adapter to the list view
        productsList.setAdapter(mCursorAdapter);
        //Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
                //the id is needed to pass as an intent extra to know which item we clicked on
                 //use getData() and getIntent() to get the associated URI
                //set the title of the editor activity depending on which situation we have.
                //If the editor activity was opened using the list view item, then we will
                //have the URI of the product so change app bar to say Edit Product
                //otherwise if this is a new product to be added, URI is null so change
                //the app bar to say Add a Product

               Intent editIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                //Form the content URI that represents the specific product that was clicked on,
                // by appending the "id"(passed as input to this method) onto the content uri.
                Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, id);
                Log.i("Aasavari: ", currentProductUri.toString());
                //Set the URI on the data field of the intent
                editIntent.setData(currentProductUri);

                //Launch the activity to display the data for the current product
                startActivity(editIntent);
            }
        });

    }


} // end of catalog activity class
