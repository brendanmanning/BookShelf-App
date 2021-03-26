package edu.temple.bookshelf;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookListFragment extends Fragment {

    private ArrayList<Book> books = new ArrayList<Book>();

    public BookListFragment() {}

    public static BookListFragment newInstance(BookList bookList) {
        System.out.println("ArrayList");
        System.out.println(bookList.toArrayList());
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("book_list", bookList.toArrayList());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            System.out.println("In onCreate in ListFragment");
            books = getArguments().getParcelableArrayList("book_list");
            System.out.println("First is: " + books.get(0).getTitle());
        } else {
            System.out.println("In onCreate in ListFragment (default)");
            books = new ArrayList<Book>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("In BookListFragment.onCreateView()");

        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        ListView listView = view.findViewById(R.id.fragmentListView);
        listView.setAdapter(new BookListAdapter(getContext(), android.R.layout.simple_list_item_1, books));
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            System.out.println("In onItemClick");
            ((ListFragmentInterface) getActivity()).onSelectItem(position, books.get(position));
        });

        return view;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface ListFragmentInterface {
        void onSelectItem(int index, Book book);
    }
}