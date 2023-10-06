package com.example.qsee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterCategories extends DialogFragment {


        public Dialog onCreateDialog(Bundle savedInstanceState) {


            // Create a new Dialog instance
            Dialog dialog = super.onCreateDialog(savedInstanceState);

            // Set a custom layout for the dialog
            dialog.setContentView(R.layout.fragment_filter_categories);

            // Customize the width of the dialog (75% of screen width)
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return dialog;
        }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_categories, container, false);

        // Customize the dialog's appearance and position
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                // window.setGravity(Gravity.TOP | Gravity.START);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            /*
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT; // Adjust as needed
            layoutParams.gravity = Gravity.START; // Optional: Set gravity to your preference
            window.setAttributes(layoutParams);
             */
        }

        Toast.makeText(getContext(), "HELLO FILTER", Toast.LENGTH_LONG).show();

        return view;
    }

}
