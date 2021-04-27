package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.function.Function;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface, ControlFragment.ControlInterface {

    private FragmentManager fm;

    private boolean twoPane;
    private BookDetailsFragment bookDetailsFragment;
    private ControlFragment controlFragment;
    private Book selectedBook, playingBook;

    private final String TAG_BOOKLIST = "booklist", TAG_BOOKDETAILS = "bookdetails";
    private final String KEY_SELECTED_BOOK = "selectedBook", KEY_PLAYING_BOOK = "playingBook";
    private final String KEY_BOOKLIST = "searchedook";
    private final int BOOK_SEARCH_REQUEST_CODE = 123;

    private boolean serviceConnected;

    private File filesDir;
    private SharedPreferences sharedPreferences;

    Intent serviceIntent;

    BookList bookList;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Player.connectService(filesDir, sharedPreferences, iBinder, bundle -> {

                int p = bundle.getInt(Player.PLAYER_UPDATE_BUNDLE_PROGRESS_KEY);
                Book b = (Book) bundle.getParcelable(Player.PLAYER_UPDATE_BUNDLE_BOOK_KEY);

                if(b == null) {
                    controlFragment.updateProgress(b, 0);
                    controlFragment.setNowPlaying(getString(R.string.control_fragment_nothing_playing_label));
                } else {
                    controlFragment.updateProgress(b, p);
                    controlFragment.setNowPlaying(getString(R.string.control_fragment_now_playing, b.getTitle()));
                }

                return null;
            });

            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesDir = this.getFilesDir();
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        serviceIntent = new Intent (this, AudiobookService.class);

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        fm = getSupportFragmentManager();

        findViewById(R.id.searchDialogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, BookSearchActivity.class), BOOK_SEARCH_REQUEST_CODE);
            }
        });

        if (savedInstanceState != null) {

            System.out.println("savedInstanceState != null");

            // Fetch selected book if there was one
            selectedBook = savedInstanceState.getParcelable(KEY_SELECTED_BOOK);

            // Fetch playing book if there was one
            playingBook = savedInstanceState.getParcelable(KEY_PLAYING_BOOK);

            // Fetch previously searched books if one was previously retrieved
            bookList = savedInstanceState.getParcelable(KEY_BOOKLIST);
        } else {

            System.out.println("savedInstanceState == null");

            // If we're coming back after the application was completely
            // closed (i.e. there is no savedInstanceState), then we can
            // check local storage for a persisted user search (BookList)
            if(this.getBookListJsonLocation().exists()) {
                System.out.println("BookList JSON exists");
                bookList = BookList.fromJson(this.getBookListJsonLocation());
            }

            // Create empty booklist if there is nothing in the instance state
            // or in the local file system.
            // We also create an empty BookList here if,  for some reason, the
            // BookList.fromJson call above returned null
            if(!this.getBookListJsonLocation().exists() || bookList == null) {
                System.out.println("BookList JSON does not exist or is otherwise null");
                bookList = new BookList();
            }
        }

        twoPane = findViewById(R.id.container2) != null;

        Fragment fragment1;
        fragment1 = fm.findFragmentById(R.id.container_1);

        // I will only ever have a single ControlFragment - if I created one before, reuse it
        if ((controlFragment = (ControlFragment) fm.findFragmentById(R.id.controller_container)) == null) {
            controlFragment = new ControlFragment();
            fm.beginTransaction()
                    .add(R.id.controller_container, controlFragment)
                    .commit();
        }


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

    }

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
        outState.putParcelable(KEY_PLAYING_BOOK, playingBook);
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

            // Update the local variable for the booklist w/o changing the pointer in memory
            bookList.clear();
            bookList.addAll((BookList) data.getParcelableExtra(BookSearchActivity.BOOKLIST_KEY));

            // Save BookList as a JSON file s.t. we can return to it later
            bookList.saveAsJson(this.getBookListJsonLocation());

            // Show a message if we didn't get anything, just for user experience
            if (bookList.size() == 0) {
                Toast.makeText(this, getString(R.string.error_no_results), Toast.LENGTH_SHORT).show();
            }
            showNewBooks();
        }
    }

    @Override
    public void play() {
        if (selectedBook != null) {
            playingBook = selectedBook;
            controlFragment.setNowPlaying(getString(R.string.control_fragment_now_playing, playingBook.getTitle()));
            if (serviceConnected) {
                Player.play(playingBook);
            }

            // Make sure that the service doesn't stop
            // if the activity is destroyed while the book is playing
            startService(serviceIntent);
        } else {
            Toast.makeText(this, "Can't play when no book selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void pause() {
        if (serviceConnected) {
            Player.pause();
        }
    }

    @Override
    public void stop() {
        if (serviceConnected)
            Player.stop();

        // mediaControl.stop();

        // If no book is playing, then it's fine to let
        // the service stop once the activity is destroyed
        stopService(serviceIntent);
    }

    @Override
    public void changePosition(int progress) {
        if (serviceConnected)
            Player.seekTo(progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private File getBookListJsonLocation() {
        return new File(this.getFilesDir().getAbsolutePath() + "/booklist.json");
    }

}
