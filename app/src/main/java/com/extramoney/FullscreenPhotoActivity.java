package com.extramoney;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class FullscreenPhotoActivity extends AppCompatActivity {
    private static final String EXTRA_URI = "extra_uri";
    public static void open(Context ctx, Uri uri) {
        Intent intent = new Intent(ctx, FullscreenPhotoActivity.class);
        intent.putExtra(EXTRA_URI, uri.toString());
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_photo);

        ImageView imageView = findViewById(R.id.fullImage);
        String uriStr = getIntent().getStringExtra(EXTRA_URI);
        if (uriStr != null) {
            Uri uri = Uri.parse(uriStr);
            Glide.with(this).load(uri).into(imageView);
        }
    }
}
