package com.example.qsee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class QuizFragment extends Fragment {
    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        Button btA = rootView.findViewById(R.id.btA);
        Button btB = rootView.findViewById(R.id.btB);
        Button btC = rootView.findViewById(R.id.btC);
        Button btD = rootView.findViewById(R.id.btD);

        TextView Q1 = rootView.findViewById(R.id.Q1);
        TextView Q2 = rootView.findViewById(R.id.Q2);
        TextView Q3 = rootView.findViewById(R.id.Q3);
        TextView Q4 = rootView.findViewById(R.id.Q4);

        btA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click event
                // You can add any desired behavior here
                Button clickedButton = (Button) v;
                clickedButton.setBackgroundResource(R.drawable.button_gradient);
                ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
                ViewCompat.setElevation(Q1, getResources().getDimensionPixelSize(R.dimen.textview_elevation));
            }
        });
        btB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click event
                // You can add any desired behavior here
                Button clickedButton = (Button) v;
                clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_start));
                ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
                ViewCompat.setElevation(Q2, getResources().getDimensionPixelSize(R.dimen.textview_elevation));
            }
        });
        btC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click event
                // You can add any desired behavior here
                Button clickedButton = (Button) v;
                clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_start));
                ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
                ViewCompat.setElevation(Q3, getResources().getDimensionPixelSize(R.dimen.textview_elevation));
            }
        });
        btD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click event
                // You can add any desired behavior here
                Button clickedButton = (Button) v;
                clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_start));
                ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
                ViewCompat.setElevation(Q4, getResources().getDimensionPixelSize(R.dimen.textview_elevation));
            }
        });

        return rootView;
    }
}
