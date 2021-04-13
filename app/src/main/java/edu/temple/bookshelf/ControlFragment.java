package edu.temple.bookshelf;

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

    private static final String BOOK_KEY = "book";
    private Book book;

    TextView labelTextView;
    Button playButton, pauseButton, stopButton;
    SeekBar seekBar;

    public ControlFragment() {
        // Required empty public constructor
    }

//    public static ControlFragment newInstance(Book book) {
//        ControlFragment fragment = new ControlFragment();
//        Bundle args = new Bundle();
//        args.putParcelable(BOOK_KEY, book);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_KEY);
        }
    }

    public void playBook(Book book) {
        labelTextView.setText("Now playing: " + book.getTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_control, container, false);

        labelTextView = v.findViewById(R.id.control_fragment_label);
        playButton = v.findViewById(R.id.control_fragment_play_button);
        pauseButton = v.findViewById(R.id.control_fragment_pause_button);
        stopButton = v.findViewById(R.id.control_fragment_stop_button);
        seekBar = v.findViewById(R.id.control_fragment_seekbar);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return v;
    }

    /*
      Interface for communicating with attached activity
     */
    interface ControlFragmentInterface {
        void onPlayButtonPressed();
        void onPauseButtonPressed();
        void onStopButtonPressed();
        void onSeekTo(double location);
    }

}