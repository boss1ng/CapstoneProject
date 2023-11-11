package com.example.qsee;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GlimpseFragment extends Fragment implements MyAdapter.OnPostItemClickListener {

    public static class Post {
        private String imageUrl;
        private String userId;
        private String caption;
        private String category;
        private String location;
        private long timestamp;

        public Post() {
            // Default constructor required for Firebase
        }

        public Post(String imageUrl, String userId, String caption, String category, String location) {
            this.imageUrl = imageUrl;
            this.userId = userId;
            this.caption = caption;
            this.category = category;
            this.location = location;
            this.timestamp = timestamp;
        }

        public String getImageURL() {
            return imageUrl;
        }

        public String getUserId() {
            return userId;
        }

        public String getCaption() {
            return caption;
        }

        public String getCategory() {
            return category;
        }

        public String getLocation() {
            return location;
        }
        public long getTimestamp() {return timestamp; }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        View view = inflater.inflate(R.layout.fragment_glimpse, container, false);
        Context context = getActivity(); // Get the context

        // Retrieve the username from the arguments
        String currentUser = getArguments().getString("userId");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        // Modify the query to filter posts by the specific user's ID
        Query query = databaseReference.orderByChild("userId").equalTo(currentUser);
        long currentTimestamp = System.currentTimeMillis();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Post> postList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post1, Post post2) {
                        // Compare the timestamps in descending order
                        return Long.compare(post2.getTimestamp(), post1.getTimestamp());
                    }
                });
                // Create and set your RecyclerView adapter here
                RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
                int numberOfColumns = 3;
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
                MyAdapter adapter = new MyAdapter(postList, GlimpseFragment.this); // Pass the GlimpseFragment instance
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

        return view;
    }

    @Override
    public void onPostItemClick(String imageUrl, String caption, String category, String location, Long timestamp) {
        PostDetailsDialog dialog = new PostDetailsDialog();
        dialog.setData(imageUrl, caption, category, location);
        String userId = getArguments().getString("userId");
        dialog.setUserData(userId);
        dialog.show(getFragmentManager(), "PostDetailsDialog");
    }
}
