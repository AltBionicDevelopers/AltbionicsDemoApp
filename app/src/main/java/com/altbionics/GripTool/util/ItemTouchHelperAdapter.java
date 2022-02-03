package com.altbionics.GripTool.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperAdapter {
    void onItemMove(RecyclerViewAdapter.ViewHolder viewHolder, int fromPosition, int toPosition);
    void onItemSwipe(@NonNull RecyclerView.ViewHolder viewHolder, int direction);
    void onItemClearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerViewAdapter.ViewHolder viewHolder);
    void onItemSelectionChanged(RecyclerViewAdapter.ViewHolder viewHolder, int actionState);
}
