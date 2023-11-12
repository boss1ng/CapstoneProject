package com.example.qsee;

//import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.PostViewHolder> {
    private List<GlimpseFragment.Post> postList;
    private OnPostItemClickListener itemClickListener;

    public interface OnPostItemClickListener {
        void onPostItemClick(String imageUrl, String caption, String category, String location, Long timestamp);
    }

    public MyAdapter(List<GlimpseFragment.Post> postList, OnPostItemClickListener itemClickListener) {
        this.postList = postList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        GlimpseFragment.Post post = postList.get(position);

        // Load the post's image into the ImageView using a library like Picasso or Glide
        Picasso.get().load(post.getImageURL()).into(holder.postImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onPostItemClick(
                        post.getImageURL(),
                        post.getCaption(),
                        post.getCategory(),
                        post.getLocation(),
                        post.getTimestamp()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;

        public PostViewHolder(View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
        }
    }
}
