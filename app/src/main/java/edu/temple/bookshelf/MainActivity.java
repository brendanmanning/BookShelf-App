package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements BookListFragment.ListFragmentInterface {

    private BookList books = Books.getSamples();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment, BookListFragment.newInstance(Books.getSamples()))
                .commit();
    }

    public void onSelectItem(Book book) {
        System.out.println("In MainActivity.onSelectItem(Book book)");
        if(findViewById(R.id.bookDetailsFragment) == null) {
            System.out.println("Passed the null check!");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, BookDetailsFragment.newInstance(book))
                    .addToBackStack(null)
                    .commit();
        }
    }
}