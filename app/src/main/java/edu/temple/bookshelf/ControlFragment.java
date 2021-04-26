package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class ControlFragment extends Fragment {

    private ControlInterface parentActivity;

    private TextView nowPlayingTextView;
    private SeekBar seekBar;

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ControlInterface)
            parentActivity = (ControlInterface) context;
        else
            throw new RuntimeException("Please implement ControlFragment.ControlInterface");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View l = inflater.inflate(R.layout.fragment_control, container, false);

        nowPlayingTextView = l.findViewById(R.id.control_fragment_label);
        seekBar = l.findViewById(R.id.control_fragment_seekbar);

        l.findViewById(R.id.control_fragment_play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.play();
            }
        });
        l.findViewById(R.id.control_fragment_pause_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.pause();
            }
        });
        l.findViewById(R.id.control_fragment_stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.stop();
            }
        });

        // If the user is dragging the seekbar, update the book position
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b)
                    parentActivity.changePosition(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        return l;
    }

    public void setNowPlaying(String title) {
        nowPlayingTextView.setText(title);
    }

    public void updateProgress(Book book, int progress) {
        int percent = (int) ((double) progress * 100 / (double) book.getDuration());
        System.out.println("Updating progress to seconds=" + progress);
        System.out.println("Updating progress to percent=" + percent);
        seekBar.setProgress(percent);
    }

    interface ControlInterface {
        void play();
        void pause();
        void stop();
        void changePosition (int progress);
    }
}