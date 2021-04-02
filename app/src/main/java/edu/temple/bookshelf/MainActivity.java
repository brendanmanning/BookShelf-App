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

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface {

    Book selectedBook;
    BookList bookList = new BookList();

    private boolean twoPane;

    private final String KEY_SAVED_BOOKS = "saved_books";
    private final String KEY_SAVED_BOOK = "saved_book";
    private int saved_book_index = -1;

    private FragmentManager fm;

    private BookListFragment blf = BookListFragment.newInstance(bookList);
    private BookDetailsFragment bdf = new BookDetailsFragment();

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.twoPane = findViewById(R.id.fragment2) != null;
        searchButton = findViewById(R.id.searchButton);

        fm = getSupportFragmentManager();
        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.fragment1);

        //Fetch selected book if there was one
        if (savedInstanceState != null) {

            if(savedInstanceState.containsKey(KEY_SAVED_BOOKS)) {
                this.bookList = savedInstanceState.getParcelable(KEY_SAVED_BOOKS);
            }

            if(savedInstanceState.containsKey(KEY_SAVED_BOOK)) {
                int selectedBookIndex = savedInstanceState.getInt(KEY_SAVED_BOOK);
                if (selectedBookIndex >= 0) {
                    this.selectedBook = this.bookList.get(selectedBookIndex);
                }
            }
        }

        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof BookDetailsFragment) {
            System.out.println("#1 - Fragment1 was a BookDetailsFragment");
            fm.popBackStack();
        } else if (!(fragment1 instanceof BookListFragment))
            System.out.println("#2 - Fragment1 was otherwise not a BookListFragment");
            fm.beginTransaction().add(R.id.fragment1, blf).commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */

        if (twoPane) {
            System.out.println("#3 - Set detail view for 2-pane");
            fm.beginTransaction().replace(R.id.fragment2, bdf).commit();
        } else if (selectedBook != null) {
            System.out.println("#4 - 1-pane mode WITH a saved book");
            fm.beginTransaction().replace(R.id.fragment1, bdf).addToBackStack(null).commit();  // switch back to replace
        }

        if(selectedBook != null) {
            System.out.println("#5 - Actually updating the saved book");
            bdf.displayBook(this.selectedBook);
        }

        // Listen for search button clicks
        searchButton.setOnClickListener(v -> {
            Intent launchSearchIntent = new Intent(this, BookSearchActivity.class);
            startActivityForResult(launchSearchIntent, BookSearchActivity.BookSearchActivityRequestCode);
        });

        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BookSearchActivity.BookSearchActivityRequestCode) {
            if(resultCode == BookSearchActivity.BookSearchActivityCompletedResponseCode) {
                BookList bl = data.getParcelableExtra(
                        BookSearchActivity.BookSearchActivityCompletedDataLocation
                );
                System.out.println("Got data from BookSearchActivityRequestCode+BookSearchActivityCompletedResponseCode ==> size=" + bl.size());

                bookList.clear();
                for(int i = 0; i < bl.size(); i++) {
                    bookList.add(bl.get(i));
                }
                blf.refresh();

                if(!twoPane && isShowingBookDetailFragment()) {
                    fm.popBackStack();
                }

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        System.out.println("Saving book list: KEY_SAVED_BOOKS=" + this.bookList.toString());
        System.out.println("Saving book index: KEY_SAVED_BOOK=" + this.saved_book_index);

        // Put the current BookList into the bundle
        savedInstanceState.putParcelable(KEY_SAVED_BOOKS, this.bookList);

        // Put the current saved book index into the bundle
        savedInstanceState.putInt(KEY_SAVED_BOOK, this.saved_book_index);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void bookSelected(int position) {
        //Store the selected book to use later if activity restarts
        selectedBook = bookList.get(position);
        this.saved_book_index = position;

        if (this.twoPane)
            bdf.displayBook(selectedBook);
        else {
            fm.beginTransaction().replace(R.id.fragment1, BookDetailsFragment.newInstance(selectedBook)).addToBackStack(null).commit();
        }
    }


    private boolean isShowingBookListFragment() {
        if(!twoPane) {
            return getFragments()[0] instanceof BookListFragment;
        } else {
            return true;
        }
    }

    private boolean isShowingBookDetailFragment() {
        if(!twoPane) {
            return getFragments()[0] instanceof BookDetailsFragment;
        } else {
            return true;
        }
    }

    private Fragment[] getFragments() {
        fm = getSupportFragmentManager();
        return new Fragment[]{
            fm.findFragmentById(R.id.fragment1),
            fm.findFragmentById(R.id.fragment2)
        };
    }

    @Override
    public void onBackPressed() {
        // If the user hits the back button, clear the selected book
        selectedBook = null;
        super.onBackPressed();
    }

}