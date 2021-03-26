package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.PersistableBundle;

public class MainActivity extends AppCompatActivity implements BookListFragment.ListFragmentInterface {

    private final String KEY_SAVED_BOOK = "saved_book";
    private int saved_book_index = -1;

    private BookList books = Books.getSamples();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int restore_book_index = -1;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_BOOK)) {
                restore_book_index = savedInstanceState.getInt(KEY_SAVED_BOOK);
            }
        }

        boolean singlePane = findViewById(R.id.fragment2) == null;
        System.out.println(singlePane ? "SINGLE PANE" : "MULTIPLE PANES");

        FragmentManager fm = getSupportFragmentManager();
        BookDetailsFragment bdf = new BookDetailsFragment();

        Fragment frag1 = getSupportFragmentManager().findFragmentById(R.id.fragment1);

        // At this point, I only want to have BookListFragment be displayed in container_1
        if (frag1 instanceof BookDetailsFragment) {
            System.out.println("Condition 1");
            fm.popBackStack();
        } else if (!(frag1 instanceof BookListFragment)) {
            System.out.println("Condition 2");
            fm.beginTransaction()
                    .add(R.id.fragment1, BookListFragment.newInstance(Books.getSamples()))
                    .commit();
        } else if (frag1 instanceof BookListFragment) {
            System.out.println("Condition 3");
            fm.beginTransaction()
                    .replace(R.id.fragment1, BookListFragment.newInstance(Books.getSamples()))
                    .commit();
        }

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
//        bdf = (restore_book_index == -1) ? new BookDetailsFragment() : BookDetailsFragment.newInstance(books.get(restore_book_index));
//        if (!singlePane) {
//            System.out.println("Condition 4");
//            fm.beginTransaction()
//                    .replace(R.id.fragment2, bdf)
//                    .commit();
//        } else if (restore_book_index != -1) {
//            System.out.println("Condition 5");
//            /*
//            If a book was selected, and we now have a single container, replace
//            BookListFragment with BookDetailsFragment, making the trans
//             */
//            onSelectItem(restore_book_index, books.get(restore_book_index));
//        }

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
        if(findViewById(R.id.fragment2) == null) {
            System.out.println("Passed the null check!");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment1, BookDetailsFragment.newInstance(book))
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment2, BookDetailsFragment.newInstance(book))
                    .addToBackStack(null)
                    .commit();
        }
    }
}