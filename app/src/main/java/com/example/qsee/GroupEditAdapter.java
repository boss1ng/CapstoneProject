package com.example.qsee;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupEditAdapter extends RecyclerView.Adapter<GroupEditAdapter.ViewHolder> {

    private List<String> memberList;
    private String groupName;

    public GroupEditAdapter(List<String> memberList, String groupName) {
        this.memberList = memberList;
        this.groupName = groupName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groupeditview_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String memberId = memberList.get(position);

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("MobileUsers").child(memberId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String encryptedFirstName = dataSnapshot.child("firstName").getValue(String.class);
                    String encryptedLastName = dataSnapshot.child("lastName").getValue(String.class);
                    String profilePictureUrl = dataSnapshot.child("profilePictureUrl").getValue(String.class);
                    if (encryptedFirstName != null && encryptedLastName != null) {
                        String firstName = AESUtils.decrypt(encryptedFirstName); // Decrypt the first name
                        String lastName = AESUtils.decrypt(encryptedLastName); // Decrypt the last name
                        if (firstName != null && lastName != null) {
                            String fullName = firstName + " " + lastName;
                            holder.memberNametextView.setText(fullName);
                            if (profilePictureUrl == null)
                                holder.userProfile.setImageResource(R.drawable.profilepicture);
                            else
                                loadUserPostImage(profilePictureUrl, holder.userProfile);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors here
            }
        });

        if (position == 0) {
            holder.deleteButton.setVisibility(View.GONE); // or View.INVISIBLE if you want to keep the space
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Delete Member");
                builder.setMessage("Are you sure you want to delete this member?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the member from the group under the specific group name
                        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);
                        final int finalPosition = holder.getAdapterPosition();
                        String memberKey = "member" + (finalPosition + 1); // Adjust index to start from 1
                        groupsReference.child(memberKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("GroupEditAdapter", "Member deletion successful.");
                                    // Remove the member from the local list
                                    memberList.remove(finalPosition);
                                    notifyItemRemoved(finalPosition);
                                    notifyItemRangeChanged(finalPosition, memberList.size());
                                } else {
                                    Log.e("GroupEditAdapter", "Error deleting member: " + task.getException());
                                    // Handle the error
                                }
                            }
                        });
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
        });


    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    private void loadUserPostImage(String imageUrl, ImageView profilePic) {
        // Use a library like Picasso or Glide to load and display the image
        if (profilePic.getContext() != null && imageUrl != null) {
            Picasso.get().load(imageUrl).into(profilePic);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView deleteButton;
        TextView memberNametextView;
        ImageView userProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.delete);
            memberNametextView = itemView.findViewById(R.id.memberName);
            userProfile = itemView.findViewById(R.id.groupIcon);
        }
    }

}
