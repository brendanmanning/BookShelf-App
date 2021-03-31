package edu.temple.bookshelf;

import android.content.Context;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Function;

public class Api {

    private static String hostname = "https://kamorris.com/lab/cis3515/";
    private static String endpoint = "search.php";
    private static String search_key = "?term=";

    public static void search(RequestQueue requestQueue, String query, Function<JSONArray, Void> callback) {

        System.out.println("In Api.search");

        // Create a full search URL
        String url = hostname + endpoint;
        if(query != null && query.length() > 0) {
            url += search_key + query;
        }

        System.out.println("Calling " + url + " ...");

        // Create a request object with callbacks attatched
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                response -> {
                    System.out.println("In JsonArrayRequest.success callback");
                    System.out.println(response);
                    callback.apply(response);
                },
                error -> {
                    System.out.println("In JsonArrayRequest.error callback");
                    System.out.println(error.toString());
                    error.printStackTrace();
                    callback.apply(null);
                }
        );

        // Add to the passed RequestQueue which should be created for each Activity
        requestQueue.add(jsonArrayRequest);

    }

}
