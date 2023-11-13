package com.example.qsee;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StartQuizFragment extends Fragment {

    public StartQuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_startquiz, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button startButton = view.findViewById(R.id.bt_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the "fragment_quiz" fragment
                QuizFragment quizFragment = new QuizFragment();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();

                // Retrieve selected categories from Bundle arguments
                Bundle getBundle = getArguments();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");
                    bundle.putString("userId", userID);
                    quizFragment.setArguments(bundle);
                }

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, quizFragment); // Replace 'fragment_container' with the ID of your container layout
                transaction.addToBackStack(null); // Optional: Add the transaction to the back stack
                transaction.commit();
            }
        });

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
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

        return view;
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
}