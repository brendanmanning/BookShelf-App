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

        // Is another book currently playing
        boolean currentlyPlayingAnotherBook = playingBook != null && playingBook.getId() != book.getId();

        // If we're currently playing another book, save the current position
        // This can be achieved by pausing it, since the pause method saves
        // the current position in user preferences
        if(currentlyPlayingAnotherBook) {
            pause();
        }

        // Now, set the new book as the one that's currently playing
        playingBook = book;

        // Play the book locally if we can
        if(doesLocalCopyExist(book)) {

            System.out.println("Playing " + book.getTitle() + " from a local copy...");

            String location = getBookLocation(playingBook.getId());
            int startPosition = startPosition(playingBook);

            System.out.println("\tLocal copy: " + location);
            System.out.println("\tStart Position: " + startPosition);
            // If we're playing from a local file, we might have the user's previous
            // If not, the startPosition method will default to 0, so no need to add
            // any extra code to handle that here
            mediaControl.play(
                new File(location),
                startPosition(playingBook)
            );

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

        // Actually pause the book, if there's a book playing
        if(mediaControl.isPlaying()) {
            mediaControl.pause();
        }

        Player.log();

    }

    /**
     * stop - stop the currently running book if there is one
     */
    public static void stop() {

        // Clear the saved current position
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PLAYER_CURRENT_BOOK, -1);
        editor.putInt(PLAYER_BOOK_PROGRESS + playingBook.getId(), 0);
        editor.commit();

        // Actually stop the book
        mediaControl.stop();

        Player.log();

    }

    /**
     * seekTo - seeks the progressbar to a specific location
     */
    public static void seekTo(int seconds) {
        mediaControl.seekTo((int) ((seconds / 100f) * playingBook.getDuration()));
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
        return sharedPreferences.getInt(PLAYER_BOOK_PROGRESS + book.getId(), 0);
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
    private static void _download(int id) {
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
                output.close();
                output.flush();
                input.close();

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

        System.out.println("******** SharedPreferences Keys ********");

        Map<String, ?> prefs = sharedPreferences.getAll();
        Iterator keyIterator = prefs.keySet().iterator();
        while(keyIterator.hasNext()) {
            String key = keyIterator.next().toString();

            if(key.startsWith("PLAYER_BOOK_PROGRESS/id=")) {
                System.out.println(key + "   ->   progress=" + sharedPreferences.getInt(key, -9999));
            }

            else if(key.equals("PLAYER_CURRENT_BOOK")) {
                System.out.println(key + "   ->   id=" + sharedPreferences.getInt(key, -9999));
            }

        }

        System.out.println("******** BooksLocation Files ********");

        File booksLocation = getBooksLocation();
        for(File book : booksLocation.listFiles()) {
            System.out.println("[" + book.getAbsolutePath() + "]: " + book.getName());
        }
    }

}
