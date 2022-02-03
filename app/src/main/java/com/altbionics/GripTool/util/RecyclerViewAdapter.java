package com.altbionics.GripTool.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.altbionics.GripTool.MainActivity;
import com.altbionics.GripTool.R;
import com.altbionics.GripTool.ui.GripsFragment;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
        ItemTouchHelperAdapter {

    private static final String TAG = "GripRecyclerViewAdapter";
    private final OnGripListener mOnGripListener;
    private static ItemTouchHelper mTouchHelper;
    /*private static int count = 0;
    private static String positionName = "";*/
    private final DataManagement dataManagement;


    public RecyclerViewAdapter(OnGripListener onGripListener) {
        dataManagement = new DataManagement(MainActivity.detectedAdapter.getContext());
        this.mOnGripListener = onGripListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_layout, parent, false);
        return new ViewHolder(view, mOnGripListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {

            if(MainActivity.GripManagement.getGripStatus(position))
                holder.gripStatus.setVisibility(View.VISIBLE);
            else
                holder.gripStatus.setVisibility(View.INVISIBLE);
            holder.userGripName.setText(MainActivity.GripManagement.getGripName(position));
            holder.userGripValues.setText(MainActivity.GripManagement.getGripValues(position));
            holder.userGripDescription.setText(MainActivity.GripManagement.getGripDescription(position));
        } catch (NullPointerException e) {
            Log.d(TAG, "onBindViewHolder: Null Pointer: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return MainActivity.gripList.size();
    }

    @Override
    public void onItemMove(ViewHolder viewHolder, int fromPosition, int toPosition) {
        String fromName = MainActivity.GripManagement.getGripName(fromPosition);
        String fromValues = MainActivity.GripManagement.getGripValues(fromPosition);
        String fromDescription = MainActivity.GripManagement.getGripDescription(fromPosition);
        boolean fromStatus = MainActivity.GripManagement.getGripStatus(fromPosition);

        MainActivity.GripManagement.removeGrip(fromPosition);
        MainActivity.GripManagement.addGrip(toPosition, fromName, fromValues, fromDescription, fromStatus);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemSwipe(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        GripsFragment.position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.LEFT) {
            /*count++;

            if (count > 0 && MainActivity.GripManagement.getGripName(viewHolder.getAdapterPosition()).equals(positionName)) {*/
                final int position = viewHolder.getAdapterPosition();
                final String name = MainActivity.GripManagement.getGripName(position);
                final String values = MainActivity.GripManagement.getGripValues(position);
                final String description = MainActivity.GripManagement.getGripDescription(position);
                final boolean status = MainActivity.GripManagement.getGripStatus(position);

                MainActivity.GripManagement.removeGrip(position);
                notifyDataSetChanged();

                Snackbar restore = Snackbar
                        .make(viewHolder.itemView, name + " was removed from the list.", Snackbar.LENGTH_LONG);
                restore.setAction("UNDO", v -> {
                    MainActivity.GripManagement.addGrip(position, name, values, description, status);
                    notifyDataSetChanged();
                });

                restore.setActionTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
                restore.show();
            /*    positionName = "";
                count = 0;
            } else {

                Snackbar swipeDelete = Snackbar
                        .make(viewHolder.itemView, "Swipe grip again to delete.", Snackbar.LENGTH_LONG);
                swipeDelete.setAction("Ok", v -> {
                });
                swipeDelete.setActionTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
                swipeDelete.show();
                positionName = MainActivity.GripManagement.getGripName(viewHolder.getAdapterPosition());
                notifyItemChanged(viewHolder.getAdapterPosition());
            }*/
        }

        if (direction == ItemTouchHelper.RIGHT) {
            if (MainActivity.device == null) {
                AlertDialog.Builder noDevice = new AlertDialog.Builder(viewHolder.itemView.getContext());
                noDevice.setTitle(R.string.no_device_title);
                noDevice.setMessage(R.string.no_device_description);
                noDevice.setCancelable(false);
                noDevice.setPositiveButton("Ok", (dialog, which) -> dialog.cancel());
                noDevice.show();
            } else {
                dataManagement.sendData(MainActivity.GripManagement.getGripName(viewHolder.getAdapterPosition()) + ":"
                        + MainActivity.GripManagement.getGripValues(viewHolder.getAdapterPosition()));
            }
            notifyItemChanged(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public void onItemClearView(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
        viewHolder.itemView.setBackground(ContextCompat.getDrawable(viewHolder.itemView.getContext(),
                R.drawable.grip_item_bg));
        viewHolder.gripNameLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
        viewHolder.gripValuesLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
        viewHolder.gripDescriptionLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
        viewHolder.userGripName.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
        viewHolder.userGripValues.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
        viewHolder.userGripDescription.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColor));
    }

    @Override
    public void onItemSelectionChanged(@NotNull ViewHolder viewHolder, int actionState) {
        viewHolder.itemView.setBackground(ContextCompat.getDrawable(viewHolder.itemView.getContext(),
                R.drawable.list_item_pressed));
        viewHolder.gripNameLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
        viewHolder.gripValuesLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
        viewHolder.gripDescriptionLabel.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
        viewHolder.userGripName.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
        viewHolder.userGripValues.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
        viewHolder.userGripDescription.setTextColor(getAttributeColor(viewHolder.itemView.getContext(),R.attr.primaryColorOnPrimary));
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        mTouchHelper = touchHelper;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener,
            GestureDetector.OnGestureListener {

        AppCompatImageView gripStatus;
        TextView gripNameLabel, gripValuesLabel, gripDescriptionLabel;
        TextView userGripName, userGripValues, userGripDescription;

        RelativeLayout mRecyclerView_item_layout;
        OnGripListener mOnGripListener;
        GestureDetector mGestureDetector;

        public ViewHolder(@NonNull View itemView, OnGripListener onGripListener) {
            super(itemView);

            mGestureDetector = new GestureDetector(itemView.getContext(), this);
            mOnGripListener = onGripListener;

            itemView.setOnTouchListener(this);

            gripStatus = itemView.findViewById(R.id.grip_status);
            gripNameLabel = itemView.findViewById(R.id.item_grip_name);
            gripValuesLabel = itemView.findViewById(R.id.item_grip_values);
            gripDescriptionLabel = itemView.findViewById(R.id.item_description);
            userGripName = itemView.findViewById(R.id.user_grip_name);
            userGripValues = itemView.findViewById(R.id.user_grip_values);
            userGripDescription = itemView.findViewById(R.id.user_description);
            mRecyclerView_item_layout = itemView.findViewById(R.id.recyclerview_item_layout);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }


        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mOnGripListener.onGripClick(getAdapterPosition());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }

    public interface OnGripListener {
        void onGripClick(int position);
    }

    private int getAttributeColor(@NotNull Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes, theme);
        } catch (Resources.NotFoundException e) {
            Log.w(TAG, "Not found color resource by id: " + colorRes);
        }
        return color;
    }
}
