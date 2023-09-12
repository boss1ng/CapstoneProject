package com.example.qsee;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserToGroup extends DialogFragment {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String username;
    private String groupName;
    private String userId;
    private String firstName;
    private String lastName;
    private Context context;

    public AddUserToGroup(String username, String groupName, String userId, String firstName, String lastName) {
        this.username = username;
        this.groupName = groupName;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.addgroup, container, false);


        // Initialize UI components from the layout
        Button inviteBtn = view.findViewById(R.id.inviteBtn);
        Button cancelButton = view.findViewById(R.id.CancelBtn);
        TextView messageTextView = view.findViewById(R.id.textView13);

        // Set the message text with firstName and lastName
        String message = "Add " + firstName + " " + lastName + " as a collaborator?";
        messageTextView.setText(message);

        // Set a click listener for the "Invite" button
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered user ID
                String enteredUserId = userId;

                // Check if the entered user ID is not empty
                if (!enteredUserId.isEmpty()) {
                    // Perform the invitation action by adding the user ID to the group members
                    addUserToGroup(enteredUserId);
                } else {
                    //
                }

                // Dismiss the AddUserFinder dialog
                AddUserFinder addUserFinderDialog = (AddUserFinder) getFragmentManager().findFragmentByTag("AddUserFinderDialog");
                if (addUserFinderDialog != null) {
                    addUserFinderDialog.dismiss();
                }

                // Close the dialog
                dismiss();
            }
        });

        // Set a click listener for the "Cancel" button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the AddUserFinder dialog
                AddUserFinder addUserFinderDialog = (AddUserFinder) getFragmentManager().findFragmentByTag("AddUserFinderDialog");
                if (addUserFinderDialog != null) {
                    addUserFinderDialog.dismiss();
                }
                dismiss();
            }
        });

        return view;
    }

    private void addUserToGroup(String userId) {
        // Find the group for the given username and groupName
        databaseReference.child("MobileUsers").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userSnapshot) {
                        for (DataSnapshot user : userSnapshot.getChildren()) {
                            String adminUserId = user.getKey();
                            // Now you have the adminUserId based on the username

                            // Now, check if the groupName exists under the "Groups" node for this adminUserId
                            databaseReference.child("Groups").child(adminUserId).orderByChild("groupName")
                                    .equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot groupSnapshot) {
                                            if (groupSnapshot.exists()) {
                                                // The groupName exists for this adminUserId
                                                String groupId = groupSnapshot.getChildren().iterator().next().getKey();

                                                // Check if the user is already a member of the group
                                                if (isUserAlreadyMember(groupSnapshot.child(groupId), userId)) {
                                                    // Display a toast for duplicate user
                                                    Toast.makeText(context, "User is already a member of the group", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, firstName + " " + lastName + " successfully added to " + groupName, Toast.LENGTH_SHORT).show();
                                                    // Determine the next member number dynamically
                                                    int nextMemberNumber = 1;
                                                    for (int i = 1; i <= 50; i++) {
                                                        if (!groupSnapshot.child(groupId).hasChild("member" + i)) {
                                                            nextMemberNumber = i;
                                                            break;
                                                        }
                                                    }

                                                    // Create a new user object with userId as the key and set it as a member in the group
                                                    DatabaseReference groupRef = databaseReference.child("Groups").child(adminUserId).child(groupId).child("member" + nextMemberNumber);
                                                    groupRef.setValue(userId);
                                                    Log.d("AddUserToGroup", "Added user " + userId + " to group " + groupName + " as a member" + nextMemberNumber);
                                                }
                                            } else {
                                                // The groupName doesn't exist for this adminUserId
                                                Log.d("AddUserToGroup", "Group doesn't exist for user: " + adminUserId);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("AddUserToGroup", "Error checking group: " + databaseError.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("AddUserToGroup", "Error finding user ID: " + databaseError.getMessage());
                    }
                });
    }

    private boolean isUserAlreadyMember(DataSnapshot groupSnapshot, String userId) {
        // Check if the user is already a member of the group
        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
            String memberId = memberSnapshot.getValue(String.class);
            if (userId.equals(memberId)) {
                return true; // User is already a member
            }
        }
        return false; // User is not a member
    }

}
