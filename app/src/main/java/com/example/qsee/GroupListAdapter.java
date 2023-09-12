package com.example.qsee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private List<Groups> groupList;

    private String username;

    public GroupListAdapter(List<Groups> groupList, String username) {
        this.groupList = groupList;
        this.username = username;

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

        // Set click listeners for the icons
        holder.addUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Handle the add user action here
                // You can show a dialog or perform any other action you need
                // You can access the group information using 'group'
                // Create an instance of the AddUserToGroup dialog fragment
                String groupName = group.getGroupName();
                AddUserFinder addUserFinderDialog = AddUserFinder.newInstance(username, groupName);

                // Show the dialog using the FragmentManager
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                addUserFinderDialog.show(fragmentManager, "AddUserFinderDialog");
            }
        });



        holder.editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the edit group action here
                // You can show an edit dialog or navigate to an edit screen
                // You can access the group information using 'group'
            }
        });

        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the delete group action here
                // You can show a confirmation dialog and delete the group if confirmed
                // You can access the group information using 'group'
            }
        });
    }


    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupIconImageView;
        TextView groupNameTextView;
        ImageView addUserImageView;
        ImageView editImageView;
        ImageView deleteImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupIconImageView = itemView.findViewById(R.id.groupIcon);
            groupNameTextView = itemView.findViewById(R.id.groupName);
            addUserImageView = itemView.findViewById(R.id.adduser);
            editImageView = itemView.findViewById(R.id.edit);
            deleteImageView = itemView.findViewById(R.id.delete);
        }
    }


    // Add a method to update the data
    public void updateData(List<Groups> newData) {
        groupList.clear();
        groupList.addAll(newData);
        notifyDataSetChanged();
    }
}

