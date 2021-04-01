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

    private final String KEY_SAVED_BOOK = "saved_book";
    private int saved_book_index = -1;

    private FragmentManager fm;

    private BookListFragment blf = BookListFragment.newInstance(bookList);
    private BookDetailsFragment bdf = new BookDetailsFragment();

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = findViewById(R.id.searchButton);

        //Fetch selected book if there was one
        if (savedInstanceState != null)
            selectedBook = savedInstanceState.getParcelable(KEY_SAVED_BOOK);

        this.twoPane = findViewById(R.id.fragment2) != null;

        System.out.println("(#1) TWOPANE="+twoPane);

        fm = getSupportFragmentManager();

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.fragment1);


        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(fragment1 instanceof BookListFragment))
            fm.beginTransaction()
                    .add(R.id.fragment1, blf) // add books here
                    .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
        bdf = (selectedBook == null) ? new BookDetailsFragment() : BookDetailsFragment.newInstance(selectedBook);
        if (twoPane) {
            fm.beginTransaction()
                    .replace(R.id.fragment2, bdf)
                    .commit();
        } else if (selectedBook != null) {
            /*
            If a book was selected, and we now have a single container, replace
            BookListFragment with BookDetailsFragment, making the transaction reversible
             */
            fm.beginTransaction()
                    .replace(R.id.fragment1, bdf) // TODO - maybe .add ?
                    .addToBackStack(null)
                    .commit();
        }

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
                    System.out.println("Are showing BookDetailFragment, will need to popBackStack");
                    fm.popBackStack();
                }

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

    public void bookSelected(int position) {
        //Store the selected book to use later if activity restarts
        selectedBook = bookList.get(position);
        System.out.println("(#2) TWOPANE="+twoPane);
        if (this.twoPane)
            /*
            Display selected book using previously attached fragment
             */
            bdf.displayBook(selectedBook);
        else {
            /*
            Display book using new fragment
             */
            fm.beginTransaction()
                    .replace(R.id.fragment1, BookDetailsFragment.newInstance(selectedBook))
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

//    public void updateBookList(BookList list) {
//        bookList = list;
//        if(twoPane) {
//            blf.setBooks(list);
//        } else {
//            /*
//            Display books using new fragment
//             */
//            System.out.println("Replacing with new BookListFragment list");
//            blf = BookListFragment.newInstance(bookList);
//            fm.beginTransaction()
//                    .replace(R.id.fragment1, blf)
//                    // Transaction is reversible
//                    //.addToBackStack(null)
//                    .commit();
//        }
//    }

    private boolean isSinglePane() {
        return findViewById(R.id.fragment2) == null;
    }

    private boolean isShowingBookListFragment() {
        if(isSinglePane()) {
            return getFragments()[0] instanceof BookListFragment;
        } else {
            return true;
        }
    }

    private boolean isShowingBookDetailFragment() {
        if(isSinglePane()) {
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