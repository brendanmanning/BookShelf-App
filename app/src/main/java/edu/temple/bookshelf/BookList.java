package edu.temple.bookshelf;

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
        Api.search(requestQueue, search, jsonObject -> {
            System.out.println("In Api.search callback");
            try {
                Book[] books = new Book[jsonObject.length()];

                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject bookJson = jsonObject.getJSONObject(String.valueOf(i));
                    books[i] = new Book(bookJson.getString("title"), bookJson.getString("author"));
                }

                BookList bookList = new BookList(books);
                callback.apply(bookList);

            } catch (JSONException e) {
                System.out.println("There was a JSONException");
            }
            return null;
        });
        callback.apply(new BookList());
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
