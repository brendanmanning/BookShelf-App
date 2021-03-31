package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.RequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.function.Function;

public class BookList {

    private ArrayList<Book> list = new ArrayList<Book>();

    public BookList() {}
    public BookList(Book[] books) {
        for(Book b : books) {
            list.add(b);
        }
    }

    public static void fromSearch(RequestQueue requestQueue, String search, Function<BookList, Void> callback) {
        System.out.println("In BookList.fromSearch");
        Api.search(requestQueue, search, jsonArray -> {
            System.out.println("In Api.search callback");

            if(jsonArray == null) {
                System.out.println("jsonArray is null");
                return null;
            } else {
                System.out.println("jsonArray is printed below");
                System.out.println(jsonArray.toString());
            }

            try {
                Book[] books = new Book[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject bookJson = jsonArray.getJSONObject(i);
                    books[i] = new Book(bookJson.getString("title"), bookJson.getString("author"));
                }

                BookList bookList = new BookList(books);
                callback.apply(bookList);

            } catch (JSONException e) {
                System.out.println("There was a JSONException");
            }
            return null;
        });
    }

    public ArrayList<Book> toArrayList() {
        return list;
    }

    public boolean add(Book book) {
        return list.add(book);
    }

    public boolean remove(Book book) {
        return list.remove(book);
    }

    public Book get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

}
