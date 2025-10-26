package com.extramoney;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;

public class FullscreenImageSliderActivity extends AppCompatActivity {
    public static final String EXTRA_URIS = "uris";
    public static final String EXTRA_INDEX = "index";

    // Usage: FullscreenImageSliderActivity.open(context, uriList, index);
    public static void open(Context ctx, ArrayList<Uri> uris, int startIndex) {
        Intent i = new Intent(ctx, FullscreenImageSliderActivity.class);
        i.putParcelableArrayListExtra(EXTRA_URIS, uris);
        i.putExtra(EXTRA_INDEX, startIndex);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_photo);

        ViewPager2 pager = findViewById(R.id.viewPager);
        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(EXTRA_URIS);
        int index = getIntent().getIntExtra(EXTRA_INDEX, 0);

        pager.setAdapter(new ImageSliderAdapter(this, uris));
        pager.setCurrentItem(index, false);
    }
}
