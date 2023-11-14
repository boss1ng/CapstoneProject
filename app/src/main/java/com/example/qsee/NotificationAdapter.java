package com.example.qsee;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notificationList;
    private String userId; // Add a member variable to store the userId
    private Context context;


    public NotificationAdapter(Context context, List<Notification> notificationList, String userId) {
        this.context = context;
        this.notificationList = notificationList;
        this.userId = userId;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Bind notification data to the UI elements
        holder.nameTextView.setText(notification.getSenderId());
        holder.groupTextView.setText(notification.getGroupId());

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "Accept" button click (e.g., add user to the group)
                acceptGroupInvitation(notification.getGroupId(), userId, notification.getMessage(), notification);
            }
        });


        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "Reject" button click (e.g., decline the invitation)
                rejectGroupInvitation(userId, notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateData(List<Notification> newNotifications) {
        notificationList.clear();
        notificationList.addAll(newNotifications);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView groupTextView;
        Button acceptButton;
        Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            groupTextView = itemView.findViewById(R.id.groupTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

    private void acceptGroupInvitation(String groupName, String member, String senderId, Notification notification) {
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        // Query for the group with the given name and admin
        groupsReference.orderByChild("groupName").equalTo(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot groupSnapshot) {
                for (DataSnapshot group : groupSnapshot.getChildren()) {
                    String adminUserId = group.child("admin").getValue(String.class);

                    // Check if the user is already a member
                    boolean isUserAlreadyMember = isUserAlreadyMember(group, member);

                    if (adminUserId != null && adminUserId.equals(member)) {
                        Toast.makeText(context, "You are the admin of the group.", Toast.LENGTH_LONG).show();
                    } else if (isUserAlreadyMember) {
                        Toast.makeText(context, "User is already a member of the group.", Toast.LENGTH_LONG).show();
                    } else {
                        // User is not the admin and not already a member, proceed with adding.
                        Toast.makeText(context, "Successfully joined " + groupName, Toast.LENGTH_LONG).show();

                        // Find the next available member number
                        int nextMemberNumber = 1;
                        for (int i = 1; i <= 50; i++) {
                            String memberKey = "member" + i;
                            if (!group.hasChild(memberKey)) {
                                nextMemberNumber = i;
                                break;
                            }
                        }

                        // Add the user as a member to the group
                        String memberKey = "member" + nextMemberNumber;
                        group.getRef().child(memberKey).setValue(member);

                        // Remove the accepted notification from the database
                        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications")
                                .child(member).child(notification.getNotificationId());

                        notificationsReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Notification removed from the database, now remove it from the list
                                    notificationList.remove(notification);
                                    notifyDataSetChanged();
                                } else {
                                    // Handle the error
                                    Log.e("RemoveNotification", "Error removing notification from the database: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("acceptGroupInvitation", "Error finding group: " + databaseError.getMessage());
            }
        });
    }


    private void rejectGroupInvitation(String senderId, Notification notification) {
        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(senderId).child(notification.getNotificationId());

        notificationsReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Notification removed from the database, now remove it from the list
                    notificationList.remove(notification);
                    notifyDataSetChanged();

                    // Notify the user that the group invitation has been rejected
                    Toast.makeText(context, "Group invitation rejected.", Toast.LENGTH_LONG).show();
                } else {
                    // Handle the error
                    Log.e("RemoveNotification", "Error removing notification from the database: " + task.getException().getMessage());
                }
            }
        });
    }




    // Helper method to check if the user is already a member of the group
    private boolean isUserAlreadyMember(DataSnapshot groupSnapshot, String member) {
        // Implement your logic to check if the user is already a member
        // You can iterate through the children of the groupSnapshot and compare with the 'member'
        for (DataSnapshot memberSnapshot : groupSnapshot.getChildren()) {
            if (memberSnapshot.getValue(String.class).equals(member)) {
                return true; // User is already a member
            }
        }
        return false; // User is not a member
    }

}
