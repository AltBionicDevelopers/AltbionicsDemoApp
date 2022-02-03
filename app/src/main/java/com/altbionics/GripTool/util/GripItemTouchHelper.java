package com.altbionics.GripTool.util;


import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.altbionics.GripTool.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class GripItemTouchHelper extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;

    public GripItemTouchHelper(ItemTouchHelperAdapter adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        mAdapter.onItemClearView(recyclerView, (RecyclerViewAdapter.ViewHolder) viewHolder);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            mAdapter.onItemSelectionChanged((RecyclerViewAdapter.ViewHolder) viewHolder, actionState);
        }
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.onItemMove((RecyclerViewAdapter.ViewHolder) viewHolder, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemSwipe(viewHolder, direction);
    }

    @Override
    public void onChildDraw(@NonNull @NotNull Canvas c, @NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        Drawable deleteIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_delete);
        Drawable uploadIcon = ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_upload);
        int intrinsicWidth = 0;
        int intrinsicHeight = 0;

        ColorDrawable background = new ColorDrawable();
        int backgroundColor;

        if (actionState != ItemTouchHelper.ACTION_STATE_DRAG) {
            int top, bottom, left, right, deleteIconMargin, uploadIconMargin;

            if (dX < 0) {
                if (deleteIcon != null) {
                    intrinsicWidth = deleteIcon.getIntrinsicWidth();
                    intrinsicHeight = deleteIcon.getIntrinsicHeight();
                }
                top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                left = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                right = itemView.getRight() - deleteIconMargin;
                bottom = top + intrinsicHeight;
                backgroundColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.red);
                background.setColor(backgroundColor);
                background.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                Objects.requireNonNull(deleteIcon).setBounds(left, top, right, bottom);
                background.draw(c);
                Objects.requireNonNull(deleteIcon).draw(c);
            } else if (dX > 0) {
                if (uploadIcon != null) {
                    intrinsicWidth = uploadIcon.getIntrinsicWidth();
                    intrinsicHeight = uploadIcon.getIntrinsicHeight();
                }
                top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                uploadIconMargin = (itemHeight - intrinsicHeight) / 2;
                left = itemView.getLeft() + uploadIconMargin;
                right = itemView.getLeft() + uploadIconMargin + intrinsicWidth;
                bottom = top + intrinsicHeight;
                backgroundColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.green);
                background.setColor(backgroundColor);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int)dX, itemView.getBottom());
                Objects.requireNonNull(uploadIcon).setBounds(left, top, right, bottom);
                background.draw(c);
                Objects.requireNonNull(uploadIcon).draw(c);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
