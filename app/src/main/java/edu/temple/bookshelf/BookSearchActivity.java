package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
                return null;
            });
            requestQueue.start();
        });

    }
}