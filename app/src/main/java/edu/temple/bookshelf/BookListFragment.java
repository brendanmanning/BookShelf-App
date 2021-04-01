package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends Fragment {

    private static final String BOOK_LIST_KEY = "booklist";
    private BookList books;

    BookSelectedInterface parentActivity;

    public BookListFragment() {}

    public static BookListFragment newInstance(BookList books) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();

        /*
         A BookList implements the Parcelable interface
         therefore we can place a BookList inside a bundle
         by using that put() method.
         */
        args.putParcelable(BOOK_LIST_KEY, books);
        fragment.setArguments(args);
        return fragment;
    }

    public void setBooks(BookList list) {
        this.books = list;

        // This allows us to use the same fragment multiple times
        if (!this.isDetached() && getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /*
         This fragment needs to communicate with its parent activity
         so we verify that the activity implemented our defined interface
         */
        if (context instanceof BookSelectedInterface) {
            parentActivity = (BookSelectedInterface) context;
        } else {
            throw new RuntimeException("Please implement the required interface(s)");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getParcelable(BOOK_LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_book_list, container, false);

        listView.setAdapter(new BookListAdapter(getContext(), books));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.bookSelected(position);
            }
        });

        return listView;
    }

    /*
    Interface for communicating with attached activity
     */
    interface BookSelectedInterface {
        void bookSelected(int index);
    }
}