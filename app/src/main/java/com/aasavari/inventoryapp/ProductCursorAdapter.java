package com.aasavari.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aasavari.inventoryapp.data.InventoryContract;
import com.aasavari.inventoryapp.data.InventoryContract.ProductEntry;

import org.w3c.dom.Text;

import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_NAME;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_PRICE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_QUANTITY;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SALE;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.COLUMN_PROD_SUPPLIER;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry.CONTENT_URI;
import static com.aasavari.inventoryapp.data.InventoryContract.ProductEntry._ID;

/**
 * Created by Aasavari on 3/7/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    ProductViewHolder mViewHolder;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //static Viewholder class to cache all our views in the list item
    //This helps in smoother scrolling of the listview as it prevents multiple redundant
    //calls to findviewbyid, every time a new listitem has to be created
    //hence this class is declared as static.

    static class ProductViewHolder{
        TextView txtName;
        TextView txtPrice;
        TextView txtQuantity;
        Button btnSell;


        public ProductViewHolder(View view){
            txtName = (TextView)view.findViewById(R.id.name);
            txtPrice = (TextView)view.findViewById(R.id.price);
            txtQuantity = (TextView)view.findViewById(R.id.quantity);
            btnSell = (Button)view.findViewById(R.id.sale_btn);
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Create and return a new blank list item
        View view =  LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        mViewHolder = new ProductViewHolder(view);
        view.setTag(mViewHolder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //Extract properties from the cursor
        String strName = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_NAME));
        float fPrice = cursor.getFloat(cursor.getColumnIndex(COLUMN_PROD_PRICE));
         final int iQuantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_QUANTITY));

        //Use the view holder to extract each view and set its data
        mViewHolder.txtName.setText(strName);
        mViewHolder.txtPrice.setText(String.valueOf(fPrice));
        mViewHolder.txtQuantity.setText(String.valueOf(iQuantity));

        //Set click listener for the sell button
        //This will reduce the existing quantity of the product by 1 and increase the number of
        //items sold for this product by 1
        mViewHolder.btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                if(iQuantity > 0) {
                    int newQuantity = iQuantity;
                    newQuantity--;
                    Log.i(LOG_TAG, "The Quantity to update " + newQuantity);
                    int itemsSold = cursor.getInt(cursor.getColumnIndex(COLUMN_PROD_SALE));
                    itemsSold++;
                    Log.i(LOG_TAG, "The solditems to update " + itemsSold);
                    int id = cursor.getInt(cursor.getColumnIndex(_ID));
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PROD_QUANTITY, newQuantity);
                    values.put(ProductEntry.COLUMN_PROD_SALE, itemsSold);
                    Uri currentProductUri = ContentUris.withAppendedId(CONTENT_URI, id);
                    Log.i(LOG_TAG, currentProductUri.toString());
                    int rows = resolver.update(currentProductUri, values,
                                                   null, null);
                }
                else{
                    Log.i(LOG_TAG, "The quantity cannot be negative");
                }
            }
        });


    }
}
