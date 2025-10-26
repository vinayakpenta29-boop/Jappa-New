package com.extramoney;

import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageVH> {
    private final Context context;
    private final List<Uri> uris;

    public ImageSliderAdapter(Context context, List<Uri> uris) {
        this.context = context; this.uris = uris;
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        photoView.setBackgroundColor(0xFF000000); // Black bg
        return new ImageVH(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageVH holder, int position) {
        Glide.with(context).load(uris.get(position)).into((PhotoView) holder.itemView);
    }

    @Override
    public int getItemCount() { return uris.size(); }

    static class ImageVH extends RecyclerView.ViewHolder {
        public ImageVH(@NonNull View itemView) { super(itemView); }
    }
}
