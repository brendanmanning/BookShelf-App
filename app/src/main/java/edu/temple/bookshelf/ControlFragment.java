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
    public void onAttach(Context context) {
        super.onAttach(context);
        parent = (ControlFragmentInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_KEY);
        }
    }

    public void updateProgress(int progress) {
        if(this.seekBar == null) {
            System.out.println("SEEK BAR IS NULL");
        } else {
            this.seekBar.setProgress(progress);
        }
    }

    public void playBook(Book book) {
        labelTextView.setText("Now playing: " + book.getTitle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        labelTextView = view.findViewById(R.id.control_fragment_label);
        playButton = view.findViewById(R.id.control_fragment_play_button);
        pauseButton = view.findViewById(R.id.control_fragment_pause_button);
        stopButton = view.findViewById(R.id.control_fragment_stop_button);
        seekBar = view.findViewById(R.id.control_fragment_seekbar);

        playButton.setOnClickListener(v -> parent.onPlayButtonPressed());
        pauseButton.setOnClickListener(v -> parent.onPauseButtonPressed());
        stopButton.setOnClickListener(v -> parent.onStopButtonPressed());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                parent.onSeekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
    }

}