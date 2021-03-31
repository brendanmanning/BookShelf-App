package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BookList {

    private ArrayList<Book> list = new ArrayList<Book>();

    public BookList() {}
    public BookList(Book[] books) {
        for(Book b : books) {
            list.add(b);
        }
    }

    public BookList(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                System.out.println("    getting jsonArray[" + i + "]");
                JSONObject bookJson = jsonArray.getJSONObject(i);
                list.add(new Book(bookJson.getString("title"), bookJson.getString("author")));
            }
        } catch (JSONException e) {
            System.out.println("There was a JSONException");
        }
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
