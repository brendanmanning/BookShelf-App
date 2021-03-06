package edu.temple.bookshelf;

import java.util.ArrayList;

public class BookList {

    private ArrayList<Book> list = new ArrayList<Book>();

    public BookList() {}
    public BookList(Book[] books) {
        for(Book b : books) {
            list.add(b);
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
