package com.extramoney;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.*;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumVH> {

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }
    private final List<Album> albums;
    private final Context context;
    private final OnAlbumClickListener listener;

    public AlbumAdapter(Context context, List<Album> albums, OnAlbumClickListener listener) {
        this.context = context;
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new AlbumVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumVH holder, int position) {
        Album album = albums.get(position);
        holder.albumName.setText(album.name);
        holder.albumCount.setText(album.photoItems.size() + " photos");
        Glide.with(context)
                .load(album.coverUri())
                .centerCrop()
                .into(holder.albumCover);
        holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumVH extends RecyclerView.ViewHolder {
        ImageView albumCover;
        TextView albumName, albumCount;
        AlbumVH(View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.albumCover);
            albumName = itemView.findViewById(R.id.albumName);
            albumCount = itemView.findViewById(R.id.albumCount);
        }
    }
}
