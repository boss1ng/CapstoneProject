package com.example.qsee;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends ArrayAdapter<String> {
    private SparseBooleanArray checkStates;

    public CategoryAdapter(Context context, List<String> categories) {
        super(context, 0, categories);
        checkStates = new SparseBooleanArray(categories.size());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewItem);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        String category = getItem(position);
        textView.setText(category);

        checkBox.setTag(position); // Set the position as a tag for the CheckBox
        checkBox.setChecked(checkStates.get(position, false)); // Set the checked state

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                checkStates.put(position, ((CheckBox) v).isChecked());
            }
        });

        return convertView;
    }

    // Method to get the checked items
    public ArrayList<String> getCheckedItems() {
        ArrayList<String> checkedItems = new ArrayList<>();
        for (int i = 0; i < checkStates.size(); i++) {
            int key = checkStates.keyAt(i);
            if (checkStates.get(key)) {
                checkedItems.add(getItem(key));
            }
        }
        return checkedItems;
    }

    // Method to set an item as checked
    public void setItemChecked(int position, boolean value) {
        checkStates.put(position, value);
    }

}


