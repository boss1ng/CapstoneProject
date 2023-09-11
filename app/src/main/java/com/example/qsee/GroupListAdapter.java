package com.example.qsee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private List<Groups> groupList;

    public GroupListAdapter(List<Groups> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Groups group = groupList.get(position);
        holder.groupNameTextView.setText(group.getGroupName()); // Use getGroupName() to access the group name
        // Set other views and click listeners as needed
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupIconImageView;
        TextView groupNameTextView;
        // Add other views as needed

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIconImageView = itemView.findViewById(R.id.groupIcon);
            groupNameTextView = itemView.findViewById(R.id.groupName);
            // Initialize other views as needed
        }
    }

    // Add a method to update the data
    public void updateData(List<Groups> newData) {
        groupList.clear();
        groupList.addAll(newData);
        notifyDataSetChanged();
    }
}

