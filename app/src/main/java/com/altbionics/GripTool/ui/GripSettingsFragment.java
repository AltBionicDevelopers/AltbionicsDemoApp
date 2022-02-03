package com.altbionics.GripTool.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.altbionics.GripTool.MainActivity;
import com.altbionics.GripTool.R;
import com.altbionics.GripTool.util.Grip;

import java.util.ArrayList;


public class GripSettingsFragment extends Fragment {
    private static final String TAG = "GripSettingsFragment";
    private Spinner deviceSpinner = null;
    public static ArrayAdapter<String> spinnerAdapter;
    private SeekBar thumb, index, middle, ring, pinky;
    private TextView thumbTooltip, indexTooltip, middleTooltip, ringTooltip, pinkyTooltip;


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.grip_settings_fragment,
                container, false);

        ArrayList<String> names = new ArrayList<>();

        for(Grip item : MainActivity.gripList){
            names.add(item.getName());
        }

        deviceSpinner = view.findViewById(R.id.device_spinner);
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_checked_text, names);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_view);
        deviceSpinner.setAdapter(spinnerAdapter);
        deviceSpinner.setSelection(GripsFragment.position);

        deviceSpinner.setOnTouchListener((v, event) -> {
            AppCompatImageButton spinnerBackground = view.findViewById(R.id.spinnerBackground);
            if(deviceSpinner.isPressed()) {
                spinnerBackground.setPressed(true);
            }
            return false;
        });


        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                thumb.setProgress(MainActivity.gripList.get(position).getThumb());
                index.setProgress(MainActivity.gripList.get(position).getIndex());
                middle.setProgress(MainActivity.gripList.get(position).getMiddle());
                ring.setProgress(MainActivity.gripList.get(position).getRing());
                pinky.setProgress(MainActivity.gripList.get(position).getPinky());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        thumb = view.findViewById(R.id.thumb_slider);
        index = view.findViewById(R.id.index_slider);
        middle = view.findViewById(R.id.middle_slider);
        ring = view.findViewById(R.id.ring_slider);
        pinky = view.findViewById(R.id.pinky_slider);

        thumb.setOnTouchListener((v, event) -> {
            thumbTooltip.setText(Integer.toString(thumb.getProgress()));
            float y = (thumb.getHeight() - thumb.getMax() - ((float)thumbTooltip.getWidth()) * (float)1.25)
                    - thumb.getThumb().getBounds().right;
            thumbTooltip.setY(y);
            thumbTooltip.setVisibility(View.VISIBLE);
            return false;
        });
        thumb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float y = (thumb.getHeight() - thumb.getMax() - ((float)thumbTooltip.getWidth()) * (float)1.25)
                        - thumb.getThumb().getBounds().right;
                thumbTooltip.setY(y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                thumbTooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                thumbTooltip.setVisibility(View.INVISIBLE);
                MainActivity.gripList.get(deviceSpinner.getSelectedItemPosition()).setThumb(thumb.getProgress());
                GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        index.setOnTouchListener((v, event) -> {
            indexTooltip.setText(Integer.toString(index.getProgress()));
            float y = (index.getHeight() - index.getMax() - ((float)indexTooltip.getWidth()) * (float)1.25)
                    - index.getThumb().getBounds().right;
            indexTooltip.setY(y);
            indexTooltip.setVisibility(View.VISIBLE);
            return false;
        });
        index.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                indexTooltip.setText(Integer.toString(progress));
                float y = (index.getHeight() - index.getMax() - ((float)indexTooltip.getWidth()) * (float)1.25)
                        - index.getThumb().getBounds().right;
                indexTooltip.setY(y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                indexTooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                indexTooltip.setVisibility(View.INVISIBLE);
                MainActivity.gripList.get(deviceSpinner.getSelectedItemPosition()).setIndex(index.getProgress());
                GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        middle.setOnTouchListener((v, event) -> {
            middleTooltip.setText(Integer.toString(middle.getProgress()));
            float y = (middle.getHeight() - middle.getMax() - ((float)middleTooltip.getWidth()) * (float)1.25)
                    - middle.getThumb().getBounds().right;
            middleTooltip.setY(y);
            middleTooltip.setVisibility(View.VISIBLE);
            return false;
        });
        middle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                middleTooltip.setText(Integer.toString(progress));
                float y = (middle.getHeight() - middle.getMax() - ((float)middleTooltip.getWidth()) * (float)1.25)
                        - middle.getThumb().getBounds().right;
                middleTooltip.setY(y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                middleTooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                middleTooltip.setVisibility(View.INVISIBLE);
                MainActivity.gripList.get(deviceSpinner.getSelectedItemPosition()).setMiddle(middle.getProgress());
                GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        ring.setOnTouchListener((v, event) -> {
            ringTooltip.setText(Integer.toString(ring.getProgress()));
            float y = (ring.getHeight() - ring.getMax() - ((float)ringTooltip.getWidth()) * (float)1.25)
                    - ring.getThumb().getBounds().right;
            ringTooltip.setY(y);
            ringTooltip.setVisibility(View.VISIBLE);
            return false;
        });
        ring.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ringTooltip.setText(Integer.toString(progress));
                float y = (ring.getHeight() - ring.getMax() - ((float)ringTooltip.getWidth()) * (float)1.25)
                        - ring.getThumb().getBounds().right;
                ringTooltip.setY(y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ringTooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ringTooltip.setVisibility(View.INVISIBLE);
                MainActivity.gripList.get(deviceSpinner.getSelectedItemPosition()).setRing(ring.getProgress());
                GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        pinky.setOnTouchListener((v, event) -> {
            pinkyTooltip.setText(Integer.toString(pinky.getProgress()));
            float y = (pinky.getHeight() - pinky.getMax() - ((float)pinkyTooltip.getWidth()) * (float)1.25)
                    - pinky.getThumb().getBounds().right;
            pinkyTooltip.setY(y);
            pinkyTooltip.setVisibility(View.VISIBLE);
            return false;
        });
        pinky.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pinkyTooltip.setText(Integer.toString(progress));
                float y = (pinky.getHeight() - pinky.getMax() - ((float)pinkyTooltip.getWidth()) * (float)1.25)
                        - pinky.getThumb().getBounds().right;
                pinkyTooltip.setY(y);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pinkyTooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pinkyTooltip.setVisibility(View.INVISIBLE);
                MainActivity.gripList.get(deviceSpinner.getSelectedItemPosition()).setPinky(pinky.getProgress());
                GripsFragment.mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        thumbTooltip = view.findViewById(R.id.thumbTooltip);
        indexTooltip = view.findViewById(R.id.indexTooltip);
        middleTooltip = view.findViewById(R.id.middleTooltip);
        ringTooltip = view.findViewById(R.id.ringTooltip);
        pinkyTooltip = view.findViewById(R.id.pinkyTooltip);

        int position = deviceSpinner.getSelectedItemPosition();
        thumb.setProgress(MainActivity.gripList.get(position).getThumb());
        index.setProgress(MainActivity.gripList.get(position).getIndex());
        middle.setProgress(MainActivity.gripList.get(position).getMiddle());
        ring.setProgress(MainActivity.gripList.get(position).getRing());
        pinky.setProgress(MainActivity.gripList.get(position).getPinky());

        return view;
    }
}