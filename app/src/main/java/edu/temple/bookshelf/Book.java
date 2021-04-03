package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    // ------ Getters and Setters ------ //
    private int id;
    private String title;
    private String author;
    private String coverURL;

    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getCoverURL() { return coverURL; }

    public Book(int id, String title, String author, String coverURL) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverURL = coverURL;
    }

    // ------ Parcelable Implementation ------ //

    protected Book(Parcel in) {
        title = in.readString();
        author = in.readString();
        coverURL = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(coverURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

}
