package edu.temple.bookshelf;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "book_title";
    private static final String ARG_PARAM2 = "book_author";

    // TODO: Rename and change types of parameters
    private String bookTitle;
    private String bookAuthor;

    View view;
    TextView titleTextView;
    TextView authorTextView;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public void displayBook(Book book) {
        System.out.println("Running displayBook(Book book)");
        this.bookTitle = book.getTitle();
        this.bookAuthor = book.getAuthor();

        if( titleTextView != null && authorTextView != null ) {
            titleTextView.setText(this.bookTitle);
            authorTextView.setText(this.bookAuthor);
        }

    }

    public static BookDetailsFragment newInstance(Book book) {
        System.out.println("In newInstance(Book book)");
        BookDetailsFragment fragment = new BookDetailsFragment();
        System.out.println("Created fragment");
        Bundle args = new Bundle();
        System.out.println("Created bundle");
        args.putString(ARG_PARAM1, book.getTitle());
        args.putString(ARG_PARAM2, book.getAuthor());
        System.out.println("Added arguments");
        fragment.setArguments(args);
        System.out.println("Set arguments");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookTitle = getArguments().getString(ARG_PARAM1);
            bookAuthor = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_book_details, container, false);

        this.titleTextView = (TextView) view.findViewById(R.id.bookDetailTitleTextView);
        this.authorTextView = (TextView) view.findViewById(R.id.bookDetailAuthorTextView);

        this.titleTextView.setText(this.bookTitle);
        this.authorTextView.setText(this.bookAuthor);

        return this.view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}