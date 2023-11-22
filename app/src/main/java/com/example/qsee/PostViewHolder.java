package com.example.qsee;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView userProfileImage;
    public TextView userName;
    public TextView postTime;
    public ImageView postImage;
    public TextView postCaption;
    public TextView usernameBelow;

    public PostViewHolder(View itemView) {
        super(itemView);
        userProfileImage = itemView.findViewById(R.id.userProfileImage);
        userName = itemView.findViewById(R.id.userName);
        postTime = itemView.findViewById(R.id.postTime);
        postImage = itemView.findViewById(R.id.postImage);
        postCaption = itemView.findViewById(R.id.postCaption);
        usernameBelow = itemView.findViewById(R.id.userNameCap);
    }
}

