package edu.temple.bookshelf;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import edu.temple.audiobookplayer.AudiobookService;

public class Player {

    private static File filesDirectory;
    private static SharedPreferences sharedPreferences;

    private static AudiobookService.MediaControlBinder mediaControl;

    public static final String PLAYER_UPDATE_BUNDLE_BOOK_KEY = "playerUpdateBook";
    public static final String PLAYER_UPDATE_BUNDLE_PROGRESS_KEY = "playerProgressBook";
    private static final String PLAYER_CURRENT_SONG_PROGRESS = "PLAYER_CURRENT_SONG_PROGRESS";

    private static Book playingBook;
    private static int playingSeconds;

    private static Function<Bundle, Void> progressCallback;

    private static Handler progressHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            // Read valid messages from the AudioBookService
            if (message.obj != null && playingBook != null) {

                // Update the UI state
                playingSeconds = (int) (((float) ((AudiobookService.BookProgress) message.obj).getProgress() / playingBook.getDuration()) * 100);

                if(progressCallback != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(PLAYER_UPDATE_BUNDLE_BOOK_KEY, playingBook);
                    bundle.putInt(PLAYER_UPDATE_BUNDLE_PROGRESS_KEY, playingSeconds);

                    progressCallback.apply(bundle);
                }

            }

            return true;
        }
    });

    public static void connectService(File filesDir, SharedPreferences sharedPrefs, IBinder iBinder, Function<Bundle, Void> callback) { //Handler progressHandler) {
        mediaControl = (AudiobookService.MediaControlBinder) iBinder;
        mediaControl.setProgressHandler(progressHandler);

        filesDirectory = filesDir;
        sharedPreferences = sharedPrefs;
        progressCallback = callback;
    }

    public static void notifyState(Book currentlyPlayingBook, int seconds) {
        playingBook = currentlyPlayingBook;
        playingSeconds = seconds;
    }

    /* ************************************************ *
     * General methods to control the AudiobookService  *
     * ************************************************ */

    /**
     * play - play a book by id
     * @param book A Book object
     */
    public static void play(Book book) {
        playingBook = book;
        if(doesLocalCopyExist(book)) {
            mediaControl.play(playingBook.getId());
        } else {
            // Play with the service


            // Download a copy in a background thread
            download(playingBook.getId());
        }
    }

    /**
     * pause - pause the currently running book if there is one
     */
    public static void pause() {

        // Save the current position
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PLAYER_CURRENT_SONG_PROGRESS, playingSeconds);
        editor.commit();


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

    private static boolean doesLocalCopyExist(Book book) {
        return new File(getBookLocation(book)).exists();
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

    private static String getBookLocation(Book book) {
        return getBooksLocation().getAbsolutePath() + book.getId() + ".mp3";
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
