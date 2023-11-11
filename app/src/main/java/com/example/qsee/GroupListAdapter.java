package com.example.qsee;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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

// ADDED
    private void loadUserPostImage(String imageUrl, ImageView groupPic) {
        // Use a library like Picasso or Glide to load and display the image
        if (groupPic.getContext() != null && imageUrl != null) {
            Picasso.get().load(imageUrl).into(groupPic);
        }
    }
// UNTIL HERE

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Groups group = groupList.get(position);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("MobileUsers");

        usersRef.child(group.getAdmin()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                if (username != null) {
                    // Perform necessary actions with the username
                    // For example, you can pass it to your AESUtils for decryption
                    String decryptedUsername = AESUtils.decrypt(username);

                    // Adding the "Owner: " prefix
                    String displayText = "Owner: " + decryptedUsername;

                    // Apply formatting to the text
                    SpannableString spannableString = new SpannableString(group.getGroupName() + "\n" + displayText);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, group.getGroupName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.ITALIC), group.getGroupName().length() + 1, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Set the formatted text to the TextView
                    holder.groupNameTextView.setText(spannableString);

                    // Adjust the text size for the username
                    int usernameStart = spannableString.toString().indexOf(displayText);
                    int usernameEnd = usernameStart + displayText.length();
                    spannableString.setSpan(new RelativeSizeSpan(0.8f), usernameStart, usernameEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Adjust the size as needed
                    holder.groupNameTextView.setText(spannableString);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });


// ADDED
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                        String imageUrl = groupSnapshot.child("GroupPhoto").getValue(String.class);
                        loadUserPostImage(imageUrl, holder.groupIconImageView);
                    }
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
// UNTIL HERE









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
                String groupName = group.getGroupName();

                // Check if the user is the admin
                if (!group.getAdmin().equals(userId)) {
                    // Show a toast message indicating that only admins can edit the group
                    Toast.makeText(v.getContext(), "Only admins can edit the group", Toast.LENGTH_SHORT).show();
                } else {
                    // User is the admin, proceed with the edit action
                    GroupEditFragment groupEditFragment = new GroupEditFragment();

                    // Pass any necessary data to the fragment using Bundle
                    Bundle args = new Bundle();
                    args.putString("groupName", groupName);
                    args.putString("userId",userId);
                    groupEditFragment.setArguments(args);

                    // Begin the transaction to replace the current fragment with GroupEditFragment
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, groupEditFragment) // Replace fragment_container with the ID of your container
                            .addToBackStack(null) // Add the transaction to the back stack
                            .commit();
                }
            }
        });

        holder.groupNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = group.getGroupName();

                    // User is the admin, proceed with the edit action
                    GroupViewFragment groupViewFragment = new GroupViewFragment();

                    // Pass any necessary data to the fragment using Bundle
                    Bundle args = new Bundle();
                    args.putString("groupName", groupName);
                    args.putString("userId",userId);
                    groupViewFragment.setArguments(args);

                    // Begin the transaction to replace the current fragment with GroupEditFragment
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, groupViewFragment) // Replace fragment_container with the ID of your container
                            .addToBackStack(null) // Add the transaction to the back stack
                            .commit();

            }
        });


        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user is the admin of the group
                String groupName = group.getGroupName();
                databaseReference.child("Groups").child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String adminId = dataSnapshot.child("admin").getValue(String.class);
                        if (adminId != null && adminId.equals(userId)) {
                            // User is the admin, show the member selection dialog
                            showMemberSelectionDialog(v.getContext(), groupName, dataSnapshot);
                        } else {
                            // User is not the admin, show a leave confirmation dialog
                            showLeaveConfirmationDialog(v.getContext(), groupName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Log.e("Firebase", "Error checking admin status: " + databaseError.getMessage());
                    }
                });
            }
        });

    }

    // Modify the showMemberSelectionDialog method to display decrypted usernames
    private void showMemberSelectionDialog(Context context, String groupName, DataSnapshot dataSnapshot) {
        // Create a list of members from the dataSnapshot
        List<String> memberIds = new ArrayList<>();
        for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
            String memberKey = memberSnapshot.getKey();
            if (!memberKey.equals("admin") && !memberKey.equals("groupName") && !memberKey.equals("member1")) {
                // Exclude "member1" and add the actual user IDs to the list
                String memberId = memberSnapshot.getValue(String.class);
                memberIds.add(memberId);
            }
        }

        // Fetch and decrypt usernames instead of user IDs
        final List<String> decryptedMemberNames = new ArrayList<>();
        DatabaseReference usersReference = databaseReference.child("MobileUsers");
        for (String memberId : memberIds) {
            usersReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String encryptedUsername = dataSnapshot.child("username").getValue(String.class);
                    if (encryptedUsername != null) {
                        // Decrypt the encrypted username
                        String decryptedUsername = AESUtils.decrypt(encryptedUsername);
                        decryptedMemberNames.add(decryptedUsername);
                    }
                    // Check if all usernames have been fetched and decrypted
                    if (decryptedMemberNames.size() == memberIds.size()) {
                        // Proceed to display the member selection dialog
                        displayMemberSelectionDialog(context, groupName, decryptedMemberNames, memberIds);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                    Log.e("Firebase", "Error fetching encrypted username: " + databaseError.getMessage());
                }
            });
        }

        // Check if there are no other members and show delete confirmation directly
        if (memberIds.isEmpty()) {
            showDeleteConfirmationDialog(context, groupName);
        }
    }



    // Create a method to display the member selection dialog with decrypted usernames
    private void displayMemberSelectionDialog(Context context, String groupName, List<String> decryptedMemberNames, List<String> memberIds) {
        // Check if there are members to delete or if the group should be deleted
        if (!decryptedMemberNames.isEmpty()) {
            // Convert the list to an array for radio button selection
            final String[] memberArray = decryptedMemberNames.toArray(new String[0]);
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
                        String newAdminName = memberArray[selectedMemberIndex[0]];
                        String newAdminId = memberIds.get(selectedMemberIndex[0]);
                        // Update the group's admin in Firebase
                        updateGroupAdmin(groupName, newAdminId, newAdminName);
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



    // Create a method to show the leave confirmation dialog for non-admin users
    private void showLeaveConfirmationDialog(Context context, String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Leave Group");
        builder.setMessage("Are you sure you want to leave the group?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Leave the group (you can implement the leave action here)
                leaveGroup(groupName);
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

    // Add a method to leave the group in Firebase
    private void leaveGroup(String groupName) {
        // Reference to the group in the Firebase database
        DatabaseReference groupReference = databaseReference.child("Groups").child(groupName);

        // Query to find the current user's position in the group's members list
        Query query = groupReference.orderByValue().equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    // Remove the user's ID from the group's members list
                    memberSnapshot.getRef().removeValue();
                }

                // Optionally, you can perform additional actions after leaving the group
                // For example, update UI or show a confirmation message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("Firebase", "Error leaving group: " + databaseError.getMessage());
            }
        });
    }


    // Create a method to show the delete confirmation dialog
    private void showDeleteConfirmationDialog(Context context, String groupName) {
        // Query Firebase to check if the user is the admin of the group
        DatabaseReference groupReference = databaseReference.child("Groups").child(groupName);
        groupReference.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String adminId = dataSnapshot.getValue(String.class);
                if (adminId != null && adminId.equals(userId)) {
                    // User is the admin, show the delete confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Group");
                    builder.setMessage("Are you sure you want to delete the group?");

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
                } else {
                    // User is not the admin, show a toast message
                    Toast.makeText(context, "Only admins can delete a group", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("Firebase", "Error checking admin status: " + databaseError.getMessage());
            }
        });
    }



    // Create a method to delete the group in Firebase
    private void deleteGroup(String groupName) {
        // Delete the group in Firebase
        DatabaseReference groupReference = databaseReference.child("Groups").child(groupName);
        groupReference.removeValue();
    }

    // Create a method to update the group's admin, set "member1," and transfer members (excluding those with newAdminId) to a new group in Firebase
    private void updateGroupAdmin(String groupName, String newAdminId, String newAdminName) {
        DatabaseReference groupReference = databaseReference.child("Groups").child(groupName);

        // Update the 'admin' field for the group to the new admin ID
        groupReference.child("admin").setValue(newAdminId);
        groupReference.child("member1").setValue(newAdminId);

        // Loop to remove the 'newAdminId' from 'member2' to 'member50'
        for (int i = 2; i <= 50; i++) {
            String memberKey = "member" + i;
            groupReference.child(memberKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String memberId = dataSnapshot.getValue(String.class);
                    if (newAdminId.equals(memberId)) {
                        // Remove 'newAdminId' from the member
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
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
/*
    private void retrieveImagesFromStorageAndSetGroupIcon(String groupName, ViewHolder holder) {
        // Retrieve the image reference for the specific group from your Firebase Realtime Database
        DatabaseReference groupReference = databaseReference.child("Groups").child(groupName);
        groupReference.child("groupIconRef").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageRef = dataSnapshot.getValue(String.class);
                // Check if the imageRef is not null and fetch the image
                if (imageRef != null) {
                    // Get the reference to the image in Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://capstone-project-ffe21.appspot.com/");
                    StorageReference imageStorageRef = storage.getReference().child("Icons").child(imageRef);
                    // Fetch the image and set it to the groupIconImageView
                    imageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Load the image into the groupIconImageView
                            Picasso.get().load(uri.toString()).into(holder.groupIconImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors while getting the download URL
                        }
                    });
                } else {
                    // Handle the case when the image reference is missing in the database
                    // You can display a placeholder image or take other actions
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

 */
    // Add a method to update the data
    public void updateData(List<Groups> newData) {
        groupList.clear();
        groupList.addAll(newData);
        notifyDataSetChanged();
    }
}

