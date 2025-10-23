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
import com.bumptech.glide.Glide;
import java.util.*;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoVH> {

    private final Context context;
    private final List<PhotoItem> allPhotos = new ArrayList<>();

    public PhotosAdapter(Context context, Map<String, List<PhotoItem>> monthGroupMap) {
        this.context = context;
        updateData(monthGroupMap);
    }

    public void updateData(Map<String, List<PhotoItem>> monthGroupMap) {
        allPhotos.clear();
        List<String> sortedMonths = new ArrayList<>(monthGroupMap.keySet());
        Collections.sort(sortedMonths, Collections.reverseOrder());
        for (String month : sortedMonths) {
            allPhotos.addAll(monthGroupMap.get(month));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        PhotoItem item = allPhotos.get(position);
        holder.photoDate.setText(item.month + " " + item.year);
        Glide.with(context).load(item.uri).centerCrop().into(holder.photoThumb);
    }

    @Override
    public int getItemCount() {
        return allPhotos.size();
    }

    static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photoThumb;
        TextView photoDate;
        PhotoVH(View itemView) {
            super(itemView);
            photoThumb = itemView.findViewById(R.id.photoThumb);
            photoDate = itemView.findViewById(R.id.photoDate);
        }
    }
}
