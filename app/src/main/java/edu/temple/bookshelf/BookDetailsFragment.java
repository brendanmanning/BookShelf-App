package edu.temple.bookshelf;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BOOK_TITLE = "book_title";
    private static final String ARG_BOOK_AUTHOR = "book_author";
    private static final String ARG_BOOK_IMAGE = "book_image";

    // TODO: Rename and change types of parameters
    private String bookTitle;
    private String bookAuthor;
    private String bookUrl;

    View view;
    TextView titleTextView;
    TextView authorTextView;
    ImageView imageView;

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public void displayBook(Book book) {
        System.out.println("Running displayBook(Book book)");
        this.bookTitle = book.getTitle();
        this.bookAuthor = book.getAuthor();
        this.bookUrl = book.getCoverURL();

        if( titleTextView != null && authorTextView != null && bookUrl != null ) {
            titleTextView.setText(this.bookTitle);
            authorTextView.setText(this.bookAuthor);
            Picasso.get().load(Uri.parse(this.bookUrl)).into(imageView);
        }

    }

    public static BookDetailsFragment newInstance(Book book) {
        System.out.println("In newInstance(Book book)");
        BookDetailsFragment fragment = new BookDetailsFragment();
        System.out.println("Created fragment");
        Bundle args = new Bundle();
        System.out.println("Created bundle");
        args.putString(ARG_BOOK_TITLE, book.getTitle());
        args.putString(ARG_BOOK_AUTHOR, book.getAuthor());
        args.putString(ARG_BOOK_IMAGE, book.getCoverURL());
        System.out.println("Added arguments");
        fragment.setArguments(args);
        System.out.println("Set arguments");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookTitle = getArguments().getString(ARG_BOOK_TITLE);
            bookAuthor = getArguments().getString(ARG_BOOK_AUTHOR);
            bookUrl = getArguments().getString(ARG_BOOK_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_book_details, container, false);

        this.titleTextView = (TextView) view.findViewById(R.id.bookDetailTitleTextView);
        this.authorTextView = (TextView) view.findViewById(R.id.bookDetailAuthorTextView);
        this.imageView = (ImageView) view.findViewById(R.id.imageView);

        this.titleTextView.setText(this.bookTitle);
        this.authorTextView.setText(this.bookAuthor);
        if(this.bookUrl != null)
            Picasso.get().load(Uri.parse(this.bookUrl)).into(imageView);

        return this.view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}