package com.extramoney;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.*;

public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.PhotoVH> {

    // Updated interface: returns both uri and index!
    public interface OnPhotoClickListener {
        void onPhotoClick(Uri uri, int position);
    }

    private final Context context;
    private final List<Uri> photoUris;
    private final OnPhotoClickListener listener;

    public PhotoGridAdapter(Context context, List<Uri> photoUris, OnPhotoClickListener listener) {
        this.context = context;
        this.photoUris = photoUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.photo_grid_item, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        Uri uri = photoUris.get(position);
        Glide.with(context).load(uri).centerCrop().into(holder.photoThumb);
        holder.itemView.setOnClickListener(v -> listener.onPhotoClick(uri, position));
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photoThumb;
        PhotoVH(View itemView) {
            super(itemView);
            photoThumb = itemView.findViewById(R.id.photoThumb);
        }
    }
}
