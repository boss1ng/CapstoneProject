package com.example.qsee;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuizFragment extends Fragment {
    // ... (other methods and variables)

    private Button correctButton; // Reference to the correct button
    private boolean isAnswerCorrect = false;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private View rootView;
    private QuizQuestion currentQuestion;
    private Button btA, btB, btC, btD;
    private TextView Q1, Q2, Q3, Q4;
    private AlertDialog wrongAnswerDialog;
    private AlertDialog correctAnswerDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ... (other code)
        rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        btA = rootView.findViewById(R.id.btA);
        btB = rootView.findViewById(R.id.btB);
        btC = rootView.findViewById(R.id.btC);
        btD = rootView.findViewById(R.id.btD);

        Q1 = rootView.findViewById(R.id.Q1);
        Q2 = rootView.findViewById(R.id.Q2);
        Q3 = rootView.findViewById(R.id.Q3);
        Q4 = rootView.findViewById(R.id.Q4);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load the background image using Picasso
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/quizbg.jpg?alt=media&token=86983ed2-0bfe-4908-9224-14ce5f9cdd04";
        ImageView backgroundImageView = new ImageView(getActivity());

        // Set a listener to be notified when the image is loaded
        Picasso.get().load(imageUrl).into(backgroundImageView, new Callback() {
            @Override
            public void onSuccess() {
                // Set the background of the profileCont LinearLayout
                ConstraintLayout quizLayout = rootView.findViewById(R.id.QuizLayout);
                quizLayout.setBackground(backgroundImageView.getDrawable());
            }

            @Override
            public void onError(Exception e) {
                // Handle error if necessary
            }
        });

        // Initialize the quiz questions
        initializeQuizQuestions();

        // Display the first quiz question
        displayQuizQuestion(0); // Display the first question in the list

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnswerCorrect) {
                    return; // If the answer is already correct, do nothing
                }

                Button clickedButton = (Button) v;
                int clickedIndex = -1; // Initialize clicked index to an invalid value

                // Determine which button was clicked and set the clickedIndex accordingly
                if (clickedButton == btA) {
                    clickedIndex = 0;
                } else if (clickedButton == btB) {
                    clickedIndex = 1;
                } else if (clickedButton == btC) {
                    clickedIndex = 2;
                } else if (clickedButton == btD) {
                    clickedIndex = 3;
                }

                if (clickedIndex == currentQuestion.getCorrectAnswerIndex()) {
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

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottomNavigationView);
        // Set the default item as highlighted
        MenuItem defaultItem = bottomNavigationView.getMenu().findItem(R.id.action_quiz);
        defaultItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    //BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        return rootView;
    }

    private void loadFragment(Fragment fragment) {
        //Bundle bundle = new Bundle();
        //bundle.putString("userId", userId);
        //fragment.setArguments(bundle);

        // Use Bundle to pass values
        Bundle bundle = new Bundle();

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            bundle.putString("userId", userID);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initializeQuizQuestions() {
        // Create quiz questions and add them to the list
        List<String> question1Choices = Arrays.asList("Quezon Memorial Circle", "Quezon Memorial Shrine", "Quezon Heritage House", "Ninoy Aquino Parks and Wildlife Center");
        quizQuestions.add(new QuizQuestion("Question 1?", question1Choices, 0, R.drawable.pic1, "Quezon Memorial Circle\n\nA national park located in Quezon City, Metro Manila, Philippines. The park is located inside a large traffic circle in the shape of an ellipse and bounded by the Elliptical Road and is the main park of Quezon City. "));

        List<String> question2Choices = Arrays.asList("Quezon Heritage House", "Art in Island", "Circle of Fun", "Eastwood Central Park");
        quizQuestions.add(new QuizQuestion("Question 2?", question2Choices, 1, R.drawable.pic2, "Art in Island\n\nThe largest Mixed Media Art Museum in the Philippines that brings media technology and various artforms together to create a unique, immersive, and interactive experience."));

        List<String> question3Choices = Arrays.asList("La Mesa Eco Park", "Ninoy Aquino Parks and Wildlife Center", "Quezon Heritage House", "Tandang Sora National Shrine");
        quizQuestions.add(new QuizQuestion("Question 3?", question3Choices, 0, R.drawable.pic3, "La Mesa Eco Park\n\nActivities that can be done by visitors in the park are hiking, mountain-biking, horseback riding, rappelling, zip-lining and fishing. They can also paddle boat ride in the lagoon. The park also has an \"Ecotrail\" and an orchidarium."));

        List<String> question4Choices = Arrays.asList("Andres Bonifacio Monument", "Tandang Sora National Shrine", "Presidential Car Museum", "Quezon Heritage House");
        quizQuestions.add(new QuizQuestion("Question 4?", question4Choices, 2, R.drawable.pic4, "Presidential Car Museum\n\nA museum that displays cars used by the former Philippine Presidents."));

        List<String> question5Choices = Arrays.asList("La Mesa Eco Park", "Quezon Memorial Circle", "UP Sunken Garden", "Eastwood Central Park");
        quizQuestions.add(new QuizQuestion("Question 5?", question5Choices, 2, R.drawable.pic5, "UP Sunken Garden\n\nThe site of many important events in UP history. It was the venue for the first UP Fair in 1925, and it has also been used for graduation ceremonies, concerts, and rallies."));

        List<String> question6Choices = Arrays.asList("Luxent Hotel", "Go Hotels North Edsa - Quezon City", "Fersal Hotel", "Seda Vertis North");
        quizQuestions.add(new QuizQuestion("Question 6?", question6Choices, 3, R.drawable.pic6, "Seda Vertis North\n\nIn a bustling business and entertainment district, this modern high-rise hotel is 2 km from both the North Avenue MRT station and lively Quezon Memorial Circle park."));

        List<String> question7Choices = Arrays.asList("North Pointe Residences", "Hop Inn Hotel Tomas Morato", "Harolds Evotel", "The Oracle Hotel and Residences");
        quizQuestions.add(new QuizQuestion("Question 7?", question7Choices, 0, R.drawable.pic7, "North Pointe Residences\n\nhas the following amenities: Free WiFi, Free parking, Accessible, Pool, Air-conditioned, Kid-friendly, and Restaurant."));

        List<String> question8Choices = Arrays.asList("Residenciale Boutique Apartment", "Sequoia Hotel", "Park Vil-la Apartelle", "Pacific Waves Resort");
        quizQuestions.add(new QuizQuestion("Question 8?", question8Choices, 3, R.drawable.pic8, "Pacific Waves Resort\n\nThis laid-back resort is 2 km from Grotto of Our Lady of Lourdes, 9 km from Pinagrealan Cave and 11 km from shopping at SM City Fairview. "));

        List<String> question9Choices = Arrays.asList("Provenciano", "The Frazzled Cook", "Half Saints", "Victorino's Restaurant");
        quizQuestions.add(new QuizQuestion("Question 9?", question9Choices, 1, R.drawable.pic9, "The Frazzled Cook\n\nA quirky restaurant put together with the mission of serving good comfort food with a cozy homey ambiance."));

        List<String> question10Choices = Arrays.asList("Provenciano", "The Frazzled Cook", "Half Saints", "Victorino's Restaurant");
        quizQuestions.add(new QuizQuestion("Question 10?", question10Choices, 0, R.drawable.pic10, "Provenciano\n\nFamily-owned neighborhood restaurant Provenciano has withstood the test of time, and remains to be one of food capital Maginhawa’s not-so-hidden but always recommended gems, ever since its founding in 2015."));

        List<String> question11Choices = Arrays.asList("SM City Fairview", "Fairview Center Mall", "Ayala Malls Fairview Terraces", "Robinsons Novaliches");
        quizQuestions.add(new QuizQuestion("Question 11?", question11Choices, 2, R.drawable.pic11, "Ayala Malls Fairview Terraces\n\nIt is the second mall by Robinsons Malls in Quezon City after Robinsons Galleria."));

        List<String> question12Choices = Arrays.asList("Trinoma", "Fairview Center Mall", "Ayala Malls Fairview Terraces", "Ayala Malls Vertis North");
        quizQuestions.add(new QuizQuestion("Question 12?", question12Choices, 0, R.drawable.pic12, "Trinoma\n\nVast shopping complex with familiar apparel brands, casual eats, a supermarket & a cinema."));

        List<String> question13Choices = Arrays.asList("Trinoma", "Fairview Center Mall", "Ayala Malls Fairview Terraces", "Ayala Malls Vertis North");
        quizQuestions.add(new QuizQuestion("Question 13?", question13Choices, 3, R.drawable.pic13, "Ayala Malls Vertis North\n\nSizable shopping center providing brand-name stores, services, informal eateries & a movie theater."));

        List<String> question14Choices = Arrays.asList("SM City Faiview", "SM City Novaliches", "SM City North EDSA", "Fisher Mall");
        quizQuestions.add(new QuizQuestion("Question 14?", question14Choices, 2, R.drawable.pic14, "SM City North EDSA\n\nIt is the first SM Supermall in the country and formerly the largest shopping mall in the Philippines from 2008 to 2011, circa 2014, and from 2015 to 2021."));

        List<String> question15Choices = Arrays.asList("Fisher Mall", "Landers Superstore", "Commonwealth Market", "Gateway Mall");
        quizQuestions.add(new QuizQuestion("Question 15?", question15Choices, 0, R.drawable.pic15, "Fisher Mall\n\nDynamic shopping complex offering an array of stores, a supermarket, an arcade & a movie theater."));

        List<String> question16Choices = Arrays.asList("Ayala Malls Fairview Terraces", "Ayala Malls Cloverleaf", "Ayala Malls Vertis North", "Gateway Mall");
        quizQuestions.add(new QuizQuestion("Question 16?", question16Choices, 1, R.drawable.pic16, "Ayala Malls Cloverleaf\n\nA shopping mall developed and managed by Ayala Malls, inside the Cloverleaf Estate in Quezon City. This is among Ayala Malls' establishments in Quezon City, after Ayala Malls Vertis North, UP Town Center and TriNoma. "));

        List<String> question17Choices = Arrays.asList("Robinsons Novaliches", "Ayala Malls Vertis North", "Gateway Mall", "Robinsons Magnolia");
        quizQuestions.add(new QuizQuestion("Question 17?", question17Choices, 3, R.drawable.pic17, "Robinsons Magnolia\n\nBustling shopping center offering clothing stores, services, a food court & movie theater."));

        List<String> question18Choices = Arrays.asList("Robinsons Novaliches", "Ayala Malls Vertis North", "Gateway Mall", "UP Town Center");
        quizQuestions.add(new QuizQuestion("Question 18?", question18Choices, 3, R.drawable.pic18, "UP Town Center\n\nAiry, contemporary retail complex with brand-name shops, outdoor walkways & a fenced dog park."));

        List<String> question19Choices = Arrays.asList("Our Lady of the Miraculous Medal Parish Church", "Parish of the Holy Sacrifice Church", "Santo Domingo Church", "Church of the Risen Lord");
        quizQuestions.add(new QuizQuestion("Question 19?", question19Choices, 0, R.drawable.pic19, "Our Lady of the Miraculous Medal Parish of the Roman Catholic Diocese of Cubao\n\nEstablished on September 4, 1976. It is located in Project 4, City of Quezon. The Parish Fiesta is celebrated every 27th day of November."));

        // Add more quiz questions with images and descriptions as needed
        Collections.shuffle(quizQuestions);
    }

    private void displayQuizQuestion(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= quizQuestions.size()) {
            // Handle the case where the question index is out of bounds
            return;
        }

        currentQuestion = quizQuestions.get(questionIndex);

        // Set the image resource for the question
        ImageView imageView = rootView.findViewById(R.id.quizImg); // Assuming you have an ImageView in your layout
        imageView.setImageResource(currentQuestion.getImageResourceId());

        // Set the answer choices on TextViews (Q1, Q2, Q3, Q4)
        Q1.setText(currentQuestion.getAnswerChoices().get(0));
        Q2.setText(currentQuestion.getAnswerChoices().get(1));
        Q3.setText(currentQuestion.getAnswerChoices().get(2));
        Q4.setText(currentQuestion.getAnswerChoices().get(3));

        // Set an OnClickListener for the answer choice buttons to check if the answer is correct

        // Handle correct answer click to show description
        if (isAnswerCorrect) {
            showCorrectAnswerDialog();
        }
    }

    // ... (other methods)


    private void handleCorrectAnswerButtonClick(Button clickedButton, TextView questionTextView) {
        clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_start));
        ViewCompat.setElevation(clickedButton, getResources().getDimensionPixelSize(R.dimen.button_elevation));
        ViewCompat.setElevation(questionTextView, getResources().getDimensionPixelSize(R.dimen.textview_elevation));

        // Do not set isAnswerCorrect to true here

        // Show the "Correct Answer" dialog without setting isAnswerCorrect
        showCorrectAnswerDialog();
    }



    private void handleWrongAnswerButtonClick(Button clickedButton) {
        clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gradient_end));
        showWrongAnswerDialog();

        // Show the correct answer after a short delay (you can adjust the delay duration)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (wrongAnswerDialog != null && wrongAnswerDialog.isShowing()) {
                    wrongAnswerDialog.dismiss();  // Dismiss the "Wrong Answer" dialog
                }
                showCorrectAnswerDialog();
            }
        }, 1000); // Delay for 1 second (adjust as needed)
    }

    private void showCorrectAnswerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        int nextQuestionIndex = quizQuestions.indexOf(currentQuestion) + 1;

        if (nextQuestionIndex < quizQuestions.size()) {
            // There are more questions
            builder.setTitle("Correct Answer")
                    .setMessage(currentQuestion.getCorrectAnswerDescription())
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Reset button colors
                            resetButtonColors();

                            // Proceed to the next question
                            displayQuizQuestion(nextQuestionIndex);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Reset button colors on Cancel
                            resetButtonColors();
                            dialogInterface.dismiss(); // Dismiss the correct answer dialog
                        }
                    });

            correctAnswerDialog = builder.create();
            correctAnswerDialog.show();
        } else {
            builder.setTitle("Correct Answer")
                    .setMessage(currentQuestion.getCorrectAnswerDescription())
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Reset button colors
                            resetButtonColors();

                            // Proceed to the next question
                            displayQuizQuestion(nextQuestionIndex);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Reset button colors on Cancel
                            resetButtonColors();
                            dialogInterface.dismiss(); // Dismiss the correct answer dialog
                        }
                    });

            correctAnswerDialog = builder.create();
            correctAnswerDialog.show();

            // All questions are answered, including the last one
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder congratsBuilder = new AlertDialog.Builder(requireContext());
                    congratsBuilder.setTitle("Congratulations!")
                            .setMessage("You have reached the end, Try Again?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Handle the completion, for example, go back to the home screen
                                    correctAnswerDialog.dismiss(); // Dismiss the correct answer dialog
                                    loadFragment(new StartQuizFragment());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Reset button colors on Cancel
                                    correctAnswerDialog.dismiss(); // Dismiss the correct answer dialog
                                    loadFragment(new HomeFragment());
                                }
                            })
                            .show();
                }
            }, 2000); // Delay in milliseconds (adjust as needed)
        }
    }


    private void resetButtonColors() {
        btA.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.default_button_color));
        btB.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.default_button_color));
        btC.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.default_button_color));
        btD.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.default_button_color));
        ViewCompat.setElevation(btA, 0);
        ViewCompat.setElevation(btB, 0);
        ViewCompat.setElevation(btC, 0);
        ViewCompat.setElevation(btD, 0);
    }


    private void showWrongAnswerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wrong Answer")
                .setMessage("Try again next time!");

        // Store the "Wrong Answer" dialog in the variable
        wrongAnswerDialog = builder.create();

        // Show the dialog
        wrongAnswerDialog.show();
    }

}