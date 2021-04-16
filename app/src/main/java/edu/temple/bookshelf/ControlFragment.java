package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlFragment extends Fragment {

    ControlFragmentInterface parent;

    private static final String BOOK_KEY = "book";
    private static final String PROGRESS_KEY = "progress";

    private Book book;
    private int progress;

    View view;
    TextView labelTextView;
    Button playButton, pauseButton, stopButton;
    SeekBar seekBar;

    public ControlFragment() {
        // Required empty public constructor
    }

    public static ControlFragment newInstance(Book book, int progress) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOK_KEY, book);
        args.putInt(PROGRESS_KEY, progress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parent = (ControlFragmentInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_KEY);
            progress = getArguments().getInt(PROGRESS_KEY);
        }
    }

    public void updateProgress(int progress) {
        this.progress = progress;
    }

    public void setPlayingBook(Book book) {
        this.book = book;
    }

    public void refresh() {
        labelTextView.setText(getCorrectLabelText());
        this.seekBar.setProgress(getScaledProgress());
    }

    private String getCorrectLabelText() {
        return this.book != null
                ? getString(R.string.control_fragment_something_playing_label_prefix) + book.getTitle()
                : getString(R.string.control_fragment_nothing_playing_label);
    }

    private int getScaledProgress() {
        if(book == null) return 0;
        return  (int) (100 * ((double) progress / (double) book.getDuration()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_control, container, false);

        labelTextView = view.findViewById(R.id.control_fragment_label);
        playButton = view.findViewById(R.id.control_fragment_play_button);
        pauseButton = view.findViewById(R.id.control_fragment_pause_button);
        stopButton = view.findViewById(R.id.control_fragment_stop_button);
        seekBar = view.findViewById(R.id.control_fragment_seekbar);

        labelTextView.setText(getCorrectLabelText());
        seekBar.setProgress(getScaledProgress());

        playButton.setOnClickListener(v -> parent.onPlayButtonPressed());
        pauseButton.setOnClickListener(v -> parent.onPauseButtonPressed());
        stopButton.setOnClickListener(v -> parent.onStopButtonPressed());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    parent.onSeekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // parent.onSeekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // parent.onSeekEnd();
            }
        });

        return view;
    }

    /*
      Interface for communicating with attached activity
     */
    interface ControlFragmentInterface {
        void onPlayButtonPressed();
        void onPauseButtonPressed();
        void onStopButtonPressed();
        void onSeekTo(int location);
        // void onSeekStart();
        // void onSeekEnd();
    }

}