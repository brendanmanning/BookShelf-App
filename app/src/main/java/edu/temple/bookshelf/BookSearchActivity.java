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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.function.Function;

public class BookSearchActivity extends AppCompatActivity {

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
            BookList.fromSearch(requestQueue, searchEditText.getText().toString(), bookList -> {
                System.out.println("Got a booklist");
                System.out.println("First title: " + bookList.get(0).getTitle());

                Intent intentData = new Intent();
                intentData.putExtra("val", bookList.toArrayList());
                setResult(456, intentData);

                return null;
            });
            requestQueue.start();
        });

    }
}