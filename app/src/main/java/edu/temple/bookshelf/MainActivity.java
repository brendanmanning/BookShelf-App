package edu.temple.bookshelf;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity
        extends AppCompatActivity
        implements BookListFragment.BookSelectedInterface, ControlFragment.ControlFragmentInterface {

    boolean is_paused = false;
    int current_track_position = 0;
    int current_track_duration = 0;

    FragmentManager fm;

    ControlFragment controlFragment;
    BookDetailsFragment bookDetailsFragment;

    AudiobookService.MediaControlBinder binder;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("ServiceConnection.onServiceConnected()");
            binder = (AudiobookService.MediaControlBinder) service;
            binder.setProgressHandler(playerProgressHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("serviceConnection.onServiceDisconnected()");
        }
    };

    Handler playerProgressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            AudiobookService.BookProgress progress = ((AudiobookService.BookProgress) msg.obj);

            if(progress == null) return false;
            if(controlFragment == null) return false;

            current_track_position = progress.getProgress();
            controlFragment.updateProgress(
                calculateSeekBarDisplayProgress(
                    progress.getProgress(), selectedBook
                )
            );

            return true;

        }
    });

    boolean twoPane;
    Book selectedBook;

    private final String TAG_BOOKLIST = "booklist", TAG_BOOKDETAILS = "bookdetails";
    private final String KEY_SELECTED_BOOK = "selectedBook";
    private final String KEY_BOOKLIST = "searchedook";
    private final int BOOK_SEARCH_REQUEST_CODE = 123;

    BookList bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.searchDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, BookSearchActivity.class), BOOK_SEARCH_REQUEST_CODE);
            }
        });

        if (savedInstanceState != null) {
            // Fetch selected book if there was one
            selectedBook = savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

            // Fetch previously searched books if one was previously retrieved
            bookList = savedInstanceState.getParcelable(KEY_BOOKLIST);
        }else {
            // Create empty booklist if
            bookList = new BookList();
        }

        twoPane = findViewById(R.id.container2) != null;

        fm = getSupportFragmentManager();

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container_1);


        // At this point, I only want to have BookListFragment be displayed in container_1
        if (fragment1 instanceof BookDetailsFragment) {
            fm.popBackStack();
        } else if (!(fragment1 instanceof BookListFragment))
            fm.beginTransaction()
                    .add(R.id.container_1, BookListFragment.newInstance(bookList), TAG_BOOKLIST)
            .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books
         */
        bookDetailsFragment = (selectedBook == null) ? new BookDetailsFragment() : BookDetailsFragment.newInstance(selectedBook);
        if (twoPane) {
            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment, TAG_BOOKDETAILS)
                    .commit();
        } else if (selectedBook != null) {
            /*
            If a book was selected, and we now have a single container, replace
            BookListFragment with BookDetailsFragment, making the transaction reversible
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, bookDetailsFragment, TAG_BOOKDETAILS)
                    .addToBackStack(null)
                    .commit();
        }

        // ************************************************* //
        // Begin code for the audiobook controller component
        // ************************************************* //
        if(! (fm.findFragmentById(R.id.controller_container) instanceof ControlFragment)) {
            controlFragment = new ControlFragment();
            fm.beginTransaction()
                    .add(R.id.controller_container, controlFragment, "TAG_CONTROLLER")
                    .commit();
        }

        System.out.println("Binding service...");
        bindService(
            new Intent(MainActivity.this, AudiobookService.class),
            serviceConnection, BIND_AUTO_CREATE
        );

    }

    // ************************************************* //
    // Begin code for the audiobook controller component
    // ************************************************* //

    // Interface methods

    @Override
    public void onPlayButtonPressed() {

        // Don't play anything if there's no selected book
        if(selectedBook == null) return;
        if(controlFragment == null) return;

        current_track_duration = selectedBook.getDuration();
        controlFragment.playBook(selectedBook);

        // If we're in the middle of the book, play basically means un-pause
        if(current_track_position > 0) {
            binder.pause();
        }

        // If we haven't started the book yet, start it now
        else {
            binder.play(selectedBook.getId());
        }
    }

    @Override
    public void onPauseButtonPressed() {
        binder.pause();
    }

    @Override
    public void onStopButtonPressed() {
        binder.stop();

        current_track_position = 0;
        controlFragment.updateProgress(0);
        controlFragment.playBook(null);

        current_track_duration = 0;
    }

    @Override
    public void onSeekTo(int location) {
        System.out.println("Trying to seek to " + location);
        binder.seekTo((int) (((double) location/100) * selectedBook.getDuration()));
//        controlFragment.updateProgress(
//            calculateSeekBarDisplayProgress(
//                location,
//                selectedBook
//            )
//        );
//        current_track_position = location;
    }

    // Utility methods
    private int calculateSeekBarDisplayProgress(int seconds, Book book) {
        double decimal_progress = (double) seconds / (double) book.getDuration();
        int rounded_progress = (int) (decimal_progress * 100);
        return rounded_progress;
    }

    // ************************************************* //
    // End code for the audiobook controller component
    // ************************************************* //


    @Override
    public void bookSelected(int index) {
        // Store the selected book to use later if activity restarts
        selectedBook = bookList.get(index);

        if (twoPane)
            /*
            Display selected book using previously attached fragment
             */
            bookDetailsFragment.displayBook(selectedBook);
        else {
            /*
            Display book using new fragment
             */
            fm.beginTransaction()
                    .replace(R.id.container_1, BookDetailsFragment.newInstance(selectedBook), TAG_BOOKDETAILS)
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Display new books when retrieved from a search
     */
    private void showNewBooks() {
        if ((fm.findFragmentByTag(TAG_BOOKDETAILS) instanceof BookDetailsFragment)) {
            fm.popBackStack();
        }
        ((BookListFragment) fm.findFragmentByTag(TAG_BOOKLIST)).showNewBooks();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_BOOK, selectedBook);
        outState.putParcelable(KEY_BOOKLIST, bookList);
    }

    @Override
    public void onBackPressed() {
        // If the user hits the back button, clear the selected book
        selectedBook = null;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            bookList.clear();
            bookList.addAll((BookList) data.getParcelableExtra(BookSearchActivity.BOOKLIST_KEY));
            if (bookList.size() == 0) {
                Toast.makeText(this, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
            }
            showNewBooks();
        }
    }

}
