package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.Function;

public class BookSearchActivity extends AppCompatActivity {

    public static final int BookSearchActivityRequestCode = 1001;
    public static final int BookSearchActivityCompletedResponseCode = 2001;
    public static final String BookSearchActivityCompletedDataLocation = "BookSearchActivityCompletedDataLocation";

    private static String hostname = "https://kamorris.com/lab/cis3515/";
    private static String endpoint = "search.php";
    private static String search_key = "?term=";

    EditText searchEditText;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        /**
         * BIND UI COMPONENTS
         */
        searchEditText = findViewById(R.id.searchSearchInput);
        searchButton = findViewById(R.id.searchSearchButton);

        /**
         * BIND UI HANDLERS
         */
        searchButton.setOnClickListener(v -> {
            // Create a request object with callbacks attatched
            String url = makeURL(searchEditText.getText().toString());
            System.out.println("Calling " + url + " ...");
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    url,
                    response -> {
                        System.out.println("In JsonArrayRequest.success callback");
                        System.out.println(response);

                        BookList list = new BookList();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                list.add(
                                        new Book(
                                                Integer.parseInt(response.getJSONObject(i).getString("id")),
                                                response.getJSONObject(i).getString("title"),
                                                response.getJSONObject(i).getString("author"),
                                                response.getJSONObject(i).getString("cover_url")
                                        )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("Created list with size " + list.size());

                        // Create an intent to transfer data
                        Intent intentData = new Intent();
                        intentData.putExtra(BookSearchActivity.BookSearchActivityCompletedDataLocation, list);
                        setResult(BookSearchActivity.BookSearchActivityCompletedResponseCode, intentData);

                        // Close activity here
                        finish();
                    },
                    error -> {
                        System.out.println("In JsonArrayRequest.error callback");
                        System.out.println(error.toString());
                        error.printStackTrace();
                    }
            );
            requestQueue.add(jsonArrayRequest);
            requestQueue.start();
        });

    }

    private String makeURL(String query) {
        // Create a full search URL
        String url = BookSearchActivity.hostname + BookSearchActivity.endpoint;
        if(query != null && query.length() > 0) {
            url += BookSearchActivity.search_key + query;
        }
        return url;
    }
}