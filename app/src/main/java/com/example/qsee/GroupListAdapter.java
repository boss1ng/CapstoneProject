package com.example.qsee;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private List<Groups> groupList;

    private String userId;

    public GroupListAdapter(List<Groups> groupList, String userId) {
        this.groupList = groupList;
        this.userId = userId;
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
                AddUserFinder addUserFinderDialog = AddUserFinder.newInstance(userId, groupName);

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
                // Query Firebase to fetch the list of members for the selected group
                String groupName = group.getGroupName();
                databaseReference.child("Groups").child(userId).child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Create a dialog to show the list of members and select a new admin
                        showMemberSelectionDialog(v.getContext(), groupName, dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Log.e("Firebase", "Error fetching group members: " + databaseError.getMessage());
                    }
                });
            }
        });
    }

    // Create a method to show the member selection dialog and confirm deletion
    private void showMemberSelectionDialog(Context context, String groupName, DataSnapshot dataSnapshot) {
        // Create a list of members from the dataSnapshot
        List<String> members = new ArrayList<>();
        for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
            String memberKey = memberSnapshot.getKey();
            if (!memberKey.equals("admin") && !memberKey.equals("groupName") && !memberKey.equals("member1")) {
                // Exclude "member1" and add the actual user IDs to the list
                String memberId = memberSnapshot.getValue(String.class);
                members.add(memberId);
            }
        }

        // Check if there are members to delete or if the group should be deleted
        if (!members.isEmpty()) {
            // Convert the list to an array for radio button selection
            final String[] memberArray = members.toArray(new String[0]);
            final int[] selectedMemberIndex = {-1}; // Initialize to no selection

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select New Admin");

            // Set up the radio button dialog
            builder.setSingleChoiceItems(memberArray, selectedMemberIndex[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle the selection of a new admin
                    selectedMemberIndex[0] = which; // Update the selected index
                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedMemberIndex[0] != -1) {
                        // A member is selected, handle the selection
                        String newAdminId = memberArray[selectedMemberIndex[0]];
                        // Update the group's admin in Firebase
                        updateGroupAdmin(groupName, newAdminId);
                    }
                    dialog.dismiss(); // Dismiss the dialog when "OK" is clicked
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // There are no other members, confirm group deletion directly
            showDeleteConfirmationDialog(context, groupName);
        }
    }

    // Create a method to show the delete confirmation dialog
    private void showDeleteConfirmationDialog(Context context, String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Leave Group");
        builder.setMessage("Are you sure you want to leave the group?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the group in Firebase
                deleteGroup(groupName);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Create a method to delete the group in Firebase if the user is an admin or remove the user from any member position
    private void deleteGroup(String groupName) {
        DatabaseReference groupsReference = databaseReference.child("Groups");

        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userGroupSnapshot : dataSnapshot.getChildren()) {
                    String groupId = userGroupSnapshot.getKey();
                    DatabaseReference groupReference = userGroupSnapshot.child(groupName).getRef();

                    // Check if the current user is a member of this group
                    groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot groupSnapshot) {
                            String adminId = groupSnapshot.child("admin").getValue(String.class);

                            if (userId.equals(adminId)) {
                                // The user is an admin, delete the group
                                groupReference.removeValue();
                            } else {
                                // The user is not an admin, check if they are a member of this group
                                for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
                                    String memberKey = memberSnapshot.getKey();
                                    if (!memberKey.equals("admin") && !memberKey.equals("groupName")) {
                                        String memberId = memberSnapshot.getValue(String.class);
                                        if (userId.equals(memberId)) {
                                            // Remove the user from this member position
                                            groupReference.child(memberKey).removeValue();
                                            break; // Exit the loop after removing the user
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Log.e("Firebase", "Error checking admin status: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("Firebase", "Error fetching groups: " + databaseError.getMessage());
            }
        });
    }

    // Create a method to update the group's admin, set "member1," and transfer members (excluding those with newAdminId) to a new group in Firebase
    private void updateGroupAdmin(String groupName, String newAdminId) {
        DatabaseReference groupReference = databaseReference.child("Groups").child(userId).child(groupName);

        // Create a new group under "Groups" with the new admin
        DatabaseReference newGroupReference = databaseReference.child("Groups").child(newAdminId).child(groupName);

        // Set "member1" for the new group with the new admin ID
        newGroupReference.child("member1").setValue(newAdminId);

        // Set the 'admin' field for the new group to the new admin ID
        newGroupReference.child("admin").setValue(newAdminId);

        // Set the 'groupName' field for the new group
        newGroupReference.child("groupName").setValue(groupName);

        // Fetch the list of members from the original group
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberKey = memberSnapshot.getKey();
                    if (!memberKey.equals("admin") && !memberKey.equals("groupName") && !memberKey.equals("member1")) {
                        // Get the member's ID
                        String memberId = memberSnapshot.getValue(String.class);

                        // Check if the member's ID is not the same as the new admin ID
                        if (!newAdminId.equals(memberId)) {
                            // Transfer each member (excluding those with newAdminId) to the new group
                            newGroupReference.child(memberKey).setValue(memberId);
                        }
                    }
                }

                // Finally, delete the original group
                groupReference.removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("Firebase", "Error transferring members: " + databaseError.getMessage());
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

