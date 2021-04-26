package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BookList implements Parcelable {
    private ArrayList<Book> books;

    public BookList() {
        books = new ArrayList<>();
    }

    protected BookList(Parcel in) {
        books = in.createTypedArrayList(Book.CREATOR);
    }

    public static final Creator<BookList> CREATOR = new Creator<BookList>() {
        @Override
        public BookList createFromParcel(Parcel in) {
            return new BookList(in);
        }

        @Override
        public BookList[] newArray(int size) {
            return new BookList[size];
        }
    };

    public static final BookList fromJson(File file) {
        try {
            String fileContents = new String(Files.readAllBytes(file.toPath()));
            JSONArray jsonArray = new JSONArray(fileContents);
            return BookList.fromJson(jsonArray);
        } catch (IOException ioe) {
            System.out.println("Error reading the JSON file (IOException)");
        } catch (JSONException jse) {
            System.out.println("Error parsing the JSON file (JSONException)");
        }
        return null;
    }

    private static final BookList fromJson(JSONArray arr) throws JSONException {
        BookList list = new BookList();
        for(int i = 0; i < arr.length(); i++) {
            list.add(Book.fromJson(arr.getJSONObject(i)));
        }
        return list;
    }

    public final boolean saveAsJson(File file) {

        boolean allOk = true;

        // Make the JSON array
        JSONArray arr = new JSONArray();
        for(Book book : books) {
            try {
                arr.put(book.toJson());
            } catch (JSONException jsonException) {
                System.out.println("One of the books (" + book.getTitle() + ") could not be converted to JSON");
                allOk = false;
            }
        }

        System.out.println("Saving BookList to " + file.getAbsolutePath());

        // Save to/Overwrite a file on local storage
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(arr.toString(4));
            fw.close();
        } catch (IOException ioe) {
            System.out.println("IOException thrown");
            allOk = false;
        } catch (JSONException jse) {
            System.out.println("JSONException thrown");
            allOk = false;
        }

        return allOk;
    }

    public void clear () {
        books.clear();
    }

    public void addAll (BookList books) {
        for (int i = 0; i < books.size(); i++) {
            this.books.add(books.get(i));
        }
    }

    public void add(Book book) {
        books.add(book);
    }

    public Book get(int position) {
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
