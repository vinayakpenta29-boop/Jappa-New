package com.extramoney;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Hosts the "Extra" and "Photos" tabs using TabLayout + ViewPager2.
 */
public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // This now hosts ONLY your TabLayout + ViewPager2!

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up adapter with both fragments
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return new ExtraFragment(); // Your sales table fragment
                } else {
                    return new PhotosFragment(); // Your photos/gallery fragment
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        // Link tabs to ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Extra");
            else tab.setText("Photos");
        }).attach();
    }
}
