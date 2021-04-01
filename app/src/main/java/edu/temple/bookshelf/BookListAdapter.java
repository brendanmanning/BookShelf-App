package edu.temple.bookshelf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;



public class BookListAdapter extends BaseAdapter {

    Context context;
    BookList list;

    public BookListAdapter(@NonNull Context context, @NonNull BookList list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return this.list != null ? this.list.size() : 0;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        System.out.println("Get item at position " + position);
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        TextView bookTitleTextView;
        TextView bookAuthorTextView;

//        if(convertView == null) {
        bookTitleTextView = new TextView(this.context);
        bookAuthorTextView = new TextView(this.context);

        bookTitleTextView.setTextSize(20);
        bookTitleTextView.setPadding(10, 10, 10, 0);

        bookAuthorTextView.setTextSize(16);
        bookAuthorTextView.setPadding(10, 10, 10, 10);
//        } else {
//            bookTitleTextView = (TextView) convertView;
//        }

        Book book = (Book) getItem(position);

        bookTitleTextView.setText(book.getTitle());
        bookAuthorTextView.setText(book.getAuthor());

        linearLayout.addView(bookTitleTextView);
        linearLayout.addView(bookAuthorTextView);

//        Item item = (Item) getItem(position);
//        textView.setText(item.getName());
//
//        return textView;

        return linearLayout;

    }

}
