package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

public class QuizFragment extends Fragment {
    // ... (other methods and variables)

    private Button correctButton; // Reference to the correct button
    private boolean isAnswerCorrect = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ... (other code)
        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        Button btA = rootView.findViewById(R.id.btA);
        Button btB = rootView.findViewById(R.id.btB);
        Button btC = rootView.findViewById(R.id.btC);
        Button btD = rootView.findViewById(R.id.btD);

        TextView Q1 = rootView.findViewById(R.id.Q1);
        TextView Q2 = rootView.findViewById(R.id.Q2);
        TextView Q3 = rootView.findViewById(R.id.Q3);
        TextView Q4 = rootView.findViewById(R.id.Q4);
        // Set the correct button
        correctButton = btA;

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnswerCorrect) {
                    return; // If the answer is already correct, do nothing
                }

                Button clickedButton = (Button) v;
                if (clickedButton == correctButton) {
                    handleCorrectAnswerButtonClick(clickedButton, Q1);
                } else {
                    handleWrongAnswerButtonClick(clickedButton);
                }
            }
        };

        btA.setOnClickListener(buttonClickListener);
        btB.setOnClickListener(buttonClickListener);
        btC.setOnClickListener(buttonClickListener);
        btD.setOnClickListener(buttonClickListener);

        return rootView;
    }

    private void handleCorrectAnswerButtonClick(Button clickedButton, TextView questionTextView) {
        clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_start));
        ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
        ViewCompat.setElevation(questionTextView, getResources().getDimensionPixelSize(R.dimen.textview_elevation));

        isAnswerCorrect = true;
        showCorrectAnswerDialog();
    }

    private void handleWrongAnswerButtonClick(Button clickedButton) {
        clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_end));
        showWrongAnswerDialog();
    }

    private void showCorrectAnswerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Correct Answer")
                .setMessage("Your answer is correct!")
                .setPositiveButton("Proceed", null)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showWrongAnswerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wrong Answer")
                .setMessage("Try again!")
                .setPositiveButton("OK", null)
                .show();
    }
}