package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class BookSearchActivity extends AppCompatActivity {

    EditText searchEditText;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        /**
         * BIND UI COMPONENTS
         */
        searchEditText = findViewById(R.id.searchSearchInput);
        searchButton = findViewById(R.id.searchSearchButton);

        /**
         * BIND UI HANDLERS
         */
        searchButton.setOnClickListener(v -> {

        });

    }
}