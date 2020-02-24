package com.example.helloworld_java;

import android.app.Application;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helloworld_java.data.AppDatabase;
import com.example.helloworld_java.data.Product;
import com.example.helloworld_java.data.ProductDao;
import com.example.helloworld_java.utilities.NetworkUtils;
import com.fullstory.FS;
import com.fullstory.FSSessionData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment implements ProductRecyclerViewAdapter.ProductAdapterHandler{
    private RecyclerView mRecyclerView;
    private ProductRecyclerViewAdapter mFruitRecyclerViewAdapter;
    private ProductDao mProductDao;
    private List<Product> mProductsList;
    AppDatabase db;



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mRecyclerView = view.findViewById(R.id.rv_product);
        LinearLayoutManager layoutManager
                =new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mFruitRecyclerViewAdapter = new ProductRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mFruitRecyclerViewAdapter);

        db = AppDatabase.getDatabase(getContext());
        mProductDao = db.productDao();

        showFruitList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_market, container, false);
    }


    private void showFruitList(){
        String urlStr = getString(R.string.aws_host);
        new FetchProductListTask().execute(urlStr);
    }


    @Override
    public void onClick(Product productObj){
        //add 1lb to cart
        final Product product;
        try {
            product = new Product(productObj.title,productObj.description,productObj.price,productObj.image,productObj.unit,1);
            new AddProductToCartTask().execute(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View createFragmentSpecificView(Product product){
        LinearLayout layout = new LinearLayout(getContext());
        TextView priceTextView = new TextView(getContext());
        priceTextView.setText(product.price.toString()+'/'+product.unit);
        layout.addView(priceTextView);

        return layout;
    }

    @Override
    public String buttonText(){
        return getString(R.string.add_to_cart);
    }


    private class AddProductToCartTask extends  AsyncTask<Product, Void, Product>{
        @Override
        protected void onPreExecute(){super.onPreExecute();}

        @Override
        protected Product doInBackground (final Product... products){
            if (products.length > 0) {
                //transaction here
                db.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        List<Product> productInCart = mProductDao.loadAllByTitles(products[0].title);
                        if(productInCart.size()>0){
                            productInCart.get(0).quantityInCart++;
                            mProductDao.updateQuantityInCart(productInCart.get(0));
                        }else {
                            mProductDao.insertAll(products[0]);
                        }

                    }
                });
                return products[0];
            }
            return null;
        }

        @Override
        protected void onPostExecute(Product product){
            if(product != null){
                Toast.makeText(getContext(),product.title + " added to cart!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class FetchProductListTask extends AsyncTask<String, Void, ArrayList<Product>> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Product> doInBackground(String... urlStr){
            if (urlStr.length > 0) {
                return NetworkUtils.getProductJSONfromURL(urlStr[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Product> productsListArr){
            String imgBaseURLStr = getString(R.string.img_host);
            mProductsList = productsListArr;
            mFruitRecyclerViewAdapter.setProductList(productsListArr,imgBaseURLStr);
        }
    }
}