/*package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MenuAdapter extends ArrayAdapter<Orders.MenuItem> {
    public MenuAdapter(Context context, List<Orders.MenuItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_menu, parent, false);
        }
        TextView textViewName = convertView.findViewById(R.id.textViewMenuItemName);
        EditText editTextQuantity = convertView.findViewById(R.id.editTextMenuItemQuantity);

        Orders.MenuItem item = getItem(position);
        textViewName.setText(item.getName());
        editTextQuantity.setText(String.valueOf(item.getQuantity()));

        return convertView;
    }
}
*/


