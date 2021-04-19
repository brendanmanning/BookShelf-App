package edu.temple.bookshelf;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Player {

    /* ************************************************ *
     * General methods to control the AudiobookService  *
     * ************************************************ */

    /**
     * play - play a book by id
     * @param id Book id
     */
    public void play(int id) {
        if(doesLocalCopyExist(id)) {

        } else {
            // Play with the service

            // Download a copy in a background thread
            download(id);
        }
    }

    /**
     * pause - pause the currently running book if there is one
     */
    public void pause() {

    }

    /**
     * stop - stop the currently running book if there is one
     */
    public void stop() {

    }

    /* ************************************************ *
     * Manage the application state between relaunches  *
     * ************************************************ */

    /**
     * startPosition - If a book is restarted, played from where the user stopped last time
     * @param id - Book id
     * @return Seconds into the book, if previously started, or 0 if the book has never been started
     */
    private int startPosition(int id) {
        return 0;
    }

    /**
     * currentlyPlayingBook - If a book was playing the last time the user left, this is the ID of that book
     * @return A book id or -1 if no book had been playing
     */
    private int currentlyPlayingBook() {
        return -1;
    }

    /* ************************************************ *
     *              Manage file downloads               *
     * ************************************************ */

    private boolean doesLocalCopyExist(int id) {
        return new File(getBookLocation(id)).exists();
    }

    /**
     * download - Download a book in the background
     * @param id
     * @return
     */
    private void download(int id) {
        new Thread(() -> {
            try {

                // Define a URL and get a connection to it
                URL url = new URL("");
                URLConnection connection = url.openConnection();
                connection.connect();

                // Get an input and output stream to download the file
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(getBookLocation(id));

                // Close the input/output connections
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {

            }
        }).start();
    }

    private String getBookLocation(int id) {
        return Environment.getDataDirectory() + "/books/" + id + ".mp3";
    }

}
