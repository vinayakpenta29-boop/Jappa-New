package com.extramoney;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final Map<String, List<PhotoItem>> monthGroupMap;
    private final List<String> months;

    public PhotosAdapter(Context context, Map<String, List<PhotoItem>> monthGroupMap) {
        this.context = context;
        this.monthGroupMap = monthGroupMap;
        this.months = new ArrayList<>(monthGroupMap.keySet());
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PhotosViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        PhotosViewHolder holder = (PhotosViewHolder) h;
        String label = months.get(position);
        holder.textView.setText(label + " (" + monthGroupMap.get(label).size() + " images)");
        // TODO: implement album/open-month logic, and use custom layout with thumbnails if desired
    }

    static class PhotosViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        PhotosViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
