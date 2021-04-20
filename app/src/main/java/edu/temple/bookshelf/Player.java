package edu.temple.bookshelf;

import android.content.ComponentName;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import edu.temple.audiobookplayer.AudiobookService;

public class Player {

    private static AudiobookService.MediaControlBinder mediaControl;
    private static File filesDirectory;

    public static void connectService(IBinder iBinder, Handler progressHandler, File filesDir) {
        mediaControl = (AudiobookService.MediaControlBinder) iBinder;
        mediaControl.setProgressHandler(progressHandler);
        filesDirectory = filesDir;
    }

    /* ************************************************ *
     * General methods to control the AudiobookService  *
     * ************************************************ */

    /**
     * play - play a book by id
     * @param id Book id
     */
    public static void play(int id) {
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
    public static void pause() {

    }

    /**
     * stop - stop the currently running book if there is one
     */
    public static void stop() {

    }

    /**
     * seekTo - seeks the progressbar to a specific location
     */
    public static void seekTo(int progressLocation) {

    }

    /* ************************************************ *
     * Manage the application state between relaunches  *
     * ************************************************ */

    /**
     * startPosition - If a book is restarted, played from where the user stopped last time
     * @param id - Book id
     * @return Seconds into the book, if previously started, or 0 if the book has never been started
     */
    private static int startPosition(int id) {
        return 0;
    }

    /**
     * currentlyPlayingBook - If a book was playing the last time the user left, this is the ID of that book
     * @return A book id or -1 if no book had been playing
     */
    private static int currentlyPlayingBook() {
        return -1;
    }

    /* ************************************************ *
     *              Manage file downloads               *
     * ************************************************ */

    private static boolean doesLocalCopyExist(int id) {
        return new File(getBookLocation(id)).exists();
    }

    /**
     * download - Download a book in the background
     * @param id
     * @return
     */
    private static void download(int id) {
        new Thread(() -> {
            try {

                // Define a URL and get a connection to it
                URL url = new URL("https://kamorris.com/lab/audlib/download.php?id=" + id);
                URLConnection connection = url.openConnection();
                connection.connect();

                System.out.println("Environment.getDataDirectory() - " + Environment.getDataDirectory());
                System.out.println("Environment.getRootDirectory() - " + Environment.getRootDirectory());


                // Get an input and output stream to download the file
                String location = getBookLocation(id);
                File file = new File(location);
                //if(!file.exists()) file.createNewFile();
                System.out.println("Book location: " + location);
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(file);

                // Close the input/output connections
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                System.out.println("Threw an exception trying to download book (" + id + ")");
                e.printStackTrace();
            }
        }).start();
    }

    private static String getBookLocation(int id) {
        return getBooksLocation().getAbsolutePath() + id + ".mp3";
    }

    private static File getBooksLocation() {
        File booksFolder = new File(filesDirectory.getAbsolutePath() + "/books/");
        if(!booksFolder.exists()) {
            System.out.println(booksFolder.getAbsolutePath() + " does not exist ... creating!");
            System.out.println("Created? = " + booksFolder.mkdirs());
        }
        return booksFolder;
    }

}
