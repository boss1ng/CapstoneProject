package com.example.qsee;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.squareup.picasso.Picasso;

public class PostDetailsDialog extends DialogFragment {
    private ImageView dialogImageView;
    private TextView dialogCaptionTextView;
    private TextView dialogCategoryTextView;
    private TextView dialogLocationTextView;

    private String imageUrl;
    private String caption;
    private String category;
    private String location;

    public PostDetailsDialog() {
        // Empty constructor is required.
    }

    public void setData(String imageUrl, String caption, String category, String location) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.location = location;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post_details, container, false);

        dialogImageView = view.findViewById(R.id.dialogImageView);
        dialogCaptionTextView = view.findViewById(R.id.dialogCaptionTextView);
        dialogCategoryTextView = view.findViewById(R.id.dialogCategoryTextView);
        dialogLocationTextView = view.findViewById(R.id.dialogLocationTextView);

        // Load the image using Picasso
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(dialogImageView);
        }

        // Set other data
        dialogCaptionTextView.setText(caption);
        dialogCategoryTextView.setText(category);
        dialogLocationTextView.setText(location);

        return view;
    }
}
