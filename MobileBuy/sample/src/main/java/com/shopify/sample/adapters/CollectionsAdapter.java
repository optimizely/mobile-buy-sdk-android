package com.shopify.sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shopify.buy.model.Collection;
import com.shopify.sample.R;

import java.util.ArrayList;

/**
 * Created by mng on 12/27/16.
 */

public class CollectionsAdapter extends ArrayAdapter<Collection> {
    public CollectionsAdapter(Context context, ArrayList<Collection> collections) {
        super(context, 0, collections);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Collection collection =  getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item, parent, false);
        }
        // Lookup view for data population
        TextView tvItemName = (TextView) convertView.findViewById(R.id.tvItemName);

        // Populate the data into the template view using the data object
        if (collection.getTitle() == null) {
            tvItemName.setText("All products (no collection)");
        } else {
            tvItemName.setText(collection.getTitle());
        }
        tvItemName.setContentDescription(String.format("collection_list_item_%s", position));

        // Return the completed view to render on screen
        return convertView;
    }
}
