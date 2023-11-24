package com.example.qsee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<Post> postList; // Assuming Post is a class you've defined for your data

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            // Inflate header layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // Inflate item layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            // Handle header data binding if needed
        } else {
            // Adjust position by -1 to account for the header
            Post post = postList.get(position - 1);
            PostViewHolder postViewHolder = (PostViewHolder) holder;
            if (post.getCaption().isEmpty()) {
                postViewHolder.infoBelow.setVisibility(View.GONE);
                postViewHolder.userName.setText(post.getUserName());
                Picasso.get().load(post.getPostImageUrl()).into(postViewHolder.postImage);
                Picasso.get().load(post.getUserProfileImageUrl()).into(postViewHolder.userProfileImage);
                postViewHolder.postTime.setText(post.getPostTime());
            } else {
                postViewHolder.infoBelow.setVisibility(View.VISIBLE);
                postViewHolder.userName.setText(post.getUserName());
                Picasso.get().load(post.getPostImageUrl()).into(postViewHolder.postImage);
                postViewHolder.postCaption.setText(post.getCaption());
                Picasso.get().load(post.getUserProfileImageUrl()).into(postViewHolder.userProfileImage);
                postViewHolder.usernameBelow.setText(post.getUserName());
                postViewHolder.postTime.setText(post.getPostTime());
            }
        }
    }

    @Override
    public int getItemCount() {
        // +1 for header
        return postList.size() + 1;
    }

    // ViewHolder for header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        // Initialize your header views here if needed
        HeaderViewHolder(View itemView) {
            super(itemView);
            // Initialize header views
        }
    }

    // ViewHolder for items (existing ViewHolder)
}
