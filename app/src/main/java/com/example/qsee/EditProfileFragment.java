package com.example.qsee;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    // Declare TextInputLayout variables here so they are accessible throughout the class
    private TextInputLayout firstNameEditText;
    private DatePickerDialog datePickerDialog;
    private TextInputLayout lastNameEditText;
    private TextInputLayout contactNumberEditText;
    private TextInputLayout birthdateEditText;
    private TextInputLayout usernameEditText;
    private DatabaseReference userReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        // Initialize TextInputLayout variables
        firstNameEditText = view.findViewById(R.id.firstName);
        lastNameEditText = view.findViewById(R.id.lastName);
        contactNumberEditText = view.findViewById(R.id.contactNo);
        birthdateEditText = view.findViewById(R.id.birthdate);
        usernameEditText = view.findViewById(R.id.username);

        // Retrieve the username from arguments
        String userId = getArguments().getString("userId");




            // Query the database to retrieve user information based on the username
            userReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
            Query query = userReference.orderByChild("userId").equalTo(userId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            // Get user data (firstName, lastName, contactNumber, birthdate)
                            String firstName = userSnapshot.child("firstName").getValue(String.class);
                            String lastName = userSnapshot.child("lastName").getValue(String.class);
                            String contactNumber = userSnapshot.child("contactNumber").getValue(String.class);
                            String birthdate = userSnapshot.child("birthdate").getValue(String.class);
                            String username = userSnapshot.child("username").getValue(String.class);

                            firstNameEditText.getEditText().setText(firstName);
                            lastNameEditText.getEditText().setText(lastName);
                            contactNumberEditText.getEditText().setText(contactNumber);
                            birthdateEditText.getEditText().setText(birthdate);

                            // Set the username text
                            usernameEditText.getEditText().setText(username);
                        }
                    } else {
                        Log.e("EditProfileFragment", "User with username not found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any database query errors here
                }
            });

        EditText dateEditText = birthdateEditText.getEditText();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date to pre-select it in the DatePicker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String formattedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                                dateEditText.setText(formattedDate);
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        // Add an OnClickListener to the "Save" button
        Button saveButton = view.findViewById(R.id.saveBt);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get updated information from TextInputLayout fields
                String newFirstName = firstNameEditText.getEditText().getText().toString();
                String newLastName = lastNameEditText.getEditText().getText().toString();
                String newContactNumber = contactNumberEditText.getEditText().getText().toString();
                String newBirthdate = birthdateEditText.getEditText().getText().toString();
                String newUsername = usernameEditText.getEditText().getText().toString();




                // Query the database to find the user with the matching username
                DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
                Query query = usersReference.orderByChild("userId").equalTo(userId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Update user information
                                userSnapshot.getRef().child("firstName").setValue(newFirstName);
                                userSnapshot.getRef().child("lastName").setValue(newLastName);
                                userSnapshot.getRef().child("contactNumber").setValue(newContactNumber);
                                userSnapshot.getRef().child("birthdate").setValue(newBirthdate);
                                userSnapshot.getRef().child("username").setValue(newUsername);

                                // Inform the user that the update was successful
                                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                // Navigate back to the ProfileFragment
                                getParentFragmentManager().popBackStack();
                            }
                        } else {
                            Log.e("EditProfileFragment", "User with username not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any database query errors here
                        Log.e("EditProfileFragment", "Database Error: " + databaseError.getMessage());
                    }
                });
            }
        });

        Button cancel = view.findViewById(R.id.cancelBt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    // Create a method to set the username as an argument
    public static EditProfileFragment newInstance(String userId) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

}
