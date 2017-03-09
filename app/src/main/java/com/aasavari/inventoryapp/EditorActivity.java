package com.aasavari.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aasavari.inventoryapp.data.InventoryContract;
import com.aasavari.inventoryapp.data.InventoryContract.ProductEntry;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_IMAGE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SALE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SUPPLIER;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_URI;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry._ID;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private EditText mName;
    private EditText mPrice;
    private EditText mQuantity;
    private EditText mSupplier;
    private String strName;
    private float fPrice;
    private int iQuantity;
    private String strSupplier;
    private Button mSaveButton;
    private Uri mCurrentProductUri;
    private static int EDIT_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one,
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //if the intent does not contain a product content URI, then we know that we are creating
        //a new product
        if(mCurrentProductUri == null){
            //This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.add_title));
            Toast.makeText(getApplicationContext(), "Adding product", Toast.LENGTH_SHORT).show();

        }
        else{
            //otherwise this is an existing product, so change the app bar to say "Edit Product"
            setTitle(getString(R.string.edit_title));
        }

        mName= (EditText)findViewById(R.id.name);
        mPrice = (EditText)findViewById(R.id.price);
        mQuantity = (EditText)findViewById(R.id.quantity);
        mSupplier = (EditText)findViewById(R.id.supplier);
        mSaveButton = (Button)findViewById(R.id.save_button);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strName = mName.getText().toString().trim();
                fPrice = Float.valueOf(mPrice.getText().toString().trim());
                iQuantity=Integer.valueOf(mQuantity.getText().toString().trim());
                strSupplier = mSupplier.getText().toString().trim();
                ContentValues values = new ContentValues();
                values.put(COLUMN_PROD_NAME, strName);
                values.put(COLUMN_PROD_PRICE, fPrice);
                values.put(COLUMN_PROD_QUANTITY, iQuantity);
                values.put(COLUMN_PROD_SUPPLIER, strSupplier);
                if(mCurrentProductUri == null) {
                    //check to see if any of the mandatory fields for the database are empty
                    // or have invalid data like -ve prices or quantities
                    if(TextUtils.isEmpty(strName) && TextUtils.isEmpty(strSupplier)
                        && iQuantity < 0 && fPrice < 0.0f)
                            return;
                    //This is a new product, so insert in database
                    getContentResolver().insert(mCurrentProductUri, values);
                }
                else {
                    //This is an existing product so update it in the database
                    int rowsAffected = getContentResolver().update(mCurrentProductUri,
                            values, null, null);
                   // Show a toast message depending on whether or not the update was successful.
                    if(rowsAffected == 0){
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(EditorActivity.this, getString(R.string.editor_update_product_failed),
                                Toast.LENGTH_SHORT).show();
                    }else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(EditorActivity.this, getString(R.string.editor_update_product_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                mName.setText("");
                mPrice.setText("");
                mQuantity.setText("");
                mSupplier.setText("");

                finish();
            }
        });

        getLoaderManager().initLoader(EDIT_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, " onCreateLoader");
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
        Log.i(LOG_TAG, "The Projection is: " + projection.toString());
        //The loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                  // Parent activity context
                mCurrentProductUri,    //Provider content uri as input to query
                projection,            // columns to include in the resulting cursor
                null,                  // no selection clause
                null,                  // no selection arguments
                null                   // default sort order
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(LOG_TAG, " onLoadFinished");
        if(cursor == null)
            return;
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if(cursor.moveToFirst()){
            // Find the columns of product attributes that we're interested in

            int nameColumnIndex = cursor.getColumnIndex(COLUMN_PROD_NAME);
            int priceColumnIndex = cursor.getColumnIndex(COLUMN_PROD_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(COLUMN_PROD_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(COLUMN_PROD_SUPPLIER);

            // Extract out the value from the Cursor for the given column index
            strName = cursor.getString(nameColumnIndex);
            fPrice = cursor.getFloat(priceColumnIndex);
            iQuantity = cursor.getInt(quantityColumnIndex);
            strSupplier = cursor.getString(supplierColumnIndex);

            //Set the views with the data.
            // Update the views on the screen with the values from the database
            mName.setText(strName);
            mPrice.setText(Float.toString(fPrice));
            mQuantity.setText(Integer.toString(iQuantity));
            mSupplier.setText(strSupplier);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "Aasavari: onLoaderReset");
        mName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
        mSupplier.setText("");

    }
}
