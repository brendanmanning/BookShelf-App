package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BookListFragment.ListFragmentInterface {

    private final String KEY_SAVED_BOOK = "saved_book";
    private int saved_book_index = -1;

    private FragmentManager fm;

    private BookListFragment blf = new BookListFragment();
    private BookDetailsFragment bdf = new BookDetailsFragment();

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * BASIC SETUP
         */

        // Bind UI items
        searchButton = findViewById(R.id.searchButton);

        // Determine the presentation view
        boolean singlePane = findViewById(R.id.fragment2) == null;

        // Get the Fragment manager and access the main fragment
        fm = getSupportFragmentManager();
        Fragment frag1 = getSupportFragmentManager().findFragmentById(R.id.fragment1);

        /**
         * SHOW THE LIST VIEW
         * via replace or add depending on the context
         */

        // At this point, I only want to have BookListFragment be displayed in container_1
        if (frag1 instanceof BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(frag1 instanceof BookListFragment)) {
            fm.beginTransaction()
                    .add(R.id.fragment1, blf)
                    .commit();
        } else if (frag1 instanceof BookListFragment) {
            fm.beginTransaction()
                    .replace(R.id.fragment1, blf)
                    .commit();
        }

        /**
         * RESTORE PREVIOUSLY SELECTED ITEM TO DETAIL VIEW
         */

        // Figure out what the saved book (if there is one) is
        int restore_book_index = -1;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_BOOK)) {
                restore_book_index = savedInstanceState.getInt(KEY_SAVED_BOOK);
            }
        }

        // Update the display fragment if there is a previous item
        if(restore_book_index != -1) {
            // TODO: - Put this line back
            // onSelectItem(restore_book_index, books.get(restore_book_index));
        }

        // The presentation will be different for landscape/large screen modes
        if (!singlePane) {
            fm.beginTransaction()
                    .replace(R.id.fragment2, bdf)
                    .commit();
        }

        /**
         * BIND LISTENERS TO UI ACTIONS
         */

        // Listen for search button clicks
        searchButton.setOnClickListener(v -> {
            Intent launchSearchIntent = new Intent(this, BookSearchActivity.class);
            startActivityForResult(launchSearchIntent, BookSearchActivity.BookSearchActivityRequestCode);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BookSearchActivity.BookSearchActivityRequestCode) {
            System.out.println("Is correct request code");
            if(resultCode == BookSearchActivity.BookSearchActivityCompletedResponseCode) {
                System.out.println("Is correct response code");
                List<Book> bookArrayList = data.getParcelableArrayListExtra(
                        BookSearchActivity.BookSearchActivityCompletedDataLocation
                );
                System.out.println("Got data from BookSearchActivityRequestCode+BookSearchActivityCompletedResponseCode ==> size=" + bookArrayList.size());
                blf.displayList(bookArrayList);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Put the current saved book index into the bundle
        savedInstanceState.putInt(KEY_SAVED_BOOK, this.saved_book_index);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onSelectItem(int position, Book book) {
        System.out.println("In MainActivity.onSelectItem(Book book)");
        this.saved_book_index = position;
        bdf.displayBook(book);
        if(findViewById(R.id.fragment2) == null) {
            System.out.println("Replacing fragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment1, bdf)
                    .addToBackStack(null)
                    .commit();
        } else {
            System.out.println("Setting in fragment 2");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment2, bdf)
                    .addToBackStack(null)
                    .commit();
        }
    }
}