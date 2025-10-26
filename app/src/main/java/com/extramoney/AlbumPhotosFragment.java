package com.extramoney;

import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import java.util.*;

public class AlbumPhotosFragment extends Fragment {

    private static final String ARG_ALBUM_NAME = "album_name";
    private static final String ARG_PHOTO_URIS = "photo_uris";

    private ArrayList<Uri> photoUris;
    private String albumName;

    public static AlbumPhotosFragment newInstance(String albumName, ArrayList<Uri> uris) {
        AlbumPhotosFragment frag = new AlbumPhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_NAME, albumName);
        args.putParcelableArrayList(ARG_PHOTO_URIS, uris);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.album_photos_fragment, container, false);
        TextView tv = v.findViewById(R.id.albumTitle);
        RecyclerView grid = v.findViewById(R.id.photosGridRecycler);

        if (getArguments() != null) {
            albumName = getArguments().getString(ARG_ALBUM_NAME, "");
            photoUris = getArguments().getParcelableArrayList(ARG_PHOTO_URIS);
            tv.setText(albumName);
        }

        grid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        PhotoGridAdapter adapter = new PhotoGridAdapter(getContext(), photoUris, (uri, position) -> {
            // Open fullscreen slider, passing all uris and the clicked index
            FullscreenImageSliderActivity.open(getContext(), photoUris, position);
        });
        grid.setAdapter(adapter);

        return v;
    }
}
