package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Button;

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

        // Bind UI items
        searchButton = findViewById(R.id.searchButton);

        // Read from the Bundle
        int restore_book_index = -1;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_BOOK)) {
                restore_book_index = savedInstanceState.getInt(KEY_SAVED_BOOK);
            }
        }

        // Determine the presentation view
        boolean singlePane = findViewById(R.id.fragment2) == null;

        // Get the Fragment manager and access the main fragment
        fm = getSupportFragmentManager();
        Fragment frag1 = getSupportFragmentManager().findFragmentById(R.id.fragment1);

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

        if(restore_book_index != -1) {
            // TODO: - Put this line back
            // onSelectItem(restore_book_index, books.get(restore_book_index));
        }

        if (!singlePane) {
            fm.beginTransaction()
                    .replace(R.id.fragment2, bdf)
                    .commit();
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