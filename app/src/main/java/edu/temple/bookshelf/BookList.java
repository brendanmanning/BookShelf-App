package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BookList implements Parcelable {
    private ArrayList<com.brendanmanning.bookshelf.Book> books;

    public BookList() {
        books = new ArrayList<>();
    }

    protected BookList(Parcel in) {
        books = in.createTypedArrayList(com.brendanmanning.bookshelf.Book.CREATOR);
    }

    public static final Creator<com.brendanmanning.bookshelf.BookList> CREATOR = new Creator<com.brendanmanning.bookshelf.BookList>() {
        @Override
        public com.brendanmanning.bookshelf.BookList createFromParcel(Parcel in) {
            return new com.brendanmanning.bookshelf.BookList(in);
        }

        @Override
        public com.brendanmanning.bookshelf.BookList[] newArray(int size) {
            return new com.brendanmanning.bookshelf.BookList[size];
        }
    };

    public void clear () {
        books.clear();
    }

    public void addAll (com.brendanmanning.bookshelf.BookList books) {
        for (int i = 0; i < books.size(); i++) {
            this.books.add(books.get(i));
        }
    }

    public void add(com.brendanmanning.bookshelf.Book book) {
        books.add(book);
    }

    public com.brendanmanning.bookshelf.Book get(int position) {
        return books.get(position);
    }

    public int size() {
        return books.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(books);
    }
}
