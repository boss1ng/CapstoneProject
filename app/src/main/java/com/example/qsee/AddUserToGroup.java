package com.example.qsee;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
    private String userId;
    private String groupName;
    private String member;
    private String firstName;
    private String lastName;
    private Context context;

    public AddUserToGroup(String userId, String groupName, String member, String firstName, String lastName) {
        this.userId = userId;
        this.groupName = groupName;
        this.member = member;
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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
                String enteredUserId = member;

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

    private void addUserToGroup(String member) {
        DatabaseReference groupsRef = databaseReference.child("Groups");

        // Check if the group exists for the user
        groupsRef.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot groupSnapshot = dataSnapshot.getChildren().iterator().next();
                    String groupId = groupSnapshot.getKey();

                    // Check if the user is already a member of the group
                    if (isUserAlreadyMember(groupSnapshot, member)) {
                        // Display a toast for duplicate user
                        Toast.makeText(context, "User is already a member of the group", Toast.LENGTH_SHORT).show();
                    } else {
                        // Create a notification for the invitation without adding the user
                        createNotification(member, groupId);

                        // Display a confirmation to the user
                        Toast.makeText(context, "Invitation sent to " + member, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // The group doesn't exist for this user
                    Toast.makeText(context, "You are not the admin of this group.", Toast.LENGTH_SHORT).show();
                    Log.d("AddUserToGroup", "Group doesn't exist for user: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AddUserToGroup", "Error checking group: " + databaseError.getMessage());
            }
        });
    }



    private void createNotification(String invitedUserId, String groupId) {
        // Create a unique notification ID, for example, using timestamp
        String notificationId = String.valueOf(System.currentTimeMillis());

        // Create a Notification object
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setSenderId(userId);
        notification.setGroupId(groupId);

        // Store the notification in the database under the user's notifications
        databaseReference.child("Notifications").child(invitedUserId).child(notificationId).setValue(notification);
    }

    private boolean isUserAlreadyMember(DataSnapshot groupSnapshot, String member) {
        // Check if the user is already a member of the group
        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
            String memberId = memberSnapshot.getValue(String.class);
            if (member.equals(memberId)) {
                return true; // User is already a member
            }
        }
        return false; // User is not a member
    }

}
