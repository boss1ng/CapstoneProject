package com.example.qsee;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ActivityViewHolder> {

    private List<ItineraryItem> itemList;

    public ItineraryAdapter() {
        this.itemList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_layout, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ItineraryItem item = itemList.get(position);
        holder.bind(item);

        holder.timeInputLayout.getEditText().setOnClickListener(v -> {
            showTimePicker(holder.timeInputLayout);
        });
    }


    private void showTimePicker(TextInputLayout textInputLayout) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(textInputLayout.getContext(),
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    if (textInputLayout.getEditText() != null) {
                        textInputLayout.getEditText().setText(selectedTime);
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(ItineraryItem item) {
        itemList.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position > 0 && position < itemList.size()) {
            itemList.remove(position);
            notifyItemRemoved(position);
        }
    }


    public List<ItineraryItem> getItemList() {
        return itemList;
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private TextInputLayout timeInputLayout;
        private TextInputLayout activityInputLayout;
        private TextInputLayout locationInputLayout;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            timeInputLayout = itemView.findViewById(R.id.timeInput);
            activityInputLayout = itemView.findViewById(R.id.activityInput);
            locationInputLayout = itemView.findViewById(R.id.locationInput);
        }

        public void bind(ItineraryItem item) {
            TextInputEditText timeEditText = (TextInputEditText) timeInputLayout.getEditText();
            TextInputEditText activityEditText = (TextInputEditText) activityInputLayout.getEditText();
            TextInputEditText locationEditText = (TextInputEditText) locationInputLayout.getEditText();

            if (timeEditText != null) {
                if (timeEditText.getText().toString().isEmpty()) {
                    timeEditText.setText(item.getTime());
                }
            }

            if (activityEditText != null) {
                if (activityEditText.getText().toString().isEmpty()) {
                    activityEditText.setText(item.getActivity());
                }
            }

            if (locationEditText != null) {
                if (locationEditText.getText().toString().isEmpty()) {
                    locationEditText.setText(item.getLocation());
                }
            }
        }
    }
}
