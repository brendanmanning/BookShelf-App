package edu.temple.bookshelf;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements com.brendanmanning.bookshelf.BookListFragment.BookSelectedInterface {

    FragmentManager fm;

    boolean twoPane;
    com.brendanmanning.bookshelf.BookDetailsFragment bookDetailsFragment;
    com.brendanmanning.bookshelf.Book selectedBook;

    private final String TAG_BOOKLIST = "booklist", TAG_BOOKDETAILS = "bookdetails";
    private final String KEY_SELECTED_BOOK = "selectedBook";
    private final String KEY_BOOKLIST = "searchedook";
    private final int BOOK_SEARCH_REQUEST_CODE = 123;

    com.brendanmanning.bookshelf.BookList bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.searchDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(com.brendanmanning.bookshelf.MainActivity.this, com.brendanmanning.bookshelf.BookSearchActivity.class), BOOK_SEARCH_REQUEST_CODE);
            }
        });

        if (savedInstanceState != null) {
            // Fetch selected book if there was one
            selectedBook = savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

            // Fetch previously searched books if one was previously retrieved
            bookList = savedInstanceState.getParcelable(KEY_BOOKLIST);
        }else {
            // Create empty booklist if
            bookList = new com.brendanmanning.bookshelf.BookList();
        }

        twoPane = findViewById(R.id.container2) != null;

        fm = getSupportFragmentManager();

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container_1);


        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof com.brendanmanning.bookshelf.BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(fragment1 instanceof com.brendanmanning.bookshelf.BookListFragment))
            fm.beginTransaction()
                    .add(R.id.container_1, com.brendanmanning.bookshelf.BookListFragment.newInstance(bookList), TAG_BOOKLIST)
            .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
        bookDetailsFragment = (selectedBook == null) ? new com.brendanmanning.bookshelf.BookDetailsFragment() : com.brendanmanning.bookshelf.BookDetailsFragment.newInstance(selectedBook);
        if (twoPane) {
            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment, TAG_BOOKDETAILS)
                    .commit();
        } else if (selectedBook != null) {
            /*
            If a book was selected, and we now have a single container, replace
            BookListFragment with BookDetailsFragment, making the transaction reversible
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, bookDetailsFragment, TAG_BOOKDETAILS)
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void bookSelected(int index) {
        // Store the selected book to use later if activity restarts
        selectedBook = bookList.get(index);

        if (twoPane)
            /*
            Display selected book using previously attached fragment
             */
            bookDetailsFragment.displayBook(selectedBook);
        else {
            /*
            Display book using new fragment
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, com.brendanmanning.bookshelf.BookDetailsFragment.newInstance(selectedBook), TAG_BOOKDETAILS)
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Display new books when retrieved from a search
     */
    private void showNewBooks() {
        if ((fm.findFragmentByTag(TAG_BOOKDETAILS) instanceof com.brendanmanning.bookshelf.BookDetailsFragment)) {
            fm.popBackStack();
        }
        ((com.brendanmanning.bookshelf.BookListFragment) fm.findFragmentByTag(TAG_BOOKLIST)).showNewBooks();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_BOOK, selectedBook);
        outState.putParcelable(KEY_BOOKLIST, bookList);
    }

    @Override
    public void onBackPressed() {
        // If the user hits the back button, clear the selected book
        selectedBook = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            bookList.clear();
            bookList.addAll((com.brendanmanning.bookshelf.BookList) data.getParcelableExtra(com.brendanmanning.bookshelf.BookSearchActivity.BOOKLIST_KEY));
            if (bookList.size() == 0) {
                Toast.makeText(this, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
            }
            showNewBooks();
        }
    }
}
