package edu.temple.bookshelf;

import android.content.Context;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
    private static String endpoint_search = "search.php?term=";

    public static void search(RequestQueue requestQueue, String query, Function<JSONObject, Void> callback) {

        System.out.println("In Api.search");

        // Create a full search URL
        String url = hostname + endpoint_search + query;

        // Create a request object with callbacks attatched
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                response -> {
                    System.out.println("In JsonObjectRequest.success callback");
                    callback.apply(response);
                },
                error -> {
                    System.out.println("In JsonObjectRequest.error callback");
                    callback.apply(null);
                }
        );

        // Add to the passed RequestQueue which should be created for each Activity
        requestQueue.add(jsonObjectRequest);

    }

}
