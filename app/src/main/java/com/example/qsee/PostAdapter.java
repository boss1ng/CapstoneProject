package com.example.qsee;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private List<Post> postList; // Assuming Post is a class you've defined for your data

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        Post post = postList.get(position);
        if (Objects.equals(post.getCaption(), "")){
            holder.infoBelow.setVisibility(View.GONE);
            // Bind data to the holder views
            holder.userName.setText(post.getUserName());
            // Load image using Picasso or similar
            Picasso.get().load(post.getPostImageUrl()).into(holder.postImage);
            Picasso.get().load(post.getUserProfileImageUrl()).into(holder.userProfileImage);
            holder.postTime.setText(post.getPostTime());
        }else {
            holder.infoBelow.setVisibility(View.VISIBLE);
            // Bind data to the holder views
            holder.userName.setText(post.getUserName());
            // Load image using Picasso or similar
            Picasso.get().load(post.getPostImageUrl()).into(holder.postImage);
            holder.postCaption.setText(post.getCaption());
            Picasso.get().load(post.getUserProfileImageUrl()).into(holder.userProfileImage);
            holder.usernameBelow.setText(post.getUserName());
            holder.postTime.setText(post.getPostTime());
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

}

