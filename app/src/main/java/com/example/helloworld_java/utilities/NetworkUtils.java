package com.example.helloworld_java.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.helloworld_java.data.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class NetworkUtils {

    public static ArrayList<Product> getProductJSONfromURL(String urlStr){
        String jsonResStr = null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        Scanner scanner = null;
        JSONArray productArr = null;
        ArrayList<Product> productList = new ArrayList<>();
        Log.d("here","here");

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                scanner = new Scanner(stream);
                scanner.useDelimiter("\\A");
                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    jsonResStr = scanner.next();
                }
            }

            Log.d("here","json"+String.valueOf(productArr));
            productArr = new JSONArray(jsonResStr);
            if (productArr != null) {
                for (int i=0;i<productArr.length();i++){
                    JSONObject p = productArr.getJSONObject(i);
                    productList.add (new Product(p.getString("title"), p.getString("description"),
                            p.getDouble("price"), p.getString("image") , p.getString("quantity"), 0));
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        } finally {
            try{
                scanner.close();
                stream.close();
                connection.disconnect();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return productList;
    }

    public static Bitmap getImageForProduct(String urlStr){
        InputStream stream = null;
        HttpURLConnection connection = null;
        Bitmap productImg = null;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                productImg = BitmapFactory.
                        decodeStream(stream);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try{
                stream.close();
                connection.disconnect();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return productImg;
    }
}
