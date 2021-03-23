package edu.temple.bookshelf;

public class Books {
    public static BookList getSamples() {
        return new BookList(
            new Book[]{
                new Book("A Tale of Two Cities", "Charles Dickens"),
                new Book("The Lord of the Rings", "J.R.R. Tolkien"),
                new Book("The Little Prince", "Antoine de Saint-Exupery"),
                new Book("Harry Potter", "J.K. Rowling"),
                new Book("The Hobbit", "J.R.R. Tolkien"),
                new Book("Alice in Wonderland", "Lewis Carroll"),
                new Book("Dream of the Red Chamber", "Cao Xueqin"),
                new Book("And Then There Were None", "Agatha Christie"),
                new Book("The Lion, the Witch and the Wardrobe", "C.S. Lewis"),
                new Book("She: A History of Adventure", "H. Rider Haggard")
            }
        );
    }
}
