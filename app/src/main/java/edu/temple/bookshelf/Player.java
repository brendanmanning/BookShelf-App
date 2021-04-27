package edu.temple.bookshelf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import edu.temple.audiobookplayer.AudiobookService;

public class Player {

    private static File filesDirectory;
    private static SharedPreferences sharedPreferences;

    private static AudiobookService.MediaControlBinder mediaControl;

    public static final String PLAYER_UPDATE_BUNDLE_BOOK_KEY = "playerUpdateBook";
    public static final String PLAYER_UPDATE_BUNDLE_PROGRESS_KEY = "playerProgressBook";

    private static final String PLAYER_CURRENT_BOOK = "PLAYER_CURRENT_BOOK";
    private static final String PLAYER_BOOK_PROGRESS = "PLAYER_BOOK_PROGRESS/id=";

    private static Book playingBook;
    private static int playingSeconds;

    private static Function<Bundle, Void> progressCallback;

    private static Handler progressHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {

            // Read valid messages from the AudioBookService
            if (message.obj != null && playingBook != null) {

                // Update the UI state
                playingSeconds = ((AudiobookService.BookProgress) message.obj).getProgress();

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

    /* ************************************************ *
     * General methods to control the AudiobookService  *
     * ************************************************ */

    /**
     * play - play a book by id
     * @param book A Book object
     */
    public static void play(Book book) {

        try {
            System.out.println("book=" + book.getTitle());
        } catch (Exception e) {}
        try {
            System.out.println("playingBook=" + playingBook.getTitle());
        } catch (Exception e) {}

        // Is the book we're selecting currently playing?
        boolean currentlyPlayingSelectedBook = playingBook != null && playingBook.getId() == book.getId();

        // If so, just pause the book
        if(currentlyPlayingSelectedBook) {
            System.out.println("Currently playing the selected book, pausing...");
            pause();
            return;
        }

        // Regardless of whether or not a book is currently playing,
        // use the Player.pause() method to backup current track progress
        // If nothing is playing, pause() will handle this gracefully
        pause();

        // Now, set the new book as the one that's currently playing
        playingBook = book;

        // Play the book locally if we can
        if(doesLocalCopyExist(playingBook)) {

            System.out.println("Playing " + book.getTitle() + " from a local copy...");

            String location = getBookLocation(playingBook.getId());
            int startPosition = startPosition(playingBook);

            System.out.println("\tLocal copy: " + location);
            System.out.println("\tStart Position: " + startPosition);
            // If we're playing from a local file, we might have the user's previous
            // If not, the startPosition method will default to 0, so no need to add
            // any extra code to handle that here
            System.out.println("Trying to play book with mediaControl...");
            mediaControl.play(
                new File(location),
                startPosition(playingBook)
            );
            System.out.println("</book play code>");

        }

        // If we don't currently have a local copy
        else {

            System.out.println("Playing " + book.getTitle() + " from the streaming service...");

            // Play with the book this time using the streaming service
            mediaControl.play(playingBook.getId());

            // Download a copy in a background thread so that next time
            // we can play from the disk
            download(playingBook.getId());
        }

        Player.log();
    }

    /**
     * pause - pause the currently running book if there is one
     */
    public static void pause() {

        // Save the current position, if there's a currently playing book
        if(playingBook != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PLAYER_CURRENT_BOOK, playingBook.getId());
            editor.putInt(PLAYER_BOOK_PROGRESS + playingBook.getId(), playingSeconds);
            editor.commit();
        }

        // This may not work...
        // If it doesn't work, handle it gracefully
        try {
            mediaControl.pause();
        } catch (Exception e) {
            System.out.println("MediaControl error thrown trying to pause Player");
        }

        Player.log();

    }

    /**
     * stop - stop the currently running book if there is one
     */
    public static void stop() {

        // Clear the saved current position
        if(playingBook != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(PLAYER_BOOK_PROGRESS + playingBook.getId());
            editor.putInt(PLAYER_CURRENT_BOOK, -1);
            editor.commit();
        }

        // There is no longer a playingBook
        playingBook = null;

        // Actually stop the book
        mediaControl.stop();

        Bundle bundle = new Bundle();
        bundle.putInt(Player.PLAYER_UPDATE_BUNDLE_PROGRESS_KEY, 0);
        bundle.putParcelable(Player.PLAYER_UPDATE_BUNDLE_BOOK_KEY, null);
        progressCallback.apply(bundle);

        Player.log();

    }

    /**
     * seekTo - seeks the progressbar to a specific location
     */
    public static void seekTo(int st) {
        if(playingBook != null) {
            System.out.println("Seeking to " + st);
            mediaControl.seekTo((int) ((st / 100f) * playingBook.getDuration()));
        }
    }

    /* ************************************************ *
     * Manage the application state between relaunches  *
     * ************************************************ */

    /**
     * startPosition - If a book is restarted, played from where the user stopped last time
     * @param book - Book book
     * @return Seconds into the book, if previously started, or 0 if the book has never been started
     */
    private static int startPosition(Book book) {
        int savedLocation = sharedPreferences.getInt(PLAYER_BOOK_PROGRESS + book.getId(), 0);

        // Backtrack 10 seconds
        if(savedLocation >= 10) {
            System.out.print("Backtracking 10 seconds from sec=" + savedLocation);
            savedLocation -= 10;
            System.out.println(" to sec=" + savedLocation);

            // Handles the case of savedLocation in (0,10)
        } else
            savedLocation = 0;
        return savedLocation;
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

                ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

                FileOutputStream fileOutputStream = new FileOutputStream(getBookLocation(id));
                FileChannel fileChannel = fileOutputStream.getChannel();

                fileOutputStream.getChannel()
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                // Log to the console
                Player.log();
            } catch (Exception e) {
                System.out.println("Threw an exception trying to download book (" + id + ")");
                e.printStackTrace();
            }
        }).start();
    }

    private static String getBookLocation(int id) {
        return getBooksLocation().getAbsolutePath() + "/" + id + ".mp3";
    }

    private static String getBookLocation(Book book) {
        return getBookLocation(book.getId());
    }

    private static File getBooksLocation() {
        File booksFolder = new File(filesDirectory.getAbsolutePath() + "/books/");
        if(!booksFolder.exists()) {
            System.out.println(booksFolder.getAbsolutePath() + " does not exist ... creating!");
            System.out.println("Created? = " + booksFolder.mkdirs());
        }
        return booksFolder;
    }

    public static void log() {

        return;

       // **************************************** //
       // Dheera,
       // I used this code to view the state of the internal storage
       // and sharedPreferences object before I knew about the
       // Device File Explorer.
       //
       // If you find it helpful, you can uncomment the code below
       // I'm leaving it in-place in case I need to do any last minute
       // debugging before the deadline
        // **************************************** //

//        System.out.println("******** SharedPreferences Keys ********");
//
//        Map<String, ?> prefs = sharedPreferences.getAll();
//        Iterator keyIterator = prefs.keySet().iterator();
//        while(keyIterator.hasNext()) {
//            String key = keyIterator.next().toString();
//
//            if(key.startsWith("PLAYER_BOOK_PROGRESS/id=")) {
//                System.out.println(key + "   ->   progress=" + sharedPreferences.getInt(key, -9999));
//            }
//
//            else if(key.equals("PLAYER_CURRENT_BOOK")) {
//                System.out.println(key + "   ->   id=" + sharedPreferences.getInt(key, -9999));
//            }
//
//        }
//
//        System.out.println("******** BooksLocation Files ********");
//
//        File booksLocation = getBooksLocation();
//        for(File book : booksLocation.listFiles()) {
//            System.out.println("[" + book.getAbsolutePath() + "]: " + book.getName());
//        }

    }

}
