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

import java.util.List;

public class GroupViewAdapter extends RecyclerView.Adapter<GroupViewAdapter.ViewHolder> {

    private List<String> memberList;
    private String groupName;

    public GroupViewAdapter(List<String> memberList, String groupName) {
        this.memberList = memberList;
        this.groupName = groupName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groupview_list_item, parent, false);
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
                    if (encryptedFirstName != null && encryptedLastName != null) {
                        String firstName = AESUtils.decrypt(encryptedFirstName); // Decrypt the first name
                        String lastName = AESUtils.decrypt(encryptedLastName); // Decrypt the last name
                        if (firstName != null && lastName != null) {
                            String fullName = firstName + " " + lastName;
                            holder.memberNametextView.setText(fullName);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors here
            }
        });

    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView deleteButton;
        TextView memberNametextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteButton = itemView.findViewById(R.id.delete);
            memberNametextView = itemView.findViewById(R.id.memberName);
        }
    }

}
